/**
 * src/main/frontend/src/pages/Book/Info/BookInfoPage.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import { getApiErrorMessage } from "@/app/api/resultData";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import type { CSSProperties } from "react";
import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import BookRatingSummary from "@/features/Book/components/BookRatingSummary/BookRatingSummary";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import type { ReportDtoType } from "@/features/Book/types/book.type";
import * as styles from "./BookInfoPage.css";

/**
 * Book Info Page 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @return 구성된 화면 요소
 */
function BookInfoPage() {

  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const reptNumb = Number(id);
  const routeBookInfo = (
    location.state as { bookInfo?: ReportDtoType } | null
  )?.bookInfo;
  const { data, error, isError, isPending } = useBookDetail(reptNumb, !routeBookInfo);

  if (!id || isNaN(reptNumb)) {
    return <div>{message("frontend.common.invalidAccess")}</div>;
  }

  if (!routeBookInfo && isPending) {
    return <Loading />;
  }

  if (!routeBookInfo && isError) {
    return <h3>{getApiErrorMessage(error, message("frontend.common.tryAgain"))}</h3>;
  }

  const bookInfo = routeBookInfo ?? data?.data;

  if (!bookInfo) {
    return <h3>{message("frontend.common.noBookInfo")}</h3>;
  }

  const pageStyle = {
    "--book-bg-image": `url("${getBookCoverImageSource(bookInfo.bookCvim)}")`,
  } as CSSProperties;

  return (
    /* 저장된 도서의 상세 정보 전체 영역 */
    <main className={styles.page} style={pageStyle}>
      <Container className={styles.content}>
        {/* 도서 표지와 평점 요약 영역 */}
        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={getBookCoverImageSource(bookInfo.bookCvim)}
              onError={handleBookCoverImageError}
              alt={bookInfo.bookTitl}
            />
          </div>
          <h1 className={styles.title}>{bookInfo.bookTitl}</h1>
          <div className={styles.authorRatingLine}>
            <p className={styles.meta}>{bookInfo.bookAthr}</p>
            {bookInfo.bookAvgGrde && (
              <span className={styles.metaSeparator}>|</span>
            )}
            {bookInfo.bookAvgGrde && (
              <BookRatingSummary rating={bookInfo.bookAvgGrde} />
            )}
          </div>
          <button
            className={styles.bookInfoButton}
            type="button"
            onClick={() =>
              navigate(
                `/report/public-reports/isbn?isbn=${encodeURIComponent(
                  bookInfo.bookIsbn,
                )}`,
                {
                  state: {
                    title: bookInfo.bookTitl,
                    author: bookInfo.bookAthr,
                    cover: bookInfo.bookCvim,
                    ratingAverage: bookInfo.bookAvgGrde,
                  },
                },
              )
            }
          >
            {/* "다른 독후감 둘러보기" */}
            {message("frontend.book.publicReports.button")}
          </button>
        </section>

        <div className={styles.contentPanel}>
          {/* 저자와 출판 정보 영역 */}
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {/* "도서 정보" */}
              {message("frontend.common.bookInfo")}
            </h2>
            <div className={styles.infoGrid}>
              <span className={styles.infoLabel}>
                {/* "저자" */}
                {message("frontend.common.author")}
              </span>
              <p className={styles.infoValue}>{bookInfo.bookAthr || "-"}</p>
              <span className={styles.infoLabel}>
                {/* "출판사" */}
                {message("frontend.common.publisher")}
              </span>
              <p className={styles.infoValue}>{bookInfo.bookPubl || "-"}</p>
              <span className={styles.infoLabel}>
                {/* "출간일" */}
                {message("frontend.common.publDate")}
              </span>
              <p className={styles.infoValue}>{bookInfo.publDate || "-"}</p>
            </div>
          </section>

          {/* 도서 소개 영역 */}
          <section className={styles.section}>
            <h2 className={styles.sectionTitle}>
              {/* "책 소개" */}
              {message("frontend.common.bookDescription")}
            </h2>
            <p className={styles.description}>
              {bookInfo.bookDesc || message("frontend.common.noBookDescription")}
            </p>
          </section>
        </div>
      </Container>
    </main>
  );
}

export default BookInfoPage;
