/**
 * src/main/frontend/src/components/Layout/Container/Container.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { ReactNode } from "react";
import { container } from "./container.css";
import clsx from "clsx";

export function Container({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return <div className={clsx(container, className)}>{children}</div>;
}