import { Link } from "react-router-dom";
import * as s from "./button.css";
import clsx from "clsx";

type ButtonProps = {
  variant?: keyof typeof s.buttonVariant;
  size?: keyof typeof s.buttonSize;
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
};

export function Button({
  variant = "primary",
  children,
  size = "md",
  onClick,
  className = "",
}: ButtonProps) {
  return (
    <button
      className={clsx(
        s.buttonBase,
        s.buttonVariant[variant],
        s.buttonSize[size],
        className,
      )}
      onClick={onClick}
    >
      {children}
    </button>
  );
}
