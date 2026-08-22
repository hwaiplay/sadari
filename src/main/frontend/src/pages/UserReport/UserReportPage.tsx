import { message } from "@/app/messages/message";
import type { UserReportLocationState } from "@/components/UserActionMenu/userActionMenu.types";
import { useState, type ChangeEvent, type FormEvent, type ReactNode } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import * as styles from "./UserReportPage.css";

const OTHER_REASON = "CMPL_OTHER";

/**
 * 전달된 글 또는 댓글 정보를 확인하고 신고 사유를 선택하는 화면을 렌더링한다.
 *
 * @author Hanwon.Jang
 * @return 신고 사유 선택 페이지
 */
const UserReportPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const reportState = location.state as UserReportLocationState | null;
  const [selectedReason, setSelectedReason] = useState("");
  const [detailReason, setDetailReason] = useState("");
  const isSubmitDisabled = !selectedReason
    || (selectedReason === OTHER_REASON && !detailReason.trim());

  // 신고 대상 정보 없이 직접 접근한 경우 안전한 기본 화면으로 이동한다.
  if (!reportState?.target) {
    // 대상이 없는 직접 접근을 홈 화면으로 대체한다
    return <Navigate to="/home" replace />;
  }

  const { target } = reportState;
  // "독후감"
  const reportTargetLabel = message("frontend.userReport.target.report");
  // "댓글"
  const replyTargetLabel = message("frontend.common.comment");
  const targetTypeLabel =
    target.targetType === "REPORT" ? reportTargetLabel : replyTargetLabel;
  // "스팸 및 홍보"
  const spamReason = message("frontend.userReport.reason.spam");
  // "욕설 및 괴롭힘"
  const abuseReason = message("frontend.userReport.reason.abuse");
  // "음란 및 성적 콘텐츠"
  const sexualReason = message("frontend.userReport.reason.sexual");
  // "개인정보 노출"
  const privacyReason = message("frontend.userReport.reason.privacy");
  // "불법 및 권리 침해"
  const illegalReason = message("frontend.userReport.reason.illegal");
  // "기타"
  const otherReason = message("frontend.userReport.reason.other");
  const reportReasons = [
    { value: "CMPL_SPAM", label: spamReason },
    { value: "CMPL_ABUSE", label: abuseReason },
    { value: "CMPL_SEXUAL", label: sexualReason },
    { value: "CMPL_PRIVACY", label: privacyReason },
    { value: "CMPL_ILLEGAL", label: illegalReason },
    { value: OTHER_REASON, label: otherReason },
  ] as const;
  // "상세 신고 사유를 입력해 주세요."
  const detailPlaceholder = message("frontend.userReport.detailPlaceholder");
  // "상세 신고 사유"
  const detailAria = message("frontend.userReport.detailAria");
  // "내용 없음"
  const emptyContent = message("frontend.userReport.target.emptyContent");

  /**
   * 선택한 신고 사유를 화면 상태에 반영한다.
   *
   * @author Hanwon.Jang
   * @param event 선택된 신고 사유 입력 이벤트
   * @return 반환값이 없다
   */
  const handleReasonChange = (event: ChangeEvent<HTMLInputElement>): void => {
    // 사용자가 선택한 신고 사유 코드를 저장한다
    setSelectedReason(event.target.value);
  };

  /**
   * 기타 상세 사유 입력값을 화면 상태에 반영한다.
   *
   * @author Hanwon.Jang
   * @param event 상세 신고 사유 입력 이벤트
   * @return 반환값이 없다
   */
  const handleDetailChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {
    // 사용자가 입력한 상세 신고 사유를 저장한다
    setDetailReason(event.target.value);
  };

  /**
   * 신고 사유 한 건을 선택 가능한 라디오 항목으로 렌더링한다.
   *
   * @author Hanwon.Jang
   * @param reason 표시할 신고 사유 코드와 문구
   * @return 신고 사유 선택 항목
   */
  const renderReasonOption = (reason: (typeof reportReasons)[number]): ReactNode => {

    // 신고 사유 코드와 표시 문구를 연결한 선택 항목을 반환한다
    return (
      <label className={styles.reasonOption} key={reason.value}>
        <input
            className={styles.radio}
            type="radio"
            name="reportReason"
            value={reason.value}
            checked={selectedReason === reason.value}
            onChange={handleReasonChange}
        />
        <span>{reason.label}</span>
      </label>
    );
  };

  /**
   * 화면에서 선택한 신고 정보를 완료 페이지로 전달한다.
   *
   * @author Hanwon.Jang
   * @param event 신고 양식 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSubmit = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 양식 전송으로 페이지가 새로고침되지 않게 한다
    event.preventDefault();

    if (isSubmitDisabled) {
      // 필수 신고 사유가 없으면 완료 화면 이동을 중단한다
      return;
    }

    // 서버에 저장하지 않고 화면 전용 신고 정보를 완료 화면으로 전달한다
    navigate("/user-report/complete", {
      replace: true,
      state: {
        target,
        selectedReason,
        detailReason,
      },
    });
  };

  // 신고 대상과 사유 입력 및 제출 버튼으로 구성된 페이지를 반환한다
  return (
    /* 신고 사유 선택 페이지 영역 */
    <form className={styles.reportPage} onSubmit={handleSubmit}>
      {/* 신고 대상 정보 영역 */}
      <article className={styles.targetCard}>
        <div className={styles.targetMeta}>
          <span>{targetTypeLabel}</span>
          <span aria-hidden="true">·</span>
          <span className={styles.targetNick}>{target.userNick}</span>
        </div>
        <p className={styles.targetContent}>{target.content || emptyContent}</p>
      </article>

      <div className={styles.reasonArea}>
        <div className={styles.heading}>
          <h1 className={styles.title}>
            {/* "신고 사유" */}
            {message("frontend.userReport.title")}
          </h1>
          <p className={styles.description}>
            {/* "회원님의 신고는 익명으로 처리돼요." */}
            {message("frontend.userReport.anonymousDescription")}
          </p>
        </div>

        {/* 신고 사유 목록 영역 */}
        <fieldset className={styles.reasonFieldset}>
          {reportReasons.map(renderReasonOption)}
        </fieldset>

        {/* 상세 신고 사유 입력 영역 */}
        <textarea
            className={styles.detailTextarea}
            value={detailReason}
            maxLength={500}
            placeholder={detailPlaceholder}
            aria-label={detailAria}
            onChange={handleDetailChange}
        />
        </div>

      {/* 신고 완료 화면 이동 영역 */}
      <footer className={styles.footer}>
        <button
          className={styles.nextButton}
          type="submit"
          disabled={isSubmitDisabled}
        >
          {/* "다음" */}
          {message("frontend.common.next")}
        </button>
      </footer>
    </form>
  );
};

export default UserReportPage;
