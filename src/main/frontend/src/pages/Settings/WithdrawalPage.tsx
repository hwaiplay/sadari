import { getApiErrorMessage } from "@/app/api/resultData";
import {
  sweetConfirm,
  sweetError,
  sweetWarning,
} from "@/app/lib/sweetAlert/sweetAlert";
import { lockBodyScroll, unlockBodyScroll } from "@/app/utils/modalUtil";
import { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
  setWithdrawalRequestApi,
  type WithdrawalReason,
  type WithdrawalType,
} from "@/features/User/api/withdrawalApi";
import {
  getWithdrawReasonByteLen,
  MAX_WITHDRAWAL_REASON_BYTES,
  truncateWithdrawalReason,
} from "@/features/User/utils/withdrawalValidation";
import { POPUP_CONTENT_KEYS } from "@/features/Popup/api/popupContentApi";
import { usePopupContent } from "@/features/Popup/hooks/usePopupContent";
import { parsePopupContentList } from "@/features/Popup/utils/popupContentUtil";
import * as styles from "./WithdrawalPage.css";

const POLICY_MODAL_ANIMATION_MILLISECONDS = 180;

const WITHDRAWAL_REASONS: Array<{ value: WithdrawalReason; label: string }> = [
  // "서비스를 자주 사용하지 않아요"
  { value: "LOW_USAGE", label: "서비스를 자주 사용하지 않아요" },
  // "이용이 불편해요"
  { value: "INCONVENIENT", label: "이용이 불편해요" },
  // "개인정보가 걱정돼요"
  { value: "PRIVACY", label: "개인정보가 걱정돼요" },
  // "기타"
  { value: "OTHER", label: "기타" },
];

const DEFAULT_SOFT_POLICY_ITEMS = [
  // "다시 로그인하면 기존 계정을 복구할 수 있어요."
  "다시 로그인하면 기존 계정을 복구할 수 있어요.",
  // "독후감은 비공개로 전환되고 알림과 푸시 구독은 복구되지 않아요."
  "독후감은 비공개로 전환되고 알림과 푸시 구독은 복구되지 않아요.",
  // "팔로우 관계는 유지되지만 다른 사용자에게 보이는 프로필 정보는 제한돼요."
  "팔로우 관계는 유지되지만 다른 사용자에게 보이는 프로필 정보는 제한돼요.",
] as const;

const DEFAULT_HARD_POLICY_ITEMS = [
  // "신청 후 30일 동안 영구 탈퇴를 취소할 수 있어요."
  "신청 후 30일 동안 영구 탈퇴를 취소할 수 있어요.",
  // "30일이 지나면 회원 정보와 관련 데이터가 영구 삭제돼요."
  "30일이 지나면 회원 정보와 관련 데이터가 영구 삭제돼요.",
  // "영구 삭제가 완료된 계정과 데이터는 복구할 수 없어요."
  "영구 삭제가 완료된 계정과 데이터는 복구할 수 없어요.",
] as const;

/**
 * 계정 비활성화와 영구 탈퇴 정책을 비교해 선택하고 Kakao 재인증을 시작합니다.
 *
 * @author HanWon.Jang
 * @param hardOnly 정지 회원에게 영구 탈퇴 선택지만 제공할지 여부
 * @return 계정 비활성화 및 영구 탈퇴 설정 화면
 */
function WithdrawalPage({ hardOnly = false }: { hardOnly?: boolean }) {

  const [wthdType, setWthdType] = useState<WithdrawalType>(hardOnly ? "HARD" : "SOFT");
  const [wthdRson, setWthdRson] = useState<WithdrawalReason | "">("");
  const [rsonCntn, setRsonCntn] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPolicyHelpOpen, setIsPolicyHelpOpen] = useState(false);
  const [isPolicyHelpClosing, setIsPolicyHelpClosing] = useState(false);
  const policyCloseButtonRef = useRef<HTMLButtonElement>(null);
  const policyCloseTimerRef = useRef<number | null>(null);
  const policyHelpClosingRef = useRef(false);
  // 계정 처리 정책 도움말에 표시할 관리자 설정 콘텐츠를 미리 조회한다
  const { data: withdrawalPolicyContent } = usePopupContent(
    POPUP_CONTENT_KEYS.accountWithdrawalPolicy,
  );
  // 비활성화 정책 JSON을 검증하고 조회 전이나 실패 시 현재 기본 문구를 유지한다
  const softPolicyItems = parsePopupContentList(
    withdrawalPolicyContent?.contFirs,
    DEFAULT_SOFT_POLICY_ITEMS,
  );
  // 영구 탈퇴 정책 JSON을 검증하고 조회 전이나 실패 시 현재 기본 문구를 유지한다
  const hardPolicyItems = parsePopupContentList(
    withdrawalPolicyContent?.contSeco,
    DEFAULT_HARD_POLICY_ITEMS,
  );
  const withdrawalReasonBytes = getWithdrawReasonByteLen(rsonCntn);

  /**
   * 비활성화와 영구 탈퇴 정책을 비교할 수 있는 도움말 팝업을 엽니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handlePolicyHelpOpen = useCallback((): void => {

    // 이전 닫기 상태를 제거해 도움말 팝업을 열림 애니메이션으로 시작합니다
    policyHelpClosingRef.current = false;
    // 화면에 남아 있는 닫기 애니메이션 상태를 제거합니다
    setIsPolicyHelpClosing(false);
    // 계정 처리 정책 도움말을 화면에 표시합니다
    setIsPolicyHelpOpen(true);
  }, []);

  /**
   * 닫기 애니메이션이 끝난 계정 처리 정책 도움말을 화면에서 제거합니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const completePolicyHelpClose = useCallback((): void => {

    // 닫기 애니메이션이 끝난 도움말 팝업을 화면에서 제거합니다
    setIsPolicyHelpOpen(false);
    // 다음 열기 동작에 닫기 스타일이 남지 않도록 상태를 초기화합니다
    setIsPolicyHelpClosing(false);
    // 다음 열기 동작에서 닫기 요청을 다시 받을 수 있도록 진행 상태를 초기화합니다
    policyHelpClosingRef.current = false;
    // 완료된 닫기 타이머 참조를 제거합니다
    policyCloseTimerRef.current = null;
  }, []);

  /**
   * 계정 처리 정책 도움말에 닫기 애니메이션을 적용합니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handlePolicyHelpClose = useCallback((): void => {

    // 이미 닫는 중인 도움말에는 중복 타이머를 만들지 않습니다
    if (policyHelpClosingRef.current) {
      // 진행 중인 닫기 애니메이션을 유지하고 처리를 종료합니다
      return;
    }

    // 상태 갱신 전 연속 입력도 차단할 수 있도록 닫기 진행 여부를 즉시 기록합니다
    policyHelpClosingRef.current = true;
    // 도움말 팝업과 배경에 닫기 애니메이션을 적용합니다
    setIsPolicyHelpClosing(true);
    // 닫기 애니메이션이 끝난 뒤 팝업을 화면에서 제거합니다
    policyCloseTimerRef.current = window.setTimeout(
      completePolicyHelpClose,
      POLICY_MODAL_ANIMATION_MILLISECONDS,
    );
  }, [completePolicyHelpClose]);

  /**
   * 계정 처리 정책 팝업 바깥의 배경을 누른 경우 도움말을 닫습니다.
   *
   * @author HanWon.Jang
   * @param event 도움말 배경 클릭 이벤트
   * @return 반환값 없음
   */
  const handlePolicyBackdropClick = (event: React.MouseEvent<HTMLDivElement>): void => {

    // 팝업 본문 클릭은 유지하고 바깥 배경을 누른 경우에만 닫습니다
    if (event.target === event.currentTarget) {
      // 계정 처리 정책 도움말에 닫기 애니메이션을 적용합니다
      handlePolicyHelpClose();
    }
  };

  /**
   * 계정 처리 정책 도움말이 열린 동안 배경 스크롤과 Escape 키 동작을 관리합니다.
   *
   * @author HanWon.Jang
   * @return 팝업 종료 시 브라우저 상태를 복구하는 함수
   */
  const syncPolicyHelpModal = useCallback((): (() => void) | undefined => {

    // 도움말이 닫혀 있으면 팝업 전용 브라우저 동작을 등록하지 않습니다
    if (!isPolicyHelpOpen) {
      // 등록할 팝업 정리 작업이 없음을 반환합니다
      return undefined;
    }

    /**
     * Escape 키를 누르면 계정 처리 정책 도움말을 닫습니다.
     *
     * @author HanWon.Jang
     * @param event 키보드 입력 이벤트
     * @return 반환값 없음
     */
    const handlePolicyHelpKeyDown = (event: KeyboardEvent): void => {

      // Escape 키 입력만 도움말 닫기 동작으로 처리합니다
      if (event.key === "Escape") {
        // 계정 처리 정책 도움말에 닫기 애니메이션을 적용합니다
        handlePolicyHelpClose();
      }
    };

    // 도움말 뒤 화면이 움직이지 않도록 본문 스크롤을 잠급니다
    lockBodyScroll();
    // 키보드 사용자가 닫기 버튼부터 조작할 수 있도록 포커스를 이동합니다
    policyCloseButtonRef.current?.focus();
    // 키보드로 도움말을 닫을 수 있도록 Escape 키 이벤트를 등록합니다
    window.addEventListener("keydown", handlePolicyHelpKeyDown);

    /**
     * 도움말이 닫히거나 페이지가 해제될 때 브라우저 상태를 복구합니다.
     *
     * @author HanWon.Jang
     * @return 반환값 없음
     */
    const cleanupPolicyHelpModal = (): void => {

      // 페이지 이동 중 남아 있는 닫기 타이머가 상태를 변경하지 않도록 해제합니다
      if (policyCloseTimerRef.current !== null) {
        // 예약된 팝업 닫기 작업을 취소합니다
        window.clearTimeout(policyCloseTimerRef.current);
        // 해제한 닫기 타이머 참조를 제거합니다
        policyCloseTimerRef.current = null;
      }

      // 도움말이 닫힌 뒤 기존 화면을 다시 스크롤할 수 있게 복구합니다
      unlockBodyScroll();
      // 중복 키보드 처리를 막기 위해 Escape 키 이벤트를 해제합니다
      window.removeEventListener("keydown", handlePolicyHelpKeyDown);
    };

    // 팝업 종료 시 실행할 브라우저 상태 정리 함수를 반환합니다
    return cleanupPolicyHelpModal;
  }, [handlePolicyHelpClose, isPolicyHelpOpen]);

  // 계정 처리 정책 도움말의 열림 상태에 맞춰 스크롤과 키보드 동작을 동기화합니다
  useEffect(syncPolicyHelpModal, [syncPolicyHelpModal]);

  /**
   * 비활성화 및 탈퇴 사유 입력을 UTF-8 최대 저장 바이트 안에서 화면 상태에 반영합니다.
   *
   * @author HanWon.Jang
   * @param event 사유 입력 이벤트
   * @return 반환값 없음
   */
  const handleWithdrawalReason = (event: React.ChangeEvent<HTMLTextAreaElement>): void => {

    // 다중 바이트 문자를 포함한 입력을 500바이트 안으로 제한합니다
    const limitedReason = truncateWithdrawalReason(event.target.value);
    // 검증된 비활성화 및 탈퇴 사유를 입력 영역에 반영합니다
    setRsonCntn(limitedReason);
  };

  /**
   * 선택한 계정 처리 정책을 확인하고 Kakao 재인증 화면으로 이동합니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handleWithdrawal = async (): Promise<void> => {

    // 필수 사유가 없으면 재인증 요청을 보내지 않습니다
    if (!wthdRson) {
      // "사유를 선택해주세요."
      await sweetWarning("사유를 선택해주세요.");
      // 필수 입력 확인 이후 처리를 종료합니다
      return;
    }

    // 기타 사유는 상세 내용을 반드시 입력받습니다
    if (wthdRson === "OTHER" && !rsonCntn.trim()) {
      // "기타 사유를 입력해주세요."
      await sweetWarning("기타 사유를 입력해주세요.");
      // 기타 사유 확인 이후 처리를 종료합니다
      return;
    }

    let confirmTitle: string;
    let confirmText: string;

    // 비활성화는 재로그인 복구 범위와 자동 복원되지 않는 데이터를 다시 안내합니다
    if (wthdType === "SOFT") {
      // "계정을 비활성화하시겠어요?"
      confirmTitle = "계정을 비활성화하시겠어요?";
      // "다시 로그인하면 계정은 활성화되지만 독후감 공개 설정과 댓글, 알림, 푸시 구독은 자동 복원되지 않아요."
      confirmText = "다시 로그인하면 계정은 활성화되지만 독후감 공개 설정과 댓글, 알림, 푸시 구독은 자동 복원되지 않아요.";
    }

    // 영구 탈퇴는 유예기간과 유예기간 안의 취소 가능 여부를 다시 안내합니다
    else {
      // "영구 탈퇴를 신청하시겠어요?"
      confirmTitle = "영구 탈퇴를 신청하시겠어요?";
      // "30일 뒤 계정과 관련 데이터가 영구 삭제돼요. 삭제 전에는 신청을 취소할 수 있어요."
      confirmText = "30일 뒤 계정과 관련 데이터가 영구 삭제돼요. 삭제 전에는 신청을 취소할 수 있어요.";
    }

    // 계정 처리 유형별 영향을 확인한 사용자만 Kakao 재인증을 진행합니다
    const confirmResult = await sweetConfirm({
      title: confirmTitle,
      text: confirmText,
    });

    // 사용자가 확인하지 않으면 계정 처리 재인증을 시작하지 않습니다
    if (!confirmResult.isConfirmed) {
      // 사용자의 취소 선택을 유지하고 처리를 종료합니다
      return;
    }

    // 중복 요청을 막기 위해 재인증 URL을 받는 동안 버튼을 잠급니다
    setIsSubmitting(true);

    // API 실패를 사용자 안내로 전환하기 위한 비동기 처리 블록입니다
    try {
      // 입력한 계정 처리 유형과 사유로 Kakao 재인증 URL을 요청합니다
      const result = await setWithdrawalRequestApi({
        wthdType,
        wthdRson,
        rsonCntn: rsonCntn.trim() || undefined,
      });

      // 서버가 재인증 URL을 반환하지 않으면 화면 이동을 중단합니다
      if (!result.data?.authUrl) {
        throw new Error("Kakao 재인증 URL이 없습니다.");
      }

      // 브라우저 전체 페이지를 Kakao 재인증 화면으로 이동합니다
      window.location.assign(result.data.authUrl);
    }

    // 계정 처리 재인증 시작 실패 원인을 사용자에게 안내합니다
    catch (error) {
      // "계정 처리 요청을 시작할 수 없어요."
      // "잠시 후 다시 시도해주세요."
      await sweetError("계정 처리 요청을 시작할 수 없어요.", getApiErrorMessage(error, "잠시 후 다시 시도해주세요."));
    }

    // 재인증 화면으로 이동하지 못한 경우 버튼을 다시 활성화합니다
    finally {
      // 계정 처리 요청 진행 상태를 해제합니다
      setIsSubmitting(false);
    }
  };

  let submitButtonLabel: string;

  // 재인증 URL을 요청하는 동안 현재 처리 상태를 버튼에 표시합니다
  if (isSubmitting) {
    // "재인증 준비 중"
    submitButtonLabel = "재인증 준비 중";
  }

  // 비활성화 선택 상태에는 실행 결과를 명확히 표시합니다
  else if (wthdType === "SOFT") {
    // "Kakao 재인증 후 비활성화"
    submitButtonLabel = "Kakao 재인증 후 비활성화";
  }

  // 영구 탈퇴 선택 상태에는 되돌리기 어려운 실행 결과를 명확히 표시합니다
  else {
    // "Kakao 재인증 후 영구 탈퇴"
    submitButtonLabel = "Kakao 재인증 후 영구 탈퇴";
  }

  /**
   * 관리자 설정 또는 기본 계정 처리 정책 문구를 목록 항목으로 표시한다
   *
   * @author HanWon.Jang
   * @param policyItem 화면에 표시할 계정 처리 정책 문구
   * @return 계정 처리 정책 목록 항목
   */
  const renderPolicyItem = (policyItem: string): React.ReactNode => {
    // 개별 정책 문구를 안정적인 문자열 key와 함께 목록 항목으로 반환한다
    return <li key={policyItem}>{policyItem}</li>;
  };

  // 계정 처리 정책과 사유를 한 화면에서 선택하는 설정 화면을 반환합니다
  return (
    <main className={styles.page}>
      {/* 계정 처리 방식 선택 영역 */}
      <section className={`${styles.section} ${styles.withdrawalTypeSection}`}>
        <h2 className={`${styles.title} ${styles.standaloneTitle}`}>
          {/* "계정 처리 방식" */}
          계정 처리 방식
        </h2>

        {/* 계정 처리 정책 도움말 버튼 영역 */}
        {/* "계정 처리 정책 도움말" */}
        <button
          className={styles.helpButton}
          type="button"
          aria-label="계정 처리 정책 도움말"
          onClick={handlePolicyHelpOpen}
        >
          {/* "?" */}
          ?
        </button>

        {/* 비활성화와 영구 탈퇴의 핵심 차이를 한 줄로 비교하는 선택 카드 목록 */}
        <div className={styles.optionList}>
          {!hardOnly && <label className={styles.option}>
            <input
              className={styles.choiceInput}
              type="radio"
              checked={wthdType === "SOFT"}
              onChange={() => setWthdType("SOFT")}
            />
            <span className={styles.optionText}>
              <span className={styles.optionHeading}>
                <strong className={styles.optionTitle}>
                  {/* "비활성화" */}
                  비활성화
                </strong>
                <span className={styles.recoverBadge}>
                  {/* "복구 가능" */}
                  복구 가능
                </span>
              </span>
              <small className={styles.optionDescription}>
                {/* "다시 로그인하면 기존 계정을 복구할 수 있어요." */}
                다시 로그인하면 기존 계정을 복구할 수 있어요.
              </small>
            </span>
          </label>}

          <label className={styles.option}>
            <input
              className={styles.choiceInput}
              type="radio"
              checked={wthdType === "HARD"}
              onChange={() => setWthdType("HARD")}
            />
            <span className={styles.optionText}>
              <span className={styles.optionHeading}>
                <strong className={styles.optionTitle}>
                  {/* "영구 탈퇴" */}
                  영구 탈퇴
                </strong>
                <span className={styles.deleteBadge}>
                  {/* "30일 유예" */}
                  30일 유예
                </span>
              </span>
              <small className={styles.optionDescription}>
                {/* "신청 후 30일 동안 영구 탈퇴를 취소할 수 있어요." */}
                신청 후 30일 동안 영구 탈퇴를 취소할 수 있어요.
              </small>
            </span>
          </label>
        </div>
      </section>

      {/* 비활성화 및 탈퇴 사유 입력 영역 */}
      <section className={styles.section}>
        <h2 className={`${styles.title} ${styles.standaloneTitle}`}>
          {/* "비활성화 및 탈퇴 사유" */}
          비활성화 및 탈퇴 사유
        </h2>
        <div className={styles.reasonList}>
          {WITHDRAWAL_REASONS.map((reason) => (
            <label className={styles.reason} key={reason.value}>
              <input
                className={styles.choiceInput}
                type="radio"
                checked={wthdRson === reason.value}
                onChange={() => setWthdRson(reason.value)}
              />
              <span>{reason.label}</span>
            </label>
          ))}
        </div>

        {/* 기타 사유 직접 입력 영역 */}
        <div className={styles.textareaWrap}>
          {/* "이유를 직접 입력해주세요." */}
          <textarea
            className={styles.textarea}
            value={rsonCntn}
            placeholder="이유를 직접 입력해주세요."
            onChange={handleWithdrawalReason}
          />

          {/* 비활성화 및 탈퇴 사유 UTF-8 바이트 표시 영역 */}
          <span className={styles.byteCounter}>
            {withdrawalReasonBytes}/{MAX_WITHDRAWAL_REASON_BYTES} byte
          </span>
        </div>
      </section>

      {/* Kakao 재인증을 거친 계정 처리 시작 버튼 영역 */}
      <button className={styles.withdrawButton} type="button" disabled={isSubmitting} onClick={handleWithdrawal}>
        {submitButtonLabel}
      </button>

      {/* 계정 처리 정책 도움말 팝업 영역 */}
      {isPolicyHelpOpen && createPortal(
        /* 계정 처리 정책 도움말 팝업 배경 영역 */
        <div
          className={`${styles.policyModalBackdrop} ${
            isPolicyHelpClosing ? styles.policyModalBackdropClosing : ""
          }`}
          onClick={handlePolicyBackdropClick}
        >
          {/* 계정 처리 정책 도움말 팝업 본문 영역 */}
          <section
            className={`${styles.policyModal} ${
              isPolicyHelpClosing ? styles.policyModalClosing : ""
            }`}
            role="dialog"
            aria-modal="true"
            aria-labelledby="withdrawal-policy-title"
          >
            {/* 계정 처리 정책 도움말 제목과 닫기 영역 */}
            <header className={styles.policyModalHeader}>
              <div>
                <h2 className={styles.policyModalTitle} id="withdrawal-policy-title">
                  {/* "계정 처리 정책 안내" */}
                  계정 처리 정책 안내
                </h2>
              </div>
              {/* "계정 처리 정책 도움말 닫기" */}
              <button
                ref={policyCloseButtonRef}
                className={styles.policyModalClose}
                type="button"
                aria-label="계정 처리 정책 도움말 닫기"
                onClick={handlePolicyHelpClose}
              >
                {/* "×" */}
                ×
              </button>
            </header>

            {/* 비활성화와 영구 탈퇴 정책 비교 영역 */}
            <div className={styles.policyModalBody}>
              {/* 계정 비활성화 정책 영역 */}
              {!hardOnly && <article className={styles.policyItem}>
                <div className={styles.policyItemHeading}>
                  <strong className={styles.policyItemTitle}>
                    {/* "비활성화" */}
                    비활성화
                  </strong>
                  <span className={styles.recoverBadge}>
                    {/* "복구 가능" */}
                    복구 가능
                  </span>
                </div>
                <ul className={styles.policyList}>
                  {/* 비활성화 정책 문구 목록 */}
                  {softPolicyItems.map(renderPolicyItem)}
                </ul>
              </article>}

              {/* 영구 탈퇴 정책 영역 */}
              <article className={styles.policyItem}>
                <div className={styles.policyItemHeading}>
                  <strong className={styles.policyItemTitle}>
                    {/* "영구 탈퇴" */}
                    영구 탈퇴
                  </strong>
                  <span className={styles.deleteBadge}>
                    {/* "30일 유예" */}
                    30일 유예
                  </span>
                </div>
                <ul className={styles.policyList}>
                  {/* 영구 탈퇴 정책 문구 목록 */}
                  {hardPolicyItems.map(renderPolicyItem)}
                </ul>
              </article>
            </div>

            {/* 계정 처리 정책 도움말 확인 버튼 영역 */}
            <button className={styles.policyModalConfirm} type="button" onClick={handlePolicyHelpClose}>
              {/* "확인" */}
              확인
            </button>
          </section>
        </div>,
        document.body,
      )}
    </main>
  );
}

export default WithdrawalPage;
