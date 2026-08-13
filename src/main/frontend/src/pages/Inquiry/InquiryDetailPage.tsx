import { getApiErrorMessage } from "@/app/api/resultData";
import Loading from "@/components/Loading/Loading";
import { getInquiryDetailApi, type Inquiry } from "@/features/Inquiry/api/inquiryApi";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import * as styles from "./InquiryPage.css";

/**
 * 본인이 접수한 고객문의 본문과 관리자 답변을 표시합니다.
 *
 * @author SeungHyeon.Kang
 * @return 고객문의 상세 화면
 */
function InquiryDetailPage() {

  const { inqrNumb } = useParams();
  const [inquiry, setInquiry] = useState<Inquiry | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {

    const target = Number(inqrNumb);

    if (!Number.isInteger(target) || target < 1) {
      setError("올바르지 않은 고객문의 번호입니다.");
      return;
    }

    let ignore = false;

    getInquiryDetailApi(target)
      .then((data) => {

        if (!ignore) {
          setInquiry(data);
        }
      })
      .catch((loadError) => {

        if (!ignore) {
          setError(getApiErrorMessage(loadError, "고객문의를 불러오지 못했습니다."));
        }
      });

    return () => {

      ignore = true;
    };
  }, [inqrNumb]);

  if (!inquiry && !error) {
    return (
      <div className={styles.page}>
        <Loading title="고객문의를 불러오는 중입니다" isFullScreen={false} />
      </div>
    );
  }

  if (error || !inquiry) {
    return (
      <div className={styles.page}>
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>{error}</p>
        </section>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <article>
        <header className={styles.detailHeader}>
          <h2 className={styles.detailTitle}>{inquiry.inqrTitl}</h2>
          <div className={styles.detailMeta}>
            <time className={styles.detailDate} dateTime={inquiry.regiDate}>
              {new Date(inquiry.regiDate).toLocaleString("ko-KR")}
            </time>
            <span className={styles.itemMetaGroup}>
              <span className={styles.category}>{inquiry.inqrCatgName}</span>
              <span className={styles.state}>{inquiry.inqrStatName}</span>
            </span>
          </div>
        </header>
        <p className={styles.body}>{inquiry.inqrCntn}</p>
      </article>

      <section className={styles.answerSection} aria-labelledby="inquiry-answer-title">
        <h3 id="inquiry-answer-title" className={styles.answerHeading}>관리자 답변</h3>
        {inquiry.answers?.length ? (
          <div className={styles.answers}>
            {inquiry.answers.map((answer) => (
              <article className={styles.answer} key={answer.answNumb}>
                <div className={styles.answerMeta}>
                  <strong className={styles.answerAuthor}>사다리 고객센터</strong>
                  <time className={styles.meta} dateTime={answer.regiDate}>
                    {new Date(answer.regiDate).toLocaleString("ko-KR")}
                  </time>
                </div>
                <p className={styles.answerBody}>{answer.answCntn}</p>
              </article>
            ))}
          </div>
        ) : (
          <p className={styles.statusText}>답변을 준비하고 있습니다.<br />조금만 기다려주세요.</p>
        )}
      </section>
    </div>
  );
}

export default InquiryDetailPage;
