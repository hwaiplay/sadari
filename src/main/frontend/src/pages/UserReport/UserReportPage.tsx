import { message } from "@/app/messages/message";
import {
  COMPLAINT_DUPLICATED_CODE,
  getApiErrorMessage,
  ResultDataError,
} from "@/app/api/resultData";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import type { UserReportLocationState } from "@/components/UserActionMenu/userActionMenu.types";
import {
  setComplaintApi,
  type ComplaintReason,
  type ComplaintTargetType,
} from "@/features/Complaint/api/complaintApi";
import { useState, type ChangeEvent, type FormEvent } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import * as styles from "./UserReportPage.css";

const OTHER_REASON: ComplaintReason = "CMPL_OTHER";
const TARGET_TYPE_CODES = {
  USER: "CMPL_USER",
  REPORT: "CMPL_BOOK_REPORT",
  REPLY: "CMPL_REPLY",
  PROFILE: "CMPL_PROF_IMAGE",
  INTRO: "CMPL_INTRO",
} as const satisfies Record<string, ComplaintTargetType>;

// 신고 화면에서 내부 대상 유형을 사용자에게 표시할 다국어 메시지 키와 연결한다
const TARGET_TYPE_LABEL_KEYS = {
  USER: "frontend.userReport.target.user",
  REPORT: "frontend.userReport.target.report",
  REPLY: "frontend.common.comment",
  PROFILE: "frontend.userReport.target.profileImage",
  INTRO: "frontend.userReport.target.introduction",
} as const;
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
  const [isSaving, setIsSaving] = useState(false);
  const isSubmitDisabled = !selectedReason
    || (selectedReason === OTHER_REASON && !detailReason.trim());

  // 신고 대상 정보 없이 직접 접근한 경우 안전한 기본 화면으로 이동한다.
  if (!reportState?.target) {
    return <Navigate to="/home" replace />;
  }

  const { target } = reportState;
  // "사용자", "독후감", "댓글", "프로필 사진", "한줄소개"
  const targetTypeLabel = message(TARGET_TYPE_LABEL_KEYS[target.targetType]);
  // "폭력, 혐오 또는 학대"
  const abuseReason = message("frontend.userReport.reason.abuse");
  // "스캠, 사기 또는 스팸"
  const scamReason = message("frontend.userReport.reason.scam");
  // "음란 및 성적 콘텐츠"
  const sexualReason = message("frontend.userReport.reason.sexual");
  // "개인정보 노출"
  const privacyReason = message("frontend.userReport.reason.privacy");
  // "불법 및 권리 침해"
  const illegalReason = message("frontend.userReport.reason.illegal");
  // "기타"
  const otherReason = message("frontend.userReport.reason.other");
  const reportReasons = [
    { value: "CMPL_SPAM", label: scamReason },
    { value: "CMPL_ABUSE", label: abuseReason },
    { value: "CMPL_SEXUAL", label: sexualReason },
    { value: "CMPL_PRIVACY", label: privacyReason },
    { value: "CMPL_ILLEGAL", label: illegalReason },
    { value: OTHER_REASON, label: otherReason },
  ] as const satisfies readonly { value: ComplaintReason; label: string }[];
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

  /** 대상 원문 스냅샷을 포함한 신고를 접수한 뒤 완료 페이지로 이동한다. */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();

    if (isSubmitDisabled || isSaving) {
      return;
    }

    setIsSaving(true);
    try {
      // 화면에 전달된 본문 대신 서버가 대상 번호로 조회한 실제 원문을 저장한다
      await setComplaintApi({
        tagtType: TARGET_TYPE_CODES[target.targetType],
        tagtNumb: target.targetNumb,
        cmplRson: selectedReason as ComplaintReason,
        cmplCntn: detailReason.trim() || null,
      });
      // 신고 저장이 완료된 경우에만 감사 안내 화면으로 이동한다
      navigate("/user-report/complete", { state: { target }, replace: true });
    } catch (saveError) {
      // "신고를 접수하지 못했습니다."
      const errorMessage = getApiErrorMessage(
        saveError,
        message("frontend.userReport.saveFailed"),
      );
      // 서버가 동일 사용자와 대상의 기존 신고를 확인하면 재신고 제한을 안내한다
      if (saveError instanceof ResultDataError
          && Number(saveError.result.code) === COMPLAINT_DUPLICATED_CODE) {
        // "이미 신고한 대상이에요."
        await sweetWarning(message("frontend.userReport.duplicateTitle"), errorMessage);
        // 중복 신고 안내 뒤 일반 실패 알림이 이어서 표시되지 않도록 제출 처리를 종료한다
        return;
      }

      // "신고 접수 실패"
      await sweetError(message("frontend.userReport.saveFailedTitle"), errorMessage);
    } finally {
      // 성공과 실패 모두에서 신고 접수 버튼을 다시 사용할 수 있도록 상태를 해제한다
      setIsSaving(false);
    }
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
            {/* "피신고자에게 신고자 정보가 공개되지 않습니다." */}
            {message("frontend.userReport.anonymousDescription")}
          </p>
        </div>

        {/* 공통코드와 일치하는 신고 사유 목록 영역 */}
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
          disabled={isSubmitDisabled || isSaving}
        >
          {/* "접수 중입니다" 또는 "신고하기" */}
          {isSaving
            ? message("frontend.userReport.saving")
            : message("frontend.userAction.report")}
        </button>
      </footer>
    </form>
  );
};

export default UserReportPage;
