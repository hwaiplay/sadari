import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import { useState } from "react";
import * as styles from "./BackgroundImage.css";

type BackgroundImageProps = {
  source: string;
  imageClassName: string;
  alt: string;
  fallbackSource?: string;
};

/**
 * 배경사진의 실제 로드 상태를 추적하여 완료 전까지 영역 중앙에 공통 소형 회전 링을 표시함
 *
 * @author HanWon.Jang
 * @param source 화면에 표시할 배경사진 경로
 * @param imageClassName 화면별 배경사진 크기와 배치를 지정하는 클래스명
 * @param alt 배경사진의 대체 문구
 * @param fallbackSource 원본 요청 실패 시 한 번만 사용할 대체 이미지 경로
 * @return 로드 상태 표시가 포함된 배경사진 요소
 */
const BackgroundImage = ({
  source,
  imageClassName,
  alt,
  fallbackSource,
}: BackgroundImageProps) => {
  const [loadedSource, setLoadedSource] = useState<string | null>(null);
  const [failedSource, setFailedSource] = useState<string | null>(null);
  const [unavailableSource, setUnavailableSource] = useState<string | null>(null);
  const canUseFallback = Boolean(fallbackSource) && source !== fallbackSource;
  const isFallbackActive = canUseFallback && failedSource === source;
  const displayedSource = isFallbackActive ? fallbackSource ?? source : source;
  const isLoading = loadedSource !== displayedSource;
  const isImageVisible = !isLoading && unavailableSource !== displayedSource;
  // "배경사진 불러오는 중"
  const loadingTitle = message("frontend.common.loadingBackgroundImage");

  /**
   * 현재 표시 대상 배경사진의 로드 완료 상태를 반영함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleImageLoad = (): void => {
    // 이전 경로의 최종 실패 상태가 현재 표시 이미지에 영향을 주지 않게 초기화함
    setUnavailableSource(null);
    // 현재 원본 또는 대체 배경사진의 로드 완료 경로를 저장함
    setLoadedSource(displayedSource);
  };

  /**
   * 원본 배경사진 요청 실패 시 지정된 대체 이미지를 한 번만 요청함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleImageError = (): void => {
    // 아직 대체 이미지를 요청하지 않았다면 원본 실패 상태를 기록해 경로를 교체함
    if (canUseFallback && !isFallbackActive) {
      // 실패한 원본 경로를 저장하여 다음 렌더링에서 대체 이미지를 사용함
      setFailedSource(source);
      // 대체 이미지 로드 완료 전까지 회전 링을 유지하도록 종료함
      return;
    }

    // 대체 이미지가 없거나 대체 요청도 실패하면 회전 링이 계속 남지 않게 종료 상태로 처리함
    setUnavailableSource(displayedSource);
    // 실패한 이미지 요소는 숨기되 현재 경로의 로드 상태 추적을 종료함
    setLoadedSource(displayedSource);
  };

  // 실제 이미지와 로드 중 공통 소형 회전 링을 같은 배경 영역에 반환함
  return (
    <>
      <img
        key={displayedSource}
        className={`${imageClassName} ${isImageVisible ? styles.imageLoaded : styles.imageLoading}`}
        src={displayedSource}
        alt={alt}
        onLoad={handleImageLoad}
        onError={handleImageError}
      />
      {isLoading && (
        /* 배경사진 로드 상태 표시 영역 */
        <div className={styles.loadingOverlay}>
          <Loading title={loadingTitle} isFullScreen={false} isCompact />
        </div>
      )}
    </>
  );
};

export default BackgroundImage;
