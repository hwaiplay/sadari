import { getApiErrorMessage } from "@/app/api/resultData";
import {
  sweetConfirm,
  sweetError,
  sweetWarning,
} from "@/app/lib/sweetAlert/sweetAlert";
import { lockBodyScroll, unlockBodyScroll } from "@/app/utils/modalUtil";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
  setWithdrawalRequestApi,
  type WithdrawalReason,
  type WithdrawalType,
} from "@/features/User/api/withdrawalApi";
import {
  getWithdrawalReasonByteLength,
  MAX_WITHDRAWAL_REASON_BYTES,
  truncateWithdrawalReasonByByte,
} from "@/features/User/utils/withdrawalValidation";
import * as styles from "./WithdrawalPage.css";

const POLICY_MODAL_ANIMATION_MILLISECONDS = 180;

const WITHDRAWAL_REASONS: Array<{ value: WithdrawalReason; label: string }> = [
  { value: "LOW_USAGE", label: "서비스를 자주 사용하지 않아요" },
  { value: "INCONVENIENT", label: "이용이 불편해요" },
  { value: "PRIVACY", label: "개인정보가 걱정돼요" },
  { value: "OTHER", label: "기타" },
];

/**
 * 소프트 탈퇴와 영구 탈퇴 정책을 선택하고 Kakao 재인증을 시작합니다.
 *
 * @author HanWon.Jang
 * @return 회원 탈퇴 설정 화면
 */
function WithdrawalPage() {

  const [wthdType, setWthdType] = useState<WithdrawalType>("SOFT");
  const [wthdRson, setWthdRson] = useState<WithdrawalReason | "">("");
  const [rsonCntn, setRsonCntn] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isPolicyHelpOpen, setIsPolicyHelpOpen] = useState(false);
  const [isPolicyHelpClosing, setIsPolicyHelpClosing] = useState(false);
  const policyCloseButtonRef = useRef<HTMLButtonElement>(null);
  const policyCloseTimerRef = useRef<number | null>(null);
  const withdrawalReasonBytes = getWithdrawalReasonByteLength(rsonCntn);

  /**
   * 탈퇴 정책 도움말 모달을 열어 서비스 탈퇴와 영구 탈퇴의 차이를 안내합니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handlePolicyHelpOpen = (): void => {

    // 이전 닫기 상태를 제거해 도움말을 페이드 인 상태로 시작합니다
    setIsPolicyHelpClosing(false);
    // 탈퇴 정책 도움말 모달을 화면에 표시합니다
    setIsPolicyHelpOpen(true);
  };

  /**
   * 페이드 아웃이 끝난 탈퇴 정책 도움말을 화면에서 제거합니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const completePolicyHelpClose = (): void => {

    // 탈퇴 정책 도움말 모달을 화면에서 제거합니다
    setIsPolicyHelpOpen(false);
    // 다음 열기 동작에서 페이드 인이 적용되도록 닫기 상태를 초기화합니다
    setIsPolicyHelpClosing(false);
    // 완료된 타이머 참조를 제거합니다
    policyCloseTimerRef.current = null;
  };

  /**
   * 탈퇴 정책 도움말 모달을 닫고 기존 탈퇴 방식 선택 화면으로 돌아갑니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handlePolicyHelpClose = (): void => {

    // 이미 닫히는 중인 모달에는 중복 타이머를 만들지 않습니다
    if (isPolicyHelpClosing) {
      // 진행 중인 페이드 아웃을 유지하고 처리를 종료합니다
      return;
    }

    // 탈퇴 정책 도움말에 페이드 아웃 스타일을 적용합니다
    setIsPolicyHelpClosing(true);
    // 페이드 아웃이 끝난 뒤 모달을 DOM에서 제거합니다
    policyCloseTimerRef.current = window.setTimeout(
      completePolicyHelpClose,
      POLICY_MODAL_ANIMATION_MILLISECONDS,
    );
  };

  /**
   * 탈퇴 정책 도움말 배경을 직접 누른 경우에만 모달을 닫습니다.
   *
   * @author HanWon.Jang
   * @param event 도움말 배경 클릭 이벤트
   * @return 반환값 없음
   */
  const handlePolicyBackdropClick = (event: React.MouseEvent<HTMLDivElement>): void => {

    // 모달 본문 클릭은 유지하고 배경을 누른 경우에만 닫습니다
    if (event.target === event.currentTarget) {
      // 탈퇴 정책 도움말 모달을 닫습니다
      handlePolicyHelpClose();
    }
  };

  /**
   * 탈퇴 정책 도움말이 열려 있는 동안 스크롤과 Escape 키 동작을 관리합니다.
   *
   * @author HanWon.Jang
   * @return 모달 스크롤과 키보드 이벤트 정리 함수
   */
  function syncPolicyHelpModal(): (() => void) | undefined {

    // 도움말이 닫혀 있으면 모달 전용 브라우저 동작을 등록하지 않습니다
    if (!isPolicyHelpOpen) {
      // 등록할 모달 정리 작업이 없음을 반환합니다
      return undefined;
    }

    /**
     * Escape 키를 누르면 탈퇴 정책 도움말을 닫습니다.
     *
     * @author HanWon.Jang
     * @param event 키보드 입력 이벤트
     * @return 반환값 없음
     */
    const handlePolicyHelpKeyDown = (event: KeyboardEvent): void => {

      // Escape 키 입력만 모달 닫기 동작으로 처리합니다
      if (event.key === "Escape") {
        // 탈퇴 정책 도움말 모달을 닫습니다
        handlePolicyHelpClose();
      }
    };

    // 모달 뒤 페이지가 움직이지 않도록 본문 스크롤을 잠급니다
    lockBodyScroll();
    // 모달을 연 직후 키보드 사용자가 닫기 버튼부터 조작할 수 있게 포커스를 이동합니다
    policyCloseButtonRef.current?.focus();
    // 모달을 키보드로 닫을 수 있도록 Escape 키 이벤트를 등록합니다
    window.addEventListener("keydown", handlePolicyHelpKeyDown);

    /**
     * 탈퇴 정책 도움말이 닫히거나 페이지가 해제될 때 브라우저 상태를 복구합니다.
     *
     * @author HanWon.Jang
     * @return 반환값 없음
     */
    const cleanupPolicyHelpModal = (): void => {

      // 페이지 이동 중 남아 있는 닫기 타이머가 상태를 변경하지 않도록 해제합니다
      if (policyCloseTimerRef.current !== null) {
        // 예약된 모달 닫기 작업을 취소합니다
        window.clearTimeout(policyCloseTimerRef.current);
        // 해제한 타이머 참조를 제거합니다
        policyCloseTimerRef.current = null;
      }

      // 모달이 닫힌 뒤 기존 페이지를 다시 스크롤할 수 있게 복구합니다
      unlockBodyScroll();
      // 중복 키보드 처리를 막기 위해 Escape 키 이벤트를 해제합니다
      window.removeEventListener("keydown", handlePolicyHelpKeyDown);
    };

    // 모달 종료 시 실행할 브라우저 상태 정리 함수를 반환합니다
    return cleanupPolicyHelpModal;
  }

  // 탈퇴 정책 도움말의 열림 상태에 맞춰 스크롤과 키보드 동작을 동기화합니다
  useEffect(syncPolicyHelpModal, [isPolicyHelpOpen]);

  /**
   * 탈퇴 사유 입력을 UTF-8 최대 저장 바이트 안에서 화면 상태에 반영합니다.
   *
   * @author HanWon.Jang
   * @param event 탈퇴 사유 입력 이벤트
   * @return 반환값 없음
   */
  const handleWithdrawalReasonChange = (event: React.ChangeEvent<HTMLTextAreaElement>): void => {

    // 다중 바이트 문자를 포함한 입력을 500바이트 안으로 제한합니다
    const limitedReason = truncateWithdrawalReasonByByte(event.target.value);
    // 검증된 탈퇴 사유를 입력 영역에 반영합니다
    setRsonCntn(limitedReason);
  };

  /**
   * 입력한 탈퇴 정책을 확인하고 Kakao 재인증 화면으로 이동합니다.
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handleWithdrawal = async (): Promise<void> => {

    // 필수 탈퇴 사유가 없으면 재인증 요청을 보내지 않습니다
    if (!wthdRson) {
      // "탈퇴 사유를 선택해주세요."
      await sweetWarning("탈퇴 사유를 선택해주세요.");
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

    // 탈퇴 유형별 영향을 확인한 사용자만 Kakao 재인증을 진행합니다
    const confirmResult = await sweetConfirm({
      title: wthdType === "SOFT" ? "서비스를 탈퇴하시겠어요?" : "영구 탈퇴를 신청하시겠어요?",
      text: wthdType === "SOFT"
        ? "다시 로그인하면 계정은 복구되지만 독후감 공개 설정과 알림은 복구되지 않아요."
        : "30일 뒤 계정과 관련 데이터가 영구 삭제돼요. 삭제 전에는 신청을 취소할 수 있어요.",
    });

    // 사용자가 확인하지 않으면 탈퇴 재인증을 시작하지 않습니다
    if (!confirmResult.isConfirmed) {
      // 사용자의 취소 선택을 유지하고 처리를 종료합니다
      return;
    }

    // 중복 요청을 막기 위해 재인증 URL을 받는 동안 버튼을 잠급니다
    setIsSubmitting(true);

    // API 실패를 사용자 안내로 전환하기 위한 비동기 처리 블록입니다
    try {
      // 입력한 탈퇴 유형과 사유로 Kakao 재인증 URL을 요청합니다
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

    // 탈퇴 재인증 시작 실패 원인을 사용자에게 안내합니다
    catch (error) {
      // "탈퇴 요청을 시작할 수 없어요."
      await sweetError("탈퇴 요청을 시작할 수 없어요.", getApiErrorMessage(error, "잠시 후 다시 시도해주세요."));
    }

    // 재인증 화면으로 이동하지 못한 경우 버튼을 다시 활성화합니다
    finally {
      // 탈퇴 요청 진행 상태를 해제합니다
      setIsSubmitting(false);
    }
  };

  // 회원 탈퇴 정책 입력 화면을 반환합니다
  return (
    <main className={styles.page}>
      {/* 탈퇴 방식 선택 영역 */}
      <section className={`${styles.section} ${styles.withdrawalTypeSection}`}>
        {/* 탈퇴 방식 제목 영역 */}
        <h2 className={`${styles.title} ${styles.standaloneTitle}`}>탈퇴 방식</h2>

        {/* 탈퇴 정책 도움말 버튼 영역 */}
        <button
          className={styles.helpButton}
          type="button"
          aria-label="탈퇴 정책 도움말"
          onClick={handlePolicyHelpOpen}
        >
          ?
        </button>
        <label className={styles.option}>
          <input
            className={styles.choiceInput}
            type="radio"
            checked={wthdType === "SOFT"}
            onChange={() => setWthdType("SOFT")}
          />
          <span className={styles.optionText}>
            <strong className={styles.optionTitle}>서비스 탈퇴</strong>
            <small className={styles.optionDescription}>다시 로그인하면 기존 계정을 복구할 수 있어요.</small>
          </span>
        </label>
        <label className={styles.option}>
          <input
            className={styles.choiceInput}
            type="radio"
            checked={wthdType === "HARD"}
            onChange={() => setWthdType("HARD")}
          />
          <span className={styles.optionText}>
            <strong className={styles.optionTitle}>영구 탈퇴</strong>
            <small className={styles.optionDescription}>30일 뒤 회원 정보와 관련 데이터가 영구 삭제돼요.</small>
          </span>
        </label>
      </section>

      {/* 탈퇴 사유 입력 영역 */}
      <section className={styles.section}>
        <h2 className={`${styles.title} ${styles.standaloneTitle}`}>탈퇴 사유</h2>
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

        {/* 탈퇴 사유 직접 입력 영역 */}
        <div className={styles.textareaWrap}>
          <textarea
            className={styles.textarea}
            value={rsonCntn}
            placeholder="탈퇴 사유를 직접 입력해주세요."
            onChange={handleWithdrawalReasonChange}
          />

          {/* 탈퇴 사유 UTF-8 바이트 표시 영역 */}
          <span className={styles.byteCounter}>
            {withdrawalReasonBytes}/{MAX_WITHDRAWAL_REASON_BYTES} byte
          </span>
        </div>
      </section>

      {/* Kakao 재인증 시작 버튼 영역 */}
      <button className={styles.withdrawButton} type="button" disabled={isSubmitting} onClick={handleWithdrawal}>
        {isSubmitting ? "재인증 준비 중" : "Kakao 재인증 후 탈퇴"}
      </button>

      {/* 탈퇴 정책 도움말 모달 영역 */}
      {isPolicyHelpOpen && createPortal(
        /* 탈퇴 정책 도움말 모달 배경 영역 */
        <div
          className={`${styles.policyModalBackdrop} ${
            isPolicyHelpClosing ? styles.policyModalBackdropClosing : ""
          }`}
          onClick={handlePolicyBackdropClick}
        >
          {/* 탈퇴 정책 도움말 모달 본문 영역 */}
          <section
            className={`${styles.policyModal} ${
              isPolicyHelpClosing ? styles.policyModalClosing : ""
            }`}
            role="dialog"
            aria-modal="true"
            aria-labelledby="withdrawal-policy-title"
          >
            {/* 탈퇴 정책 도움말 제목과 닫기 영역 */}
            <header className={styles.policyModalHeader}>
              <div>
                <span className={styles.policyModalEyebrow}>회원 탈퇴</span>
                <h2 className={styles.policyModalTitle} id="withdrawal-policy-title">
                  탈퇴 정책 안내
                </h2>
              </div>
              <button
                ref={policyCloseButtonRef}
                className={styles.policyModalClose}
                type="button"
                aria-label="탈퇴 정책 도움말 닫기"
                onClick={handlePolicyHelpClose}
              >
                ×
              </button>
            </header>

            {/* 서비스 탈퇴와 영구 탈퇴 정책 비교 영역 */}
            <div className={styles.policyModalBody}>
              {/* 서비스 탈퇴 정책 영역 */}
              <article className={styles.policyItem}>
                <div className={styles.policyItemHeading}>
                  <strong className={styles.policyItemTitle}>서비스 탈퇴</strong>
                  <span className={styles.policyRecoverBadge}>복구 가능</span>
                </div>
                <ul className={styles.policyList}>
                  <li>다시 로그인하면 기존 계정을 복구할 수 있어요.</li>
                  <li>독후감은 비공개로 전환되고 알림과 푸시 구독은 복구되지 않아요.</li>
                  <li>팔로우 관계는 유지되지만 다른 사용자에게 보이는 프로필 정보는 제한돼요.</li>
                </ul>
              </article>

              {/* 영구 탈퇴 정책 영역 */}
              <article className={styles.policyItem}>
                <div className={styles.policyItemHeading}>
                  <strong className={styles.policyItemTitle}>영구 탈퇴</strong>
                  <span className={styles.policyDeleteBadge}>30일 유예</span>
                </div>
                <ul className={styles.policyList}>
                  <li>신청 후 30일 동안 영구 탈퇴를 취소할 수 있어요.</li>
                  <li>30일이 지나면 회원 정보와 관련 데이터가 영구 삭제돼요.</li>
                  <li>영구 삭제가 완료된 계정과 데이터는 복구할 수 없어요.</li>
                </ul>
              </article>
            </div>

            {/* 탈퇴 정책 도움말 확인 버튼 영역 */}
            <button className={styles.policyModalConfirm} type="button" onClick={handlePolicyHelpClose}>
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
