import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import { getInquiryListApi, type Inquiry } from "@/features/Inquiry/api/inquiryApi";
import { type MouseEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./InquiryPage.css";

/**
 * 인증 사용자가 접수한 고객문의와 답변 상태를 표시합니다.
 *
 * @author SeungHyeon.Kang
 * @return 고객문의 목록 화면
 */
function InquiryListPage() {

  const navigate = useNavigate();
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  /**
   * 요청한 페이지의 문의 내역을 조회하여 화면 목록에 반영합니다.
   *
   * @author SeungHyeon.Kang
   * @param targetPage 조회할 페이지 번호
   * @return 반환값이 없습니다
   */
  const loadPage = useCallback(async (targetPage: number): Promise<void> => {

    setIsLoading(true);
    setError("");

    try {
      const data = await getInquiryListApi(targetPage);

      setInquiries((current) => (
        targetPage === 1 ? data.list : [...current, ...data.list]
      ));
      setPage(data.page);
      setHasNext(data.hasNext);
    } catch (loadError) {
      // "고객문의를 불러오지 못했습니다."
      setError(getApiErrorMessage(loadError, message("frontend.inquiry.list.loadFailed")));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {

    void loadPage(1);
  }, [loadPage]);

  /**
   * 문의 작성 화면으로 이동합니다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없습니다
   */
  const handleWrite = (): void => {

    navigate("/inquiry/write");
  };

  /**
   * 선택한 문의 상세 화면으로 이동합니다.
   *
   * @author SeungHyeon.Kang
   * @param event 문의 목록 버튼 클릭 이벤트
   * @return 반환값이 없습니다
   */
  const handleInquiryClick = (event: MouseEvent<HTMLButtonElement>): void => {

    const inqrNumb = Number(event.currentTarget.dataset.inqrNumb);

    if (Number.isInteger(inqrNumb) && inqrNumb > 0) {
      navigate(`/inquiry/detail/${inqrNumb}`);
    }
  };

  if (isLoading && inquiries.length === 0 && !error) {
    return (
      <div className={styles.page}>
        <Loading isFullScreen={false} />
      </div>
    );
  }

  return (
    <div className={styles.listPage}>
      {/* 고객문의 페이지 설명 영역 */}
      <section className={styles.intro} aria-labelledby="inquiry-list-description">
        <p id="inquiry-list-description" className={styles.description}>
          {/* "문의하신 내용과 관리자 답변을 확인할 수 있어요." */}
          {message("frontend.inquiry.list.description")}
        </p>
      </section>

      {error && inquiries.length === 0 ? (
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>{error}</p>
          <button
            className={styles.actionButton}
            type="button"
            disabled={isLoading}
            onClick={() => void loadPage(1)}
          >
            {/* "다시 시도" */}
            {message("frontend.common.retry")}
          </button>
        </section>
      ) : inquiries.length === 0 ? (
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>
            {/* "아직 접수한 문의가 없습니다." */}
            {message("frontend.inquiry.list.emptyTitle")}
            <br />
            {/* "궁금한 점이 있다면 문의를 남겨주세요." */}
            {message("frontend.inquiry.list.emptyDescription")}
          </p>
        </section>
      ) : (
        <>
          {/* "고객문의 목록" */}
          <section className={styles.list} aria-label={message("frontend.inquiry.list.label")}>
            {inquiries.map((inquiry) => (
              <button
                className={styles.item}
                type="button"
                data-inqr-numb={inquiry.inqrNumb}
                key={inquiry.inqrNumb}
                onClick={handleInquiryClick}
              >
                <span className={styles.itemTitle}>{inquiry.inqrTitl}</span>
                <span className={styles.itemBottom}>
                  <time className={styles.meta} dateTime={inquiry.regiDate}>
                    {new Date(inquiry.regiDate).toLocaleDateString("ko-KR")}
                  </time>
                  <span className={styles.itemMetaGroup}>
                    <span className={styles.category}>{inquiry.inqrCatgName}</span>
                    <span className={styles.state}>{inquiry.inqrStatName}</span>
                  </span>
                  {inquiry.unreadCount > 0 && (
                    <span className={styles.unread}>
                      <span className={styles.unreadDot} aria-hidden="true" />
                      {/* "새 답변" */}
                      {message("frontend.inquiry.list.newAnswer")}
                    </span>
                  )}
                </span>
              </button>
            ))}
          </section>
        </>
      )}

      {inquiries.length > 0 && (
        <InfiniteScrollTrigger
          hasNext={hasNext}
          isLoading={isLoading}
          onLoadMore={() => void loadPage(page + 1)}
        >
          {/* "불러오는 중..." */}
          {message("frontend.common.loadingMore")}
        </InfiniteScrollTrigger>
      )}

      {error && inquiries.length > 0 && (
        <p className={styles.error} aria-live="polite">{error}</p>
      )}

      {/* 고객문의 작성 버튼 영역 */}
      <div className={styles.listWriteArea}>
        <ActionButton variant="primary" size="lg" width="full" onClick={handleWrite}>
          {/* "문의하기" */}
          {message("frontend.inquiry.list.write")}
        </ActionButton>
      </div>
    </div>
  );
}

export default InquiryListPage;
