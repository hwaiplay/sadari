/**
 * fileName       : Skeleton
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 전달받은 크기로 콘텐츠 로딩 영역을 표시한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */
import type { CSSProperties, ReactElement } from "react";
import { clsx } from "clsx";
import * as styles from "./Skeleton.css";

type SkeletonProps = {
  width: CSSProperties["width"];
  height: CSSProperties["height"];
  borderRadius?: CSSProperties["borderRadius"];
  className?: string;
  ariaLabel?: string;
};

/**
 * 비동기 콘텐츠가 차지할 크기를 유지하면서 로딩 상태를 표시한다
 *
 * @author Hanwon.Jang
 * @param width 스켈레톤 너비
 * @param height 스켈레톤 높이
 * @param borderRadius 스켈레톤 모서리 반경
 * @param className 추가할 외부 스타일 클래스
 * @param ariaLabel 보조 기술에 전달할 로딩 상태 문구
 * @return 지정된 크기와 모양의 공통 스켈레톤 요소
 */
function Skeleton({
  width,
  height,
  borderRadius = 0,
  className,
  ariaLabel,
}: SkeletonProps): ReactElement {
  // 공통 모양과 사용 화면의 추가 레이아웃 스타일을 결합한다
  const skeletonClassName = clsx(styles.skeleton, className);
  const skeletonStyle: CSSProperties = {
    width,
    height,
    borderRadius,
  };

  // 로딩 대상의 크기를 유지하는 공통 스켈레톤 요소를 반환한다
  return (
    <span
      className={skeletonClassName}
      style={skeletonStyle}
      role="status"
      aria-label={ariaLabel}
      aria-hidden={!ariaLabel}
    />
  );
}

export default Skeleton;
