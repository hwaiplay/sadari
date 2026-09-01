import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { formatDashedDateToDot } from "@/app/utils/dateUtil";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Skeleton from "@/components/Skeleton/Skeleton";
import {
  getBookCoverImageSource,
  handleBookCoverImageError,
} from "@/features/Book/utils/bookCoverImage";
import {
  type ReadingHistoryOverview,
  useReadingHistoryPage,
} from "@/features/ReadingClub/hooks/useReadingHistoryPage";
import { getGoalProgressColor } from "@/features/User/utils/goalProgress";
import { clsx } from "clsx";
import * as styles from "./ClubReadingHistoryPage.css";

type ReadingHistoryCardProps = {
  history: ReadingHistoryOverview;
  onSelect: (history: ReadingHistoryOverview) => void;
};

/**
 * 종료 회차의 공동 독서 기간을 목록 표시 형식으로 변환한다.
 *
 * @author HanWon.Jang
 * @param goalStdt 목표 독서 시작일
 * @param goalEndt 목표 독서 종료일
 * @return 같은 연도에서 종료 연도를 생략한 독서 기간
 */
const formatReadingPeriod = (goalStdt: string, goalEndt: string): string => {
  const startDate = formatDashedDateToDot(goalStdt);
  const endDate = formatDashedDateToDot(goalEndt);

  // 같은 연도의 회차는 피그마 표시처럼 종료 연도를 생략해 반환한다
  return goalStdt.slice(0, 4) === goalEndt.slice(0, 4)
    ? `${startDate} ~ ${endDate.slice(5)}`
    : `${startDate} ~ ${endDate}`;
};

/**
 * 종료 회차 한 건의 도서와 목표 달성 진행률을 표시한다.
 *
 * @author HanWon.Jang
 * @param props 이전 독서 기록 카드 표시와 선택 처리값
 * @return 피그마 카드 구조의 종료 회차 항목
 */
const ReadingHistoryCard = ({
  history,
  onSelect,
}: ReadingHistoryCardProps) => {
  // 비정상 집계에서도 진행 막대에 사용할 유효한 달성률을 유지한다
  const achievementRate = history.partCnt > 0
    ? Math.min(100, Math.max(0, (history.goalAchvCnt / history.partCnt) * 100))
    : 0;
  const goalProgressColor = getGoalProgressColor(achievementRate);

  /**
   * 현재 카드의 완료 회차를 목표 결과 조회 대상으로 선택한다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleSelect = (): void => {
    // 카드가 표현하는 회차와 결과 상세 접근 권한을 페이지 선택 상태에 전달한다
    onSelect(history);
  };

  // 도서 정보와 공통 달성률 색상 정책을 적용한 회차 카드를 반환한다
  return (
    <li className={styles.historyItem}>
      <button
        className={clsx(styles.historyCard, styles.compactCard)}
        type="button"
        disabled={!history.resultAccessible}
        onClick={handleSelect}
      >
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
      </button>
    </li>
  );
};

/**
 * 활성 모임원과 공개 중인 활성 모임 조회자에게 모든 종료 회차를 표시한다.
 *
 * @author HanWon.Jang
 * @return 이전 독서 기록 목록 페이지
 */
const ClubReadingHistoryPage = () => {
  // 페이지의 경로 검증, 종료 회차 조회와 결과 오버레이 상태를 전용 훅에서 조회한다
  const {
    historyList,
    historyQuery,
    isValidRoute,
    handleLoadMore,
    handleSelectReading,
  } = useReadingHistoryPage();

  /**
   * 이전 독서 기록 한 건을 선택 가능한 회차 카드로 표시한다.
   *
   * @author HanWon.Jang
   * @param history 표시할 이전 독서 기록
   * @return 선택한 회차 결과 조회 상태를 반영한 카드
   */
  const renderReadingHistory = (history: ReadingHistoryOverview) => {
    // 현재 조회 중인 회차만 중복 선택을 막은 카드로 반환한다
    return (
      <ReadingHistoryCard
        key={history.rondNumb}
        history={history}
        onSelect={handleSelectReading}
      />
    );
  };

  // 모임 번호가 유효하지 않으면 서버 요청 없이 잘못된 접근을 안내한다
  if (!isValidRoute) {
    // 잘못된 이전 독서 기록 경로 안내를 반환한다
    return <p className={styles.invalidAccess}>{message("frontend.common.invalidAccess")}</p>;
  }

  // 첫 페이지를 조회하는 동안 로딩 화면을 표시한다
  if (historyQuery.isPending) {
    // 이전 독서 기록 카드 크기의 로딩 화면을 반환한다
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

  // 카드 목록과 뷰포트 기준 결과 오버레이를 포함한 이전 독서 기록 화면을 반환한다
  return (
    <section className={styles.page} aria-label={message("frontend.readingClub.history.title")}>
      <ul className={styles.historyList}>
        {historyList.map(renderReadingHistory)}
      </ul>
      <InfiniteScrollTrigger
        hasNext={Boolean(historyQuery.hasNextPage)}
        isLoading={historyQuery.isFetchingNextPage}
        onLoadMore={handleLoadMore}
      >
        <p className={styles.loadingMore}>
          {message("frontend.readingClub.history.loading")}
        </p>
      </InfiniteScrollTrigger>
    </section>
  );
};

export default ClubReadingHistoryPage;
