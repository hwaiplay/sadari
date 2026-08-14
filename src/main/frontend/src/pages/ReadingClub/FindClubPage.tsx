import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { getFindClubListApi, type ReadingClub } from "@/features/ReadingClub/api/readingClubApi";
import InterestSelectModal from "@/features/ReadingClub/components/InterestSelectModal";
import { getUserInterestCatalogApi, getUserInterestListApi, updateUserInterestsApi, type UserInterest } from "@/features/User/api/userApi";
import { type FormEvent, useEffect, useState } from "react";
import ClubCard from "./ClubCard";
import * as styles from "./FindClubPage.css";

/** 관심분야를 선행 조건으로 공개 모임 검색 화면을 구성한다. @author SeungHyeon.Kang @return 모임 찾기 화면 */
export default function FindClubPage() {
  const [keyword, setKeyword] = useState("");
  const [clubs, setClubs] = useState<ReadingClub[]>([]);
  const [catalog, setCatalog] = useState<UserInterest[]>([]);
  const [selectedInterests, setSelectedInterests] = useState<UserInterest[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  /** 공개 모임을 현재 검색어로 조회한다. @author SeungHyeon.Kang @param nextKeyword 적용할 검색어 @return 반환값이 없다 */
  const loadClubs = async (nextKeyword: string): Promise<void> => {
    // 관심분야 우선 정렬된 공개 모임을 요청한다
    setClubs(await getFindClubListApi(nextKeyword));
  };

  useEffect(() => {
    // 관심분야 보유 여부와 선택 팝업 목록을 함께 조회한다
    void Promise.all([getUserInterestCatalogApi(), getUserInterestListApi()]).then(async ([nextCatalog, nextSelected]) => {
      // 팝업 선택 후보를 설정한다
      setCatalog(nextCatalog);
      // 현재 사용자 선택을 설정한다
      setSelectedInterests(nextSelected);
      // 관심분야가 있으면 공개 모임을 즉시 조회한다
      if (nextSelected.length > 0) await loadClubs("");
    }).catch((error) => void sweetError(
      message("frontend.readingClub.error.fetchTitle"),
      getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
    )).finally(() => setIsLoading(false));
  }, []);

  /** 검색 폼을 제출한다. @author SeungHyeon.Kang @param event 폼 제출 이벤트 @return 반환값이 없다 */
  const submitSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 기본 새로고침을 막는다
    event.preventDefault();
    // 입력한 검색어로 목록을 다시 조회한다
    void loadClubs(keyword);
  };

  /** 관심분야를 저장하고 모임 찾기를 시작한다. @author SeungHyeon.Kang @param codes 선택한 세부코드 @return 반환값이 없다 */
  const saveInterests = async (codes: string[]): Promise<void> => {
    // 선택한 세부코드 전체를 사용자 관심분야로 저장한다
    await updateUserInterestsApi({ interestList: codes.map((intrCode) => ({ intrCode })) });
    // 저장된 관심분야 표시 정보를 구성한다
    setSelectedInterests(catalog.filter((interest) => codes.includes(interest.intrCode)));
    // 관심분야 기반 공개 모임 목록을 조회한다
    await loadClubs("");
  };

  // 관심분야 선택과 검색 목록 화면을 반환한다
  return (
    <div className={styles.page}>
      <form onSubmit={submitSearch}>
        <label className={styles.searchLabel}>
          <span className={styles.hiddenLabel}>{message("frontend.readingClub.find.searchLabel")}</span>
          <input className={styles.searchInput} value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder={message("frontend.readingClub.find.searchPlaceholder")} />
          <button className={styles.searchButton} type="submit" aria-label={message("frontend.readingClub.find.searchButton")}>⌕</button>
        </label>
      </form>
      {selectedInterests.length > 0 && <div className={styles.chips}>{selectedInterests.map((interest) => <span className={styles.chip} key={interest.intrCode}>{interest.intrName}</span>)}</div>}
        {isLoading ? <p className={styles.loading}>{message("frontend.readingClub.common.loading")}</p> : clubs.length > 0 ? <div className={styles.list}>{clubs.map((club) => <ClubCard club={club} key={club.clubNumb} />)}</div> : selectedInterests.length > 0 && <p className={styles.empty}>{message("frontend.readingClub.find.empty")}</p>}
      {/* 관심분야가 한 개도 없으면 닫을 수 없는 필수 선택 팝업을 노출한다 */}
      {!isLoading && selectedInterests.length === 0 && <InterestSelectModal catalog={catalog} initialCodes={[]} minimum={1} onSave={(codes) => void saveInterests(codes)} />}
    </div>
  );
}
