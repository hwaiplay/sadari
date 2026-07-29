/**
 * 독후감 등록 화면의 도서 요약과 직접 입력 및 도서 정보 전환을 구성한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { formatCompactDate } from "@/app/utils/dateUtil";
import FormField from "@/features/Book/Set/components/form/field/FormField";
import * as styles from "./SetReportPage.css";
import * as detailStyles from "@/pages/Book/Detail/DetailPage.css";
import SearchBookButton from "@/features/Book/Set/components/searchBookButton/SearchBookButton";
import { useLocation, useNavigate } from "react-router-dom";
import { clsx } from "clsx";
import type { CSSProperties, FormEvent } from "react";
import { useEffect, useRef, useState } from "react";
import type {
  NaverApiResultType,
  ReadingStatusType,
} from "@/features/Book/types/book.type";
import Loading from "@/components/Loading/Loading";
import { useSetReportForm } from "@/features/Book/Set/hooks/useSetReportForm";
import BookSummary from "@/features/Book/Set/components/form/bookSummary/BookSummary";
import ReportStatsEditor from "@/features/Book/Set/components/form/reportStatsEditor/ReportStatsEditor";
import { getBookCoverColorApi } from "@/features/Book/api/bookApi";
import {
  MAX_REPORT_CONTENT_BYTES,
  REPORT_COLOR_CODE_GROUP,
  REPORT_FORM_CODE_GROUPS,
  REPORT_STATUS_CODE_GROUP,
  REPORT_STATUS_READ,
} from "@/features/Book/constants/reportForm";
import {
  getReportContentStorageByteLength,
  normalizeBookAuthor,
  stripHtmlTags,
  truncateUtf8Bytes,
} from "@/features/Book/utils/reportValidation";
import { useCodeGroupList } from "@/features/Common/utils/codeUtil";

const CONTENT_FADE_OUT_MILLISECONDS = 180;

/**
 * 선택한 도서를 기준으로 독후감 등록 입력과 도서 정보 전환 화면을 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function SetReportPage() {

  const location = useLocation();
  const navigate = useNavigate();
  const selectedBook = (
    location.state as { selectedBook?: NaverApiResultType } | null
  )?.selectedBook;

  const [status, setStatus] = useState<ReadingStatusType>("");
  const [grade, setGrade] = useState(0);
  const [reptColr, setReptColr] = useState("");
  const [pubcYsno, setPubcYsno] = useState<"Y" | "N">("N");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [contentByteLength, setContentByteLength] = useState(0);
  const [showBookInfo, setShowBookInfo] = useState(false);
  const [isContentFadingOut, setIsContentFadingOut] = useState(false);
  const contentSwitchTimerRef = useRef<number | null>(null);

  // 상태와 달력 색상을 같은 코드 묶음으로 요청하여 화면 진입 시 발생하는 API 호출을 한 번으로 줄인다
  const { data: codeGroupList = {} } = useCodeGroupList(
    REPORT_FORM_CODE_GROUPS,
  );
  const statusCodes = codeGroupList[REPORT_STATUS_CODE_GROUP] ?? [];
  const colorCodes = codeGroupList[REPORT_COLOR_CODE_GROUP] ?? [];
  const validStatusCodes = statusCodes.map((item) => item.comdCode);
  const validReportColors = colorCodes.map((item) => item.comdCode);
  const { isPending, handleSubmit } = useSetReportForm(
    selectedBook,
    validStatusCodes,
    validReportColors,
  );
  const pageStyle = selectedBook?.image
    ? ({
        "--book-bg-image": `url("${selectedBook.image}")`,
      } as CSSProperties)
    : undefined;

  /**
   * form Action 기능을 처리한다
   *
   * @author HanWon.Jang
   * @param e e 입력값
   * @return 처리 결과
   */
  const formAction = (e: FormEvent<HTMLFormElement>) => {

    e.preventDefault();
    handleSubmit(e.currentTarget);
  };

  const isReadingStatus = status === REPORT_STATUS_READ;
  const periodTitle = isReadingStatus ? "목표 독서 기간" : "독서 기간";

  useEffect(() => {

    if (!status && statusCodes.length > 0) {
      setStatus(statusCodes[0].comdCode);
    }
  }, [status, statusCodes]);

  useEffect(() => {

    let ignore = false;
    const fallbackColorCode = colorCodes[0]?.comdCode ?? "";

    // 표지 분석이 끝나기 전에도 등록 가능한 활성 공통코드 기본값을 유지한다
    setReptColr(fallbackColorCode);

    // 선택한 네이버 도서 표지와 활성 색상 목록이 준비된 경우에만 자동 색상을 조회한다
    if (selectedBook?.image && fallbackColorCode) {
      // 외부 이미지 분석은 백엔드에서 수행해 브라우저 CORS 제한을 피한다
      void getBookCoverColorApi(selectedBook.image)
        .then((coverColor) => {

          // 화면이 바뀐 뒤 도착한 이전 표지 분석 결과는 현재 등록 상태에 반영하지 않는다
          if (ignore) {
            return;
          }

          // 서버 응답이 현재 활성 공통코드에 포함된 경우에만 자동 선택값으로 사용한다
          const matchedColorCode = colorCodes.find(
            (colorCode) => colorCode.comdCode === coverColor?.reptColr,
          );
          // 유효한 분석 결과가 없으면 정렬 순서가 가장 빠른 활성 색상을 유지한다
          setReptColr(matchedColorCode?.comdCode ?? fallbackColorCode);
        })
        .catch(() => {

          // 이미지 분석 실패는 독후감 등록을 막지 않고 활성 공통코드 기본값으로 복구한다
          if (!ignore) {
            setReptColr(fallbackColorCode);
          }
        });
    }

    // 도서가 바뀌거나 화면을 벗어나면 이전 비동기 응답을 무시한다
    return () => {

      ignore = true;
    };
  }, [selectedBook?.image, colorCodes]);

  useEffect(() => {

    // 화면을 벗어난 뒤 예약된 콘텐츠 전환이 실행되지 않도록 타이머를 정리한다
    return () => {

      // 등록 화면 하단 전환 타이머가 있을 때만 브라우저 예약 작업을 취소한다
      if (contentSwitchTimerRef.current !== null) {
        window.clearTimeout(contentSwitchTimerRef.current);
      }
    };
  }, []);

  /**
   * 달력에서 선택한 독서 시작일과 종료일을 등록 상태에 반영한다
   *
   * @author HanWon.Jang
   * @param nextStartDate 선택한 독서 시작일
   * @param nextEndDate 선택한 독서 종료일
   * @return 반환값이 없다
   */
  function handleRangeChange(nextStartDate: string, nextEndDate: string) {

    // 달력에서 확정한 기간을 4열 요약과 등록 요청에 함께 사용한다
    setStartDate(nextStartDate);
    setEndDate(nextEndDate);
  }

  /**
   * 독후감 등록을 취소하고 등록 이전의 기본 진입 화면으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handleCancel() {

    // 등록 과정의 검색 이력이 다시 노출되지 않도록 홈 화면으로 대체 이동한다
    navigate("/", { replace: true });
  }

  /**
   * 책 영역을 유지한 채 하단의 독후감 입력과 도서 정보 영역을 전환한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function handleBookInfoToggle() {

    // 이미 페이드아웃 중이면 중복 클릭으로 전환 순서가 뒤집히지 않게 한다
    if (isContentFadingOut) {
      return;
    }

    // 현재 하단 콘텐츠를 먼저 페이드아웃한다
    setIsContentFadingOut(true);
    // 페이드아웃이 끝난 뒤 반대 콘텐츠를 표시하고 페이드인을 시작한다
    contentSwitchTimerRef.current = window.setTimeout(() => {

      setShowBookInfo((currentValue) => !currentValue);
      setIsContentFadingOut(false);
      contentSwitchTimerRef.current = null;
    }, CONTENT_FADE_OUT_MILLISECONDS);
  }

  const selectedBookAuthor = normalizeBookAuthor(selectedBook?.author);
  const selectedBookPublisher = stripHtmlTags(selectedBook?.publisher);
  const selectedBookPublishDate = formatCompactDate(
    stripHtmlTags(selectedBook?.pubdate),
  );
  // "등록된 책 소개가 없습니다."
  const selectedBookDescription =
    stripHtmlTags(selectedBook?.description) ||
    message("frontend.common.noBookDescription");
  // "독후감을 남겨보세요"
  const contentPlaceholder = message("frontend.report.placeholder.content");

  return isPending ? (
    <Loading title={message("frontend.report.loading.create")} />
  ) : (
    /* 독후감 등록 입력 전체 영역 */
    <main className={styles.page} style={pageStyle}>
      {/* 도서와 독서 정보 입력 영역 */}
      <form className={styles.form} onSubmit={formAction}>
        {/* 표지 대표색을 기반으로 자동 선택된 책장 색상 값 */}
        <input type="hidden" name="reptColr" value={reptColr} />
        {selectedBook?.image ? (
          <BookSummary
            image={selectedBook.image}
            title={selectedBook.title}
            author={selectedBook.author}
            publisher={selectedBook.publisher}
            onShowBookInfo={handleBookInfoToggle}
            showingBookInfo={showBookInfo}
            onChangeBook={() =>
              navigate("/book/search", { state: { keepSearchResult: true } })
            }
          />
        ) : (
          <div className={styles.searchBookArea}>
            <SearchBookButton />
          </div>
        )}

        {showBookInfo && selectedBook ? (
          <div
            key="set-book-info"
            className={clsx(
              detailStyles.contentPanel,
              isContentFadingOut
                ? detailStyles.contentSwitchFadeOut
                : detailStyles.contentSwitchFade,
            )}
          >
            {/* 등록할 도서의 저자와 출판사 및 출간일 요약 영역 */}
            <section
              className={detailStyles.reportStatsSection}
              aria-label={/* "도서 정보" */ message("frontend.common.bookInfo")}
            >
              <div className={detailStyles.bookInfoRows}>
                {/* 등록할 도서 저자 정보 행 */}
                <div className={detailStyles.bookInfoRow}>
                  <span className={detailStyles.bookInfoLabel}>
                    {/* "저자" */}
                    {message("frontend.common.author")}
                  </span>
                  <strong className={detailStyles.bookInfoValue}>
                    {selectedBookAuthor || "-"}
                  </strong>
                </div>

                {/* 등록할 도서 출판사 정보 행 */}
                <div className={detailStyles.bookInfoRow}>
                  <span className={detailStyles.bookInfoLabel}>
                    {/* "출판사" */}
                    {message("frontend.common.publisher")}
                  </span>
                  <strong className={detailStyles.bookInfoValue}>
                    {selectedBookPublisher || "-"}
                  </strong>
                </div>

                {/* 등록할 도서 출간일 정보 행 */}
                <div className={detailStyles.bookInfoRow}>
                  <span className={detailStyles.bookInfoLabel}>
                    {/* "출간일" */}
                    {message("frontend.common.publDate")}
                  </span>
                  <strong className={detailStyles.bookInfoValue}>
                    {selectedBookPublishDate || "-"}
                  </strong>
                </div>
              </div>
            </section>

            {/* 독후감 상세의 기록 카드 위치와 같은 등록 도서 소개 영역 */}
            <div className={detailStyles.recordArea}>
              <section className={detailStyles.recordSection}>
                <div className={detailStyles.recordTitleRow}>
                  <h2 className={detailStyles.sectionTitle}>
                    {/* "책 소개" */}
                    {message("frontend.common.bookDescription")}
                  </h2>
                </div>
                <p className={detailStyles.contentBox}>
                  {selectedBookDescription}
                </p>
              </section>
            </div>
          </div>
        ) : (
          <div
            key="set-report-input"
            className={clsx(
              styles.contentPanel,
              isContentFadingOut
                ? detailStyles.contentSwitchFadeOut
                : detailStyles.contentSwitchFade,
            )}
          >
            {/* 독서 상태와 공개 여부 및 평점과 독서 기간을 수정하는 4열 요약 영역 */}
            <ReportStatsEditor
              statusCodes={statusCodes}
              status={status}
              grade={grade}
              pubcYsno={pubcYsno}
              startDate={startDate}
              endDate={endDate}
              periodTitle={periodTitle}
              onStatusChange={setStatus}
              onGradeChange={setGrade}
              onPublicChange={setPubcYsno}
              onRangeChange={handleRangeChange}
            />

            {/* 독후감 기록 입력 영역 */}
            <section className={styles.recordSection}>
              <FormField title={message("frontend.report.field.content")}>
                <div className={styles.textAreaWrap}>
                  <span className={styles.counter}>
                    ({contentByteLength}/{MAX_REPORT_CONTENT_BYTES} byte)
                  </span>
                  <textarea
                    className={styles.textArea}
                    name="content"
                    id="content"
                    placeholder={contentPlaceholder}
                    onChange={(e) => {

                      // 입력된 기록을 DB 저장 바이트 한계 안으로 보정한다
                      const nextValue = truncateUtf8Bytes(
                        e.currentTarget.value,
                      );
                      // 보정된 기록이 등록 요청에 전달되도록 입력값을 교체한다
                      e.currentTarget.value = nextValue;
                      // 화면의 현재 기록 바이트 수를 저장 기준으로 갱신한다
                      setContentByteLength(
                        getReportContentStorageByteLength(nextValue),
                      );
                    }}
                  />
                </div>
              </FormField>
            </section>

            {/* 독후감 등록 취소와 저장 명령 영역 */}
            <div className={styles.formActions}>
              <button
                className={styles.cancelButton}
                type="button"
                onClick={handleCancel}
              >
                {/* "취소" */}
                {message("frontend.common.cancel")}
              </button>
              <button className={styles.saveButton} type="submit">
                <svg
                  className={styles.buttonIcon}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path
                    d="M5 4h11l3 3v13H5V4Z"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinejoin="round"
                  />
                  <path
                    d="M8 4v6h8M8 17h8"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                {/* "저장" */}
                {message("frontend.report.save")}
              </button>
            </div>
          </div>
        )}
      </form>
    </main>
  );
}

export default SetReportPage;
