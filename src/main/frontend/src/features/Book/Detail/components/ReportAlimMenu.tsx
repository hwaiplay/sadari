/**
 * 독후감별 좋아요와 댓글 알림 설정 더보기 메뉴를 표시한다
 *
 * @author SeungHyeon.Kang
 */
import { message } from "@/app/messages/message";
import { useRef, useState, type FocusEvent, type KeyboardEvent } from "react";
import type { ReportAlimType } from "../../api/bookApi";
import * as styles from "./ReportAlimMenu.css";

type ReportAlimMenuProps = {
  likeAlimYsno: "Y" | "N";
  replyAlimYsno: "Y" | "N";
  disabled?: boolean;
  onChange: (alimType: ReportAlimType, useYsno: "Y" | "N") => void;
};

/**
 * 독후감 공개 여부와 관계없이 유형별 알림 사용 여부를 변경하는 메뉴를 표시한다
 *
 * @author SeungHyeon.Kang
 * @param props 현재 설정, 변경 중 여부와 설정 변경 함수
 * @return 독후감 알림 설정 더보기 메뉴
 */
const ReportAlimMenu = ({
  likeAlimYsno,
  replyAlimYsno,
  disabled = false,
  onChange,
}: ReportAlimMenuProps) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const triggerRef = useRef<HTMLButtonElement>(null);

  /**
   * 독후감 알림 설정 더보기 메뉴의 열림 상태를 변경한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleToggleMenu = (): void => {
    // 현재 열림 상태를 반대로 변경해 같은 버튼으로 메뉴를 열고 닫는다
    setIsMenuOpen(!isMenuOpen);
  };

  /**
   * 독후감 알림 설정 메뉴 바깥으로 초점이 이동하면 메뉴를 닫는다
   *
   * @author SeungHyeon.Kang
   * @param event 메뉴 영역의 초점 이동 이벤트
   * @return 반환값이 없다
   */
  const handleMenuBlur = (event: FocusEvent<HTMLDivElement>): void => {
    // 새 초점이 메뉴 내부에 없을 때만 열림 상태를 해제한다
    if (!event.currentTarget.contains(event.relatedTarget)) {
      setIsMenuOpen(false);
    }
  };

  /**
   * Escape 입력으로 독후감 알림 설정 메뉴를 닫고 더보기 버튼에 초점을 복원한다
   *
   * @author SeungHyeon.Kang
   * @param event 메뉴 영역의 키보드 입력 이벤트
   * @return 반환값이 없다
   */
  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>): void => {
    // Escape 이외의 키 입력은 메뉴 기본 조작을 유지한다
    if (event.key !== "Escape") {
      // 별도 키보드 처리가 필요하지 않음을 반환한다
      return;
    }

    // 브라우저 기본 Escape 동작보다 메뉴 닫기와 초점 복원을 우선한다
    event.preventDefault();
    // 열려 있는 메뉴를 닫는다
    setIsMenuOpen(false);
    // 키보드 사용자가 계속 조작할 수 있도록 더보기 버튼에 초점을 복원한다
    triggerRef.current?.focus();
  };

  /**
   * 선택한 독후감 알림 유형의 현재 설정을 반대로 변경하고 메뉴를 닫는다
   *
   * @author SeungHyeon.Kang
   * @param alimType 변경할 좋아요 또는 댓글 알림 유형
   * @param currentYsno 현재 알림 사용 여부
   * @return 반환값이 없다
   */
  const handleChange = (alimType: ReportAlimType, currentYsno: "Y" | "N"): void => {
    // 설정 변경 요청을 시작하기 전에 더보기 메뉴를 닫는다
    setIsMenuOpen(false);
    // 현재 설정의 반대값을 부모 상세 화면의 변경 함수에 전달한다
    onChange(alimType, currentYsno === "Y" ? "N" : "Y");
  };

  /**
   * 독후감 좋아요 알림의 현재 설정을 반대로 변경한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleLikeClick = (): void => {
    // 현재 좋아요 알림 설정을 기준으로 변경 요청을 위임한다
    handleChange("like", likeAlimYsno);
  };

  /**
   * 독후감 댓글 알림의 현재 설정을 반대로 변경한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReplyClick = (): void => {
    // 현재 댓글 알림 설정을 기준으로 변경 요청을 위임한다
    handleChange("reply", replyAlimYsno);
  };

  // 독후감별 좋아요와 댓글 알림 설정 더보기 메뉴를 반환한다
  return (
    /* 독후감별 좋아요와 댓글 알림 설정 더보기 메뉴 영역 */
    <div
      className={styles.root}
      onBlur={handleMenuBlur}
      onKeyDown={handleKeyDown}
    >
      <button
        ref={triggerRef}
        className={styles.trigger}
        type="button"
        aria-label={/* "독후감 알림 설정" */ message("frontend.report.alim.menu")}
        aria-haspopup="menu"
        aria-expanded={isMenuOpen}
        disabled={disabled}
        onClick={handleToggleMenu}
      >
        <img
          className={styles.triggerIcon}
          src="/img/icons/icon-more.svg"
          alt=""
        />
      </button>

      {isMenuOpen ? (
        <div className={styles.menu} role="menu">
          <button
            className={styles.menuOption}
            type="button"
            role="menuitem"
            disabled={disabled}
            onClick={handleLikeClick}
          >
            <span>
              {/* "좋아요 알림" */}
              {message("frontend.report.alim.like.label")}
            </span>
            {likeAlimYsno === "Y" ? (
              <span className={styles.statusOn}>
                {/* "켜짐" */}
                {message("frontend.report.alim.status.on")}
              </span>
            ) : (
              <span className={styles.statusOff}>
                {/* "꺼짐" */}
                {message("frontend.report.alim.status.off")}
              </span>
            )}
          </button>
          <button
            className={styles.menuOption}
            type="button"
            role="menuitem"
            disabled={disabled}
            onClick={handleReplyClick}
          >
            <span>
              {/* "댓글 알림" */}
              {message("frontend.report.alim.reply.label")}
            </span>
            {replyAlimYsno === "Y" ? (
              <span className={styles.statusOn}>
                {/* "켜짐" */}
                {message("frontend.report.alim.status.on")}
              </span>
            ) : (
              <span className={styles.statusOff}>
                {/* "꺼짐" */}
                {message("frontend.report.alim.status.off")}
              </span>
            )}
          </button>
        </div>
      ) : null}
    </div>
  );
};

export default ReportAlimMenu;
