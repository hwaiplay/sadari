import type { ImgHTMLAttributes, SyntheticEvent } from "react";

export const DEFAULT_PROFILE_IMAGE = "/img/common/icon-user.svg";

type ProfileImageProps = Omit<
  ImgHTMLAttributes<HTMLImageElement>,
  "src" | "onError"
> & {
  src?: string | null;
};

/**
 * 서버 프로필 이미지 경로를 브라우저에서 사용할 수 있는 값으로 보정한다.
 *
 * @author HanWon.Jang
 * @param source 서버 또는 이미지 미리보기에서 전달받은 프로필 이미지 경로
 * @return 사용할 프로필 이미지 경로
 */
function normalizeProfileImageSource(source?: string | null) {

  // 문자열 경로의 불필요한 공백을 제거해 공백만 있는 값을 이미지 요청에 사용하지 않게 한다
  const normalizedSource = source?.trim() ?? "";
  // 대소문자 차이와 관계없이 비어 있는 문자열 값과 알려진 경로 형식을 판정한다
  const lowerSource = normalizedSource.toLowerCase();

  // DB 또는 API에서 빈 값을 문자열로 반환한 경우에도 기본 프로필 이미지를 사용한다
  if (!normalizedSource || lowerSource === "null" || lowerSource === "undefined") {
    // 기본 프로필 이미지 경로를 반환한다
    return DEFAULT_PROFILE_IMAGE;
  }

  // HTTPS 화면에서 차단될 수 있는 Kakao 프로필 CDN 주소는 보안 프로토콜로 변환한다
  if (lowerSource.startsWith("http://k.kakaocdn.net/")) {
    // Kakao 프로필 이미지의 경로는 유지하고 프로토콜만 HTTPS로 변경한다
    return normalizedSource.replace("http://", "https://");
  }

  // 업로드 상대 경로에 루트 슬래시가 빠졌으면 현재 화면 URL에 종속되지 않게 보정한다
  if (lowerSource.startsWith("uploads/")) {
    // 서버 업로드 정적 리소스의 루트 상대 경로를 반환한다
    return `/${normalizedSource}`;
  }

  // 유효한 프로필 이미지 경로를 반환한다
  return normalizedSource;
}

/**
 * 프로필 이미지가 없거나 로드되지 않을 때 기본 프로필 이미지를 표시한다.
 *
 * @author HanWon.Jang
 * @param props 이미지 경로와 프로필 이미지 요소 속성
 * @return 실패 대체 처리가 적용된 프로필 이미지 요소
 */
function ProfileImage({ src, ...imageProps }: ProfileImageProps) {

  // 화면에 전달할 프로필 이미지 경로를 빈 값 정책에 맞게 보정한다
  const imageSource = normalizeProfileImageSource(src);

  /**
   * 원본 이미지 로드 실패 시 기본 프로필 이미지로 한 번만 교체한다.
   *
   * @author HanWon.Jang
   * @param event 이미지 로드 실패 이벤트
   * @return 반환값이 없다
   */
  const handleImageError = (event: SyntheticEvent<HTMLImageElement>) => {

    const failedImage = event.currentTarget;

    // 기본 이미지 자체의 실패에서는 같은 경로를 반복 대입하지 않는다
    if (failedImage.getAttribute("src") === DEFAULT_PROFILE_IMAGE) {
      // 추가 대체 처리 없이 종료한다
      return;
    }

    // 만료되거나 존재하지 않는 원본 경로를 기본 프로필 이미지로 교체한다
    failedImage.src = DEFAULT_PROFILE_IMAGE;
  };

  // 경로 보정과 로드 실패 대체가 적용된 프로필 이미지를 반환한다
  return <img {...imageProps} src={imageSource} onError={handleImageError} />;
}

export default ProfileImage;
