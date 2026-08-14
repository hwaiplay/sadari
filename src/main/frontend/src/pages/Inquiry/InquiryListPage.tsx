import { getApiErrorMessage } from "@/app/api/resultData";
import { ActionButton } from "@/components/Button/ActionButton";
import Loading from "@/components/Loading/Loading";
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
      setError(getApiErrorMessage(loadError, "고객문의를 불러오지 못했습니다."));
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
        <Loading title="고객문의를 불러오는 중입니다" isFullScreen={false} />
      </div>
    );
  }

  return (
    <div className={styles.listPage}>
      {/* 고객문의 목록 안내 영역 */}
      <section className={styles.intro} aria-label="고객문의 안내">
        <img src={"/img/icons/icon-megaphone.svg"} alt={"아이콘"} />
        <p className={styles.description}>
          문의하신 내용과 관리자 답변을 확인할 수 있어요.
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
            다시 시도
          </button>
        </section>
      ) : inquiries.length === 0 ? (
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>아직 접수한 문의가 없습니다.<br />궁금한 점이 있다면 문의를 남겨주세요.</p>
        </section>
      ) : (
        <section className={styles.list} aria-label="고객문의 목록">
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
                    새 답변
                  </span>
                )}
              </span>
            </button>
          ))}
        </section>
      )}

      {hasNext && inquiries.length > 0 && (
        <button
          className={styles.moreButton}
          type="button"
          disabled={isLoading}
          onClick={() => void loadPage(page + 1)}
        >
          {isLoading ? "불러오는 중..." : "더 보기"}
        </button>
      )}

      {error && inquiries.length > 0 && (
        <p className={styles.error} aria-live="polite">{error}</p>
      )}

      {/* 고객문의 작성 버튼 영역 */}
      <div className={styles.listWriteArea}>
        <ActionButton variant="primary" size="lg" width="full" onClick={handleWrite}>
          {/* "문의하기" */}
          문의하기
        </ActionButton>
      </div>
    </div>
  );
}

export default InquiryListPage;
