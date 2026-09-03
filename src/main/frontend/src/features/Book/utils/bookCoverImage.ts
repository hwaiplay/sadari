import type { SyntheticEvent } from "react";

export const BOOK_COVER_FALLBACK_IMAGE = "/img/common/no-image.png";

/**
 * 도서 표지 URL이 비어 있으면 공통 기본 이미지를 반환함
 *
 * @author HanWon.Jang
 * @param image 도서 표지 이미지 URL
 * @return 화면에 표시할 도서 표지 이미지 URL
 */
export function getBookCoverImageSource(
  image?: string | null,
): string {

  return image?.trim() || BOOK_COVER_FALLBACK_IMAGE;
}

/**
 * 도서 표지 로드 실패 시 검색 썸네일 또는 공통 기본 이미지로 교체함
 *
 * @author HanWon.Jang
 * @param event 도서 표지 이미지 오류 이벤트
 * @return 반환값이 없음
 */
export function handleBookCoverImageError(
  event: SyntheticEvent<HTMLImageElement>,
): void {

  const imageElement = event.currentTarget;
  const fallbackImage = imageElement.dataset.fallbackImage;
  const fallbackCandidates = Array.from(new Set([
    fallbackImage,
    BOOK_COVER_FALLBACK_IMAGE,
  ].filter((image): image is string => Boolean(image?.trim()))));
  const nextFallbackImage = fallbackCandidates.find((image) => (
    new URL(image, document.baseURI).href !== imageElement.src
  ));

  // 사용할 수 있는 대체 이미지가 더 없으면 오류 이벤트의 반복을 차단함
  if (!nextFallbackImage) {
    imageElement.onerror = null;
    return;
  }

  // 검색 썸네일과 공통 기본 이미지 순서로 다음 대체 이미지를 표시함
  imageElement.src = nextFallbackImage;
}
