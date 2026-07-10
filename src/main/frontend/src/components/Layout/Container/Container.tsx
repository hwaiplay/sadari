import { ReactNode } from "react";
import { container } from "./container.css";
import clsx from "clsx";

/**
 * 페이지 콘텐츠의 최대 너비와 공통 여백을 적용하는 레이아웃 래퍼를 렌더링한다.
 * @Author Hanwon.Jang
 * @param children 감쌀 하위 콘텐츠
 * @param className 외부에서 전달하는 추가 스타일 클래스
 * @return 공통 컨테이너 컴포넌트
 */
export function Container({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return <div className={clsx(container, className)}>{children}</div>;
}
