/**
 * 검색 결과에서 선택한 도서의 상세 정보와 독후감 등록 진입 기능을 제공한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useState } from "react";
import type { CSSProperties } from "react";
import { ActionButton } from "@/components/Button/ActionButton";
import { Container } from "@/components/Layout/Container/Container";
import type { BookSearchResultType } from "@/features/Book/types/book.type";
import { useBookRatingAvg } from "@/features/Book/Detail/hook/useBookRatingAverage";
import { normalizeBookAuthor, stripHtmlTags } from "@/app/utils/htmlUtil";
import { formatCompactDate } from "@/app/utils/dateUtil";
import { moveToReportEntry } from "@/features/Book/utils/reportEntry";
import {
  READING_TIMER_SEARCH_SOURCE,
  type SearchBookPageState,
} from "@/features/Book/Search/lib/bookSearchNavigation";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import * as detailStyles from "@/pages/Book/Detail/DetailPage.css";

/**
 * Search Book Info Page 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function SearchBookInfoPage() {

  const location = useLocation();
  const navigate = useNavigate();
  const { clubNumb: clubNumbParam } = useParams<{ clubNumb: string }>();
  const isClubBookSearch = clubNumbParam !== undefined;
  const clubNumb = Number(clubNumbParam);
  const hasValidClubNumb = Number.isSafeInteger(clubNumb) && clubNumb > 0;
  const pageState = (location.state ?? {}) as SearchBookPageState & {
    book?: BookSearchResultType;
  };
  const book = pageState.book;
  const isTimerBookSearch =
    !isClubBookSearch &&
    pageState.entrySource === READING_TIMER_SEARCH_SOURCE;
  const [isSelectingBook, setIsSelectingBook] = useState(false);
  const { data: ratingAverageData } = useBookRatingAvg(
    book?.isbn ?? "",
    Boolean(book?.isbn),
  );

  // 검색 결과에서 전달된 도서가 없으면 상세 화면을 구성할 수 없음을 안내한다
  if (!book) {
    // "도서 정보가 없습니다."
    const noBookInfoMessage = message("frontend.common.noBookInfo");
    // 검색 도서 정보가 없음을 알리는 안내 화면을 반환한다
    return <h3>{noBookInfoMessage}</h3>;
  }

  const selectedBook = book;
  const title = stripHtmlTags(book.title);
  const author = normalizeBookAuthor(book.author);
  const publisher = stripHtmlTags(book.publisher);
  const description = stripHtmlTags(book.description);
  const pubdate = formatCompactDate(stripHtmlTags(book.pubdate));
  const rawRatingAverage = Number(ratingAverageData?.data);
  const hasRatingAverage =
    Number.isFinite(rawRatingAverage) && rawRatingAverage > 0;
  const ratingAverage = hasRatingAverage ? rawRatingAverage : undefined;
  // "등록된 책 소개가 없습니다."
  const bookDescription =
    description || message("frontend.common.noBookDescription");
  const pageStyle = {
    "--book-bg-image": `url("${book.image}")`,
    "--book-bg-fade-height": "720px",
  } as CSSProperties;

  /**
   * 선택한 도서와 같은 ISBN의 공개 독후감 목록으로 이동한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  function goPublicReportsPage(): void {

    // 선택한 도서 정보와 평균 평점을 공개 독후감 화면으로 전달한다
    navigate(
      `/report/public-reports/isbn?isbn=${encodeURIComponent(selectedBook.isbn)}`,
      {
        state: {
          title,
          author,
          cover: selectedBook.image,
          ratingAverage,
        },
      },
    );
  }

  /**
   * 선택한 도서를 사용하여 독후감 등록 화면으로 이동한다
   *
   * @author HanWon.Jang
   * @return 기존 독후감 확인과 화면 이동이 끝난 Promise
   */
  async function goSelectedBookPage(): Promise<void> {
    // 기존 독후감 확인이 진행 중이면 중복 화면 이동을 차단한다
    if (isSelectingBook) {
      // 진행 중인 독후감 선택 요청을 유지한다
      return;
    }

    // 타이머 진입 흐름은 선택 도서를 공용 검색 화면의 목표기간 모달로 전달한다.
    if (isTimerBookSearch) {
      const searchState: SearchBookPageState = {
        entrySource: READING_TIMER_SEARCH_SOURCE,
        keepSearchResult: true,
        timerBook: selectedBook,
      };
      // 상세 화면을 검색 화면으로 교체하고 선택 직후 목표 독서기간 모달을 연다.
      navigate("/book/search", { replace: true, state: searchState });
      // 일반 독후감 확인과 등록 화면 이동을 실행하지 않는다.
      return;
    }

    // 이 책으로 기록하기 버튼의 중복 요청을 막도록 진행 상태를 설정한다
    // 모임 독서용 검색에서는 개인 독후감 작성 여부를 확인하지 않는다.
    if (isClubBookSearch) {
      // 올바르지 않은 모임 번호로는 독서 등록 URL을 만들지 않는다.
      if (!hasValidClubNumb) {
        // 모임 목록으로 돌아가 다시 진입하도록 한다.
        navigate("/reading-clubs/mine", { replace: true });
        // 잘못된 모임 번호의 책 선택을 종료한다.
        return;
      }

      // 독서 수정에서 진입했다면 같은 회차의 수정 화면으로 선택 도서를 돌려보낸다.
      const editRondNumb = Number(pageState.clubReadingEditRondNumb);
      const readingEntryPath = Number.isSafeInteger(editRondNumb) && editRondNumb > 0
        ? `/reading-clubs/${clubNumb}/${editRondNumb}/edit`
        : `/reading-clubs/${clubNumb}/set`;
      // 선택한 책 정보와 ISBN을 등록 또는 수정 화면에 전달한다.
      navigate(
        `${readingEntryPath}?isbn=${encodeURIComponent(selectedBook.isbn)}`,
        { state: { book: selectedBook } },
      );
      // 모임 독서 등록 URL 이동 후 개인 독후감 흐름을 실행하지 않는다.
      return;
    }

    setIsSelectingBook(true);
    // 선택 흐름이 예외로 끝나도 버튼 진행 상태를 복원한다
    try {
      // 기존 독후감 수정과 추가 작성 선택 흐름으로 이동한다
      await moveToReportEntry(selectedBook, navigate);
    }

    finally {
      // 선택 안내가 끝난 뒤 다시 시도할 수 있도록 진행 상태를 해제한다
      setIsSelectingBook(false);
    }
  }

  // 독후감 상세의 도서 정보 화면과 같은 구조로 검색 도서 정보를 반환한다
  return (
    /* 검색한 도서의 상세 정보 전체 영역 */
    <main className={detailStyles.page} style={pageStyle}>
      <Container className={detailStyles.detail}>
        {/* 도서 표지와 평균 평점 및 공개 독후감 이동 영역 */}
        <section className={detailStyles.header}>
          <div className={detailStyles.coverFrame}>
            <img
              className={detailStyles.coverImage}
              src={getBookCoverImageSource(book.image)}
              data-fallback-image={book.thumbnailImage}
              onError={handleBookCoverImageError}
              alt={title}
            />
          </div>
          <h1 className={detailStyles.title}>{title}</h1>

          {/* 도서 평균 평점 영역 */}
          <div className={detailStyles.bookAverageSummary}>
            {hasRatingAverage ? (
              <>
                {/* 평균 평점이 있으면 평균 문구와 별 아이콘 및 점수를 표시한다 */}
                <span className={detailStyles.bookAverageLabel}>
                  {/* "평균" */}
                  {message("frontend.book.ratingAverageShort")}
                </span>
                <svg
                  className={detailStyles.bookAverageStar}
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
                <strong className={detailStyles.bookAverageScore}>
                  {ratingAverage}
                </strong>
              </>
            ) : (
              <span className={detailStyles.bookAverageEmpty}>
                {/* "아직 별점이 없습니다." */}
                {message("frontend.book.ratingAverageEmpty")}
              </span>
            )}
          </div>

          {/* 같은 도서의 공개 독후감 이동 영역 */}
          <div className={detailStyles.bookInfoActionRow}>
            <button
              className={detailStyles.bookInfoButton}
              type="button"
              onClick={goPublicReportsPage}
            >
              {/* "다른 독후감 둘러보기" */}
              {message("frontend.book.publicReports.button")}
            </button>
          </div>
        </section>

        {/* 도서 정보와 책 소개 카드 영역 */}
        <div className={detailStyles.contentPanel}>
          {/* 저자와 출판사 및 출간일의 세로 요약 영역 */}
          <section
            className={detailStyles.reportStatsSection}
            aria-label={/* "도서 정보" */ message("frontend.common.bookInfo")}
          >
            <div className={detailStyles.bookInfoRows}>
              {/* 도서 저자 정보 행 */}
              <div className={detailStyles.bookInfoRow}>
                <span className={detailStyles.bookInfoLabel}>
                  {/* "저자" */}
                  {message("frontend.common.author")}
                </span>
                <strong className={detailStyles.bookInfoValue}>
                  {author || "-"}
                </strong>
              </div>

              {/* 도서 출판사 정보 행 */}
              <div className={detailStyles.bookInfoRow}>
                <span className={detailStyles.bookInfoLabel}>
                  {/* "출판사" */}
                  {message("frontend.common.publisher")}
                </span>
                <strong className={detailStyles.bookInfoValue}>
                  {publisher || "-"}
                </strong>
              </div>

              {/* 도서 출간일 정보 행 */}
              <div className={detailStyles.bookInfoRow}>
                <span className={detailStyles.bookInfoLabel}>
                  {/* "출간일" */}
                  {message("frontend.common.publDate")}
                </span>
                <strong className={detailStyles.bookInfoValue}>
                  {pubdate || "-"}
                </strong>
              </div>
            </div>
          </section>

          {/* 배경 전환 위에 표시되는 도서 소개 영역 */}
          <div className={detailStyles.recordArea}>
            {/* 독후감 상세의 책 소개와 같은 도서 소개 카드 영역 */}
            <section className={detailStyles.recordSection}>
              <div className={detailStyles.recordTitleRow}>
                <h2 className={detailStyles.sectionTitle}>
                  {/* "책 소개" */}
                  {message("frontend.common.bookDescription")}
                </h2>
              </div>
              <p className={detailStyles.contentBox}>{bookDescription}</p>
            </section>
          </div>
        </div>

        {/* 선택한 도서의 독후감 등록 이동 영역 */}
        <ActionButton
          variant="primary"
          size="lg"
          width="full"
          onClick={() => void goSelectedBookPage()}
          disabled={isSelectingBook}
        >
          {/* "이 책으로 기록하기" */}
          {isClubBookSearch
            ? /* "이 책 선택하기" */ message(
                "frontend.readingClub.reading.selectBook",
              )
            : /* "이 책으로 기록하기" */ message(
                "frontend.book.search.writeThisBook",
              )}
        </ActionButton>
      </Container>
    </main>
  );
}

export default SearchBookInfoPage;
