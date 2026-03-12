import { ReactNode } from "react";
import { container } from "./container.css";

export function Container({ children }: { children: ReactNode }) {
  return <div className={container}>{children}</div>;
}
