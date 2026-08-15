import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import Skeleton from "@/components/Skeleton/Skeleton";
import type { ReadingClub } from "@/features/ReadingClub/api/readingClubApi";
import InterestSelectModal from "@/features/ReadingClub/components/InterestSelectModal";
import { useFindClubPage } from "@/features/ReadingClub/hooks/useFindClubPage";
import type { UserInterest } from "@/features/User/api/userApi";
import ClubCard from "./ClubCard";
import * as styles from "./FindClubPage.css";

const LOADING_CARD_KEYS = ["first", "second"];

/**
 * 사용자 관심분야에서 관심분야 코드만 추출한다
 *
 * @author Hanwon.Jang
 * @param interest 코드를 추출할 사용자 관심분야
 * @return 관심분야 코드
 */
const getInterestCode = (interest: UserInterest): string => {
  // 관심 카테고리 팝업 초기 선택에 사용할 코드를 반환한다
  return interest.intrCode;
};

/**
 * 사용자 관심분야 한 항목을 읽기 전용 칩으로 표시한다
 *
 * @author Hanwon.Jang
 * @param interest 표시할 사용자 관심분야
 * @return 사용자 관심분야 칩
 */
const renderInterest = (interest: UserInterest) => {
  // 서버가 제공한 관심분야 이름을 표시하는 칩을 반환한다
  return (
    <span className={styles.interestChip} key={interest.intrCode}>
      {interest.intrName}
    </span>
  );
};

/**
 * 추천 공개 모임 한 건을 Figma 카드로 표시한다
 *
 * @author Hanwon.Jang
 * @param club 표시할 추천 공개 모임
 * @return 추천 모임 카드
 */
const renderClub = (club: ReadingClub) => {
  // 모임 번호를 목록 식별값으로 사용하는 추천 카드를 반환한다
  return <ClubCard club={club} key={club.clubNumb} />;
};

/**
 * 추천 모임 카드 크기를 유지하는 로딩 스켈레톤을 표시한다
 *
 * @author Hanwon.Jang
 * @param key 스켈레톤 목록 식별값
 * @return 추천 모임 카드 스켈레톤
 */
const renderLoadingCard = (key: string) => {
  // Figma 추천 카드와 같은 너비와 높이를 유지하는 스켈레톤을 반환한다
  return (
    <Skeleton
      className={styles.cardSkeleton}
      width="100%"
      height={240}
      borderRadius={22}
      key={key}
    />
  );
};

/**
 * 관심분야 기반 추천과 공개 모임 검색 화면을 구성한다
 *
 * @author Hanwon.Jang
 * @return Figma 모임 찾기 화면
 */
export default function FindClubPage() {
  const {
    catalog,
    clubs,
    handleCloseInterests,
    handleKeywordChange,
    handleOpenInterests,
    handleSaveInterests,
    handleSearch,
    isInterestModalOpen,
    isLoading,
    keyword,
    selectedInterests,
  } = useFindClubPage();
  const shouldShowInterestModal = !isLoading
    && (selectedInterests.length === 0 || isInterestModalOpen);
  // "모임 이름을 검색해보세요"
  const searchPlaceholder = message("frontend.readingClub.find.searchPlaceholder");
  // "검색"
  const searchButtonLabel = message("frontend.common.search");
  // "모임을 불러오고 있어요."
  const loadingLabel = message("frontend.readingClub.common.loading");

  // 검색, 관심분야, 추천 모임과 카테고리 탐색 영역을 반환한다
  return (
    <main className={styles.page}>
      {/* 모임 이름 검색 영역 */}
      <form className={styles.searchForm} onSubmit={handleSearch}>
        <label className={styles.searchLabel}>
          <span className={styles.hiddenLabel}>
            {/* "모임 검색" */}
            {message("frontend.readingClub.find.searchLabel")}
          </span>
          <input
            className={styles.searchInput}
            value={keyword}
            onChange={handleKeywordChange}
            placeholder={searchPlaceholder}
          />
          <ActionButton
            className={styles.searchButton}
            variant="secondary"
            size="sm"
            type="submit"
            aria-label={searchButtonLabel}
            icon={<img className={styles.searchIcon} src="/img/icons/icon-search.svg" alt="" />}
          />
        </label>
      </form>

      {/* 내 관심분야 확인과 수정 영역 */}
      <section className={styles.interestSection}>
        <div className={styles.sectionTitleRow}>
          <h2 className={styles.sectionTitle}>
            {/* "내 관심분야" */}
            {message("frontend.readingClub.find.interestTitle")}
          </h2>
          <ActionButton
            className={styles.editButton}
            variant="secondary"
            size="sm"
            type="button"
            onClick={handleOpenInterests}
            icon={<img className={styles.editIcon} src="/img/icons/icon-chevron-right-gray.svg" alt="" />}
            iconPosition="right"
          >
            {/* "수정" */}
            {message("frontend.readingClub.find.editInterest")}
          </ActionButton>
        </div>
        <div className={styles.interestChips}>
          {selectedInterests.map(renderInterest)}
        </div>
      </section>

      {/* 관심분야 기반 추천 모임 영역 */}
      <section className={styles.recommendSection}>
        <header className={styles.recommendHeader}>
          <h2 className={styles.sectionTitle}>
            {/* "회원님과 잘 맞는 모임" */}
            {message("frontend.readingClub.find.recommendTitle")}
          </h2>
          <p className={styles.sectionDescription}>
            {/* "관심분야를 바탕으로 사다리가 추천드려요!" */}
            {message("frontend.readingClub.find.recommendDescription")}
          </p>
        </header>

        {/* 추천 모임 목록과 조회 상태 영역 */}
        {isLoading ? (
          <div className={styles.clubList} aria-label={loadingLabel}>
            {LOADING_CARD_KEYS.map(renderLoadingCard)}
          </div>
        ) : clubs.length > 0 ? (
          <div className={styles.clubList}>{clubs.map(renderClub)}</div>
        ) : selectedInterests.length > 0 ? (
          <p className={styles.empty}>
            {/* "조건에 맞는 공개 모임이 아직 없어요." */}
            {message("frontend.readingClub.find.empty")}
          </p>
        ) : null}
      </section>

      {/* 관심 카테고리를 다시 선택하는 탐색 진입 영역 */}
      <button
        className={styles.categoryBrowse}
        type="button"
        onClick={handleOpenInterests}
      >
        <span className={styles.categoryCopy}>
          <span className={styles.categoryTitle}>
            {/* "카테고리로 둘러보기" */}
            {message("frontend.readingClub.find.categoryTitle")}
          </span>
          <span className={styles.sectionDescription}>
            {/* "나에게 맞는 모임을 찾아보세요!" */}
            {message("frontend.readingClub.find.categoryDescription")}
          </span>
        </span>
        <img className={styles.browseIcon} src="/img/icons/icon-chevron-right.svg" alt="" />
      </button>

      {/* 관심 카테고리 선택 팝업 영역 */}
      {shouldShowInterestModal && (
        <InterestSelectModal
          catalog={catalog}
          initialCodes={selectedInterests.map(getInterestCode)}
          minimum={1}
          onSave={handleSaveInterests}
          onClose={selectedInterests.length > 0 ? handleCloseInterests : undefined}
        />
      )}
    </main>
  );
}
