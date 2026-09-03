import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { setUserBlockApi } from "@/features/Social/api/socialApi";
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
  // "· 서로 팔로우 중이라면 양쪽 관계가 모두 삭제되고 자동으로 복원되지 않습니다."
  message("frontend.userAction.block.description.followRemoved"),
  // "· 같은 독서 모임의 운영 콘텐츠는 계속 표시될 수 있습니다."
  message("frontend.userAction.block.description.clubVisible"),
  // "· 차단을 해제하면 보존된 과거 좋아요와 댓글이 다시 표시될 수 있습니다."
  message("frontend.userAction.block.description.reactionsRestored"),
] as const;

type UserActionMenuProps = {
  userNick: string;
  reportTarget: SafetyReportTarget;
  reportOptions?: readonly SafetyReportOption[];
  rootClassName?: string;
  triggerClassName?: string;
  triggerIconClassName?: string;
  menuClassName?: string;
  onBlockConfirm?: () => Promise<void> | void;
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
  onBlockConfirm?: () => Promise<void> | void,
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

  // 사용자가 확인한 경우에만 실제 차단 처리가 끝날 때까지 기다린다
  if (result.isConfirmed) {
    // 확인 화면을 호출한 기능의 차단 처리 콜백을 실행한다
    await onBlockConfirm?.();
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
    /**
     * 차단 API와 화면별 완료 처리를 하나의 상태 변경 작업으로 실행한다
     *
     * @author HanWon.Jang
     * @return 반환값이 없다
     * @throws 차단 API 또는 화면 후처리 실패 시 발생
     */
    const blockUser = async (): Promise<void> => {
      // 공통 메뉴가 가진 신고 대상의 사용자 번호로 실제 차단 관계를 등록한다
      await setUserBlockApi(reportTarget.userNumb);
      // 차단 완료 뒤 현재 화면이 제공한 캐시와 이동 후처리를 실행한다
      await onBlockConfirm?.();
    };

    setIsMenuOpen(false);
    // "사용자를 차단하고 있어요."
    const loadingText = message("frontend.userAction.block.processing");
    // "차단했어요."
    const successTitle = message("frontend.userAction.block.success");
    try {
      // 사용자가 확인하면 처리 중 화면과 이동 가드를 유지한 채 차단한다
      const isConfirmed = await confirmUserBlock(userNick, async () => {
        // 차단 처리 완료까지 같은 모달을 유지하고 성공 상태로 전환한다
        await runBlockingOperation(blockUser, {
          title: loadingText,
          success: { title: successTitle },
        });
      });

      // 차단 완료 후 화면별 콜백이 없으면 안전한 이전 화면 또는 현재 목록 새로 조회로 이동한다
      if (isConfirmed && !onBlockConfirm) {
        // 공개 프로필과 독후감 상세는 차단한 대상 화면을 더 이상 유지하지 않는다
        if (window.location.pathname.startsWith("/social/profile/")
            || window.location.pathname.startsWith("/report/public-reports/target/")) {
          // 차단 성공 확인 후 사용자가 보던 안전한 이전 화면으로 이동한다
          navigate(-1);
        }

        // 목록과 댓글 화면은 서버 원본의 첫 페이지를 다시 조회한다
        else {
          // 현재 경로를 다시 열어 차단 사용자의 항목과 집계를 서버 기준으로 제거한다
          navigate(0);
        }
      }
    }

    catch (error) {
      // "사용자를 차단하지 못했습니다."
      await sweetError(
        message("frontend.userAction.block.failed"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    finally {
      // 화면 이동이 없으면 키보드 사용자가 기존 더보기 버튼에서 조작을 이어가도록 초점을 복원한다
      window.requestAnimationFrame(() => triggerRef.current?.focus());
    }
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
