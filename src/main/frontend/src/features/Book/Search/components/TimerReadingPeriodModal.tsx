import { message } from "@/app/messages/message";
import { formatDateValue } from "@/app/utils/dateUtil";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import * as modalControlStyles from "@/components/Modal/ModalControls.css";
import * as modalStyles from "@/features/Book/Set/components/form/reportStatsEditor/ReportStatsEditor.css";
import { useEffect, useRef, useState, type MouseEvent } from "react";
import { createPortal } from "react-dom";
import * as styles from "./TimerReadingPeriodModal.css";

const DEFAULT_TARGET_DAYS = 7;
const MIN_TARGET_DAYS = 1;

/**
 * 오늘과 목표일까지 걸리는 일수로 독후감 저장 기간을 계산한다.
 *
 * @author SeungHyeon.Kang
 * @param targetDays 오늘부터 목표일까지 걸리는 일수
 * @return yyyy-MM-dd 형식의 시작일과 종료일
 */
function getPeriodDates(targetDays: number) {

  const startDate = new Date();
  const endDate = new Date(startDate);
  // 선택한 일수가 지난 로컬 날짜를 목표 종료일로 계산한다.
  endDate.setDate(endDate.getDate() + targetDays);
  // 브라우저 로컬 날짜 기준의 독서 시작일과 종료일을 반환한다.
  return {
    startDate: formatDateValue(startDate),
    endDate: formatDateValue(endDate),
  };
}

type TimerReadingPeriodModalProps = {
  isSaving: boolean;
  onClose: () => void;
  onConfirm: (startDate: string, endDate: string) => void | Promise<void>;
};

/**
 * 독서 타이머에서 선택한 도서의 목표일까지 걸리는 일수를 입력받는 모달을 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param isSaving 읽는 중 독후감 등록 진행 여부
 * @param onClose 독서 타이머로 돌아갈 때 실행할 함수
 * @param onConfirm 선택 기간으로 읽는 중 독후감을 등록할 함수
 * @return 타이머 전용 목표 독서기간 모달
 */
function TimerReadingPeriodModal({
  isSaving,
  onClose,
  onConfirm,
}: TimerReadingPeriodModalProps) {

  const [targetDays, setTargetDays] = useState(DEFAULT_TARGET_DAYS);
  const closeButtonRef = useRef<HTMLButtonElement | null>(null);

  // 목표 독서기간 모달이 열린 동안 배경 화면 스크롤을 잠근다.
  useBodyScrollLock(true);

  /**
   * 목표 독서기간 모달의 초기 포커스와 Escape 닫기를 준비한다.
   *
   * @author SeungHyeon.Kang
   * @return 키보드 이벤트 정리 함수
   */
  function prepareModal(): () => void {

    // 키보드 사용자가 바로 닫기 명령을 찾도록 초기 포커스를 이동한다.
    closeButtonRef.current?.focus();

    /**
     * Escape 키로 목표 독서기간 모달을 닫는다.
     *
     * @author SeungHyeon.Kang
     * @param event 키보드 입력 이벤트
     * @return 반환값이 없다
     */
    function handleKeyDown(event: KeyboardEvent): void {

      // 저장 중이거나 Escape 외의 입력이면 현재 모달 상태를 유지한다.
      if (isSaving || event.key !== "Escape") {
        // 닫기 대상이 아닌 키 입력 처리를 종료한다.
        return;
      }

      // 선택값을 저장하지 않고 독서 타이머로 돌아간다.
      onClose();
    }

    // 모달이 열린 동안 Escape 키 입력을 감지한다.
    document.addEventListener("keydown", handleKeyDown);

    /**
     * 목표 독서기간 모달의 키보드 이벤트를 정리한다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    function cleanupModal(): void {
      // 닫힌 모달의 전역 키보드 이벤트를 제거한다.
      document.removeEventListener("keydown", handleKeyDown);
    }

    // Effect 해제 시 등록한 키보드 이벤트를 제거할 함수를 반환한다.
    return cleanupModal;
  }

  // 모달 생명주기에 맞춰 초기 포커스와 Escape 입력을 관리한다.
  useEffect(prepareModal, [isSaving, onClose]);

  /**
   * 목표일까지 걸리는 기간을 하루 늘린다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function increaseTargetDays(): void {

    // 현재 목표기간에 하루를 더한다.
    setTargetDays(targetDays + 1);
  }

  /**
   * 목표일까지 걸리는 기간을 최소 하루 범위에서 하루 줄인다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  function decreaseTargetDays(): void {

    // 오늘 이후의 목표일이 유지되도록 최소 하루로 보정한다.
    setTargetDays(Math.max(MIN_TARGET_DAYS, targetDays - 1));
  }

  /**
   * 모달 배경을 직접 누르면 선택값을 저장하지 않고 타이머로 돌아간다.
   *
   * @author SeungHyeon.Kang
   * @param event 모달 배경 마우스 이벤트
   * @return 반환값이 없다
   */
  function handleBackdrop(event: MouseEvent<HTMLDivElement>): void {

    // 저장 중이거나 모달 본문에서 시작된 클릭은 닫기 처리하지 않는다.
    if (isSaving || event.currentTarget !== event.target) {
      // 배경 닫기 대상이 아닌 클릭 처리를 종료한다.
      return;
    }

    // 선택값을 저장하지 않고 독서 타이머로 돌아간다.
    onClose();
  }

  /**
   * 선택한 목표 독서기간으로 읽는 중 독후감 등록을 요청한다.
   *
   * @author SeungHyeon.Kang
   * @return 독후감 등록 요청이 끝나면 완료되는 Promise
   * @throws 상위 독후감 등록 요청에 실패하면 발생한다
   */
  async function handleConfirm(): Promise<void> {

    // 저장 중에는 같은 독후감 등록 요청을 다시 보내지 않는다.
    if (isSaving) {
      // 진행 중인 저장 요청을 유지하고 중복 처리를 종료한다.
      return;
    }

    const periodDates = getPeriodDates(targetDays);
    // 오늘과 선택 일수로 계산한 목표 독서기간을 상위 등록 흐름에 전달한다.
    await onConfirm(periodDates.startDate, periodDates.endDate);
  }

  // 타이머 전용 목표일 증감 모달을 독후감 등록 모달과 같은 외형으로 반환한다.
  return createPortal(
    /* 목표 독서기간 모달 배경 영역 */
    <div className={modalStyles.modalOverlay} role="presentation" onMouseDown={handleBackdrop}>
      {/* 목표 독서기간 모달 본문 영역 */}
      <section
        className={`${modalStyles.modal} ${styles.periodModal}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="timer-reading-period-title"
      >
        {/* 목표 독서기간 제목과 닫기 영역 */}
        <header className={modalStyles.modalHeader}>
          <h2 className={modalStyles.modalTitle} id="timer-reading-period-title">
            {/* "목표 독서기간" */}
            {message("frontend.report.field.targetPeriod")}
          </h2>
          <button
            ref={closeButtonRef}
            className={modalControlStyles.roundClose}
            type="button"
            disabled={isSaving}
            aria-label={/* "닫기" */ message("frontend.common.close")}
            title={/* "닫기" */ message("frontend.common.close")}
            onClick={onClose}
          >
            ×
          </button>
        </header>

        {/* 목표일까지 걸리는 일수 설정 또는 모달 안 저장 진행 링 영역 */}
        <div className={`${modalStyles.modalBody} ${styles.periodBody}`}>
          {isSaving ? (
            <Loading isCompact />
          ) : (
            <div className={styles.periodEditor}>
              <div className={styles.periodStepper}>
                <button
                  className={styles.periodStepButton}
                  type="button"
                  disabled={targetDays <= MIN_TARGET_DAYS}
                  aria-label={/* "목표기간 줄이기" */ message("frontend.timer.book.periodDecrease")}
                  onClick={decreaseTargetDays}
                >
                  -
                </button>
                <output className={styles.periodValue} aria-live="polite">
                  {/* "{0}일" */}
                  {message("frontend.timer.book.periodDays", [targetDays])}
                </output>
                <button
                  className={styles.periodStepButton}
                  type="button"
                  aria-label={/* "목표기간 늘리기" */ message("frontend.timer.book.periodIncrease")}
                  onClick={increaseTargetDays}
                >
                  +
                </button>
              </div>
              <p className={styles.periodGuide}>
                {/* "오늘부터 {0}일 후까지 읽어요." */}
                {message("frontend.timer.book.periodSelected", [targetDays])}
              </p>
            </div>
          )}
        </div>

        {/* 목표 독서기간 닫기와 등록 확인 영역 */}
        <footer className={modalControlStyles.pairedActions}>
          <ActionButton
            variant="secondary"
            size="lg"
            width="full"
            disabled={isSaving}
            onClick={onClose}
          >
            {/* "닫기" */}
            {message("frontend.common.close")}
          </ActionButton>
          <ActionButton
            variant="primary"
            size="lg"
            width="full"
            disabled={isSaving}
            onClick={handleConfirm}
          >
            {/* "확인" */}
            {message("frontend.common.confirm")}
          </ActionButton>
        </footer>
      </section>
    </div>,
    document.body,
  );
}

export default TimerReadingPeriodModal;
