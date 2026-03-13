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
