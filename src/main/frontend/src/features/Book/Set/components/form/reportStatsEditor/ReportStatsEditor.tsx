/**
 * 독후감 등록과 상세 직접 편집의 4열 요약 및 단계형 입력 모달을 구성한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import CalendarDatePicker from "@/features/Book/Set/components/form/datePicker/CalendarDatePicker";
import RatingField from "@/features/Book/Set/components/form/ratingField/RatingField";
import {
  REPORT_STATUS_DONE,
  REPORT_STATUS_READ,
  REPORT_STATUS_STOP,
} from "@/features/Book/constants/reportForm";
import type { ReadingStatusType } from "@/features/Book/types/book.type";
import type { CodeDetail } from "@/features/Common/utils/codeUtil";
import type { ChangeEvent, MouseEvent } from "react";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import * as styles from "./ReportStatsEditor.css";

const MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
const REPORT_STAT_STEPS = [0, 1, 2, 3] as const;

type ReportStatsEditorProps = {
  statusCodes: CodeDetail[];
  status: ReadingStatusType;
  statusFallbackLabel?: string;
  grade: number;
  pubcYsno: "Y" | "N";
  startDate: string;
  endDate: string;
  periodTitle: string;
  onStatusChange: (status: ReadingStatusType) => void | Promise<void>;
  onGradeChange: (grade: number) => void;
  onPublicChange: (pubcYsno: "Y" | "N") => void;
  onRangeChange: (startDate: string, endDate: string) => void;
  onEditStart?: () => void;
};

/**
 * YYYY-MM-DD 값을 독서기간 계산에 사용할 UTC 기준 일련번호로 변환한다
 *
 * @author HanWon.Jang
 * @param value 변환할 날짜 문자열
 * @return UTC 기준 일련번호 또는 잘못된 날짜의 null
 */
function getDateSerial(value: string) {

  // 날짜가 모두 입력되기 전에는 독서기간을 계산하지 않는다
  if (!value) {
    // 미입력 날짜의 계산 결과를 반환한다
    return null;
  }

  // 브라우저 시간대에 따른 날짜 차이를 제거하도록 연월일을 분리한다
  const [year, month, day] = value.split("-").map(Number);

  // 날짜 형식이 올바르지 않으면 요약값 대신 미입력 상태를 유지한다
  if (!year || !month || !day) {
    // 잘못된 날짜의 계산 결과를 반환한다
    return null;
  }

  // 시간대 차이가 없는 UTC 기준 날짜 일련번호를 반환한다
  return Date.UTC(year, month - 1, day);
}

/**
 * 현재 로컬 날짜를 독서기간 계산에 사용할 UTC 기준 일련번호로 변환한다
 *
 * @author HanWon.Jang
 * @return 오늘 날짜의 UTC 기준 일련번호
 */
function getTodayDateSerial() {

  // 사용자 브라우저의 오늘 날짜를 기준으로 읽는 중인 기간을 계산한다
  const today = new Date();

  // 시간값을 제외한 오늘 날짜 일련번호를 반환한다
  return Date.UTC(today.getFullYear(), today.getMonth(), today.getDate());
}

/**
 * 입력된 독서 상태와 기간을 상세 화면과 같은 일수 요약으로 변환한다
 *
 * @author HanWon.Jang
 * @param startDate 독서 시작일
 * @param endDate 독서 종료일 또는 목표 종료일
 * @param isReadingStatus 읽는 중 상태 여부
 * @return N일 또는 N일째 형식의 기간 요약
 */
function getPeriodSummary(startDate: string, endDate: string, isReadingStatus: boolean) {

  // 시작일과 종료일이 모두 입력된 경우에만 4열 영역에 기간을 표시한다
  if (!startDate || !endDate) {
    // 아직 입력되지 않은 기간 표시값을 반환한다
    return "-";
  }

  // 독서 시작일을 날짜 차이 계산 기준으로 변환한다
  const startSerial = getDateSerial(startDate);
  // 읽는 중이면 오늘까지의 진행일을 사용하고 완료 또는 중단이면 저장할 종료일을 사용한다
  const endSerial = isReadingStatus ? getTodayDateSerial() : getDateSerial(endDate);

  // 유효하지 않거나 역전된 날짜는 저장 전 검증 대상이므로 요약값을 표시하지 않는다
  if (startSerial === null || endSerial === null || endSerial < startSerial) {
    // 계산할 수 없는 기간 표시값을 반환한다
    return "-";
  }

  // 시작일을 포함한 독서일 수를 계산한다
  const durationDays = Math.floor((endSerial - startSerial) / MILLISECONDS_PER_DAY) + 1;

  // 읽는 중인 독후감은 현재 몇 번째 독서일인지 표시한다
  if (isReadingStatus) {
    // "{0}일째"
    return message("frontend.report.period.inProgressDays", [durationDays]);
  }

  // "{0}일"
  return message("frontend.report.period.completedDays", [durationDays]);
}

/**
 * 독서 상태 코드에 맞는 4열 요약 글자색을 결정한다
 *
 * @author HanWon.Jang
 * @param status 독서 상태 코드
 * @return 읽는 중과 완료 및 중단 상태에 맞는 글자색 클래스
 */
function getStatusValueClassName(status: ReadingStatusType) {

  // 완료 상태는 상세 화면과 같은 연녹색으로 구분한다
  if (status === REPORT_STATUS_DONE) {
    // 완료 상태 글자색 클래스를 반환한다
    return styles.statusDone;
  }

  // 중단 상태는 상세 화면과 같은 연한 빨간색으로 구분한다
  if (status === REPORT_STATUS_STOP) {
    // 중단 상태 글자색 클래스를 반환한다
    return styles.statusStop;
  }

  // 읽는 중과 미입력 상태는 기본 글자색 클래스를 반환한다
  return styles.statusRead;
}

/**
 * 독후감 입력값을 4열로 요약하고 선택한 항목을 단계형 모달에서 수정한다
 *
 * @author HanWon.Jang
 * @param props 독후감 4열 입력값과 변경 콜백
 * @return 독후감 4열 요약과 단계형 편집 모달
 */
function ReportStatsEditor({
  statusCodes,
  status,
  statusFallbackLabel,
  grade,
  pubcYsno,
  startDate,
  endDate,
  periodTitle,
  onStatusChange,
  onGradeChange,
  onPublicChange,
  onRangeChange,
  onEditStart,
}: ReportStatsEditorProps) {

  const [activeStep, setActiveStep] = useState<number | null>(null);
  const closeButtonRef = useRef<HTMLButtonElement | null>(null);
  const portalTarget = typeof document === "undefined" ? null : document.body;
  const isReadingStatus = status === REPORT_STATUS_READ;
  const periodSummary = getPeriodSummary(startDate, endDate, isReadingStatus);
  const periodText =
    startDate && endDate
      ? `${startDate.replaceAll("-", ".")} ~ ${endDate.replaceAll("-", ".")}`
      : "-";

  // "독서 상태"
  const statusTitle = message("frontend.report.field.status");
  // "공개 여부"
  const publicTitle = message("frontend.report.field.public");
  // "평점"
  const gradeTitle = message("frontend.report.field.grade");
  // "공개"
  const publicOnLabel = message("frontend.report.public.on");
  // "비공개"
  const publicOffLabel = message("frontend.report.public.off");
  // "닫기"
  const closeLabel = message("frontend.common.close");
  // "이전"
  const previousLabel = message("frontend.common.previous");
  // "다음"
  const nextLabel = message("frontend.common.next");
  const stepTitles = [statusTitle, publicTitle, gradeTitle, periodTitle];
  let statusLabel = statusFallbackLabel || "-";

  // 현재 상태 코드에 대응하는 서버 공통코드명을 4열 요약에 사용한다
  for (const statusCode of statusCodes) {
    // 현재 선택값과 일치한 코드명을 찾으면 이후 불필요한 순회를 중단한다
    if (statusCode.comdCode === status) {
      statusLabel = statusCode.comdName;
      break;
    }
  }

  // 모달이 열린 동안 배경 스크롤과 네비게이션 위치를 함께 고정한다
  useBodyScrollLock(activeStep !== null);

  useEffect(() => {

    // 닫힌 상태에서는 키보드 이벤트와 포커스를 변경하지 않는다
    if (activeStep === null) {
      return undefined;
    }

    // 단계가 바뀔 때 모달의 닫기 버튼으로 포커스를 이동한다
    closeButtonRef.current?.focus();

    /**
     * Escape 입력으로 단계형 편집 모달을 닫는다
     *
     * @author HanWon.Jang
     * @param event 브라우저 키보드 입력 이벤트
     * @return 반환값이 없다
     */
    function handleKeyDown(event: KeyboardEvent) {

      // Escape 외의 키 입력은 현재 입력 컴포넌트에 그대로 전달한다
      if (event.key !== "Escape") {
        return;
      }

      // 입력된 값은 유지하고 단계형 편집 모달만 닫는다
      setActiveStep(null);
    }

    // 모달을 키보드로 닫을 수 있도록 문서 이벤트를 등록한다
    document.addEventListener("keydown", handleKeyDown);

    // 모달이 닫히거나 단계가 바뀌면 중복 키보드 이벤트를 제거한다
    return () => {

      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [activeStep]);

  /**
   * 선택한 4열 요약 항목의 입력 단계 모달을 연다
   *
   * @author HanWon.Jang
   * @param event 단계 번호를 가진 요약 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  function handleSummaryClick(event: MouseEvent<HTMLButtonElement>) {

    const nextStep = Number(event.currentTarget.dataset.step);

    // 허용된 네 단계만 열어 잘못된 data 속성으로 빈 모달이 나타나지 않게 한다
    if (!REPORT_STAT_STEPS.includes(nextStep as (typeof REPORT_STAT_STEPS)[number])) {
      return;
    }

    // 사용자가 선택한 독후감 입력 단계를 연다
    onEditStart?.();
    setActiveStep(nextStep);
  }

  /**
   * 현재까지 입력된 값을 유지하고 단계형 편집 모달을 닫는다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handleClose() {

    // 4열 요약이 최신 입력값을 보여주도록 모달 상태만 제거한다
    setActiveStep(null);
  }

  /**
   * 모달 바깥 배경을 직접 누른 경우 입력값을 유지하고 모달을 닫는다
   *
   * @author HanWon.Jang
   * @param event 모달 배경 클릭 이벤트
   * @return 반환값이 없다
   */
  function handleBackdropMouseDown(event: MouseEvent<HTMLDivElement>) {

    // 모달 본문에서 시작된 이벤트는 입력을 계속할 수 있도록 닫기 처리하지 않는다
    if (event.currentTarget !== event.target) {
      return;
    }

    // 배경을 직접 누른 경우 현재 입력 단계만 닫는다
    handleClose();
  }

  /**
   * 이전 독후감 입력 단계로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handlePrevious() {

    // 첫 단계보다 앞으로 이동하지 않도록 이전 단계 번호를 보정한다
    setActiveStep((currentStep) => Math.max(0, (currentStep ?? 0) - 1));
  }

  /**
   * 다음 독후감 입력 단계로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handleNext() {

    // 마지막 단계보다 뒤로 이동하지 않도록 다음 단계 번호를 보정한다
    setActiveStep((currentStep) =>
      Math.min(REPORT_STAT_STEPS.length - 1, (currentStep ?? 0) + 1),
    );
  }

  /**
   * 모달에서 선택한 독서 상태를 상위 폼 상태에 반영한다
   *
   * @author HanWon.Jang
   * @param event 독서 상태 라디오 변경 이벤트
   * @return 반환값이 없다
   */
  function handleStatusInputChange(event: ChangeEvent<HTMLInputElement>) {

    // 수정 화면의 종료일 확인 분기까지 실행되도록 상위 상태 변경 콜백을 호출한다
    void onStatusChange(event.currentTarget.value as ReadingStatusType);
  }

  /**
   * 공개 상태를 상위 폼 상태에 반영한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handlePublicOn() {

    // 공개 선택값을 상위 폼 상태에 설정한다
    onPublicChange("Y");
  }

  /**
   * 비공개 상태를 상위 폼 상태에 반영한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handlePublicOff() {

    // 비공개 선택값을 상위 폼 상태에 설정한다
    onPublicChange("N");
  }

  /**
   * 독서 상태 공통코드를 선택 가능한 모달 항목으로 변환한다
   *
   * @author HanWon.Jang
   * @param statusCode 독서 상태 세부코드
   * @return 독서 상태 선택 항목
   */
  function renderStatusOption(statusCode: CodeDetail) {

    // 독서 상태 공통코드별 선택 항목을 반환한다
    return (
      <label className={styles.optionLabel} key={statusCode.comdCode}>
        <input
          className={styles.optionInput}
          type="radio"
          value={statusCode.comdCode}
          checked={status === statusCode.comdCode}
          onChange={handleStatusInputChange}
        />
        <span className={styles.optionButton}>{statusCode.comdName}</span>
      </label>
    );
  }

  /**
   * 현재 단계와 전체 단계 수를 표시하는 진행 점을 생성한다
   *
   * @author HanWon.Jang
   * @param step 독후감 입력 단계 번호
   * @return 현재 단계 여부가 반영된 진행 점
   */
  function renderProgressDot(step: (typeof REPORT_STAT_STEPS)[number]) {

    // 현재 단계가 강조된 진행 점을 반환한다
    return (
      <span
        className={step === activeStep ? styles.progressDotActive : styles.progressDot}
        key={step}
        aria-hidden="true"
      />
    );
  }

  // 독후감 입력값을 유지하는 4열 요약과 단계형 모달을 반환한다
  return (
    <>
      {/* 모달이 닫혀도 기존 폼 전송 계약을 유지하는 독후감 요약 입력값 영역 */}
      <input type="hidden" name="status" value={status} />
      <input type="hidden" name="pubcYsno" value={pubcYsno} />
      <input type="hidden" name="grade" value={grade} />
      <input type="hidden" name="startDate" value={startDate} />
      <input type="hidden" name="endDate" value={endDate} />

      {/* 독서 상태와 공개 여부 및 평점과 독서기간 편집 진입 영역 */}
      <section
        className={styles.statsSection}
        aria-label={/* "독후감 요약" */ message("frontend.report.summary.aria")}
      >
        <div className={styles.statsGrid}>
          {/* 독서 상태 편집 진입 영역 */}
          <button
            className={styles.statsItem}
            type="button"
            data-step="0"
            onClick={handleSummaryClick}
          >
            <span className={styles.statsLabel}>{statusTitle}</span>
            <strong className={`${styles.statsValue} ${getStatusValueClassName(status)}`}>
              {statusLabel}
            </strong>
          </button>

          {/* 공개 여부 편집 진입 영역 */}
          <button
            className={styles.statsItem}
            type="button"
            data-step="1"
            onClick={handleSummaryClick}
          >
            <span className={styles.statsLabel}>{publicTitle}</span>
            <strong className={styles.statsValue}>
              {pubcYsno === "Y" ? publicOnLabel : publicOffLabel}
            </strong>
          </button>

          {/* 평점 편집 진입 영역 */}
          <button
            className={styles.statsItem}
            type="button"
            data-step="2"
            onClick={handleSummaryClick}
          >
            <span className={styles.statsLabel}>{gradeTitle}</span>
            <strong className={styles.gradeValue}>
              <svg className={styles.gradeStar} viewBox="0 0 24 24" aria-hidden="true">
                <path
                  d="m12 3.5 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.5Z"
                  fill="currentColor"
                  stroke="currentColor"
                  strokeWidth="1.4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
              {grade}
            </strong>
          </button>

          {/* 독서기간 편집 진입 영역 */}
          <button
            className={styles.statsItem}
            type="button"
            data-step="3"
            title={periodText === "-" ? undefined : periodText}
            onClick={handleSummaryClick}
          >
            <span className={styles.statsLabel}>{periodTitle}</span>
            <strong className={styles.statsValue}>{periodSummary}</strong>
          </button>
        </div>
      </section>

      {/* 선택한 독후감 요약값을 수정하는 단계형 모달 영역 */}
      {activeStep !== null && portalTarget
        ? createPortal(
            /* 단계형 독후감 입력 모달 배경 영역 */
            <div
              className={styles.modalOverlay}
              role="presentation"
              onMouseDown={handleBackdropMouseDown}
            >
              {/* 단계형 독후감 입력 모달 본문 영역 */}
              <section
                className={styles.modal}
                role="dialog"
                aria-modal="true"
                aria-labelledby="report-stats-modal-title"
              >
                {/* 현재 입력 단계 제목과 닫기 영역 */}
                <header className={styles.modalHeader}>
                  <h2 className={styles.modalTitle} id="report-stats-modal-title">
                    {stepTitles[activeStep]}
                  </h2>
                  <button
                    ref={closeButtonRef}
                    className={styles.closeButton}
                    type="button"
                    aria-label={closeLabel}
                    title={closeLabel}
                    onClick={handleClose}
                  >
                    <svg
                      className={styles.closeIcon}
                      viewBox="0 0 24 24"
                      aria-hidden="true"
                    >
                      <path d="m6 6 12 12M18 6 6 18" />
                    </svg>
                  </button>
                </header>

                {/* 현재 단계에 해당하는 독후감 입력 영역 */}
                <div className={styles.modalBody}>
                  {/* 독서 상태 선택 영역 */}
                  {activeStep === 0 ? (
                    <div className={styles.optionGrid}>
                      {statusCodes.map(renderStatusOption)}
                    </div>
                  ) : null}

                  {/* 공개 여부 선택 영역 */}
                  {activeStep === 1 ? (
                    <div className={styles.publicEditor}>
                      <div className={styles.publicOptionGrid}>
                        <button
                          className={
                            pubcYsno === "Y"
                              ? styles.publicOptionActive
                              : styles.publicOption
                          }
                          type="button"
                          onClick={handlePublicOn}
                        >
                          {publicOnLabel}
                        </button>
                        <button
                          className={
                            pubcYsno === "N"
                              ? styles.publicOptionActive
                              : styles.publicOption
                          }
                          type="button"
                          onClick={handlePublicOff}
                        >
                          {publicOffLabel}
                        </button>
                      </div>
                      <p className={styles.publicHelp}>
                        {/* "공개하면 다른 사용자가 이 도서 정보에서 독후감과 별점을 볼 수 있습니다." */}
                        {message("frontend.report.public.help")}
                      </p>
                    </div>
                  ) : null}

                  {/* 평점 선택 영역 */}
                  {activeStep === 2 ? (
                    <div className={styles.gradeEditor}>
                      <p className={styles.gradeHelp}>
                        {/* "평점을 남겨주세요!" */}
                        {message("frontend.report.gradeHelp")}
                      </p>
                      <RatingField value={grade} onChange={onGradeChange} />
                    </div>
                  ) : null}

                  {/* 독서 시작일과 종료일 선택 영역 */}
                  {activeStep === 3 ? (
                    <div className={styles.periodEditor}>
                      <CalendarDatePicker
                        name="modalStartDate"
                        endName="modalEndDate"
                        value={startDate}
                        endValue={endDate}
                        placeholder={
                          /* "목표 시작일" */
                          message("frontend.report.placeholder.startDate")
                        }
                        endPlaceholder={
                          /* "목표 종료일" */
                          message("frontend.report.placeholder.endDate")
                        }
                        onRangeChange={onRangeChange}
                      />
                    </div>
                  ) : null}
                </div>

                {/* 이전 단계와 현재 위치 및 다음 단계 이동 영역 */}
                <footer className={styles.modalFooter}>
                  <button
                    className={styles.stepButton}
                    type="button"
                    aria-label={previousLabel}
                    title={previousLabel}
                    disabled={activeStep === 0}
                    onClick={handlePrevious}
                  >
                    <svg
                      className={styles.stepIcon}
                      viewBox="0 0 24 24"
                      aria-hidden="true"
                    >
                      <path d="m15 5-7 7 7 7" />
                    </svg>
                  </button>
                  <div className={styles.progressDots} aria-hidden="true">
                    {REPORT_STAT_STEPS.map(renderProgressDot)}
                  </div>
                  <button
                    className={styles.stepButton}
                    type="button"
                    aria-label={nextLabel}
                    title={nextLabel}
                    disabled={activeStep === REPORT_STAT_STEPS.length - 1}
                    onClick={handleNext}
                  >
                    <svg
                      className={styles.stepIcon}
                      viewBox="0 0 24 24"
                      aria-hidden="true"
                    >
                      <path d="m9 5 7 7-7 7" />
                    </svg>
                  </button>
                </footer>
              </section>
            </div>,
            portalTarget,
          )
        : null}
    </>
  );
}

export default ReportStatsEditor;
