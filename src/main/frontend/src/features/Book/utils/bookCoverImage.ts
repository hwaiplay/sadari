import type { SyntheticEvent } from "react";

/**
 * 도서 원본 표지 로드 실패 시 카카오 썸네일로 한 번만 대체한다
 *
 * @author HanWon.Jang
 * @param event 표지 이미지 오류 이벤트
 * @return 반환값이 없다
 */
export function handleBookCoverImageError(
  event: SyntheticEvent<HTMLImageElement>,
): void {

  const imageElement = event.currentTarget;
  const fallbackImage = imageElement.dataset.fallbackImage;

  // 대체 이미지가 없거나 이미 대체를 시도했다면 오류 반복을 차단한다
  if (!fallbackImage || imageElement.src === fallbackImage) {
    // 같은 이미지의 오류 이벤트가 반복되지 않도록 핸들러를 해제한다
    imageElement.onerror = null;
    // 추가 대체 요청 없이 오류 처리를 종료한다
    return;
  }

  // 대체 이미지도 실패할 때 같은 주소를 다시 요청하지 않도록 값을 제거한다
  delete imageElement.dataset.fallbackImage;
  // 검증된 원본을 불러오지 못한 표지를 공식 카카오 썸네일로 교체한다
  imageElement.src = fallbackImage;
}
