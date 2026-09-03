import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { ActionButton } from "@/components/Button/ActionButton";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import {
  delUserBlockApi,
  getBlockUserPageApi,
  type BlockedUser,
} from "@/features/Social/api/socialApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useCallback, useEffect, useRef, useState } from "react";
import * as styles from "./BlockedUsersPage.css";

const FIRST_PAGE = 1;

/**
 * 로그인 사용자가 직접 차단한 사용자 목록과 해제 기능을 제공한다
 *
 * @author HanWon.Jang
 * @return 차단 사용자 관리 화면
 */
const BlockedUsersPage = () => {
  const [blockedUsers, setBlockedUsers] = useState<BlockedUser[]>([]);
  const [page, setPage] = useState(FIRST_PAGE);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [unblockingUserNumb, setUnblockingUserNumb] = useState<number | null>(null);
  const isLoadingMoreRef = useRef(false);

  /**
   * 차단 사용자 페이지를 조회하고 기존 목록 뒤에 중복 없이 추가한다
   *
   * @author HanWon.Jang
   * @param nextPage 조회할 페이지 번호
   * @return 조회 완료 Promise
   * @throws 차단 목록 API 또는 공통 응답 검증 실패 시 발생
   */
  const getBlockedUsers = useCallback(async (nextPage: number): Promise<void> => {
    // 이미 다음 페이지를 조회 중이면 중복 요청을 시작하지 않는다
    if (isLoadingMoreRef.current) {
      // 진행 중인 조회가 목록을 갱신하도록 추가 처리를 종료한다
      return;
    }

    // 자동 하단 감지가 같은 페이지를 반복 호출하지 않도록 조회 상태를 잠근다
    isLoadingMoreRef.current = true;

    try {
      // 로그인 사용자가 직접 만든 차단 방향의 한 페이지를 조회한다
      const result = await getBlockUserPageApi(nextPage);
      // 첫 페이지는 서버 원본으로 교체하고 다음 페이지는 기존 목록 뒤에 추가한다
      setBlockedUsers((currentUsers) => nextPage === FIRST_PAGE
        ? result.list
        : [...currentUsers, ...result.list]);
      // 서버가 정규화한 현재 페이지 번호를 다음 조회 기준으로 저장한다
      setPage(result.page);
      // 다음 페이지 존재 여부를 하단 감지 상태에 반영한다
      setHasNext(result.hasNext);
    }

    catch (error) {
      // "차단 목록을 불러오지 못했습니다."
      await sweetError(
        message("frontend.settings.blocked.loadFailed"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    finally {
      // 성공과 실패 모두 이후 조회가 가능하도록 요청 잠금을 해제한다
      isLoadingMoreRef.current = false;
      // 최초 조회 화면의 공통 로딩 상태를 종료한다
      setIsLoading(false);
    }
  }, []);

  /**
   * 목록 하단에 도달하면 서버의 다음 차단 사용자 페이지를 조회한다
   *
   * @author HanWon.Jang
   * @return 반환값이 없다
   */
  const handleLoadMore = (): void => {
    // 서버가 다음 페이지를 확정한 경우에만 현재 페이지 다음 번호를 요청한다
    if (hasNext) {
      // 다음 차단 사용자 페이지를 비동기로 조회한다
      void getBlockedUsers(page + 1);
    }
  };

  /**
   * 선택한 한 방향 차단을 확인한 뒤 서버에서 해제하고 목록에서 제거한다
   *
   * @author HanWon.Jang
   * @param blockedUser 차단 해제 대상 사용자
   * @return 차단 해제 완료 Promise
   */
  const handleUnblock = async (blockedUser: BlockedUser): Promise<void> => {
    const displayName = blockedUser.userNick
      ?? blockedUser.userStatName
      // "알 수 없는 사용자"
      ?? message("frontend.settings.blocked.unknownUser");
    // "{닉네임} 님의 차단을 해제하시겠어요?"
    const result = await sweetConfirm({
      title: message("frontend.settings.blocked.confirmTitle", [displayName]),
      // "차단을 해제하면 서로의 공개 프로필과 콘텐츠를 다시 볼 수 있습니다."
      text: message("frontend.settings.blocked.confirmDescription"),
      // "차단 해제"
      confirmButtonText: message("frontend.settings.blocked.unblock"),
      // "취소"
      cancelButtonText: message("frontend.common.cancel"),
    });

    // 사용자가 취소하면 차단 관계와 화면 목록을 그대로 유지한다
    if (!result.isConfirmed) {
      // 확인된 상태 변경이 없으므로 후속 처리를 종료한다
      return;
    }

    // 현재 해제 대상 버튼을 비활성화하도록 사용자 번호를 저장한다
    setUnblockingUserNumb(blockedUser.blocNumb);

    try {
      // 처리 중 이동을 차단하고 로그인 사용자가 소유한 한 방향 차단만 해제한다
      await runBlockingOperation(() => delUserBlockApi(blockedUser.blocNumb), {
        // "차단을 해제하고 있어요."
        title: message("frontend.settings.blocked.processing"),
        success: {
          // "차단을 해제했어요."
          title: message("frontend.settings.blocked.success"),
        },
      });
      // 성공한 사용자만 현재 차단 관리 목록에서 제거한다
      setBlockedUsers((currentUsers) => currentUsers.filter(
        (currentUser) => currentUser.blocNumb !== blockedUser.blocNumb,
      ));
    }

    catch (error) {
      // "차단을 해제하지 못했습니다."
      await sweetError(
        message("frontend.settings.blocked.unblockFailed"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    finally {
      // 성공과 실패 모두 선택한 사용자의 버튼 비활성 상태를 해제한다
      setUnblockingUserNumb(null);
    }
  };

  /**
   * 차단 사용자 항목의 해제 버튼 클릭을 현재 사용자 데이터와 연결한다
   *
   * @author HanWon.Jang
   * @param blockedUser 차단 해제 대상 사용자
   * @return 반환값이 없다
   */
  const handleUnblockClick = (blockedUser: BlockedUser): void => {
    // 비동기 차단 해제 실패는 함수 내부 공통 오류 알림에서 처리한다
    void handleUnblock(blockedUser);
  };

  // 화면 진입 시 최신 차단 사용자의 첫 페이지를 조회한다
  useEffect(() => {
    // 로그인 사용자가 만든 차단 목록 첫 페이지를 조회한다
    void getBlockedUsers(FIRST_PAGE);
  }, [getBlockedUsers]);

  // 최초 차단 목록 조회 중에는 페이지 전체 공통 로딩 화면을 표시한다
  if (isLoading) {
    // 차단 관리 화면의 최초 로딩 상태를 반환한다
    return <Loading />;
  }

  // 차단 목록과 각 사용자별 해제 명령을 포함한 관리 화면을 반환한다
  return (
    /* 차단 사용자 관리 전체 영역 */
    <main className={styles.page}>
      {/* 차단 관리 제목과 기능 설명 영역 */}
      <header className={styles.header}>
        <h1 className={styles.title}>
          {/* "차단한 사용자" */}
          {message("frontend.settings.blocked.title")}
        </h1>
        <p className={styles.description}>
          {/* "차단한 사용자는 서로의 공개 프로필과 콘텐츠를 볼 수 없습니다." */}
          {message("frontend.settings.blocked.description")}
        </p>
      </header>

      {/* 로그인 사용자가 직접 차단한 사용자 목록 영역 */}
      {blockedUsers.length === 0 ? (
        <p className={styles.empty}>
          {/* "차단한 사용자가 없습니다." */}
          {message("frontend.settings.blocked.empty")}
        </p>
      ) : (
        <ul className={styles.list}>
          {/* 차단 사용자별 기본 이미지와 상태 및 해제 명령 영역 */}
          {blockedUsers.map((blockedUser) => {
            const displayName = blockedUser.userNick
              ?? blockedUser.userStatName
              // "알 수 없는 사용자"
              ?? message("frontend.settings.blocked.unknownUser");

            // 차단 사용자의 실제 사진을 노출하지 않는 개별 관리 항목을 반환한다
            return (
              <li className={styles.item} key={blockedUser.blocNumb}>
                {/* 실제 사진 대신 기본 이미지와 현재 사용자 상태를 표시하는 영역 */}
                <div className={styles.userInfo}>
                  <ProfileImage className={styles.avatar} alt="" />
                  <span className={styles.userName}>{displayName}</span>
                </div>
                {/* 현재 로그인 사용자가 소유한 차단 방향을 해제하는 명령 영역 */}
                <ActionButton
                  variant="secondary"
                  size="sm"
                  disabled={unblockingUserNumb === blockedUser.blocNumb}
                  onClick={() => handleUnblockClick(blockedUser)}
                >
                  {/* "차단 해제" */}
                  {message("frontend.settings.blocked.unblock")}
                </ActionButton>
              </li>
            );
          })}
        </ul>
      )}

      {/* 차단 사용자 다음 페이지 자동 조회 상태 영역 */}
      <InfiniteScrollTrigger
        hasNext={hasNext}
        isLoading={isLoadingMoreRef.current}
        onLoadMore={handleLoadMore}
      >
        {/* "차단 목록을 불러오는 중..." */}
        {message("frontend.settings.blocked.loadingMore")}
      </InfiniteScrollTrigger>
    </main>
  );
};

export default BlockedUsersPage;
