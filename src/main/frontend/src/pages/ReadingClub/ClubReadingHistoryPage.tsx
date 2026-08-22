import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Skeleton from "@/components/Skeleton/Skeleton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import type { ClubReadingHistory } from "@/features/ReadingClub/api/readingClubApi";
import { useReadingHistory } from "@/features/ReadingClub/hooks/useReadingHistory";
import { getGoalProgressColor } from "@/features/User/utils/goalProgress";
import { clsx } from "clsx";
import { useMemo } from "react";
import { useParams } from "react-router-dom";
import * as styles from "./ClubReadingHistoryPage.css";

/**
 * 종료 회차의 공동 독서 기간을 목록 표시 형식으로 변환한다.
 *
 * @author SeungHyeon.Kang
 * @param goalStdt 목표 독서 시작일
 * @param goalEndt 목표 독서 종료일
 * @return 같은 연도에서 종료 연도를 생략한 독서 기간
 */
function formatReadingPeriod(goalStdt: string, goalEndt: string): string {
  const startDate = formatDashedDateToDot(goalStdt);
  const endDate = formatDashedDateToDot(goalEndt);

  // 같은 연도의 회차는 피그마 표시처럼 종료 연도를 생략해 반환한다
  return goalStdt.slice(0, 4) === goalEndt.slice(0, 4)
    ? `${startDate} ~ ${endDate.slice(5)}`
    : `${startDate} ~ ${endDate}`;
}

/**
 * 종료 회차 한 건의 도서와 목표 달성 진행률을 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param history 표시할 이전 독서 기록
 * @return 피그마 카드 구조의 종료 회차 항목
 */
function renderReadingHistory(history: ClubReadingHistory) {
  // 비정상 집계에서도 진행 막대에 사용할 유효한 달성률을 유지한다
  const achievementRate = history.partCnt > 0
    ? Math.min(100, Math.max(0, (history.goalAchvCnt / history.partCnt) * 100))
    : 0;
  const goalProgressColor = getGoalProgressColor(achievementRate);

  // 도서 정보와 공통 달성률 색상 정책을 적용한 회차 카드를 반환한다
  return (
    <li className={clsx(styles.historyCard, styles.compactCard)} key={history.rondNumb}>
      <img
        className={styles.bookCover}
        src={getBookCoverImageSource(history.bookCvim)}
        alt={history.bookTitl}
        onError={handleBookCoverImageError}
      />
      <div className={styles.historyContent}>
        <div className={styles.bookSummary}>
          <div className={styles.bookIdentity}>
            <strong className={styles.bookTitle}>{history.bookTitl}</strong>
            {history.bookAthr ? (
              <span className={styles.bookAuthor}>{history.bookAthr}</span>
            ) : null}
          </div>
          <span className={styles.readingPeriod}>
            {formatReadingPeriod(history.goalStdt, history.goalEndt)}
          </span>
        </div>
        <div className={styles.progressArea}>
          <div className={styles.progressTrack}>
            <span
              className={styles.progressFill}
              style={{
                width: `${achievementRate}%`,
                backgroundColor: goalProgressColor,
              }}
            />
          </div>
          <span className={styles.progressDescription}>
            {message("frontend.readingClub.detail.goalAchievement", [
              history.goalAchvCnt,
              history.partCnt,
            ])}
          </span>
        </div>
      </div>
    </li>
  );
}

/**
 * 현재 활성 모임원에게 가입 이전을 포함한 모든 종료 회차를 표시한다.
 *
 * @author SeungHyeon.Kang
 * @return 이전 독서 기록 목록 페이지
 */
export default function ClubReadingHistoryPage() {
  // 서버 접근 검증에 사용할 모임 번호를 경로에서 조회한다
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const isValidRoute = Number.isFinite(clubNumb) && clubNumb > 0;
  // 현재 활성 모임원에게 허용된 모든 종료 회차를 페이지 단위로 조회한다
  const historyQuery = useReadingHistory(clubNumb, isValidRoute);
  // 조회된 서버 페이지를 최신 회차 순서의 단일 목록으로 연결한다
  const historyList = useMemo(() => {
    // 아직 응답하지 않은 페이지는 빈 목록으로 처리해 반환한다
    return historyQuery.data?.pages.flatMap((page) => page.list) ?? [];
  }, [historyQuery.data]);

  // 모임 번호가 유효하지 않으면 서버 요청 없이 잘못된 접근을 안내한다
  if (!isValidRoute) {
    // 잘못된 이전 독서 기록 경로 안내를 반환한다
    return <p className={styles.invalidAccess}>{message("frontend.common.invalidAccess")}</p>;
  }

  // 첫 페이지를 조회하는 동안 피그마 카드 크기의 로딩 화면을 표시한다
  if (historyQuery.isPending) {
    // 카드 목록과 같은 간격의 로딩 영역을 반환한다
    return (
      <div className={styles.page} aria-busy="true">
        <div className={styles.loadingList}>
          <Skeleton width="100%" height={174} borderRadius={22} />
          <Skeleton width="100%" height={174} borderRadius={22} />
          <Skeleton width="100%" height={174} borderRadius={22} />
        </div>
      </div>
    );
  }

  // 접근 거절 또는 목록 조회 실패 시 서버 오류 문구를 표시한다
  if (historyQuery.isError) {
    // 이전 독서 기록 조회 실패 안내를 반환한다
    return (
      <p className={styles.stateMessage}>
        {getApiErrorMessage(historyQuery.error, message("frontend.common.tryAgain"))}
      </p>
    );
  }

  // 조회 가능한 종료 회차가 없으면 빈 목록 안내를 표시한다
  if (historyList.length === 0) {
    // 이전 독서 기록 빈 상태 안내를 반환한다
    return (
      <p className={styles.stateMessage}>
        {message("frontend.readingClub.history.empty")}
      </p>
    );
  }

  // 피그마 카드 목록과 다음 페이지 자동 조회 영역을 반환한다
  return (
    <section className={styles.page} aria-label={message("frontend.readingClub.history.title")}>
      <ul className={styles.historyList}>
        {historyList.map(renderReadingHistory)}
      </ul>
      <InfiniteScrollTrigger
        hasNext={Boolean(historyQuery.hasNextPage)}
        isLoading={historyQuery.isFetchingNextPage}
        onLoadMore={() => {
          // 하단 감지 시 다음 종료 회차 페이지를 조회한다
          void historyQuery.fetchNextPage();
        }}
      >
        <p className={styles.loadingMore}>
          {message("frontend.readingClub.history.loading")}
        </p>
      </InfiniteScrollTrigger>
    </section>
  );
}
