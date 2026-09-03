/**
 * src/main/frontend/src/components/Layout/Container/Container.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당함
 *
 * @author HanWon.Jang
 */
import type { ReactNode } from "react";
import { container } from "./container.css";
import { clsx } from "clsx";

/**
 * Container 화면 또는 컴포넌트를 구성함
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
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
