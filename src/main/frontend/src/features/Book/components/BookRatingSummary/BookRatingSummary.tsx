import { message } from "@/app/messages/message";
import * as styles from "./BookRatingSummary.css";

type BookRatingSummaryProps = {
  rating: number | string;
};

/**
 * 도서 정보 화면과 목록에서 재사용하는 평균 별점 요약을 표시함
 *
 * @author SeungHyeon.Kang
 * @param props 표시할 도서 평균 별점
 * @return 노란 별 아이콘과 평균 별점 값
 */
function BookRatingSummary({ rating }: BookRatingSummaryProps) {

  // 도서 정보 화면과 같은 별 아이콘 및 점수 스타일을 반환함
  return (
    <span
      className={styles.ratingSummary}
      aria-label={message("frontend.report.gradeValue", [rating])}
    >
      <span className={styles.ratingStar}>{"\u2605"}</span>
      <span className={styles.ratingValue}>{rating}</span>
    </span>
  );
}

export default BookRatingSummary;
