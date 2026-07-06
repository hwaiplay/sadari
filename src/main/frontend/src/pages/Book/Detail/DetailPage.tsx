import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";
import { useDeleteMutation } from "@/features/Book/Delete/useDeleteMutation";
import * as styles from "./DetailPage.css";

const MAX_DESCRIPTION_LENGTH = 100;

function RatingStars({ grade }: { grade: string }) {
  const rating = Math.max(0, Math.min(5, Number(grade) || 0));

  return (
    <div className={styles.stars} aria-label={`평점 ${rating}점`}>
      {[1, 2, 3, 4, 5].map((value) => (
        <span
          key={value}
          className={value <= rating ? styles.starFilled : undefined}
        >
          ★
        </span>
      ))}
    </div>
  );
}

function DetailPage() {
  const { id } = useParams();
  const idNum = Number(id);
  const navigate = useNavigate();
  const [isDescriptionOpen, setIsDescriptionOpen] = useState(false);

  const goUpdatePage = (reportNumb: number) => {
    navigate(`/book/upt/${reportNumb}`);
  };

  const { mutate } = useDeleteMutation();

  const deleteOnClick = (reportNumb: number) => {
    const confirmed = window.confirm(
      "독후감을 삭제하면 복구할 수 없습니다. 정말 삭제할까요?",
    );

    if (confirmed) {
      mutate(reportNumb);
    }
  };

  const { data, isPending } = useBookDetail(idNum);

  if (data?.code === 2004) {
    return <div>{data.message}</div>;
  }

  if (isPending) {
    return <Loading title={"독후감을 불러오는 중"} />;
  }

  const bookData = data?.data;

  if (data?.code !== 200 || !bookData) {
    return <h3>{data?.message}</h3>;
  }

  const description = bookData.bookDesc || "";
  const hasLongDescription = description.length > MAX_DESCRIPTION_LENGTH;
  const visibleDescription =
    hasLongDescription && !isDescriptionOpen
      ? `${description.slice(0, MAX_DESCRIPTION_LENGTH)}...`
      : description;

  return (
    <main className={styles.page}>
      <Container className={styles.detail}>
        <div className={styles.actions}>
          <button
            className={styles.actionButton}
            type="button"
            onClick={() => goUpdatePage(idNum)}
          >
            수정
          </button>
          <button
            className={styles.deleteButton}
            type="button"
            onClick={() => deleteOnClick(idNum)}
          >
            삭제
          </button>
        </div>

        <section className={styles.header}>
          <div className={styles.coverFrame}>
            <img
              className={styles.coverImage}
              src={bookData.bookCvim}
              alt={bookData.bookTitl}
            />
          </div>
          <div>
            <h1 className={styles.title}>{bookData.bookTitl}</h1>
            <p className={styles.meta}>
              {bookData.bookAthr} · {bookData.bookPubl}
            </p>
          </div>
        </section>

        <section className={styles.panel}>
          <div>
            <h2 className={styles.sectionTitle}>독서 기간</h2>
            <div className={styles.period}>
              {bookData.reportStdt} ~ {bookData.reportEndt}
            </div>
          </div>

          <div>
            <h2 className={styles.sectionTitle}>평점</h2>
            <RatingStars grade={bookData.reportGrde} />
          </div>

          <div>
            <h2 className={styles.sectionTitle}>독후감</h2>
            <p className={styles.content}>{bookData.reportCntn}</p>
          </div>
        </section>

        <section className={styles.panel}>
          <h2 className={styles.sectionTitle}>책 소개</h2>
          <div className={styles.bookInfoGrid}>
            <span className={styles.infoLabel}>저자</span>
            <p className={styles.infoValue}>{bookData.bookAthr}</p>
            <span className={styles.infoLabel}>출판사</span>
            <p className={styles.infoValue}>{bookData.bookPubl}</p>
            <span className={styles.infoLabel}>ISBN</span>
            <p className={styles.infoValue}>{bookData.bookIsbn}</p>
          </div>
          <div>
            <p className={styles.content}>{visibleDescription}</p>
            {hasLongDescription && (
              <button
                className={styles.toggleButton}
                type="button"
                onClick={() => setIsDescriptionOpen((prev) => !prev)}
              >
                {isDescriptionOpen ? "접기" : "더보기"}
              </button>
            )}
          </div>
        </section>
      </Container>
    </main>
  );
}

export default DetailPage;
