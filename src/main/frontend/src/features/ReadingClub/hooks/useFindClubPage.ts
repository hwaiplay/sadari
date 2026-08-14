/**
 * fileName       : useFindClubPage
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 모임 찾기 화면의 조회와 관심분야 수정 상태를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  getFindClubListApi,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import {
  getUserInterestCatalogApi,
  getUserInterestListApi,
  updateUserInterestsApi,
  type UserInterest,
} from "@/features/User/api/userApi";
import {
  type ChangeEvent,
  type FormEvent,
  useCallback,
  useEffect,
  useState,
} from "react";

/**
 * 관심분야 코드를 사용자 관심분야 수정 요청 항목으로 변환한다
 *
 * @author Hanwon.Jang
 * @param intrCode 저장할 관심분야 코드
 * @return 사용자 관심분야 수정 요청 항목
 */
const getInterestParam = (intrCode: string): { intrCode: string } => {
  // API 요청에 필요한 관심분야 코드 객체를 반환한다
  return { intrCode };
};

/**
 * 모임 찾기 화면의 조회 조건과 관심분야 수정 동작을 제공한다
 *
 * @author Hanwon.Jang
 * @return 모임 찾기 화면 상태와 이벤트 처리 함수
 */
export const useFindClubPage = () => {
  const [keyword, setKeyword] = useState("");
  const [clubs, setClubs] = useState<ReadingClub[]>([]);
  const [catalog, setCatalog] = useState<UserInterest[]>([]);
  const [selectedInterests, setSelectedInterests] = useState<UserInterest[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isInterestModalOpen, setIsInterestModalOpen] = useState(false);

  /**
   * 모임 찾기 조회 오류를 공통 알림으로 표시한다
   *
   * @author Hanwon.Jang
   * @param error 모임 찾기 조회 오류
   * @return 반환값이 없다
   */
  const showLoadError = useCallback((error: unknown): void => {
    // "조회하지 못했어요"
    const errorTitle = message("frontend.readingClub.error.fetchTitle");
    // "다시 시도해 주세요."
    const retryMessage = message("frontend.readingClub.common.retry");
    // 서버 오류 문구가 없을 때 공통 재시도 안내로 보정한다
    const errorMessage = getApiErrorMessage(error, retryMessage);
    // 모임 조회 실패 원인을 사용자에게 표시한다
    void sweetError(errorTitle, errorMessage);
  }, []);

  /**
   * 관심분야와 추천 모임을 조회하여 화면 초기 상태를 구성한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   * @throws 관심분야 또는 모임 조회 요청이 실패하면 공통 오류 알림을 표시한다
   */
  const getInitialData = useCallback(async (): Promise<void> => {
    // 첫 화면 데이터가 준비될 때까지 로딩 상태를 표시한다
    setIsLoading(true);

    try {
      // 관심 카테고리 원본과 사용자의 현재 선택을 함께 조회한다
      const [nextCatalog, nextSelected] = await Promise.all([
        getUserInterestCatalogApi(),
        getUserInterestListApi(),
      ]);

      // 관심 카테고리 팝업에서 사용할 전체 목록을 설정한다
      setCatalog(nextCatalog);
      // 내 관심분야 칩에 표시할 현재 선택을 설정한다
      setSelectedInterests(nextSelected);

      // 관심분야가 있는 사용자만 추천 모임 API를 호출한다
      if (nextSelected.length > 0) {
        // 관심분야 일치도를 적용한 공개 모임을 조회한다
        const nextClubs = await getFindClubListApi("");
        // 추천 모임 목록을 최신 응답으로 교체한다
        setClubs(nextClubs);
      }
    } catch (error: unknown) {
      // 초기 조회 실패 원인을 공통 오류 알림으로 전달한다
      showLoadError(error);
    } finally {
      // 성공 여부와 관계없이 초기 로딩 표시를 종료한다
      setIsLoading(false);
    }
  }, [showLoadError]);

  /**
   * 화면 진입 시 관심분야와 추천 모임 초기 조회를 시작한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const initializePage = useCallback((): void => {
    // 화면 초기 데이터를 비동기로 조회한다
    void getInitialData();
  }, [getInitialData]);

  // 화면 생명주기와 초기 조회 함수를 연결한다
  useEffect(initializePage, [initializePage]);

  /**
   * 모임 검색어 입력 상태를 변경한다
   *
   * @author Hanwon.Jang
   * @param event 검색어 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleKeywordChange = (event: ChangeEvent<HTMLInputElement>): void => {
    // 사용자가 입력한 모임 검색어를 설정한다
    setKeyword(event.target.value);
  };

  /**
   * 현재 검색어로 공개 모임 목록을 다시 조회한다
   *
   * @author Hanwon.Jang
   * @param event 모임 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearch = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저의 기본 폼 이동을 차단하고 SPA 검색을 유지한다
    event.preventDefault();
    // 검색 결과가 준비될 때까지 로딩 상태를 표시한다
    setIsLoading(true);

    /**
     * 모임 검색 응답을 화면 상태에 반영한다
     *
     * @author Hanwon.Jang
     * @return 반환값이 없다
     */
    const getSearchResult = async (): Promise<void> => {
      try {
        // 현재 입력한 검색어로 공개 모임을 조회한다
        const nextClubs = await getFindClubListApi(keyword);
        // 추천 모임 영역을 검색 결과로 교체한다
        setClubs(nextClubs);
      } catch (error: unknown) {
        // 검색 실패 원인을 공통 오류 알림으로 전달한다
        showLoadError(error);
      } finally {
        // 검색 요청이 끝나면 로딩 표시를 종료한다
        setIsLoading(false);
      }
    };

    // 검색 폼 제출과 비동기 조회를 연결한다
    void getSearchResult();
  };

  /**
   * 관심 카테고리 수정 팝업을 연다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleOpenInterests = (): void => {
    // 현재 선택값을 수정할 수 있도록 관심 카테고리 팝업을 표시한다
    setIsInterestModalOpen(true);
  };

  /**
   * 선택값이 있는 사용자의 관심 카테고리 수정 팝업을 닫는다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleCloseInterests = (): void => {
    // 기존 선택을 유지하고 관심 카테고리 팝업을 숨긴다
    setIsInterestModalOpen(false);
  };

  /**
   * 선택한 관심분야를 저장하고 추천 모임을 갱신한다
   *
   * @author Hanwon.Jang
   * @param codes 저장할 관심분야 코드 목록
   * @return 반환값이 없다
   */
  const handleSaveInterests = (codes: string[]): void => {
    /**
     * 관심분야 저장과 추천 모임 재조회를 순서대로 처리한다
     *
     * @author Hanwon.Jang
     * @return 반환값이 없다
     */
    const uptInterests = async (): Promise<void> => {
      try {
        // 선택한 코드를 사용자 관심분야 교체 요청 형식으로 전달한다
        await updateUserInterestsApi({
          interestList: codes.map(getInterestParam),
        });
        /**
         * 저장된 코드에 포함된 관심분야인지 판정한다
         *
         * @author Hanwon.Jang
         * @param interest 전체 카탈로그의 관심분야
         * @return 저장된 관심분야 코드에 포함되면 true
         */
        const hasSelectedCode = (interest: UserInterest): boolean => {
          // 저장된 코드 목록에 포함되는지 반환한다
          return codes.includes(interest.intrCode);
        };

        // 저장된 코드에 해당하는 관심분야 표시 정보를 구성한다
        const nextSelected = catalog.filter(hasSelectedCode);
        // 내 관심분야 칩을 저장된 선택으로 갱신한다
        setSelectedInterests(nextSelected);
        // 관심분야 변경 후 전체 추천을 보여주도록 검색어를 초기화한다
        setKeyword("");
        // 변경된 관심분야 일치도를 적용한 추천 모임을 다시 조회한다
        const nextClubs = await getFindClubListApi("");
        // 추천 모임 목록을 최신 응답으로 교체한다
        setClubs(nextClubs);
        // 저장이 완료된 뒤 관심 카테고리 팝업을 닫는다
        setIsInterestModalOpen(false);
      } catch (error: unknown) {
        // 저장 또는 재조회 실패 원인을 공통 오류 알림으로 전달한다
        showLoadError(error);
      }
    };

    // 관심분야 선택 완료와 비동기 저장을 연결한다
    void uptInterests();
  };

  // 모임 찾기 화면에서 사용하는 상태와 동작만 반환한다
  return {
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
  };
};
