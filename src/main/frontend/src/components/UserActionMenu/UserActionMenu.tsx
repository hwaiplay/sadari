import { sweetConfirm } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { clsx } from "clsx";
import { useRef, useState, type FocusEvent, type KeyboardEvent, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./UserActionMenu.css";
import type { SafetyReportOption, SafetyReportTarget } from "./userActionMenu.types";

const BLOCK_DESCRIPTIONS = [
  // "· 차단된 사람은 사다리에서 회원님의 프로필 또는 독후감 콘텐츠를 찾을 수 없게 됩니다."
  message("frontend.userAction.block.description.hidden"),
  // "· 상대방에게는 회원님이 차단한 사실을 알리지 않습니다."
  message("frontend.userAction.block.description.private"),
  // "· 설정에서 언제든지 차단을 해제할 수 있습니다."
  message("frontend.userAction.block.description.reversible"),
] as const;

type UserActionMenuProps = {
  userNick: string;
  reportTarget: SafetyReportTarget;
  reportOptions?: readonly SafetyReportOption[];
  rootClassName?: string;
  triggerClassName?: string;
  triggerIconClassName?: string;
  menuClassName?: string;
  onBlockConfirm?: () => void;
};

type ReportMenuOptionsProps = {
  options: readonly SafetyReportOption[];
  onSelect: (target: SafetyReportTarget) => void;
  isMenuOpen: boolean;
  index?: number;
};

/**
 * 신고 대상 선택지를 순서대로 메뉴 항목으로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param props 신고 대상 선택지 렌더링 속성
 * @return 현재 신고 선택지와 다음 선택지 영역
 */
const ReportMenuOptions = ({
  options,
  onSelect,
  isMenuOpen,
  index = 0,
}: ReportMenuOptionsProps): ReactNode => {
  const option = options[index];

  // 표시할 신고 선택지가 없으면 재귀 렌더링을 종료한다.
  if (!option) {
    // 남은 신고 선택지가 없는 빈 영역을 반환한다.
    return null;
  }

  /**
   * 현재 메뉴 항목의 신고 대상을 상위 메뉴에 전달한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleOptionClick = (): void => {
    // 사용자가 선택한 신고 대상을 신고 화면 이동 처리에 전달한다.
    onSelect(option.target);
  };

  // 현재 신고 항목 뒤에 남은 신고 항목을 이어서 표시한다.
  return (
    <>
      <button
        className={styles.menuOption}
        type="button"
        role="menuitem"
        tabIndex={isMenuOpen ? 0 : -1}
        onClick={handleOptionClick}
      >
        {option.label}
      </button>
      <ReportMenuOptions
        options={options}
        onSelect={onSelect}
        isMenuOpen={isMenuOpen}
        index={index + 1}
      />
    </>
  );
};

/**
 * 공통 SweetAlert로 사용자 차단 여부를 확인한다.
 *
 * @author HanWon.Jang
 * @param userNick 차단 대상 사용자 닉네임
 * @param onBlockConfirm 차단 확인 후 실행할 선택 콜백
 * @return 사용자의 차단 확인 여부
 */
export const confirmUserBlock = async (
  userNick: string,
  onBlockConfirm?: () => void,
): Promise<boolean> => {
  // "{닉네임} 님을 차단 하시겠어요?"
  const result = await sweetConfirm({
    title: message("frontend.userAction.block.confirmTitle", [userNick]),
    texts: BLOCK_DESCRIPTIONS,
    // "차단"
    confirmButtonText: message("frontend.userAction.block.confirm"),
    // "취소"
    cancelButtonText: message("frontend.common.cancel"),
    customClass: "sadari-swal-user-block",
  });

  // 사용자가 확인한 경우에만 추후 연결할 차단 처리를 호출한다.
  if (result.isConfirmed) {
    onBlockConfirm?.();
  }

  // 차단 확인 결과를 호출 화면에 전달한다.
  return result.isConfirmed;
};

/**
 * 다른 사용자를 신고하거나 차단할 수 있는 공통 더보기 메뉴를 표시한다.
 *
 * @author HanWon.Jang
 * @param props 사용자 액션 메뉴 속성
 * @return 신고 및 차단 메뉴
 */
const UserActionMenu = ({
  userNick,
  reportTarget,
  reportOptions,
  rootClassName,
  triggerClassName,
  triggerIconClassName,
  menuClassName,
  onBlockConfirm,
}: UserActionMenuProps) => {
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  // "신고하기"
  const defaultReportOption: SafetyReportOption = {
    label: message("frontend.userAction.report"),
    target: reportTarget,
  };
  const resolvedReportOptions = reportOptions ?? [defaultReportOption];

  /** 더보기 메뉴의 열림 상태를 변경한다. */
  const handleToggleMenu = (): void => setIsMenuOpen((isOpen) => !isOpen);

  /** 선택한 신고 대상 정보를 화면 이동 상태에 담아 신고 사유 선택 페이지로 이동한다. */
  const handleReportClick = (target: SafetyReportTarget): void => {
    setIsMenuOpen(false);
    navigate("/user-report", { state: { target } });
  };

  /** 메뉴를 닫고 공통 SweetAlert로 차단 여부를 확인한다. */
  const handleBlockClick = async (): Promise<void> => {
    setIsMenuOpen(false);
    await confirmUserBlock(userNick, onBlockConfirm);

    window.requestAnimationFrame(() => triggerRef.current?.focus());
  };

  /** 메뉴 바깥으로 초점이 이동하면 메뉴를 닫는다. */
  const handleMenuBlur = (event: FocusEvent<HTMLDivElement>): void => {
    if (!event.currentTarget.contains(event.relatedTarget)) {
      setIsMenuOpen(false);
    }
  };

  /** Escape 입력으로 메뉴를 닫는다. */
  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>): void => {
    if (event.key !== "Escape") {
      return;
    }

    event.preventDefault();
    setIsMenuOpen(false);
    triggerRef.current?.focus();
  };

  return (
    /* 신고 및 차단 더보기 메뉴 영역 */
    <div
      className={clsx(styles.root, rootClassName)}
      onBlur={handleMenuBlur}
      onKeyDown={handleKeyDown}
    >
      <button
        ref={triggerRef}
        className={clsx(styles.trigger, triggerClassName)}
        type="button"
        aria-label={message("frontend.userAction.more")}
        aria-haspopup="menu"
        aria-expanded={isMenuOpen}
        onClick={handleToggleMenu}
      >
        <img
          className={clsx(styles.triggerIcon, triggerIconClassName)}
          src="/img/icons/icon-more.svg"
          alt="icon"
        />
      </button>

      <div
        className={clsx(styles.menu, isMenuOpen && styles.menuOpen, menuClassName)}
        role="menu"
        aria-hidden={!isMenuOpen}
      >
        <ReportMenuOptions
          options={resolvedReportOptions}
          onSelect={handleReportClick}
          isMenuOpen={isMenuOpen}
        />
        <button
          className={styles.menuOption}
          type="button"
          role="menuitem"
          tabIndex={isMenuOpen ? 0 : -1}
          onClick={handleBlockClick}
        >
          {/* "차단하기" */}
          {message("frontend.userAction.block.action")}
        </button>
      </div>
    </div>
  );
};

export default UserActionMenu;
