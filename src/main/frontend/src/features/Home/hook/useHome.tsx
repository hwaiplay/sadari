/**
 * fileName       : useHome
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 홈 화면의 독후감 조회와 검색 및 정렬 상태를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */
import { getApiErrorMessage } from "@/app/api/resultData.ts";
import { message } from "@/app/messages/message.ts";
import type { HomeBookType } from "@/features/Book/types/book.type.ts";
import { useGetListQuery } from "@/features/Home/hook/useGetListQuery.tsx";
import type { ChangeEvent, FormEvent, ReactNode } from "react";
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

export type HomeSortType =
  | "END_DATE_DESC"
  | "START_DATE_DESC"
  | "GRADE_DESC";

export type HomeBookGroup = {
  key: string;
  label: ReactNode;
  rows: HomeBookType[][];
};

type HomeLocationState = {
  resetHomeSearch?: boolean;
};

type HomeSortOption = {
  value: HomeSortType;
  label: string;
};

type HomeGroupLabel = {
  key: string;
  label: ReactNode;
};

const BOOKS_PER_ROW = 3;

const SORT_OPTION_KEYS: ReadonlyArray<{
  value: HomeSortType;
  labelKey: string;
}> = [
  {
    value: "END_DATE_DESC",
    labelKey: "frontend.home.sort.endDateDesc",
  },
  {
    value: "START_DATE_DESC",
    labelKey: "frontend.home.sort.startDateDesc",
  },
  {
    value: "GRADE_DESC",
    labelKey: "frontend.home.sort.gradeDesc",
  },
];

/**
 * 독후감의 독서 날짜를 연월 그룹 정보로 변환한다
 *
 * @author Hanwon.Jang
 * @param book 그룹을 계산할 독후감
 * @param sortType 현재 정렬 기준
 * @return 연월 그룹 식별값과 화면 라벨
 */
function getMonthGroup(
  book: HomeBookType,
  sortType: HomeSortType,
): HomeGroupLabel {
  // 선택한 날짜 정렬 기준과 동일한 독서 날짜를 그룹 계산에 사용한다
  const targetDate =
    sortType === "START_DATE_DESC" ? book.reptStdt : book.reptEndt;
  // 날짜 문자열에서 연도와 월만 안전하게 추출한다
  const match = targetDate?.match(/^(\d{4})-(\d{2})/);

  // 독서 날짜가 없거나 형식이 잘못되면 별도 그룹으로 구분한다
  if (!match) {
    // 날짜가 없는 독후감의 그룹 정보를 반환한다
    return {
      key: "unknown",
      label: "날짜 없음",
    };
  }

  const [, year, month] = match;

  // 화면에 표시할 두 자리 연도와 월 그룹 정보를 반환한다
  return {
    key: `${year}-${month}`,
    label: `${year.slice(2)}.${month}`,
  };
}

/**
 * 독후감 평점을 별 아이콘 그룹 정보로 변환한다
 *
 * @author Hanwon.Jang
 * @param book 그룹을 계산할 독후감
 * @return 평점 그룹 식별값과 화면 라벨
 */
function getGradeGroup(book: HomeBookType): HomeGroupLabel {
  const rawGrade = Number(book.reptGrde);
  // 숫자로 변환할 수 있는 평점만 화면에서 지원하는 0점부터 5점 범위로 제한한다
  const grade = Number.isFinite(rawGrade)
    ? Math.max(0, Math.min(5, rawGrade))
    : 0;
  const starCount = Math.floor(grade);
  const gradeIcons: ReactNode[] = [];

  // 평점이 없을 때도 평점 기준 그룹임을 식별할 수 있도록 빈 별을 표시한다
  if (grade === 0) {
    // 빈 별 아이콘을 평점 그룹 라벨에 추가한다
    gradeIcons.push(
      <img
        key="empty-star"
        src="/img/icons/icon-star-rate-empty.svg"
        alt=""
        aria-hidden="true"
      />,
    );
  } else {
    // 평점 수만큼 채워진 별을 표시한다
    for (let index = 0; index < starCount; index += 1) {
      // 채워진 별 아이콘을 평점 그룹 라벨에 추가한다
      gradeIcons.push(
        <img
          key={`filled-star-${index}`}
          src="/img/icons/icon-star-rate.svg"
          alt=""
          aria-hidden="true"
        />,
      );
    }
  }

  const gradeLabel = (
    <span aria-label={`평점 ${starCount}점`}>{gradeIcons}</span>
  );

  // 화면에 표시할 평점 그룹 정보를 반환한다
  return {
    key: String(starCount),
    label: gradeLabel,
  };
}

/**
 * 독후감 목록을 지정한 크기의 행 목록으로 나눈다
 *
 * @author Hanwon.Jang
 * @param bookList 행으로 나눌 독후감 목록
 * @param size 한 행에 포함할 독후감 개수
 * @return 행 단위로 분리된 독후감 목록
 */
function getBookRows(bookList: HomeBookType[], size: number): HomeBookType[][] {
  // 독후감 목록을 화면 그리드의 행 단위로 변환한다
  return Array.from(
    { length: Math.ceil(bookList.length / size) },
    /**
     * 행 순서에 해당하는 독후감 목록을 추출한다
     *
     * @author Hanwon.Jang
     * @param _ 생성 배열에서 사용하지 않는 현재 값
     * @param index 생성할 행의 순서
     * @return 현재 행에 포함되는 독후감 목록
     */
    (_, index) => bookList.slice(index * size, index * size + size),
  );
}

/**
 * 독후감 목록을 현재 정렬 기준의 연속 그룹으로 묶는다
 *
 * @author Hanwon.Jang
 * @param bookList 그룹으로 묶을 독후감 목록
 * @param sortType 현재 정렬 기준
 * @return 화면에 표시할 독후감 그룹 목록
 */
function getBookGroups(
  bookList: HomeBookType[],
  sortType: HomeSortType,
): HomeBookGroup[] {
  const groups: Array<HomeGroupLabel & { books: HomeBookType[] }> = [];

  // 서버 정렬 순서를 유지하면서 연속된 독후감을 같은 그룹으로 묶는다
  for (const book of bookList) {
    // 평점 정렬은 별점으로 묶고 날짜 정렬은 해당 연월로 묶는다
    const groupLabel =
      sortType === "GRADE_DESC"
        ? getGradeGroup(book)
        : getMonthGroup(book, sortType);
    const currentGroup = groups[groups.length - 1];

    // 바로 앞 그룹과 식별값이 같으면 해당 그룹에 독후감을 추가한다
    if (currentGroup?.key === groupLabel.key) {
      currentGroup.books.push(book);
      continue;
    }

    // 새로운 식별값은 다음 화면 그룹으로 추가한다
    groups.push({
      ...groupLabel,
      books: [book],
    });
  }

  // 각 그룹을 화면 그리드의 행 구조로 변환해 반환한다
  return groups.map(
    /**
     * 독후감 그룹을 화면 렌더링 모델로 변환한다
     *
     * @author Hanwon.Jang
     * @param group 변환할 독후감 그룹
     * @return 행 구조가 포함된 화면 그룹
     */
    (group) => ({
      key: group.key,
      label: group.label,
      rows: getBookRows(group.books, BOOKS_PER_ROW),
    }),
  );
}

/**
 * 정렬 메시지 키를 홈 화면 선택 옵션으로 변환한다
 *
 * @author Hanwon.Jang
 * @param option 변환할 정렬 옵션 정의
 * @return 번역된 라벨을 포함한 정렬 옵션
 */
function getSortOption(option: {
  value: HomeSortType;
  labelKey: string;
}): HomeSortOption {
  // 현재 언어의 메시지를 적용한 정렬 옵션을 반환한다
  return {
    value: option.value,
    // "종료일순", "시작일순", "별점순"
    label: message(option.labelKey),
  };
}

/**
 * 홈 화면의 독후감 조회 상태와 검색 및 정렬 동작을 제공한다
 *
 * @author Hanwon.Jang
 * @return 홈 화면이 렌더링에 사용할 상태와 이벤트 처리 함수
 */
export function useHome() {
  // 홈 검색 초기화 요청 상태를 조회한다
  const location = useLocation();
  // 검색 결과가 없을 때 도서 검색 화면으로 이동할 함수를 조회한다
  const navigate = useNavigate();
  const [sortType, setSortType] = useState<HomeSortType>("END_DATE_DESC");
  const [searchKeyword, setSearchKeyword] = useState("");
  const [appliedSearchKeyword, setAppliedSearchKeyword] = useState("");
  // 적용된 검색어와 정렬 기준으로 홈 독후감 목록을 조회한다
  const homeQuery = useGetListQuery({
    bookKeyword: appliedSearchKeyword,
    sortType,
  });
  // 서버 페이지가 바뀔 때만 독후감 목록을 화면 정렬 순서대로 연결한다
  const bookList = useMemo(
    () => homeQuery.data?.pages.flatMap((page) => page.data?.list ?? []) ?? [],
    [homeQuery.data?.pages],
  );

  /**
   * 현재 독후감 목록과 정렬 기준으로 화면 그룹을 계산한다
   *
   * @author Hanwon.Jang
   * @return 행 구조가 포함된 독후감 그룹 목록
   */
  const calculateBookGroups = (): HomeBookGroup[] => {
    // 홈 화면에 표시할 독후감 그룹 목록을 반환한다
    return getBookGroups(bookList, sortType);
  };

  // 목록이나 정렬 기준이 바뀔 때만 화면 그룹을 다시 계산한다
  const bookGroups = useMemo(calculateBookGroups, [bookList, sortType]);

  /**
   * 홈 화면의 정렬 선택 옵션을 현재 언어로 생성한다
   *
   * @author Hanwon.Jang
   * @return 현재 언어의 라벨을 포함한 정렬 옵션 목록
   */
  const calculateSortOptions = (): HomeSortOption[] => {
    // 메시지가 적용된 홈 정렬 옵션 목록을 반환한다
    return SORT_OPTION_KEYS.map(getSortOption);
  };

  // 홈 화면이 다시 렌더링되어도 고정 정렬 옵션을 재사용한다
  const sortOptions = useMemo(calculateSortOptions, []);
  const hasSearchCondition = appliedSearchKeyword.trim().length > 0;

  /**
   * 다른 화면에서 전달된 홈 검색 초기화 요청을 반영한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const resetHomeSearch = (): void => {
    const state = location.state as HomeLocationState | null;

    // 홈 검색 초기화 요청이 없으면 현재 검색 조건을 유지한다
    if (!state?.resetHomeSearch) {
      return;
    }

    // 입력 중인 홈 검색어를 초기화한다
    setSearchKeyword("");
    // 적용된 홈 검색 조건을 초기화한다
    setAppliedSearchKeyword("");
  };

  // 홈 이동 상태가 바뀌면 검색 초기화 요청을 확인한다
  useEffect(resetHomeSearch, [location.key, location.state]);

  /**
   * 홈 검색 입력값을 화면 상태에 반영한다
   *
   * @author Hanwon.Jang
   * @param event 검색 입력 변경 이벤트
   * @return 반환값이 없다
   */
  const handleSearchChange = (event: ChangeEvent<HTMLInputElement>): void => {
    // 사용자가 입력 중인 검색어를 저장한다
    setSearchKeyword(event.target.value);
  };

  /**
   * 입력된 검색어를 독후감 목록 조회 조건으로 적용한다
   *
   * @author Hanwon.Jang
   * @param event 홈 검색 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSearchSubmit = (event: FormEvent<HTMLFormElement>): void => {
    // 브라우저 폼 제출로 전체 화면이 새로고침되지 않게 한다
    event.preventDefault();
    // 검색어 앞뒤 공백을 제거한 값을 목록 조회 조건으로 적용한다
    setAppliedSearchKeyword(searchKeyword.trim());
  };

  /**
   * 홈 독후감 목록의 정렬 기준을 변경한다
   *
   * @author Hanwon.Jang
   * @param nextSortType 사용자가 선택한 정렬 기준
   * @return 반환값이 없다
   */
  const handleSortChange = (nextSortType: HomeSortType): void => {
    // 선택한 정렬 기준으로 독후감 목록을 다시 조회한다
    setSortType(nextSortType);
  };

  /**
   * 홈 검색어를 유지하여 도서 검색 화면으로 이동한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleBookSearch = (): void => {
    // 독후감 검색 결과가 없을 때 같은 검색어로 도서 검색을 시작한다
    navigate("/book/search", {
      state: { initialSearchKeyword: appliedSearchKeyword.trim() },
    });
  };

  // 조회 실패 시에만 공통 재시도 문구를 기본값으로 사용해 오류 메시지를 정제한다
  const errorMessage = homeQuery.isError
    // "다시 시도해주세요."
    ? getApiErrorMessage(homeQuery.error, message("frontend.common.tryAgain"))
    : "";

  // 홈 화면이 계산 없이 사용할 조회 상태와 사용자 동작을 반환한다
  return {
    data: homeQuery.data?.pages[0],
    isPending: homeQuery.isPending,
    isError: homeQuery.isError,
    errorMessage,
    bookList,
    bookGroups,
    sortType,
    sortOptions,
    searchKeyword,
    appliedSearchKeyword,
    hasSearchCondition,
    hasNextBook: Boolean(homeQuery.hasNextPage),
    isNextBookLoading: homeQuery.isFetchingNextPage,
    loadMoreBook: homeQuery.fetchNextPage,
    handleSearchChange,
    handleSearchSubmit,
    handleSortChange,
    handleBookSearch,
  };
}
