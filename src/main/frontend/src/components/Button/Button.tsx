/**
 * src/main/frontend/src/components/Button/Button.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import * as s from "./button.css";
import clsx from "clsx";

type ButtonProps = {
  variant?: keyof typeof s.buttonVariant;
  size?: keyof typeof s.buttonSize;
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
};

/**
 * Button 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
 */
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