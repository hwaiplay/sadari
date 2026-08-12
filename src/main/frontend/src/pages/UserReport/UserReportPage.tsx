import { message } from "@/app/messages/message";
import type { UserReportLocationState } from "@/components/UserActionMenu/userActionMenu.types";
import { useState, type ChangeEvent, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import * as styles from "./UserReportPage.css";

const OTHER_REASON = "OTHER";
/**
 * 전달된 글 또는 댓글 정보를 확인하고 신고 사유를 선택하는 화면을 렌더링한다.
 *
 * @author HanWon.Jang
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
    return <Navigate to="/home" replace />;
  }

  const { target } = reportState;
  // "독후감"
  const reportTargetLabel = message("frontend.userReport.target.report");
  // "댓글"
  const replyTargetLabel = message("frontend.userReport.target.reply");
  const targetTypeLabel =
    target.targetType === "REPORT" ? reportTargetLabel : replyTargetLabel;
  // "마음에 들지 않습니다"
  const dislikeReason = message("frontend.userReport.reason.dislike");
  // "폭력, 혐오 또는 학대"
  const abuseReason = message("frontend.userReport.reason.abuse");
  // "스캠, 사기 또는 스팸"
  const scamReason = message("frontend.userReport.reason.scam");
  // "지식재산권 침해"
  const intellectualPropertyReason = message("frontend.userReport.reason.ip");
  // "기타"
  const otherReason = message("frontend.userReport.reason.other");
  const reportReasons = [
    { value: "DISLIKE", label: dislikeReason },
    { value: "ABUSE", label: abuseReason },
    { value: "SCAM", label: scamReason },
    { value: "INTELLECTUAL_PROPERTY", label: intellectualPropertyReason },
    { value: OTHER_REASON, label: otherReason },
  ] as const;
  // "상세 신고 사유를 입력해 주세요."
  const detailPlaceholder = message("frontend.userReport.detailPlaceholder");
  // "상세 신고 사유"
  const detailAria = message("frontend.userReport.detailAria");
  // "내용 없음"
  const emptyContent = message("frontend.userReport.target.emptyContent");

  /** 선택한 신고 사유를 상태에 반영한다. */
  const handleReasonChange = (event: ChangeEvent<HTMLInputElement>): void => {
    setSelectedReason(event.target.value);
  };

  /** 기타 상세 사유 입력값을 상태에 반영한다. */
  const handleDetailChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {
    setDetailReason(event.target.value);
  };

  /** 화면 전용 신고 정보를 완료 페이지로 전달한다. */
  const handleSubmit = (event: FormEvent<HTMLFormElement>): void => {
    event.preventDefault();

    if (isSubmitDisabled) {
      return;
    }

    navigate("/user-report/complete", {
      state: {
        target,
        selectedReason,
        detailReason,
      },
    });
  };

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

        {/* 임시 신고 사유 목록 영역 */}
        <fieldset className={styles.reasonFieldset}>
          {reportReasons.map((reason) => (
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
          ))}
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
