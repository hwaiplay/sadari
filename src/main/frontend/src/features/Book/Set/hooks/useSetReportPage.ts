/**
 * 독후감 등록 페이지에서 사용하는 상태와 사용자 동작을 관리함
 *
 * @author SeungHyeon.Kang
 */

import { message } from "@/app/messages/message";
import { useHomeNavigation } from "@/app/navigation/HomeNavigationProvider";
import { formatCompactDate } from "@/app/utils/dateUtil";
import { getBookCoverColorApi } from "@/features/Book/api/bookApi";
import {
  MAX_REPORT_CONTENT_BYTES,
  REPORT_COLOR_CODE_GROUP,
  REPORT_FORM_CODE_GROUPS,
  REPORT_STATUS_CODE_GROUP,
  REPORT_STATUS_READ,
} from "@/features/Book/constants/reportForm";
import type {
  BookSearchResultType,
  ReadingStatusType,
} from "@/features/Book/types/book.type";
import { getBookCoverImageSource } from "@/features/Book/utils/bookCoverImage";
import {
  getReportContentByteLen,
  normalizeBookAuthor,
  stripHtmlTags,
  truncateUtf8Bytes,
} from "@/features/Book/utils/reportValidation";
import { useCodeGroupList } from "@/features/Common/utils/codeUtil";
import { getUserSettingApi } from "@/features/User/api/userApi";
import type { ChangeEvent, CSSProperties, FormEvent } from "react";
import { useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useSetReportForm } from "./useSetReportForm";

const CONTENT_FADE_OUT_MILLISECONDS = 90;

/**
 * 독후감 등록 페이지의 상태와 이벤트 처리 함수를 제공함
 *
 * @author Hanwon.Jang
 * @return 독후감 등록 페이지에서 사용하는 상태와 이벤트 처리 함수
 */
export function useSetReportPage() {

  const location = useLocation();
  const navigate = useNavigate();
  const moveHome = useHomeNavigation();
  const selectedBook = (
    location.state as { selectedBook?: BookSearchResultType } | null
  )?.selectedBook;

  const [status, setStatus] = useState<ReadingStatusType>(REPORT_STATUS_READ);
  const [grade, setGrade] = useState(0);
  const [reptColr, setReptColr] = useState("");
  const [pubcYsno, setPubcYsno] = useState<"Y" | "N">("N");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [contentByteLength, setContentByteLength] = useState(0);
  const [showBookInfo, setShowBookInfo] = useState(false);
  const [isContentFadingOut, setIsContentFadingOut] = useState(false);
  const contentSwitchTimerRef = useRef<number | null>(null);
  const reportPublicDefaultRef = useRef<"Y" | "N">("N");

  const { data: codeGroupList } = useCodeGroupList(REPORT_FORM_CODE_GROUPS);
  const statusCodes = useMemo(
    () => codeGroupList?.[REPORT_STATUS_CODE_GROUP] ?? [],
    [codeGroupList],
  );
  const colorCodes = useMemo(
    () => codeGroupList?.[REPORT_COLOR_CODE_GROUP] ?? [],
    [codeGroupList],
  );
  const validStatusCodes = useMemo(
    () => statusCodes.map((item) => item.comdCode),
    [statusCodes],
  );
  const validReportColors = useMemo(
    () => colorCodes.map((item) => item.comdCode),
    [colorCodes],
  );
  const { isPending, handleSubmit } = useSetReportForm(
    selectedBook,
    validStatusCodes,
    validReportColors,
  );

  const pageStyle = selectedBook
    ? ({
        "--book-bg-image": `url("${getBookCoverImageSource(selectedBook.image)}")`,
        "--book-bg-fade-height": showBookInfo ? "720px" : "680px",
      } as CSSProperties)
    : undefined;
  const periodTitle = status === REPORT_STATUS_READ
    ? /* "목표 독서 기간" */ message("frontend.report.field.targetPeriod")
    : /* "독서 기간" */ message("frontend.report.field.period");
  const selectedBookAuthor = normalizeBookAuthor(selectedBook?.author);
  const selectedBookPublisher = stripHtmlTags(selectedBook?.publisher);
  const selectedBookPublishDate = formatCompactDate(
    stripHtmlTags(selectedBook?.pubdate),
  );
  const selectedBookDescription =
    stripHtmlTags(selectedBook?.description) ||
    // "등록된 책 소개가 없습니다."
    message("frontend.common.noBookDescription");
  // "독후감을 남겨보세요"
  const contentPlaceholder = message("frontend.report.placeholder.content");

  useEffect(() => {

    let ignore = false;
    void getUserSettingApi()
      .then((userSetting) => {
        if (!ignore) {
          reportPublicDefaultRef.current = userSetting.reportPublicDefaultYsno;
        }
      })
      .catch(() => {
        // 설정 조회 실패 시 안전한 비공개 기본값을 유지함
      });

    return () => {
      ignore = true;
    };
  }, []);

  useEffect(() => {

    if (
      statusCodes.length > 0 &&
      !statusCodes.some((item) => item.comdCode === status)
    ) {
      setStatus(statusCodes[0].comdCode);
    }
  }, [status, statusCodes]);

  useEffect(() => {

    let ignore = false;
    const fallbackColorCode = colorCodes[0]?.comdCode ?? "";

    setReptColr(fallbackColorCode);

    if (selectedBook?.image && fallbackColorCode) {
      void getBookCoverColorApi(selectedBook.image)
        .then((coverColor) => {

          if (ignore) {
            return;
          }

          const matchedColorCode = colorCodes.find(
            (colorCode) => colorCode.comdCode === coverColor?.reptColr,
          );
          setReptColr(matchedColorCode?.comdCode ?? fallbackColorCode);
        })
        .catch(() => {

          if (!ignore) {
            setReptColr(fallbackColorCode);
          }
        });
    }

    return () => {

      ignore = true;
    };
  }, [selectedBook?.image, colorCodes]);

  useEffect(() => {

    return () => {

      if (contentSwitchTimerRef.current !== null) {
        window.clearTimeout(contentSwitchTimerRef.current);
      }
    };
  }, []);

  /**
   * 독후감 등록 폼을 검증하고 저장함
   *
   * @author HanWon.Jang
   * @param event 폼 제출 이벤트
   * @return 반환값이 없음
   */
  function handleFormSubmit(event: FormEvent<HTMLFormElement>): void {

    event.preventDefault();
    void handleSubmit(event.currentTarget);
  }

  /**
   * 선택한 독서 기간을 등록 상태에 반영함
   *
   * @author HanWon.Jang
   * @param nextStartDate 독서 시작일
   * @param nextEndDate 독서 종료일
   * @return 반환값이 없음
   */
  function handleRangeChange(
    nextStartDate: string,
    nextEndDate: string,
  ): void {

    setStartDate(nextStartDate);
    setEndDate(nextEndDate);
  }

  /**
   * 독서 상태를 변경하고 상태에 맞지 않는 값을 초기화함
   *
   * @author HanWon.Jang
   * @param nextStatus 변경할 독서 상태
   * @return 반환값이 없음
   */
  function handleStatusChange(nextStatus: ReadingStatusType): void {

    setStatus(nextStatus);

    if (nextStatus === REPORT_STATUS_READ) {
      setGrade(0);
      setPubcYsno("N");
    } else if (status === REPORT_STATUS_READ) {
      setPubcYsno(reportPublicDefaultRef.current);
    }
  }

  /**
   * 독후감 평점을 변경함
   *
   * @author HanWon.Jang
   * @param nextGrade 변경할 평점
   * @return 반환값이 없음
   */
  function handleGradeChange(nextGrade: number): void {

    setGrade(nextGrade);
  }

  /**
   * 독후감 공개 여부를 변경함
   *
   * @author HanWon.Jang
   * @param nextPubcYsno 변경할 공개 여부
   * @return 반환값이 없음
   */
  function handlePublicChange(nextPubcYsno: "Y" | "N"): void {

    setPubcYsno(nextPubcYsno);
  }

  /**
   * 독후감 등록을 취소하고 이전 기본 화면으로 이동함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  function handleCancel(): void {

    moveHome();
  }

  /**
   * 책 요약과 책 상세 정보 영역을 전환함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  function handleBookInfoToggle(): void {

    if (isContentFadingOut) {
      return;
    }

    setIsContentFadingOut(true);
    contentSwitchTimerRef.current = window.setTimeout(() => {

      setShowBookInfo((currentValue) => !currentValue);
      setIsContentFadingOut(false);
      contentSwitchTimerRef.current = null;
    }, CONTENT_FADE_OUT_MILLISECONDS);
  }

  /**
   * 책 검색 결과를 유지한 채 선택 도서 변경 화면으로 이동함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  function handleBookChange(): void {

    navigate("/book/search", { state: { keepSearchResult: true } });
  }

  /**
   * 독후감 내용을 저장 가능한 바이트 수로 제한하고 길이를 갱신함
   *
   * @author HanWon.Jang
   * @param event 독후감 내용 변경 이벤트
   * @return 반환값이 없음
   */
  function handleContentChange(event: ChangeEvent<HTMLTextAreaElement>): void {

    const nextValue = truncateUtf8Bytes(event.currentTarget.value);

    event.currentTarget.value = nextValue;
    setContentByteLength(getReportContentByteLen(nextValue));
  }

  return {
    selectedBook,
    statusCodes,
    status,
    grade,
    reptColr,
    pubcYsno,
    startDate,
    endDate,
    contentByteLength,
    maxContentBytes: MAX_REPORT_CONTENT_BYTES,
    showBookInfo,
    isContentFadingOut,
    isPending,
    pageStyle,
    periodTitle,
    selectedBookAuthor,
    selectedBookPublisher,
    selectedBookPublishDate,
    selectedBookDescription,
    contentPlaceholder,
    handleFormSubmit,
    handleRangeChange,
    handleStatusChange,
    handleGradeChange,
    handlePublicChange,
    handleCancel,
    handleBookInfoToggle,
    handleBookChange,
    handleContentChange,
  };
}
