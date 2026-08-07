import { getApiErrorMessage } from "@/app/api/resultData";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import Loading from "@/components/Loading/Loading";
import { getNoticeDetailApi, type Notice } from "@/features/Notice/api/noticeApi";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import * as styles from "./NoticePage.css";

// "공지사항을 불러오는 중입니다"
const NOTICE_LOADING_TITLE = "공지사항을 불러오는 중입니다";

/**
 * 현재 배포 중인 공지사항 본문을 표시한다.
 *
 * @author SeungHyeon.Kang
 * @return 사용자 공지사항 상세 화면
 */
function NoticeDetailPage() {

  const { noticeNumb } = useParams();
  const [notice, setNotice] = useState<Notice | null>(null);
  const [error, setError] = useState("");

  /**
   * 주소의 공지사항 주키를 검증하고 현재 배포 버전 상세를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const loadNotice = useCallback((): void => {

    const parsedNoticeNumb = Number(noticeNumb);

    // 숫자가 아닌 주소로 접근하면 서버를 호출하지 않고 잘못된 주소를 안내한다.
    if (!Number.isInteger(parsedNoticeNumb) || parsedNoticeNumb < 1) {
      // "공지사항 주소가 올바르지 않습니다."
      setError("공지사항 주소가 올바르지 않습니다.");
      // 잘못된 주소로 상세 API를 호출하지 않고 현재 조회 흐름을 종료한다.
      return;
    }

    /**
     * 검증된 공지사항 주키로 상세를 요청하고 성공 또는 실패 상태를 화면에 반영한다.
     *
     * @author SeungHyeon.Kang
     * @return 상세 조회 완료 Promise
     */
    const requestNotice = async (): Promise<void> => {
      // 상세 API 실패를 사용자 안내 상태로 격리한다.
      try {
        // 주소의 공지사항 주키에 해당하는 현재 배포 버전을 조회한다.
        const detail = await getNoticeDetailApi(parsedNoticeNumb);
        // 조회한 현재 배포 버전을 상세 화면 상태에 반영한다.
        setNotice(detail);
      }

      catch (loadError) {
        // "공지사항을 불러오지 못했습니다."
        setError(getApiErrorMessage(loadError, "공지사항을 불러오지 못했습니다."));
      }
    };

    // 검증된 공지사항 상세 비동기 조회를 시작한다.
    void requestNotice();
  }, [noticeNumb]);

  // 주소가 바뀔 때 해당 공지사항의 현재 배포 버전을 다시 조회한다.
  useEffect(loadNotice, [loadNotice]);

  // 상세 조회에 실패하면 공통 여백 안에서 오류 문구를 안내한다.
  if (error) {
    // 공지사항 상세 조회 실패 안내 화면을 반환한다.
    return (
      <main className={styles.page}>
        {/* 공지사항 상세 조회 실패 안내 영역 */}
        <section className={styles.statusPanel} aria-live="polite">
          <p className={styles.statusText}>{error}</p>
        </section>
      </main>
    );
  }

  // 상세 조회 중에는 사용자 공통 인라인 로딩 화면을 표시한다.
  if (!notice) {
    // 공지사항 상세 조회 상태 화면을 반환한다.
    return (
      <main className={styles.page}>
        {/* 공지사항 상세 조회 상태 영역 */}
        <Loading title={NOTICE_LOADING_TITLE} isFullScreen={false} />
      </main>
    );
  }

  // 배포 일시에서 날짜 부분만 프로젝트 공통 점 표기로 변환한다.
  const displayDate = formatDashedDateToDot(notice.dplyDate?.slice(0, 10));

  // 공지사항 제목과 배포일 및 정제된 본문 화면을 반환한다.
  return (
    /* 배포된 공지사항 상세 전체 영역 */
    <main className={styles.page}>
      {/* 공지사항 제목과 배포일 영역 */}
      <header className={styles.detailHeader}>
        <div className={styles.detailMeta}>
          {notice.topxYsno === "Y" && (
            <svg className={styles.pinIcon} viewBox="0 0 24 24" aria-label="상단 고정">
              <path d="m14 4 6 6-2 2-2.5-1.5-3 3 .5 3.5-2 2-6-6 2-2 3.5.5 3-3L12 6l2-2Z" />
            </svg>
          )}
          <span className={styles.category}>{notice.cateName}</span>
        </div>
        <h1 className={styles.detailTitle}>{notice.notiTitl}</h1>
        <time className={styles.date} dateTime={notice.dplyDate}>
          {displayDate}
        </time>
      </header>

      {/* 본문은 관리자 서버에서 허용 태그와 공지 전용 이미지 경로만 남겨 저장한 HTML이다. */}
      <article className={styles.content} dangerouslySetInnerHTML={{ __html: notice.notiCntn ?? "" }} />
    </main>
  );
}

export default NoticeDetailPage;
