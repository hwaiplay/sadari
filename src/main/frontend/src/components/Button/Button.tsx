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
 * 공통 버튼 스타일 variant와 size를 적용한 button 요소를 렌더링한다.
 * @Author Hanwon.Jang
 * @param variant 버튼 색상과 강조 스타일 종류
 * @param children 버튼 내부 콘텐츠
 * @param size 버튼 크기 종류
 * @param onClick 클릭 이벤트 콜백
 * @param className 외부에서 전달하는 추가 스타일 클래스
 * @return 공통 버튼 컴포넌트
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
