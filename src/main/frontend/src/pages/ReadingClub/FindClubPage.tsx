/**
 * fileName       : FindClubPage
 * author         : Hanwon.Jang
 * date           : 2026-09-01
 * description    : 관심분야 기반 추천과 공개 모임 검색 화면 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-01        Hanwon.Jang    최초 생성
 */

import {message} from "@/app/messages/message";
import {ActionButton} from "@/components/Button/ActionButton";
import Skeleton from "@/components/Skeleton/Skeleton";
import InterestSelectModal from "@/features/ReadingClub/components/InterestSelectModal";
import {useFindClubPage} from "@/features/ReadingClub/hooks/useFindClubPage";
import ClubCard from "./ClubCard";
import * as styles from "./FindClubPage.css";


const LOADING_CARD_KEYS = ["first", "second"];

/**
 * 추천 모임 카드 크기를 유지하는 로딩 스켈레톤을 표시
 *
 * @author Hanwon.Jang
 * @param key 스켈레톤 목록 식별값
 * @return 추천 모임 카드 스켈레톤
 */
const renderLoadingCard = (key: string) => {
  return (
    <Skeleton
      width="100%"
      height={240}
      borderRadius={22}
      key={key}
    />
  );
};

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
            icon={<img className={styles.searchIcon} src="/img/icons/icon-search.svg" alt=""/>}
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
            icon={<img className={styles.editIcon} src="/img/icons/icon-chevron-right-gray.svg" alt=""/>}
            iconPosition="right"
          >
            {/* "수정" */}
            {message("frontend.readingClub.find.editInterest")}
          </ActionButton>
        </div>

        {/* 관심 분야 칩 */}
        <div className={styles.interestChips}>
          {selectedInterests.map((interest) => (
            <span className={styles.interestChip} key={interest.intrCode}>
                {interest.intrName}
              </span>
          ))}
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
          <div className={styles.clubList}>
            {clubs.map((club) => (
              <ClubCard club={club} key={club.clubNumb} />
            ))}
          </div>
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
        <img className={styles.browseIcon} src="/img/icons/icon-chevron-right.svg" alt=""/>
      </button>

      {/* 관심 카테고리 선택 팝업 영역 */}
      {shouldShowInterestModal && (
        <InterestSelectModal
          catalog={catalog}
          initialCodes={selectedInterests.map((interest) => interest.intrCode)}
          minimum={1}
          onSave={handleSaveInterests}
          onClose={selectedInterests.length > 0 ? handleCloseInterests : undefined}
        />
      )}
    </main>
  );
}
