import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import * as stickyStyles from "@/components/Search/StickySearchBar/StickySearchBar.css";
import { useStickySearch } from "@/components/Search/StickySearchBar/useStickySearch";
import {
  delSocialFollowApi,
  getMyFollowPageApi,
  setSocialFollowApi,
  type FollowListType,
  type FollowUser,
} from "@/features/Social/api/socialApi";
import { isFollowedByMe } from "@/features/Social/utils/followStatus";
import * as userListStyles from "@/features/Social/components/LikeUserListButton.css";
import ProfileImage from "@/features/User/components/ProfileImage";
import { clsx } from "clsx";
import type { FormEvent } from "react";
import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./FollowManagementPage.css";

const FIRST_PAGE = 1;

type FollowManagementPageProps = {
  type: FollowListType;
};

/**
 * 마이페이지와 동일한 팔로우 관계 목록과 닉네임 검색 및 관계 변경 제공
 *
 * @author SeungHyeon.Kang
 * @param props 팔로잉 또는 팔로워 목록 유형
 * @return 팔로우 관계 관리 화면
 */
const FollowManagementPage = ({ type }: FollowManagementPageProps) => {
  const navigate = useNavigate();
  // 공통 검색 영역의 실제 고정 상태와 감지 경계 조회
  const { isSticky, sentinelRef } = useStickySearch();
  const [users, setUsers] = useState<FollowUser[]>([]);
  const [page, setPage] = useState(FIRST_PAGE);
  const [hasNext, setHasNext] = useState(false);
  const [keywordInput, setKeywordInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [updatingUserNumb, setUpdatingUserNumb] = useState<number | null>(null);

  /**
   * 현재 목록 유형과 닉네임 조건에 해당하는 한 페이지 조회
   *
   * @author SeungHyeon.Kang
   * @param nextPage 조회할 페이지 번호
   * @param searchKeyword 닉네임 검색어
   * @param replace 기존 목록 교체 여부
   * @return 목록 조회 완료 Promise
   * @throws 팔로우 목록 API 또는 공통 응답 검증 실패 시 발생
   */
  const getUsers = useCallback(async (
    nextPage: number,
    searchKeyword: string,
    replace: boolean,
  ): Promise<void> => {
    // 서버의 관계 범위와 닉네임 검색 조건을 함께 적용한 페이지 조회
    const pageData = (await getMyFollowPageApi(type, nextPage, searchKeyword)).data;

    // 불완전한 공통 성공 응답의 화면 반영 차단
    if (!pageData) {
      // 오류 처리 경로 연결용 내부 예외
      throw new Error("FOLLOW_PAGE_EMPTY");
    }

    // 새 검색은 첫 페이지로 교체하고 추가 조회는 기존 목록 뒤에 연결
    setUsers((currentUsers) => replace ? pageData.list : [...currentUsers, ...pageData.list]);
    // 서버가 정규화한 현재 페이지 저장
    setPage(pageData.page);
    // 다음 페이지 존재 여부 저장
    setHasNext(pageData.hasNext);
  }, [type]);

  /**
   * 입력한 닉네임을 확정하고 첫 페이지부터 재조회
   *
   * @author SeungHyeon.Kang
   * @param event 검색 폼 제출 이벤트
   * @return 검색 완료 Promise
   */
  const handleSearchSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 폼 제출로 인한 전체 새로고침 방지
    event.preventDefault();

    // 중복 검색과 추가 페이지 조회의 상태 경합 방지
    if (isSearching || isLoadingMore) {
      // 진행 중인 요청이 화면을 갱신하도록 종료
      return;
    }

    const normalizedKeyword = keywordInput.trim();
    // 확정 검색어와 검색 진행 상태 저장
    setKeyword(normalizedKeyword);
    setIsSearching(true);

    try {
      // 현재 입력값으로 검색 결과 첫 페이지 교체
      await getUsers(FIRST_PAGE, normalizedKeyword, true);
    }

    catch (error) {
      // "조회에 실패했습니다."
      await sweetError(
        message("frontend.alert.loadFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    finally {
      // 검색 성공과 실패 뒤 입력 및 목록 조작 재허용
      setIsSearching(false);
    }
  };

  /**
   * 목록 하단 도달 시 현재 검색 조건의 다음 페이지 조회
   *
   * @author SeungHyeon.Kang
   * @return 반환값 없음
   */
  const handleLoadMore = (): void => {
    // 마지막 페이지와 중복 요청의 추가 조회 차단
    if (!hasNext || isLoadingMore || isSearching) {
      // 현재 목록 유지
      return;
    }

    // 목록 하단의 추가 조회 상태 표시
    setIsLoadingMore(true);

    // 다음 페이지 성공과 실패를 기존 목록 유지 정책으로 처리
    void getUsers(page + 1, keyword, false)
      .catch(async (error) => {
        // "조회에 실패했습니다."
        await sweetError(
          message("frontend.alert.loadFailedTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      })
      .finally(() => {
        // 추가 페이지 재요청 허용
        setIsLoadingMore(false);
      });
  };

  /**
   * 선택한 목록 사용자의 프로필 화면 이동
   *
   * @author SeungHyeon.Kang
   * @param user 이동할 팔로우 목록 사용자
   * @return 반환값 없음
   */
  const handleProfileClick = (user: FollowUser): void => {
    // 내 계정은 마이페이지로 이동하고 다른 계정은 공개 프로필로 이동
    navigate(user.meYsno === "Y" ? "/mypage/profile" : `/social/profile/${user.userNumb}`);
  };

  /**
   * 현재 버튼 상태에 따른 팔로우 또는 언팔로우 처리
   *
   * @author SeungHyeon.Kang
   * @param user 관계를 변경할 팔로우 목록 사용자
   * @return 관계 변경 완료 Promise
   */
  const handleStatusClick = async (user: FollowUser): Promise<void> => {
    // 다른 관계 변경 중이거나 내 계정인 행의 중복 조작 차단
    if (updatingUserNumb || user.meYsno === "Y") {
      // 현재 관계 상태 유지
      return;
    }

    // 서버 버튼명에 따른 로그인 사용자의 팔로우 방향 확인
    const isFollowing = isFollowedByMe(user.followStatName);

    // 팔로잉 또는 친구 관계 해제 전 사용자 확인
    if (isFollowing) {
      const result = await sweetConfirm({
        // "언팔로우하시겠어요?"
        title: message("frontend.social.unfollow.title"),
        // "팔로우 관계만 해제돼요."
        text: message("frontend.settings.follow.unfollowConfirmText"),
        // "언팔로우"
        confirmButtonText: message("frontend.social.unfollow.confirm"),
        // "취소"
        cancelButtonText: message("frontend.common.cancel"),
      });

      // 취소한 관계 변경의 서버 요청 차단
      if (!result.isConfirmed) {
        // 기존 목록과 버튼 상태 유지
        return;
      }
    }

    // 현재 관계 변경 대상의 중복 제출 차단
    setUpdatingUserNumb(user.userNumb);

    try {
      // 관계 변경 전체 구간의 화면 이동 차단과 같은 모달 성공 전환
      const response = await runBlockingOperation(
        () => isFollowing
          ? delSocialFollowApi(user.userNumb)
          : setSocialFollowApi(user.userNumb),
        {
          // "언팔로우하고 있어요." 또는 "팔로우하고 있어요."
          title: message(isFollowing
            ? "frontend.settings.follow.unfollowProcessing"
            : "frontend.settings.follow.followProcessing"),
          success: {
            // "언팔로우했어요." 또는 "팔로우했어요."
            title: message(isFollowing
              ? "frontend.settings.follow.unfollowSuccess"
              : "frontend.settings.follow.followSuccess"),
          },
        },
      );

      // 팔로잉 관리에서 해제한 사용자는 현재 목록 범위에서 제거
      if (type === "following" && isFollowing) {
        // 해제한 한 방향 관계의 사용자 행 제거
        setUsers((currentUsers) => currentUsers.filter(
          (currentUser) => currentUser.userNumb !== user.userNumb,
        ));
        // 관계 해제 뒤 추가 상태 갱신 없이 종료
        return;
      }

      // 팔로워 관리에서는 상대의 팔로워 관계를 유지하고 내 버튼 상태만 갱신
      setUsers((currentUsers) => currentUsers.map((currentUser) =>
        currentUser.userNumb === user.userNumb
          ? {
            ...currentUser,
            followStatName: response.data?.followStatName ?? currentUser.followStatName,
          }
          : currentUser));
    }

    catch (error) {
      // "관계를 변경하지 못했습니다."
      await sweetError(
        message("frontend.settings.follow.updateFailed"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    finally {
      // 성공과 실패 뒤 관계 버튼 재활성화
      setUpdatingUserNumb(null);
    }
  };

  /**
   * 비동기 관계 변경 실패를 공통 오류 처리에 연결
   *
   * @author SeungHyeon.Kang
   * @param user 관계를 변경할 팔로우 목록 사용자
   * @return 반환값 없음
   */
  const handleStatusButton = (user: FollowUser): void => {
    // 관계 변경 내부의 확인과 실패 알림 경로 실행
    void handleStatusClick(user);
  };

  // 목록 유형 변경 시 검색 조건과 첫 페이지 초기화
  useEffect(() => {
    // 이전 목록 유형의 화면 상태 제거
    setUsers([]);
    setPage(FIRST_PAGE);
    setHasNext(false);
    setKeywordInput("");
    setKeyword("");
    setIsLoading(true);

    // 현재 목록 유형의 첫 페이지 조회와 초기 오류 안내
    void getUsers(FIRST_PAGE, "", true)
      .catch(async (error) => {
        // "조회에 실패했습니다."
        await sweetError(
          message("frontend.alert.loadFailedTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      })
      .finally(() => {
        // 최초 목록 조회 완료 상태 반영
        setIsLoading(false);
      });
  }, [getUsers]);

  // 최초 관계 목록 조회 중 공통 페이지 로딩 화면
  if (isLoading) {
    // 현재 메뉴명 기반 공통 로딩 화면 반환
    return <Loading />;
  }

  const emptyKey = keyword
    ? "frontend.settings.follow.noSearchResult"
    : type === "following"
      ? "frontend.profile.followingList.empty"
      : "frontend.profile.followerList.empty";

  // 검색과 관계 버튼을 포함한 팔로우 관리 화면 반환
  return (
    /* 팔로우 또는 팔로워 관리 전체 영역 */
    <main className={styles.page}>
      {/* 닉네임 검색 영역이 고정되는 시점 감지 경계 */}
      <span ref={sentinelRef} className={stickyStyles.sentinel} aria-hidden="true" />
      {/* 스크롤 중 공통 헤더 아래에 유지되는 닉네임 검색 영역 */}
      <form
        className={clsx(
          styles.searchForm,
          stickyStyles.surface,
          isSticky && stickyStyles.stuck,
        )}
        onSubmit={handleSearchSubmit}
      >
        <label className={styles.searchLabel}>
          <span className={styles.hiddenLabel}>
            {/* "닉네임 검색" */}
            {message("frontend.settings.follow.searchLabel")}
          </span>
          <input
            className={styles.searchInput}
            type="search"
            value={keywordInput}
            placeholder={/* "닉네임으로 검색" */ message("frontend.settings.follow.searchPlaceholder")}
            disabled={isSearching}
            onChange={(event) => setKeywordInput(event.target.value)}
          />
          <button
            className={styles.searchButton}
            type="submit"
            aria-label={/* "검색" */ message("frontend.common.search")}
            disabled={isSearching || isLoadingMore}
          >
            <svg
              className={styles.searchIcon}
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                d="M10.8 5.2a5.6 5.6 0 1 1 0 11.2 5.6 5.6 0 0 1 0-11.2Z"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
              />
              <path
                d="m15 15 4 4"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.8"
                strokeLinecap="round"
              />
            </svg>
          </button>
        </label>
      </form>

      {/* 검색 중 공통 소형 로딩과 관계 목록 영역 */}
      {isSearching ? (
        <Loading
          title={/* "목록 조회 중" */ message("frontend.common.loadingList")}
          isFullScreen={false}
          isCompact
        />
      ) : users.length === 0 ? (
        <p className={styles.empty}>
          {/* "검색 결과가 없습니다." 또는 관계 목록 빈 상태 */}
          {message(emptyKey)}
        </p>
      ) : (
        <ul className={styles.list}>
          {/* 팔로우 관계 사용자별 프로필과 현재 관계 버튼 영역 */}
          {users.map((user) => (
            <li className={styles.item} key={user.userNumb}>
              {/* 사용자 프로필 이동 영역 */}
              <button
                className={styles.profileButton}
                type="button"
                onClick={() => handleProfileClick(user)}
              >
                <ProfileImage
                  className={styles.avatar}
                  src={user.porfPath}
                  alt={user.userNick ?? /* "닉네임" */ message("frontend.profile.nick")}
                />
                <span className={styles.userText}>
                  <strong className={styles.userName}>{user.userNick || "-"}</strong>
                  <span className={styles.userIntro}>
                    {user.intrCntn || /* "한줄 소개를 등록해보세요." */ message("frontend.profile.intro.empty")}
                  </span>
                </span>
              </button>

              {/* 로그인 사용자 기준 팔로우 관계 변경 영역 */}
              {user.meYsno !== "Y" && (
                <button
                  className={userListStyles.statusButton}
                  data-follow-status={user.followStatName}
                  type="button"
                  disabled={updatingUserNumb === user.userNumb}
                  onClick={() => handleStatusButton(user)}
                >
                  {user.followStatName}
                </button>
              )}
            </li>
          ))}
        </ul>
      )}

      {/* 현재 검색 조건의 다음 관계 페이지 자동 조회 영역 */}
      <InfiniteScrollTrigger
        hasNext={!isSearching && hasNext}
        isLoading={isLoadingMore}
        onLoadMore={handleLoadMore}
      />
    </main>
  );
};

export default FollowManagementPage;
