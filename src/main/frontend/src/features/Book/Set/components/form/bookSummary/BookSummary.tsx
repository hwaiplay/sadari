/**
 * 독후감 등록 화면의 도서 요약과 하단 콘텐츠 전환 명령을 구성한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import * as styles from "./BookSummary.css";

type BookSummaryProps = {
  image: string;
  title: string;
  author?: string;
  publisher?: string;
  onChangeBook?: () => void;
  onShowBookInfo?: () => void;
  showingBookInfo?: boolean;
};

/**
 * 선택된 책의 표지와 기본 정보 및 도서 정보 전환 명령을 표시한다
 *
 * @author HanWon.Jang
 * @param image 책 표지 이미지 URL
 * @param title 책 제목
 * @param author 책 저자
 * @param publisher 책 출판사
 * @param onChangeBook 책 변경 버튼 클릭 시 실행할 콜백
 * @param onShowBookInfo 책 정보 더보기 버튼 클릭 시 실행할 콜백
 * @param showingBookInfo 도서 정보 영역 표시 여부
 * @return 책 요약 정보 컴포넌트
 */
function BookSummary({
  image,
  title,
  author,
  onChangeBook,
  onShowBookInfo,
  showingBookInfo = false,
}: BookSummaryProps) {

  return (
    <div className={styles.coverArea}>
      <div className={styles.coverFrame}>
        <img className={styles.coverImage} src={image} alt={title} />
      </div>
      <div className={styles.bookMeta}>
        <h1 className={styles.bookTitle}>{title}</h1>
        {author && <p className={styles.bookSubInfo}>{author}</p>}
      </div>
      {(onShowBookInfo || onChangeBook) && (
        <div className={styles.buttonGroup}>
          {onShowBookInfo && (
            <button
              className={styles.bookInfoButton}
              type="button"
              onClick={onShowBookInfo}
            >
              {showingBookInfo ? (
                <>
                  {/* "돌아가기" */}
                  {message("frontend.report.backToReport")}
                </>
              ) : (
                <>
                  {/* "도서 정보 자세히보기" */}
                  {message("frontend.report.bookInfoMore")}
                </>
              )}
            </button>
          )}
          {onChangeBook && (
            <button
              className={styles.changeButton}
              type="button"
              onClick={onChangeBook}
            >
              {/* "책 변경" */}
              {message("frontend.report.bookChange")}
            </button>
          )}
        </div>
      )}
    </div>
  );
}

export default BookSummary;
