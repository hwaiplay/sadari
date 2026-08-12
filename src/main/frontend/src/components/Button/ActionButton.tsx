/**
 * 모든 화면의 저장과 수정 및 삭제 명령 버튼을 공통 구조로 제공한다
 *
 * @author SeungHyeon.Kang
 */
import { clsx } from "clsx";
import type { ButtonHTMLAttributes, ReactNode } from "react";
import * as styles from "./ActionButton.css";

type ActionButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: keyof typeof styles.variant;
  size?: keyof typeof styles.size;
  width?: keyof typeof styles.width;
  icon?: ReactNode;
};

/**
 * 화면 명령의 중요도와 크기 및 선택적 왼쪽 아이콘을 버튼에 적용한다
 *
 * @author SeungHyeon.Kang
 * @param props 버튼 변형과 크기 및 기본 버튼 속성
 * @return 공통 화면 명령 버튼
 */
export function ActionButton({
  variant = "primary",
  size = "md",
  width = "auto",
  icon,
  className,
  children,
  type = "button",
  ...buttonProps
}: ActionButtonProps) {
  // 선택한 공통 스타일과 왼쪽 아이콘을 적용한 화면 명령 버튼을 반환한다
  return (
    <button
      {...buttonProps}
      className={clsx(
        styles.button,
        styles.variant[variant],
        styles.size[size],
        styles.width[width],
        className,
      )}
      type={type}
    >
      {/* 버튼 텍스트 왼쪽의 선택적 장식 아이콘 영역 */}
      <span className={styles.icon} aria-hidden="true">
        {icon}
      </span>
      {/* 버튼 명령 텍스트 영역 */}
      <span className={styles.label}>{children}</span>
    </button>
  );
}
