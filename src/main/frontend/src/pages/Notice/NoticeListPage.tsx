import { getApiErrorMessage } from "@/app/api/resultData";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import Loading from "@/components/Loading/Loading";
import { getNoticeListApi, type Notice } from "@/features/Notice/api/noticeApi";
import { type MouseEvent, useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./NoticePage.css";

// "공지사항을 불러오는 중입니다"
const NOTICE_LOADING_TITLE = "공지사항을 불러오는 중입니다";

/**
 * 현재 배포 중인 공지사항을 최근 배포 순서로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @return 사용자 공지사항 목록 화면
 */
function NoticeListPage() {

  const navigate = useNavigate();
  const [notices, setNotices] = useState<Notice[]>([]);
  const [page, setPage] = useState(1);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  /**
   * 요청 페이지를 조회하여 첫 페이지를 교체하거나 다음 페이지를 기존 목록 뒤에 연결한다.
   *
   * @author SeungHyeon.Kang
   * @param targetPage 조회할 페이지 번호
   * @return 반환값이 없다
   */
  const loadPage = useCallback(async (targetPage: number): Promise<void> => {

    // 목록 요청 동안 중복 조회를 막고 기존 오류 안내를 제거한다.
    setIsLoading(true);
    setError("");

    try {
      // 현재 배포 중인 공지사항만 페이지 단위로 조회한다.
      const data = await getNoticeListApi(targetPage);

      /**
       * 조회한 페이지를 현재 공지사항 목록에 반영한다.
       *
       * @author SeungHyeon.Kang
       * @param current 현재 화면의 공지사항 목록
       * @return 첫 페이지 교체 또는 다음 페이지가 연결된 공지사항 목록
       */
      const mergeNoticeList = (current: Notice[]): Notice[] => {
        // 첫 페이지는 새 목록으로 교체하고 다음 페이지는 현재 목록 뒤에 추가한다.
        return targetPage === 1 ? data.list : [...current, ...data.list];
      };

      // 첫 페이지는 새 목록으로 교체하고 다음 페이지는 현재 목록 뒤에 추가한다.
      setNotices(mergeNoticeList);
      // 서버가 반환한 현재 페이지 번호를 다음 조회 기준으로 저장한다.
      setPage(data.page);
      // 다음 공지사항 페이지 존재 여부를 더 보기 버튼 상태에 반영한다.
      setHasNext(data.hasNext);
    } catch (loadError) {
      // "공지사항을 불러오지 못했습니다."
      setError(getApiErrorMessage(loadError, "공지사항을 불러오지 못했습니다."));
    } finally {
      // 요청 성공 여부와 관계없이 로딩 상태를 종료한다.
      setIsLoading(false);
    }
  }, []);

  /**
   * 화면 최초 진입 시 공지사항 첫 페이지 조회를 시작한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const loadInitialPage = useCallback((): void => {
    // 최신 공지사항부터 표시하기 위해 첫 페이지를 조회한다.
    void loadPage(1);
  }, [loadPage]);

  // 화면 최초 진입 시 최신 공지사항부터 조회한다.
  useEffect(loadInitialPage, [loadInitialPage]);

  /**
   * 선택한 공지사항 상세 화면으로 이동한다.
   *
   * @author SeungHyeon.Kang
   * @param noticeNumb 이동할 공지사항 주키
   * @return 반환값이 없다
   */
  const openNotice = (noticeNumb: number): void => {

    // 선택한 공지사항의 현재 배포 버전 상세 경로로 이동한다.
    navigate(`/notice/list/${noticeNumb}`);
  };

  /**
   * 선택한 목록 행의 공지사항 번호를 읽어 상세 화면을 연다.
   *
   * @author SeungHyeon.Kang
   * @param event 공지사항 목록 버튼 클릭 이벤트
   * @return 반환값이 없다
   */
  const handleNoticeClick = (event: MouseEvent<HTMLButtonElement>): void => {
    // 목록 버튼에 저장한 공지사항 주키를 숫자로 변환한다.
    const noticeNumb = Number(event.currentTarget.dataset.noticeNumb);

    // 유효한 공지사항 주키만 상세 경로에 사용한다.
    if (Number.isInteger(noticeNumb) && noticeNumb > 0) {
      // 검증된 공지사항의 상세 화면을 연다.
      openNotice(noticeNumb);
    }
  };

  /**
   * 첫 페이지 공지사항 조회를 다시 시도한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleRetry = (): void => {
    // 오류 상태를 새 요청으로 교체하기 위해 첫 페이지를 다시 조회한다.
    void loadPage(1);
  };

  /**
   * 현재 목록 다음의 공지사항 페이지를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleLoadMore = (): void => {
    // 서버가 마지막으로 반환한 페이지 다음 번호를 조회한다.
    void loadPage(page + 1);
  };

  /**
   * 개별 공지사항을 제목과 배포일 및 상세 이동 버튼으로 구성한다.
   *
   * @author SeungHyeon.Kang
   * @param notice 화면에 표시할 공지사항
   * @return 공지사항 목록의 개별 행
   */
  const renderNoticeItem = (notice: Notice) => {
    // 배포 일시에서 날짜 부분만 프로젝트 공통 점 표기로 변환한다.
    const displayDate = formatDashedDateToDot(notice.dplyDate?.slice(0, 10));
    // 읽은 공지는 알림센터와 같은 낮은 명도 스타일을 함께 적용한다.
    const itemClassName = notice.readYsno === "Y" ? `${styles.item} ${styles.itemRead}` : styles.item;

    // 공지사항 카테고리와 제목 및 배포일을 포함한 카드형 행을 반환한다.
    return (
      /* 개별 공지사항 제목과 배포일 및 상세 이동 영역 */
      <button
        className={itemClassName}
        type="button"
        data-notice-numb={notice.notiNumb}
        key={notice.notiNumb}
        onClick={handleNoticeClick}
      >
        <span className={styles.itemText}>
          <span className={styles.titleRow}>
            {notice.topxYsno === "Y" && (
              <svg className={styles.pinIcon} viewBox="0 0 24 24" aria-label="상단 고정">
                <path d="m14 4 6 6-2 2-2.5-1.5-3 3 .5 3.5-2 2-6-6 2-2 3.5.5 3-3L12 6l2-2Z" />
              </svg>
            )}
            <span className={styles.titleWithUnread}>
              <span className={styles.title}>{notice.notiTitl}</span>
              {notice.readYsno === "N" && <span className={styles.unreadDot} aria-label="읽지 않음" />}
            </span>
          </span>
          <span className={styles.itemBottom}>
            <time className={styles.date} dateTime={notice.dplyDate}>
              {displayDate}
            </time>
            <span className={styles.category}>{notice.cateName}</span>
          </span>
        </span>
      </button>
    );
  };

  // 최초 목록을 조회하는 동안 사용자 공통 인라인 로딩 화면을 표시한다.
  if (isLoading && notices.length === 0 && !error) {
    // 공지사항 최초 조회 상태 화면을 반환한다.
    return (
      <main className={styles.listPage}>
        {/* 공지사항 최초 조회 상태 영역 */}
        <Loading title={NOTICE_LOADING_TITLE} isFullScreen={false} />
      </main>
    );
  }

  // 공지사항 목록과 조회 상태 및 다음 페이지 제어 화면을 반환한다.
  return (
    /* 배포된 사용자 공지사항 목록 전체 영역 */
    <main className={styles.listPage}>
      {/* 공지사항 목록 안내 영역 */}
      <section className={styles.intro} aria-label="공지사항 안내">
        {/* "사다리의 새로운 소식과 주요 안내를 확인할 수 있어요." */}
        <p className={styles.description}>사다리의 새로운 소식과 주요 안내를 확인할 수 있어요.</p>
      </section>

      {error && notices.length === 0 ? (
        /* 공지사항 조회 실패 안내와 재시도 영역 */
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>{error}</p>
          <button
            className={styles.retryButton}
            type="button"
            disabled={isLoading}
            onClick={handleRetry}
          >
            {/* "다시 시도" */}
            다시 시도
          </button>
        </section>
      ) : notices.length === 0 ? (
        /* 등록된 공지사항이 없는 상태 안내 영역 */
        <section className={styles.statusPanel} aria-live="polite">
          {/* "등록된 공지사항이 없습니다." */}
          <p className={styles.statusText}>등록된 공지사항이 없습니다.</p>
        </section>
      ) : (
        /* 최근 배포 순서의 공지사항 목록 영역 */
        <section className={styles.list} aria-label="공지사항 목록">
          {notices.map(renderNoticeItem)}
        </section>
      )}

      {/* 다음 공지사항 페이지 조회 영역 */}
      {hasNext && notices.length > 0 && (
        <button
          className={styles.moreButton}
          type="button"
          disabled={isLoading}
          onClick={handleLoadMore}
        >
          {isLoading ? (
            <>
              {/* "불러오는 중..." */}
              불러오는 중...
            </>
          ) : (
            <>
              {/* "더 보기" */}
              더 보기
            </>
          )}
        </button>
      )}

      {/* 다음 페이지 조회 실패 안내 영역 */}
      {error && notices.length > 0 && (
        <p className={styles.moreError} aria-live="polite">{error}</p>
      )}
    </main>
  );
}

export default NoticeListPage;
