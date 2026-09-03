export type ImagePreviewErrorCode = "invalid-format" | "invalid-dimensions" | "preview-failed";

export type ImagePreviewResult = {
  url: string;
  width: number;
  height: number;
};

type ImageDimensions = {
  width: number;
  height: number;
};

const JPEG_START_MARKER = 0xFF;
const JPEG_START_OF_IMAGE_MARKER = 0xD8;
const JPEG_END_OF_IMAGE_MARKER = 0xD9;
const JPEG_START_OF_SCAN_MARKER = 0xDA;
const PNG_SIGNATURE = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A] as const;
const PNG_WIDTH_OFFSET = 16;
const PNG_HEIGHT_OFFSET = 20;
const PNG_MINIMUM_HEADER_BYTES = 24;
const IMAGE_MAX_PIXELS = 20_000_000;
const IMAGE_MAX_DIMENSION = 8_192;
const PREVIEW_JPEG_QUALITY = 0.86;
const JPEG_START_OF_FRAME_MARKERS = new Set([
  0xC0,
  0xC1,
  0xC2,
  0xC3,
  0xC5,
  0xC6,
  0xC7,
  0xC9,
  0xCA,
  0xCB,
  0xCD,
  0xCE,
  0xCF,
]);

/**
 * 이미지 형식과 해상도 또는 미리보기 생성 실패 원인을 호출 화면에 전달함
 *
 * @author SeungHyeon.Kang
 */
export class ImagePreviewError extends Error {
  readonly code: ImagePreviewErrorCode;

  /**
   * 이미지 미리보기 실패 유형과 내부 오류 문구를 생성함
   *
   * @author SeungHyeon.Kang
   * @param code 화면 메시지를 결정할 실패 유형
   * @param cause 브라우저 이미지 처리에서 발생한 원본 오류
   * @return 생성된 이미지 미리보기 오류 객체
   */
  constructor(code: ImagePreviewErrorCode, cause?: unknown) {
    // 화면에는 공통 메시지를 사용하고 개발 오류 체인에는 실패 유형만 남김
    super(code, { cause });
    this.name = "ImagePreviewError";
    this.code = code;
  }
}

/**
 * PNG 고정 시그니처가 파일 선두 바이트와 일치하는지 판정함
 *
 * @author SeungHyeon.Kang
 * @param bytes 이미지 파일 바이트
 * @return PNG 시그니처 일치 여부
 */
function hasPngSignature(bytes: Uint8Array): boolean {
  // PNG 헤더보다 짧은 파일은 실제 이미지로 판정하지 않음
  if (bytes.length < PNG_MINIMUM_HEADER_BYTES) {
    // 유효한 PNG 시그니처가 없음을 반환함
    return false;
  }

  // 고정된 여덟 바이트가 모두 일치해야 PNG 이미지로 인정함
  for (let index = 0; index < PNG_SIGNATURE.length; index += 1) {
    // 하나라도 다른 바이트가 있으면 변조되거나 다른 형식인 파일로 판정함
    if (bytes[index] !== PNG_SIGNATURE[index]) {
      // 유효한 PNG 시그니처가 없음을 반환함
      return false;
    }
  }

  // 파일 선두의 PNG 고정 시그니처가 모두 일치함을 반환함
  return true;
}

/**
 * PNG IHDR 영역에서 디코딩 없이 이미지 너비와 높이를 조회함
 *
 * @author SeungHyeon.Kang
 * @param bytes PNG 이미지 파일 바이트
 * @return PNG 이미지 너비와 높이
 */
function getPngDimensions(bytes: Uint8Array): ImageDimensions {
  // PNG 헤더가 아니면 고정 위치의 해상도 값을 신뢰하지 않음
  if (!hasPngSignature(bytes)) {
    // 허용되지 않은 이미지 형식 오류를 발생시킴
    throw new ImagePreviewError("invalid-format");
  }

  // PNG IHDR의 네 바이트 정수를 안전하게 읽을 DataView를 생성함
  const headerView = new DataView(bytes.buffer, bytes.byteOffset, PNG_MINIMUM_HEADER_BYTES);
  const width = headerView.getUint32(PNG_WIDTH_OFFSET, false);
  const height = headerView.getUint32(PNG_HEIGHT_OFFSET, false);

  // PNG 헤더에서 검증할 이미지 너비와 높이를 반환함
  return { width, height };
}

/**
 * JPEG 세그먼트를 순회해 픽셀 디코딩 없이 이미지 너비와 높이를 조회함
 *
 * @author SeungHyeon.Kang
 * @param bytes JPEG 이미지 파일 바이트
 * @return JPEG 이미지 너비와 높이
 */
function getJpegDimensions(bytes: Uint8Array): ImageDimensions {
  // JPEG 시작 마커가 없으면 세그먼트 길이와 해상도 값을 신뢰하지 않음
  if (bytes.length < 4 || bytes[0] !== JPEG_START_MARKER
      || bytes[1] !== JPEG_START_OF_IMAGE_MARKER) {
    // 허용되지 않은 이미지 형식 오류를 발생시킴
    throw new ImagePreviewError("invalid-format");
  }

  let offset = 2;

  // 실제 픽셀 크기가 기록된 Start Of Frame 세그먼트를 찾을 때까지 헤더만 순회함
  while (offset < bytes.length) {
    // 세그먼트 사이의 패딩이나 손상된 바이트는 다음 마커까지 건너뜀
    if (bytes[offset] !== JPEG_START_MARKER) {
      offset += 1;
      // 다음 바이트부터 JPEG 마커 탐색을 계속함
      continue;
    }

    // 여러 개의 연속 0xFF 패딩 뒤에 있는 실제 마커 값을 찾음
    while (offset < bytes.length && bytes[offset] === JPEG_START_MARKER) {
      offset += 1;
    }

    // 마커 값이 파일 끝을 벗어나면 해상도 정보가 없는 손상 이미지로 판정함
    if (offset >= bytes.length) {
      // 더 이상 탐색할 JPEG 세그먼트가 없으므로 반복을 종료함
      break;
    }

    const marker = bytes[offset];
    offset += 1;

    // 이미지 종료나 픽셀 데이터 시작 전에 해상도 세그먼트를 찾지 못하면 파일을 거부함
    if (marker === JPEG_END_OF_IMAGE_MARKER || marker === JPEG_START_OF_SCAN_MARKER) {
      // 디코딩 전에 읽을 수 있는 JPEG 헤더 탐색을 종료함
      break;
    }

    // 독립형 마커는 길이 필드가 없으므로 다음 마커부터 탐색함
    if (marker === JPEG_START_OF_IMAGE_MARKER || (marker >= 0xD0 && marker <= 0xD7)
        || marker === 0x01) {
      // 다음 JPEG 세그먼트 탐색을 계속함
      continue;
    }

    // 세그먼트 길이 두 바이트를 읽을 수 없으면 손상 이미지로 판정함
    if (offset + 1 >= bytes.length) {
      // 더 이상 안전하게 읽을 세그먼트가 없으므로 반복을 종료함
      break;
    }

    const segmentLength = (bytes[offset] << 8) | bytes[offset + 1];

    // JPEG 세그먼트는 길이 필드 자체를 포함해 최소 두 바이트여야 함
    if (segmentLength < 2 || offset + segmentLength > bytes.length) {
      // 잘못된 세그먼트 길이를 가진 이미지는 해상도를 신뢰할 수 없으므로 반복을 종료함
      break;
    }

    // Start Of Frame 세그먼트에는 정밀도 다음으로 높이와 너비가 기록됨
    if (JPEG_START_OF_FRAME_MARKERS.has(marker) && segmentLength >= 7) {
      const height = (bytes[offset + 3] << 8) | bytes[offset + 4];
      const width = (bytes[offset + 5] << 8) | bytes[offset + 6];

      // JPEG 헤더에서 검증할 이미지 너비와 높이를 반환함
      return { width, height };
    }

    offset += segmentLength;
  }

  // 디코딩 전에 해상도를 확인할 수 없는 손상 JPEG 파일을 거부함
  throw new ImagePreviewError("invalid-format");
}

/**
 * MIME 형식에 맞는 이미지 헤더에서 너비와 높이를 조회함
 *
 * @author SeungHyeon.Kang
 * @param bytes 이미지 파일 바이트
 * @param mimeType 브라우저가 제공한 이미지 MIME 형식
 * @return 이미지 너비와 높이
 */
function getImageDimensions(bytes: Uint8Array, mimeType: string): ImageDimensions {
  // JPEG 파일은 가변 세그먼트에서 Start Of Frame 해상도를 조회함
  if (mimeType === "image/jpeg") {
    // 검증된 JPEG 헤더의 이미지 너비와 높이를 반환함
    return getJpegDimensions(bytes);
  }

  // PNG 파일은 IHDR 고정 위치에서 해상도를 조회함
  if (mimeType === "image/png") {
    // 검증된 PNG 헤더의 이미지 너비와 높이를 반환함
    return getPngDimensions(bytes);
  }

  // 업무에서 허용하지 않은 이미지 MIME 형식을 거부함
  throw new ImagePreviewError("invalid-format");
}

/**
 * 서버 이미지 검증과 같은 해상도 상한을 만족하는지 확인함
 *
 * @author SeungHyeon.Kang
 * @param dimensions 이미지 너비와 높이
 * @return 반환값이 없음
 */
function validateImageDimensions(dimensions: ImageDimensions): void {
  const pixelCount = dimensions.width * dimensions.height;

  // 잘못된 크기와 서버 상한을 넘는 이미지는 모바일 브라우저가 원본 픽셀을 디코딩하기 전에 거부함
  if (dimensions.width <= 0 || dimensions.height <= 0
      || dimensions.width > IMAGE_MAX_DIMENSION || dimensions.height > IMAGE_MAX_DIMENSION
      || !Number.isSafeInteger(pixelCount) || pixelCount > IMAGE_MAX_PIXELS) {
    // 서버 저장이 불가능하고 모바일 메모리를 과도하게 사용할 이미지 해상도 오류를 발생시킴
    throw new ImagePreviewError("invalid-dimensions");
  }
}

/**
 * 원본 비율을 유지하면서 미리보기 한 변의 최대 크기에 맞는 픽셀 크기를 계산함
 *
 * @author SeungHyeon.Kang
 * @param dimensions 원본 이미지 너비와 높이
 * @param maxEdge 미리보기 한 변의 최대 픽셀 길이
 * @return 축소된 미리보기 너비와 높이
 */
function getPreviewDimensions(dimensions: ImageDimensions, maxEdge: number): ImageDimensions {
  const scale = Math.min(1, maxEdge / Math.max(dimensions.width, dimensions.height));

  // 브라우저 디코더에 전달할 1픽셀 이상의 미리보기 크기를 반환함
  return {
    width: Math.max(1, Math.round(dimensions.width * scale)),
    height: Math.max(1, Math.round(dimensions.height * scale)),
  };
}

/**
 * 축소된 Canvas 픽셀을 브라우저에서 표시할 이미지 Blob으로 변환함
 *
 * @author SeungHyeon.Kang
 * @param canvas 미리보기 픽셀이 그려진 Canvas
 * @param mimeType 원본 이미지 MIME 형식
 * @return 미리보기 이미지 Blob
 */
function getCanvasBlob(canvas: HTMLCanvasElement, mimeType: string): Promise<Blob> {
  let resolveBlob: ((blob: Blob) => void) | null = null;
  let rejectBlob: ((reason: ImagePreviewError) => void) | null = null;

  /**
   * Canvas 변환 콜백을 호출부에서 기다릴 Promise 완료 함수를 보관함
   *
   * @author SeungHyeon.Kang
   * @param resolve 미리보기 Blob 생성 성공 함수
   * @param reject 미리보기 Blob 생성 실패 함수
   * @return 반환값이 없음
   */
  function setCanvasBlobPromise(
    resolve: (blob: Blob) => void,
    reject: (reason: ImagePreviewError) => void,
  ): void {
    resolveBlob = resolve;
    rejectBlob = reject;
  }

  /**
   * Canvas 변환 결과가 있을 때 미리보기 Blob 처리를 완료함
   *
   * @author SeungHyeon.Kang
   * @param blob Canvas에서 생성된 미리보기 이미지 Blob
   * @return 반환값이 없음
   */
  function handleCanvasBlob(blob: Blob | null): void {
    // 브라우저가 Blob을 만들지 못하면 원본 직접 렌더링 없이 안전하게 실패 처리함
    if (blob === null) {
      // 모바일 메모리 보호가 적용된 미리보기 생성 오류를 전달함
      rejectBlob?.(new ImagePreviewError("preview-failed"));
      // 빈 Blob 실패 처리를 종료함
      return;
    }

    // 생성된 축소 미리보기 Blob으로 비동기 변환을 완료함
    resolveBlob?.(blob);
  }

  // Canvas 비동기 콜백 결과를 호출부에서 순차 처리할 Promise로 변환함
  const blobPromise = new Promise<Blob>(setCanvasBlobPromise);
  // JPEG는 미리보기 용량을 낮추고 PNG는 투명도를 보존하도록 원본 MIME 형식으로 변환함
  canvas.toBlob(handleCanvasBlob, mimeType, PREVIEW_JPEG_QUALITY);

  // Canvas 변환이 끝날 때 미리보기 Blob으로 완료되는 Promise를 반환함
  return blobPromise;
}

/**
 * 원본 이미지 전체를 DOM에 표시하지 않고 제한된 크기의 안전한 미리보기 URL을 생성함
 *
 * @author SeungHyeon.Kang
 * @param file 사용자가 앨범에서 선택한 원본 이미지 파일
 * @param maxEdge 화면 대상별 미리보기 한 변의 최대 픽셀 길이
 * @return 축소된 미리보기 URL과 실제 미리보기 크기
 * @throws ImagePreviewError 이미지 형식과 해상도가 잘못됐거나 브라우저 축소 처리에 실패하면 발생함
 */
export async function createSafeImagePreview(file: File, maxEdge: number): Promise<ImagePreviewResult> {
  // 이미지 디코딩보다 먼저 파일 헤더만 읽어 서버 제한과 실제 형식을 검사함
  const bytes = new Uint8Array(await file.arrayBuffer());
  const dimensions = getImageDimensions(bytes, file.type.toLowerCase());
  // 서버가 저장할 수 없는 고해상도 이미지를 원본 픽셀 디코딩 전에 차단함
  validateImageDimensions(dimensions);

  // 원본 전체 픽셀 대신 브라우저 디코더가 바로 축소할 안전한 미리보기 크기를 계산함
  const previewDimensions = getPreviewDimensions(dimensions, maxEdge);

  // 축소 디코딩을 지원하지 않는 브라우저에서 고해상도 원본을 직접 렌더링하지 않음
  if (typeof window.createImageBitmap !== "function") {
    // 모바일 메모리 보호를 유지하도록 미리보기 생성 오류를 발생시킴
    throw new ImagePreviewError("preview-failed");
  }

  let imageBitmap: ImageBitmap | null = null;

  // 브라우저 이미지 디코더 실패를 화면용 오류로 변환하고 생성한 그래픽 자원은 항상 해제함
  try {
    // EXIF 촬영 방향을 적용하면서 원본 전체가 아닌 제한된 크기로 이미지를 디코딩함
    imageBitmap = await window.createImageBitmap(file, {
      imageOrientation: "from-image",
      resizeWidth: previewDimensions.width,
      resizeHeight: previewDimensions.height,
      resizeQuality: "high",
    });

    // 축소된 픽셀만 담을 화면 밖 Canvas를 생성함
    const canvas = document.createElement("canvas");
    canvas.width = imageBitmap.width;
    canvas.height = imageBitmap.height;
    const context = canvas.getContext("2d", { alpha: file.type.toLowerCase() === "image/png" });

    // Canvas 컨텍스트를 만들 수 없으면 원본을 DOM에 표시하지 않고 실패 처리함
    if (context === null) {
      // 모바일 메모리 보호가 적용된 미리보기 생성 오류를 발생시킴
      throw new ImagePreviewError("preview-failed");
    }

    // 축소 디코딩된 픽셀을 작은 Canvas에 한 번만 그림
    context.drawImage(imageBitmap, 0, 0);
    // Canvas 픽셀을 DOM에서 표시할 작은 Blob으로 변환함
    const previewBlob = await getCanvasBlob(canvas, file.type.toLowerCase());
    // 원본 File과 독립적으로 해제할 수 있는 미리보기 URL을 생성함
    const previewUrl = URL.createObjectURL(previewBlob);

    // 호출 화면이 미리보기와 임시 URL 수명을 관리하도록 결과를 반환함
    return {
      url: previewUrl,
      width: imageBitmap.width,
      height: imageBitmap.height,
    };
  }

  // 브라우저의 원시 디코딩 오류를 공통 이미지 미리보기 오류로 변환함
  catch (error) {
    // 이미 분류된 이미지 오류는 화면이 정확한 안내 문구를 선택하도록 그대로 전달함
    if (error instanceof ImagePreviewError) {
      // 형식과 해상도 또는 미리보기 생성 실패 오류를 반환함
      throw error;
    }

    // 원본 오류 내용은 화면에 노출하지 않고 미리보기 생성 실패 유형만 전달함
    throw new ImagePreviewError("preview-failed", error);
  }

  // 성공과 실패 모두 디코더의 원본 픽셀 메모리를 즉시 반환함
  finally {
    // 브라우저가 생성한 ImageBitmap 그래픽 자원을 해제함
    imageBitmap?.close();
  }
}
