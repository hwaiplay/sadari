/**
 * 독후감 상세 조회와 직접 편집 및 도서 정보 전환 화면을 구성한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { formatDateValue } from "@/app/utils/dateUtil";
import { sweetConfirm, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { useNavigate, useParams } from "react-router-dom";
import type { ChangeEvent, CSSProperties, MouseEvent } from "react";
import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { clsx } from "clsx";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { usePublicReportLikeMutation } from "@/features/Book/Detail/hook/usePublicReports";
import { useUpdateMutation } from "@/features/Book/Update/useUpdateMutation";
import { useDeleteMutation } from "@/features/Book/Delete/useDeleteMutation";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import ReportStatsEditor from "@/features/Book/Set/components/form/reportStatsEditor/ReportStatsEditor";
import {
  MAX_REPORT_CONTENT_BYTES,
  REPORT_COLOR_CODE_GROUP,
  REPORT_FORM_CODE_GROUPS,
  REPORT_STATUS_CODE_GROUP,
  REPORT_STATUS_DONE,
  REPORT_STATUS_READ,
  REPORT_STATUS_STOP,
} from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  sanitizeText,
  truncateUtf8Bytes,
  validateReportForm,
} from "@/features/Book/utils/reportValidation";
import type { ReadingStatusType } from "@/features/Book/types/book.type";
import { useCodeGroupList } from "@/features/Common/utils/codeUtil";
import * as styles from "./DetailPage.css";

// 댓글 API 연결 전 기록 헤더의 댓글 수 배치를 확인하기 위한 임시 표시값이다
const TEMPORARY_COMMENT_COUNT = 0;
const CONTENT_FADE_OUT_MILLISECONDS = 90;
const RECORD_CARET_VIEWPORT_OFFSET_PIXELS = 32;

type RecordCaretTarget = {
  // 편집 입력창에 복원할 기록 문자 위치
  caretOffset: number;
  // 기록 본문 시작점부터 클릭한 커서 줄까지의 세로 거리
  caretTopOffset: number;
};

/**
 * 읽기 상태의 기록 본문에서 사용자가 클릭한 문자와 커서 줄 위치를 계산한다
 *
 * @author HanWon.Jang
 * @param event 기록 본문 클릭 이벤트
 * @param contentLength 기록 본문 문자 길이
 * @return 편집 입력창에 복원할 커서 문자와 세로 위치
 */
function getRecordCaretTarget(
  event: MouseEvent<HTMLButtonElement>,
  contentLength: number,
): RecordCaretTarget {

  const recordButton = event.currentTarget;
  // 클릭한 커서 줄의 상대 위치를 계산할 기록 본문 영역을 조회한다
  const recordButtonRect = recordButton.getBoundingClientRect();
  let caretOffset = contentLength;
  // 표준 좌표 기반 커서 API로 클릭한 문자 위치를 조회한다
  const caretPosition = document.caretPositionFromPoint?.(
    event.clientX,
    event.clientY,
  );

  // 표준 API가 기록 텍스트 위치를 반환하면 클릭한 문자 오프셋을 사용한다
  if (caretPosition
          && caretPosition.offsetNode.nodeType === Node.TEXT_NODE
          && recordButton.contains(caretPosition.offsetNode)) {
    caretOffset = caretPosition.offset;
  }

  // 표준 API에서 기록 텍스트 위치를 찾지 못하면 WebKit 호환 API를 사용한다
  if (caretOffset === contentLength) {
    // WebKit 계열 브라우저에서 클릭한 문자 범위를 조회한다
    const fallbackRange = document.caretRangeFromPoint?.(
      event.clientX,
      event.clientY,
    );

    // 호환 API가 기록 텍스트 위치를 반환하면 클릭한 문자 오프셋을 사용한다
    if (fallbackRange
            && fallbackRange.startContainer.nodeType === Node.TEXT_NODE
            && recordButton.contains(fallbackRange.startContainer)) {
      caretOffset = fallbackRange.startOffset;
    }

  }

  // 커서 문자 위치가 실제 기록 길이를 넘지 않도록 보정한다
  const normalizedCaretOffset = Math.min(caretOffset, contentLength);
  // 키보드 편집은 본문 시작점으로 이동하고 포인터 편집은 실제 클릭 높이를 사용한다
  const caretTopOffset = event.detail === 0
    ? 0
    : Math.max(event.clientY - recordButtonRect.top, 0);

  // 입력창에 복원할 클릭 문자 위치와 본문 기준 세로 거리를 반환한다
  return {
    caretOffset: normalizedCaretOffset,
    caretTopOffset,
  };
}

/**
 * Detail Page 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function DetailPage() {

  const { id } = useParams();
  const idNum = Number(id);
  const navigate = useNavigate();
  const { data, error, isError, isPending } = useBookDetail(idNum);
  const bookData = data?.data;
  const likeMutation = usePublicReportLikeMutation();
  const { mutate: updateReport, isPending: isUpdatePending } = useUpdateMutation();
  const { mutate: deleteReport, isPending: isDeletePending } = useDeleteMutation();
  const [showBookInfo, setShowBookInfo] = useState(false);
  const [isContentFadingOut, setIsContentFadingOut] = useState(false);
  const contentSwitchTimerRef = useRef<number | null>(null);
  const recordTextAreaRef = useRef<HTMLTextAreaElement | null>(null);
  // 기록 편집으로 전환한 뒤 복원할 클릭 커서 위치를 보관한다
  const recordCaretTargetRef = useRef<RecordCaretTarget | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isRecordEditing, setIsRecordEditing] = useState(false);
  const [status, setStatus] = useState<ReadingStatusType>("");
  const [initialStatus, setInitialStatus] = useState<ReadingStatusType>("");
  const [grade, setGrade] = useState(0);
  const [pubcYsno, setPubcYsno] = useState<"Y" | "N">("N");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [content, setContent] = useState("");
  const [contentByteLength, setContentByteLength] = useState(0);

  // 상세 직접 편집과 등록 화면이 같은 공통코드 캐시를 사용하도록 상태와 색상 코드를 함께 조회한다
  const { data: codeGroupList = {} } = useCodeGroupList(
    REPORT_FORM_CODE_GROUPS,
  );
  const statusCodes = codeGroupList[REPORT_STATUS_CODE_GROUP] ?? [];
  const colorCodes = codeGroupList[REPORT_COLOR_CODE_GROUP] ?? [];

  useEffect(() => {

    // 상세 조회가 완료되기 전에는 편집 상태를 초기화하지 않는다
    if (!bookData) {
      return;
    }

    // 서버에서 조회한 독서 상태를 상세 직접 편집의 현재값과 복원 기준값으로 설정한다
    setStatus(bookData.reptStat ?? "");
    setInitialStatus(bookData.reptStat ?? "");
    // 서버에서 조회한 평점을 숫자 입력 상태로 설정한다
    setGrade(Number(bookData.reptGrde) || 0);
    // 서버에서 조회한 공개 여부를 Y와 N 중 하나로 보정해 설정한다
    setPubcYsno(bookData.pubcYsno === "Y" ? "Y" : "N");
    // 서버에서 조회한 독서 시작일과 종료일을 기간 편집 상태로 설정한다
    setStartDate(bookData.reptStdt ?? "");
    setEndDate(bookData.reptEndt ?? "");
    // 서버에서 조회한 기록과 저장 기준 바이트 길이를 함께 설정한다
    setContent(bookData.reptCntn ?? "");
    setContentByteLength(
      getReportContentStorageByteLength(bookData.reptCntn ?? ""),
    );
  }, [bookData]);

  useEffect(() => {

    // 상세 화면을 벗어난 뒤 예약된 콘텐츠 전환이 실행되지 않도록 타이머를 정리한다
    return () => {

      // 상세 하단 전환 타이머가 있을 때만 브라우저 예약 작업을 취소한다
      if (contentSwitchTimerRef.current !== null) {
        window.clearTimeout(contentSwitchTimerRef.current);
      }
    };
  }, []);

  useLayoutEffect(() => {

    const recordTextArea = recordTextAreaRef.current;

    // 기록 편집 전에는 읽기 영역의 높이를 변경하지 않는다
    if (!isRecordEditing || !recordTextArea) {
      return;
    }

    // 내용이 줄어든 경우에도 실제 줄 수에 맞춰 다시 계산할 수 있도록 기존 높이를 해제한다
    recordTextArea.style.height = "auto";
    // 스크롤 없이 전체 기록이 보이도록 현재 내용의 전체 높이를 입력창에 반영한다
    recordTextArea.style.height = `${recordTextArea.scrollHeight}px`;

    const recordCaretTarget = recordCaretTargetRef.current;

    // 기록 편집을 시작한 최초 렌더링에서만 클릭한 커서 위치를 복원한다
    if (recordCaretTarget) {
      // 입력 중 재렌더링에서 같은 이동이 반복되지 않도록 커서 복원 대상을 제거한다
      recordCaretTargetRef.current = null;
      // 커서를 복원하기 전 브라우저 기본 포커스 스크롤을 차단한다
      recordTextArea.focus({ preventScroll: true });
      // 클릭한 문자 위치가 현재 기록 길이를 넘지 않도록 보정한다
      const caretOffset = Math.min(recordCaretTarget.caretOffset, content.length);
      // 읽기 상태에서 클릭한 문자와 같은 위치에 편집 커서를 설정한다
      recordTextArea.setSelectionRange(caretOffset, caretOffset);
      // 입력창으로 전환된 뒤 커서 줄의 문서상 위치를 계산할 영역을 조회한다
      const recordTextAreaRect = recordTextArea.getBoundingClientRect();
      // 클릭한 커서 줄이 화면 상단 여백 아래에 오도록 목표 스크롤 위치를 계산한다
      const scrollTop = Math.max(
        window.scrollY + recordTextAreaRect.top
          + recordCaretTarget.caretTopOffset
          - RECORD_CARET_VIEWPORT_OFFSET_PIXELS,
        0,
      );
      // 클릭한 커서 줄을 향해 화면이 부드럽게 올라가도록 스크롤한다
      window.scrollTo({
        top: scrollTop,
        behavior: "smooth",
      });
    }

  }, [content, isRecordEditing]);

  /**
   * 독후감 상세와 도서 정보 하단 콘텐츠를 페이드아웃 후 교체한다
   *
   * @author HanWon.Jang
   * @param nextShowBookInfo 도서 정보 표시 여부
   * @return 반환값이 없다
   */
  function switchDetailContent(nextShowBookInfo: boolean) {

    // 이미 목표 콘텐츠가 보이거나 전환 중이면 중복 애니메이션을 시작하지 않는다
    if (showBookInfo === nextShowBookInfo || isContentFadingOut) {
      return;
    }

    // 현재 하단 콘텐츠를 먼저 페이드아웃한다
    setIsContentFadingOut(true);
    // 페이드아웃이 끝난 뒤 목표 콘텐츠를 표시하고 페이드인을 시작한다
    contentSwitchTimerRef.current = window.setTimeout(() => {

      setShowBookInfo(nextShowBookInfo);
      setIsContentFadingOut(false);
      contentSwitchTimerRef.current = null;
    }, CONTENT_FADE_OUT_MILLISECONDS);
  }

  /**
   * 독후감 상세에서 도서 정보 화면으로 전환한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const showBookInfoView = () => {

    // 같은 상세 페이지 안에서 도서 정보가 페이드 전환되도록 목표 화면을 설정한다
    switchDetailContent(true);
  };

  /**
   * 4열 요약 항목을 누른 최초 시점부터 상세 화면을 편집 상태로 전환한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handleEditStart() {

    // 편집 시도 전에는 숨겨진 삭제와 취소 및 저장 명령을 표시한다
    setIsEditing(true);
  }

  /**
   * 독서 상태 변경을 반영하고 읽는 중에서 완료 또는 중단으로 바뀌면 종료일 보정을 확인한다
   *
   * @author HanWon.Jang
   * @param nextStatus 사용자가 선택한 다음 독서 상태 코드
   * @return 반환값이 없다
   */
  async function handleStatusChange(nextStatus: ReadingStatusType) {

    // 선택한 독서 상태를 상세 화면 요약과 저장 요청에 즉시 반영한다
    setStatus(nextStatus);
    const needsEndDateConfirm =
      initialStatus === REPORT_STATUS_READ &&
      status === REPORT_STATUS_READ &&
      (nextStatus === REPORT_STATUS_DONE || nextStatus === REPORT_STATUS_STOP);

    // 읽는 중에서 완료나 중단으로 처음 전환하는 경우에만 종료일 변경을 확인한다
    if (!needsEndDateConfirm) {
      return;
    }

    // "독서 종료일을 오늘로 설정할까요"
    const confirmed = await sweetConfirm({
      title: message("frontend.report.doneDateConfirmTitle"),
      text: message("frontend.report.doneDateConfirmText"),
      confirmButtonText: message("frontend.common.confirm"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자가 날짜 보정에 동의하면 독서 종료일을 오늘로 설정한다
    if (confirmed.isConfirmed) {
      setEndDate(formatDateValue(new Date()));
    }
  }

  /**
   * 달력에서 확정한 독서 시작일과 종료일을 상세 편집 상태에 반영한다
   *
   * @author HanWon.Jang
   * @param nextStartDate 선택한 독서 시작일
   * @param nextEndDate 선택한 독서 종료일
   * @return 반환값이 없다
   */
  function handleRangeChange(nextStartDate: string, nextEndDate: string) {

    // 선택한 기간을 4열 요약과 저장 요청에 함께 사용할 수 있도록 설정한다
    setStartDate(nextStartDate);
    setEndDate(nextEndDate);
  }

  /**
   * 기록 본문을 클릭하면 테두리 없는 직접 입력 상태로 전환한다
   *
   * @author HanWon.Jang
   * @param event 기록 본문 클릭 이벤트
   * @return 반환값이 없다
   */
  function handleRecordEditStart(event: MouseEvent<HTMLButtonElement>) {

    // 읽기 상태에서 클릭한 문자와 커서 줄 위치를 편집 전환 후 복원할 대상으로 설정한다
    recordCaretTargetRef.current = getRecordCaretTarget(event, content.length);
    // 기록 입력과 상세 편집 명령을 함께 활성화한다
    setIsRecordEditing(true);
    setIsEditing(true);
  }

  /**
   * 기록 입력값을 저장 허용 바이트 안으로 보정해 상세 편집 상태에 반영한다
   *
   * @author HanWon.Jang
   * @param event 기록 입력 영역 변경 이벤트
   * @return 반환값이 없다
   */
  function handleContentChange(event: ChangeEvent<HTMLTextAreaElement>) {

    // UTF-8 저장 허용량을 넘는 뒷부분을 제거한 기록값을 계산한다
    const nextContent = truncateUtf8Bytes(event.currentTarget.value);

    // 보정한 기록 본문과 저장 기준 바이트 길이를 함께 갱신한다
    setContent(nextContent);
    setContentByteLength(getReportContentStorageByteLength(nextContent));
  }

  /**
   * 상세 편집값을 마지막 서버 조회값으로 복원하고 편집 명령을 숨긴다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handleEditCancel() {

    // 취소 시 서버 조회값을 다시 반영해 아직 저장하지 않은 변경을 모두 제거한다
    if (bookData) {
      setStatus(bookData.reptStat ?? "");
      setInitialStatus(bookData.reptStat ?? "");
      setGrade(Number(bookData.reptGrde) || 0);
      setPubcYsno(bookData.pubcYsno === "Y" ? "Y" : "N");
      setStartDate(bookData.reptStdt ?? "");
      setEndDate(bookData.reptEndt ?? "");
      setContent(bookData.reptCntn ?? "");
      setContentByteLength(
        getReportContentStorageByteLength(bookData.reptCntn ?? ""),
      );
    }

    // 편집 입력과 하단 명령 영역을 읽기 상태로 되돌린다
    setIsRecordEditing(false);
    setIsEditing(false);
  }

  /**
   * 상세 화면에서 변경한 독후감 값을 검증하고 저장한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  async function handleEditSave() {

    // 상세 조회값이 없으면 수정 대상과 책장 색상을 확정할 수 없어 저장하지 않는다
    if (!bookData) {
      return;
    }

    // 등록 화면과 같은 필수값 및 공통코드 검증을 상세 직접 편집에도 적용한다
    const validationMessage = validateReportForm({
      status,
      startDate,
      endDate,
      grade: String(grade),
      reptColr: bookData.reptColr,
      content,
      validStatusCodes: statusCodes.map((item) => item.comdCode),
      validReportColors: colorCodes.map((item) => item.comdCode),
    });

    // 검증에 실패하면 저장 요청 없이 누락되거나 잘못된 항목을 안내한다
    if (validationMessage) {
      // "입력이 필요합니다."
      void sweetWarning(
        message("frontend.alert.inputRequired"),
        validationMessage,
      );
      return;
    }

    // "저장하시겠습니까"
    const confirmed = await sweetConfirm({
      title: message("frontend.alert.saveConfirmTitle"),
      text: message("frontend.report.saveConfirmText"),
      confirmButtonText: message("frontend.report.save"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 저장 확인을 취소하면 현재 편집값을 유지한다
    if (!confirmed.isConfirmed) {
      return;
    }

    // 상세 화면이 보유한 값으로 별도 수정 페이지 없이 독후감 수정 요청을 전송한다
    updateReport(
      {
        reptNumb: idNum,
        data: {
          reptStat: status,
          reptStdt: startDate,
          reptEndt: endDate,
          reptGrde: String(grade),
          reptColr: bookData.reptColr,
          pubcYsno,
          reptCntn: sanitizeText(content),
        },
      },
      {
        onSuccess: () => {

          // 저장이 완료되면 상세 화면을 읽기 상태로 되돌린다
          setIsRecordEditing(false);
          setIsEditing(false);
        },
      },
    );
  }

  /**
   * 현재 독후감 삭제 여부를 확인한 뒤 삭제 요청을 전송한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  async function handleDelete() {

    // "삭제하시겠습니까"
    const confirmed = await sweetConfirm({
      icon: "warning",
      title: message("frontend.alert.deleteConfirmTitle"),
      text: message("frontend.report.deleteConfirmText"),
      confirmButtonText: message("frontend.report.delete"),
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 삭제 확인을 취소하면 상세 편집 상태를 그대로 유지한다
    if (!confirmed.isConfirmed) {
      return;
    }

    // 확인된 독후감 번호로 삭제 요청을 전송한다
    deleteReport(idNum);
  }

  /**
   * get Like Count Label 정보를 조회한다
   *
   * @author HanWon.Jang
   * @param likeCnt like Cnt 입력값
   * @return 처리 결과
   */
  const getLikeCountLabel = (likeCnt?: number) => {

    const count = Number(likeCnt) || 0;
    return count > 99 ? "99+" : String(count);
  };

  if (isPending) {
    return <Loading title={message("frontend.report.loading.detail")} />;
  }

  if (isError) {
    return <h3>{getApiErrorMessage(error, message("frontend.common.tryAgain"))}</h3>;
  }

  if (!bookData) {
    return <h3>{data?.message}</h3>;
  }

  const pageStyle = {
    "--book-bg-image": `url("${bookData.bookCvim}")`,
  } as CSSProperties;
  const isReadingStatus = status === REPORT_STATUS_READ;
  // 읽는 중인 독후감은 저장된 종료일이 목표일이므로 목표 독서기간으로 구분한다
  const periodTitle = isReadingStatus
    ? /* "목표 독서기간" */ message("frontend.report.field.targetPeriod")
    : /* "독서 기간" */ message("frontend.report.field.period");
  const rawBookAverageGrade = Number(bookData.bookAvgGrde);
  const hasBookAverageGrade =
    Number.isFinite(rawBookAverageGrade) && rawBookAverageGrade > 0;
  // "등록된 책 소개가 없습니다."
  const bookDescription =
    bookData.bookDesc || message("frontend.common.noBookDescription");
  // "독후감을 남겨보세요"
  const contentPlaceholder = message("frontend.report.placeholder.content");
  // "작성된 기록이 없습니다."
  const emptyReportContent = message("frontend.common.noWrittenReport");

  /**
   * 도서 정보 화면에서 독후감 상세 화면으로 전환한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const showReportDetailView = () => {

    // 같은 상세 페이지 안에서 독후감 정보가 페이드 전환되도록 목표 화면을 설정한다
    switchDetailContent(false);
  };

  /**
   * 현재 도서와 같은 ISBN으로 작성된 공개 독후감 목록으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const goPublicReportsPage = () => {

    // 현재 도서의 ISBN을 URL 쿼리에서 사용할 수 있는 문자열로 변환한다
    const encodedBookIsbn = encodeURIComponent(bookData.bookIsbn);

    // 현재 조회된 도서 정보를 전달해 공개 독후감 목록의 헤더를 즉시 구성한다
    navigate(
      `/book/public-reports/isbn?isbn=${encodedBookIsbn}`,
      {
        state: {
          title: bookData.bookTitl,
          author: bookData.bookAthr,
          cover: bookData.bookCvim,
          ratingAverage: bookData.bookAvgGrde,
        },
      },
    );
  };

  /**
   * 현재 독후감의 좋아요 상태를 변경한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLikeToggle = () => {

    // 상세 화면에 이미 조회된 대상 정보를 사용해 별도 독후감 조회 없이 좋아요를 변경한다
    likeMutation.mutate({
      tagtType: "REPORT",
      tagtNumb: idNum,
      targetUserNumb: bookData.userNumb,
    });
  };

  // 같은 상세 API에서 받은 도서 정보를 사용해 추가 조회 없이 도서 정보 화면을 구성한다
  if (showBookInfo) {
    return (
      /* 독후감에 연결된 도서 정보 전체 영역 */
      <main className={styles.page} style={pageStyle}>
        <Container className={styles.detail}>
          {/* 도서 표지와 도서 정보 전환 영역 */}
          <section className={styles.header}>
            <div className={styles.coverFrame}>
              <img
                className={styles.coverImage}
                src={bookData.bookCvim}
                alt={bookData.bookTitl}
              />
            </div>
            <h1 className={styles.title}>{bookData.bookTitl}</h1>

            {/* 독후감 상세의 저자 표시 줄과 높이를 맞춘 도서 평균 평점 영역 */}
            <div className={styles.bookAverageSummary}>
              {hasBookAverageGrade ? (
                <>
                  {/* 평균 평점이 있으면 평균 문구와 별 아이콘 및 점수를 표시한다 */}
                  <span className={styles.bookAverageLabel}>
                    {/* "평균" */}
                    {message("frontend.book.ratingAverageShort")}
                  </span>
                  <svg
                    className={styles.bookAverageStar}
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <path
                      d="m12 3.5 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.5Z"
                      fill="currentColor"
                      stroke="currentColor"
                      strokeWidth="1.4"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                  <strong className={styles.bookAverageScore}>
                    {bookData.bookAvgGrde}
                  </strong>
                </>
              ) : (
                <span className={styles.bookAverageEmpty}>
                  {/* "아직 별점이 없습니다." */}
                  {message("frontend.book.ratingAverageEmpty")}
                </span>
              )}
            </div>

            {/* 독후감 상세 복귀와 같은 도서의 공개 독후감 이동 영역 */}
            <div className={styles.bookInfoActionRow}>
              <button
                className={styles.bookInfoButton}
                type="button"
                onClick={showReportDetailView}
              >
                <svg
                  className={styles.bookInfoButtonIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M15 6 9 12l6 6"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                {/* "돌아가기" */}
                {message("frontend.report.backToReport")}
              </button>
              <button
                className={styles.bookInfoButton}
                type="button"
                onClick={goPublicReportsPage}
              >
                {/* "다른 사람이 쓴 독후감 보기" */}
                {message("frontend.book.publicReports.button")}
              </button>
            </div>
          </section>

          <div
            key="book-info"
            className={clsx(
              styles.contentPanel,
              isContentFadingOut
                ? styles.contentSwitchFadeOut
                : styles.contentSwitchFade,
            )}
          >
            {/* 저자와 출판사 및 출간일의 세로 요약 영역 */}
            <section
              className={styles.reportStatsSection}
              aria-label={/* "도서 정보" */ message("frontend.common.bookInfo")}
            >
              <div className={styles.bookInfoRows}>
                {/* 도서 저자 정보 행 */}
                <div className={styles.bookInfoRow}>
                  <span className={styles.bookInfoLabel}>
                    {/* "저자" */}
                    {message("frontend.common.author")}
                  </span>
                  <strong className={styles.bookInfoValue}>
                    {bookData.bookAthr || "-"}
                  </strong>
                </div>

                {/* 도서 출판사 정보 행 */}
                <div className={styles.bookInfoRow}>
                  <span className={styles.bookInfoLabel}>
                    {/* "출판사" */}
                    {message("frontend.common.publisher")}
                  </span>
                  <strong className={styles.bookInfoValue}>
                    {bookData.bookPubl || "-"}
                  </strong>
                </div>

                {/* 도서 출간일 정보 행 */}
                <div className={styles.bookInfoRow}>
                  <span className={styles.bookInfoLabel}>
                    {/* "출간일" */}
                    {message("frontend.common.publDate")}
                  </span>
                  <strong className={styles.bookInfoValue}>
                    {bookData.publDate || "-"}
                  </strong>
                </div>
              </div>
            </section>

            {/* 배경 전환 위에 표시되는 도서 소개 영역 */}
            <div className={styles.recordArea}>
              {/* 독후감 기록 카드와 같은 위치의 책 소개 영역 */}
              <section className={styles.recordSection}>
                <div className={styles.recordTitleRow}>
                  <h2 className={styles.sectionTitle}>
                    {/* "책 소개" */}
                    {message("frontend.common.bookDescription")}
                  </h2>
                </div>
                <p className={styles.contentBox}>{bookDescription}</p>
              </section>
            </div>
          </div>
        </Container>
      </main>
    );
  }

  return (
    /* 독후감 상세 정보 전체 영역 */
    <main className={styles.page} style={pageStyle}>
      <Container className={styles.detail}>
        {/* 도서 표지와 독후감 전환 영역 */}
        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={bookData.bookCvim}
              alt={bookData.bookTitl}
            />
          </div>
          <h1 className={styles.title}>{bookData.bookTitl}</h1>
          <p className={styles.meta}>{bookData.bookAthr}</p>
          <button
            className={styles.bookInfoButton}
            type="button"
            onClick={showBookInfoView}
          >
            {/* "도서 정보 자세히보기" */}
            {message("frontend.report.bookInfoMore")}
          </button>
        </section>

        <div
          key="report-detail"
          className={clsx(
            styles.contentPanel,
            isContentFadingOut
              ? styles.contentSwitchFadeOut
              : styles.contentSwitchFade,
          )}
        >
          {/* 독서 상태와 공개 여부 및 평점과 독서기간 직접 편집 영역 */}
          <ReportStatsEditor
            statusCodes={statusCodes}
            status={status}
            statusFallbackLabel={bookData.reptStatName || bookData.reptStat}
            grade={grade}
            pubcYsno={pubcYsno}
            startDate={startDate}
            endDate={endDate}
            periodTitle={periodTitle}
            onStatusChange={handleStatusChange}
            onGradeChange={setGrade}
            onPublicChange={setPubcYsno}
            onRangeChange={handleRangeChange}
            onEditStart={handleEditStart}
          />

          {/* 배경 전환 위에 표시되는 독후감 기록 영역 */}
          <div className={styles.recordArea}>
            {/* 독후감 기록과 좋아요 및 댓글 지표 영역 */}
            <section className={styles.recordSection}>
              <div className={styles.recordTitleRow}>
                <h2 className={styles.sectionTitle}>
                  {/* "기록" */}
                  {message("frontend.report.field.content")}
                </h2>
              </div>
              {/* 기록 반응 지표 영역 */}
              <div className={styles.recordMetrics}>
                <button
                  className={styles.likeButton}
                  type="button"
                  aria-label={/* "좋아요" */ message("frontend.report.like.aria")}
                  aria-pressed={bookData.likeYsno === "Y"}
                  disabled={likeMutation.isPending}
                  onClick={handleLikeToggle}
                >
                  <svg
                    className={styles.likeIcon}
                    viewBox="0 0 24 24"
                    aria-hidden="true"
                  >
                    <path
                      d="M12 20.4S4.5 16.1 3.1 10.6C2.2 7 4.3 4.5 7.1 4.5c1.7 0 3.2.9 4.1 2.2.9-1.3 2.4-2.2 4.1-2.2 2.8 0 4.9 2.5 4 6.1C17.9 16.1 12 20.4 12 20.4Z"
                      fill={bookData.likeYsno === "Y" ? "currentColor" : "none"}
                      stroke="currentColor"
                      strokeWidth="1.8"
                      strokeLinejoin="round"
                    />
                  </svg>
                  <span className={styles.likeCount}>
                    {getLikeCountLabel(bookData.likeCnt)}
                  </span>
                </button>
                <span
                  className={styles.commentIndicator}
                  role="img"
                  aria-label={/* "댓글" */ message("frontend.report.comment.aria")}
                >
                  <img
                    className={styles.commentIcon}
                    src="/img/icons/icon-comment.svg"
                    alt=""
                  />
                  <span className={styles.commentCount}>
                    {TEMPORARY_COMMENT_COUNT}
                  </span>
                </span>
              </div>
              {/* 기록 본문 직접 편집 영역 */}
              {isRecordEditing ? (
                <div className={styles.recordEditor}>
                  <textarea
                    ref={recordTextAreaRef}
                    className={styles.recordTextArea}
                    value={content}
                    aria-label={/* "기록" */ message("frontend.report.field.content")}
                    placeholder={contentPlaceholder}
                    onChange={handleContentChange}
                  />
                  <span className={styles.recordByteCounter}>
                    ({contentByteLength}/{MAX_REPORT_CONTENT_BYTES} byte)
                  </span>
                </div>
              ) : (
                <button
                  className={styles.contentEditButton}
                  type="button"
                  onClick={handleRecordEditStart}
                >
                  {content || emptyReportContent}
                </button>
              )}
            </section>

            {/* 최초 편집 시도 이후에만 표시되는 삭제와 취소 및 저장 명령 영역 */}
            {isEditing ? (
              <div className={styles.editActions}>
                <button
                  className={styles.deleteButton}
                  type="button"
                  disabled={isDeletePending || isUpdatePending}
                  onClick={handleDelete}
                >
                  {/* "삭제" */}
                  {message("frontend.report.delete")}
                </button>
                <button
                  className={styles.cancelButton}
                  type="button"
                  disabled={isDeletePending || isUpdatePending}
                  onClick={handleEditCancel}
                >
                  {/* "취소" */}
                  {message("frontend.common.cancel")}
                </button>
                <button
                  className={styles.saveButton}
                  type="button"
                  disabled={isDeletePending || isUpdatePending}
                  onClick={handleEditSave}
                >
                  {/* "저장" */}
                  {message("frontend.report.save")}
                </button>
              </div>
            ) : null}
          </div>
        </div>
      </Container>
    </main>
  );
}

export default DetailPage;
