import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import { useCheckAuth } from "@/features/Auth/hooks/useCheckAuth";
import { getInquiryDetailApi, type Inquiry } from "@/features/Inquiry/api/inquiryApi";
import { clsx } from "clsx";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import * as styles from "./InquiryPage.css";

/**
 * 고객문의와 관리자 답변의 등록일시를 분 단위로 표시함
 *
 * @author SeungHyeon.Kang
 * @param value 표시할 등록일시
 * @return 초와 날짜 끝 마침표를 제외한 한국어 등록일시
 */
function formatInquiryDateTime(value: string): string {
  // 문의 상세에서 초를 숨기고 한국어 오전과 오후 표기를 유지함
  const displayDate = new Date(value).toLocaleString("ko-KR", {
    year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  });

  // 날짜와 오전 또는 오후 사이의 마지막 마침표를 제거한 표시값을 반환함
  return displayDate.replace(/\.\s*(?=오전|오후)/, " ");
}

/**
 * 본인이 접수한 고객문의 본문과 관리자 답변을 표시함
 *
 * @author SeungHyeon.Kang
 * @return 고객문의 상세 화면
 */
function InquiryDetailPage() {

  const navigate = useNavigate();
  const { isSuspended } = useCheckAuth();
  const { inqrNumb } = useParams();
  const [inquiry, setInquiry] = useState<Inquiry | null>(null);
  const [error, setError] = useState("");

  /**
   * 정지 회원을 문의 상세 진입 전 정지 안내 화면으로 이동시킴
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const handleSuspensionBack = (): void => {

    navigate("/suspension", { replace: true });
  };

  // 정지 회원이 문의 상세와 오류 화면에서 빠져나갈 수 있는 전용 이동 버튼임
  const suspensionBackButton = isSuspended ? (
    <div className={styles.suspensionBackBar}>
      <div className={styles.suspensionBackInner}>
        <button
          className={styles.submitButton}
          type="button"
          onClick={handleSuspensionBack}
        >
          {/* "돌아가기" */}
          {message("frontend.inquiry.detail.back")}
        </button>
      </div>
    </div>
  ) : null;

  useEffect(() => {

    const target = Number(inqrNumb);

    if (!Number.isInteger(target) || target < 1) {
      // "올바르지 않은 고객문의 번호입니다."
      setError(message("frontend.inquiry.detail.invalidNumber"));
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
          // "고객문의를 불러오지 못했습니다."
          setError(getApiErrorMessage(loadError, message("frontend.inquiry.list.loadFailed")));
        }
      });

    return () => {

      ignore = true;
    };
  }, [inqrNumb]);

  if (!inquiry && !error) {
    return (
      <div className={clsx(styles.page, styles.detailPage, isSuspended && styles.suspendedDetailPage)}>
        <Loading isFullScreen={false} />
        {suspensionBackButton}
      </div>
    );
  }

  if (error || !inquiry) {
    return (
      <div className={clsx(styles.page, styles.detailPage, isSuspended && styles.suspendedDetailPage)}>
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>{error}</p>
        </section>
        {suspensionBackButton}
      </div>
    );
  }

  return (
    <div className={clsx(styles.page, styles.detailPage, isSuspended && styles.suspendedDetailPage)}>
      <article>
        <header className={styles.detailHeader}>
          <h2 className={styles.detailTitle}>{inquiry.inqrTitl}</h2>
          <div className={styles.detailMeta}>
            <time className={styles.detailDate} dateTime={inquiry.regiDate}>
              {/* 고객문의 등록일시 표시 영역 */}
              {formatInquiryDateTime(inquiry.regiDate)}
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
        <h3 id="inquiry-answer-title" className={styles.answerHeading}>
          {/* "관리자 답변" */}
          {message("frontend.inquiry.detail.answerTitle")}
        </h3>
        {inquiry.answers?.length ? (
          <div className={styles.answers}>
            {inquiry.answers.map((answer) => (
              <article className={styles.answer} key={answer.answNumb}>
                <div className={styles.answerMeta}>
                  <strong className={styles.answerAuthor}>
                    {/* "사다리 고객센터" */}
                    {message("frontend.inquiry.detail.supportName")}
                  </strong>
                  <time className={styles.meta} dateTime={answer.regiDate}>
                    {/* 관리자 답변 등록일시 표시 영역 */}
                    {formatInquiryDateTime(answer.regiDate)}
                  </time>
                </div>
                <p className={styles.answerBody}>{answer.answCntn}</p>
              </article>
            ))}
          </div>
        ) : (
          <p className={styles.statusText}>
            {/* "답변을 준비하고 있습니다." */}
            {message("frontend.inquiry.detail.waitingTitle")}
            <br />
            {/* "조금만 기다려주세요." */}
            {message("frontend.inquiry.detail.waitingDescription")}
          </p>
        )}
      </section>
      {suspensionBackButton}
    </div>
  );
}

export default InquiryDetailPage;
