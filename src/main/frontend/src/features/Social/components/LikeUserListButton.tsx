import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { useBodyScrollLock } from "@/app/utils/modalUtil";
import InfiniteScrollTrigger from "@/components/InfiniteScroll/InfiniteScrollTrigger";
import Loading from "@/components/Loading/Loading";
import * as modalControlStyles from "@/components/Modal/ModalControls.css";
import {
  delSocialFollowApi,
  getLikeUserPageApi,
  setSocialFollowApi,
  type FollowUser,
  type LikeTargetType,
} from "@/features/Social/api/socialApi";
import { isFollowedByMe } from "@/features/Social/utils/followStatus";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import * as styles from "./LikeUserListButton.css";

type LikeUserListButtonProps = {
  tagtType: LikeTargetType;
  tagtNumb: number;
  countLabel: string | number;
  className?: string;
};

/**
 * 좋아요 숫자와 팔로우 관계 변경이 가능한 활성 사용자 목록 팝업을 공통으로 제공함
 *
 * @author HanWon.Jang
 * @param props 좋아요 대상과 숫자 버튼 표시 정보
 * @return 좋아요 숫자 버튼과 팔로워 및 팔로잉 형식의 사용자 목록 팝업
 */
const LikeUserListButton = ({
  tagtType,
  tagtNumb,
  countLabel,
  className,
}: LikeUserListButtonProps) => {
  const navigate = useNavigate();
  const triggerButtonRef = useRef<HTMLButtonElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [users, setUsers] = useState<FollowUser[]>([]);
  const [page, setPage] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isNextLoading, setIsNextLoading] = useState(false);
  const [isListScrolling, setIsListScrolling] = useState(false);
  const [updatingUserNumb, setUpdatingUserNumb] = useState<number | null>(null);
  const scrollTimeoutRef = useRef<number | null>(null);
  useBodyScrollLock(isOpen);

  /** 지정한 페이지의 활성 좋아요 사용자를 조회함 */
  const loadPage = async (targetPage: number): Promise<void> => {
    // 첫 페이지와 추가 페이지의 로딩 상태를 구분해 기존 목록을 유지함
    if (targetPage === 1) setIsLoading(true);
    else setIsNextLoading(true);

    try {
      // 서버가 활성 상태와 대상 접근 권한을 검증한 목록을 조회함
      const pageData = (await getLikeUserPageApi(tagtType, tagtNumb, targetPage)).data;
      // 누락된 페이지 응답은 불완전한 성공으로 처리함
      if (!pageData) throw new Error("LIKE_USER_PAGE_EMPTY");
      // 첫 페이지는 목록을 교체하고 이후 페이지는 기존 목록 뒤에 연결함
      setUsers((current) => targetPage === 1 ? pageData.list : [...current, ...pageData.list]);
      setPage(pageData.page);
      setHasNext(pageData.hasNext);
    } catch (error) {
      // 목록 조회 실패를 내부 정보 없이 공통 재시도 문구로 안내함
      await sweetError(
        /* "조회에 실패했습니다." */ message("frontend.alert.loadFailedTitle"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      );
    } finally {
      // 성공과 실패 모두 현재 페이지의 로딩 상태를 종료함
      if (targetPage === 1) setIsLoading(false);
      else setIsNextLoading(false);
    }
  };

  /** 좋아요 사용자 목록 팝업을 열고 첫 페이지를 조회함 */
  const openList = (): void => {
    setUsers([]);
    setPage(0);
    setHasNext(false);
    setIsListScrolling(false);
    setIsOpen(true);
    void loadPage(1);
  };

  /**
   * 좋아요 사용자 목록 팝업과 스크롤 표시 상태를 닫음
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const closeList = (): void => {
    // 팝업을 닫을 때 일시적인 스크롤 표시 상태도 초기화함
    setIsListScrolling(false);
    setIsOpen(false);
  };

  /**
   * 좋아요 사용자 목록을 스크롤하는 동안 팔로우 목록과 같은 스크롤 막대를 표시함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleListScroll = (): void => {
    // 사용자가 목록을 이동 중인 상태를 표시함
    setIsListScrolling(true);

    // 연속 스크롤 중에는 마지막 입력을 기준으로 숨김 시간을 다시 계산함
    if (scrollTimeoutRef.current !== null) {
      window.clearTimeout(scrollTimeoutRef.current);
    }

    // 팔로우 목록과 같은 시간 동안 스크롤 막대를 유지함
    scrollTimeoutRef.current = window.setTimeout(() => {
      // 스크롤이 멈춘 뒤 스크롤 막대를 다시 숨김
      setIsListScrolling(false);
      scrollTimeoutRef.current = null;
    }, 650);
  };

  /**
   * 좋아요 사용자의 본인 또는 공개 프로필 화면으로 이동함
   *
   * @author HanWon.Jang
   * @param user 이동할 좋아요 사용자
   * @return 반환값이 없음
   */
  const handleProfileClick = (user: FollowUser): void => {
    // 프로필 이동 전에 현재 좋아요 사용자 목록을 닫음
    closeList();
    // 본인 여부에 맞는 프로필 경로로 이동함
    navigate(user.meYsno === "Y" ? "/mypage/profile" : `/social/profile/${user.userNumb}`);
  };

  /**
   * 좋아요 사용자의 현재 관계에 맞춰 팔로우 또는 언팔로우하고 버튼 상태를 갱신함
   *
   * @author HanWon.Jang
   * @param user 관계를 변경할 좋아요 사용자
   * @return 팔로우 관계 변경 완료 Promise
   * @throws 팔로우 또는 언팔로우 요청 실패 시 공통 오류 안내를 표시함
   */
  const handleStatusClick = async (user: FollowUser): Promise<void> => {
    // 다른 관계 변경이 진행 중이거나 본인 행이면 추가 조작을 허용하지 않음
    if (updatingUserNumb !== null || user.meYsno === "Y") {
      // 현재 목록의 관계 상태를 유지함
      return;
    }

    // 팔로잉과 친구 상태는 언팔로우 대상으로 판정함
    const isFollowing = isFollowedByMe(user.followStatName);

    // 기존 팔로우 관계를 삭제하기 전에 팔로워 및 팔로잉 목록과 같은 확인을 받음
    if (isFollowing) {
      // "언팔로우하시겠어요?"
      const result = await sweetConfirm({
        title: message("frontend.social.unfollow.title"),
        // "팔로잉 목록에서 삭제돼요."
        text: message("frontend.social.unfollow.text"),
        // "언팔로우"
        confirmButtonText: message("frontend.social.unfollow.confirm"),
        // "취소"
        cancelButtonText: message("frontend.common.cancel"),
      });

      // 사용자가 취소한 경우 기존 관계를 유지함
      if (!result.isConfirmed) {
        // 팔로우 관계 변경 없이 종료함
        return;
      }
    }

    // 중복 관계 변경을 막기 위해 현재 사용자를 처리 중으로 설정함
    setUpdatingUserNumb(user.userNumb);

    // 서버가 반환한 최신 관계 상태만 현재 좋아요 사용자 행에 반영함
    try {
      // 현재 관계에 맞춰 팔로우 등록 또는 삭제 API를 호출함
      const response = isFollowing
        ? await delSocialFollowApi(user.userNumb)
        : await setSocialFollowApi(user.userNumb);

      // 변경한 사용자의 친구 및 맞팔로우 버튼명을 서버 결과로 갱신함
      setUsers((currentUsers) => currentUsers.map((currentUser) => (
        currentUser.userNumb === user.userNumb
          ? { ...currentUser, followStatName: response.data?.followStatName ?? currentUser.followStatName }
          : currentUser
      )));
    }

    // 관계 변경 실패 시 목록을 유지하고 공통 오류 문구로 안내함
    catch (error) {
      // "수정에 실패했습니다."
      await sweetError(
        message("frontend.alert.updateFailedTitle"),
        getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
      );
    }

    // 성공과 실패 모두 관계 변경 진행 상태를 해제함
    finally {
      setUpdatingUserNumb(null);
    }
  };

  useEffect(() => {
    if (!isOpen) return undefined;
    const focusFrame = window.requestAnimationFrame(() => closeButtonRef.current?.focus());
    const handleKeyDown = (event: KeyboardEvent): void => {
      // Escape 입력으로 현재 팝업을 닫음
      if (event.key === "Escape") closeList();
    };
    window.addEventListener("keydown", handleKeyDown);
    // 팝업이 닫히면 이벤트를 정리하고 숫자 버튼으로 키보드 초점을 복원함
    return () => {
      window.cancelAnimationFrame(focusFrame);
      window.removeEventListener("keydown", handleKeyDown);
      // 팝업이 닫히거나 컴포넌트가 해제되면 스크롤 표시 타이머를 정리함
      if (scrollTimeoutRef.current !== null) window.clearTimeout(scrollTimeoutRef.current);
      triggerButtonRef.current?.focus();
    };
  }, [isOpen]);

  // 숫자 버튼과 열린 경우의 공통 사용자 목록 팝업을 반환함
  return (
    <>
      <button
        ref={triggerButtonRef}
        className={`${styles.countButton}${className ? ` ${className}` : ""}`}
        type="button"
        aria-label={/* "좋아요 누른 사람 목록 보기" */ message("frontend.like.users.open")}
        onClick={openList}
      >
        {countLabel}
      </button>
      {isOpen ? createPortal((
        /* 좋아요 사용자 목록 팝업 전체 영역 */
        <div
          className={styles.overlay}
          data-image-viewer-overlay="true"
          role="presentation"
          onMouseDown={(event) => {
            // 배경을 직접 누른 경우에만 목록 팝업을 닫음
            if (event.currentTarget === event.target) closeList();
          }}
        >
          {/* 좋아요 사용자 목록 팝업 본문 영역 */}
          <section className={styles.modal} role="dialog" aria-modal="true" aria-labelledby="like-user-list-title">
            {/* 좋아요 사용자 목록 제목과 닫기 영역 */}
            <header className={styles.header}>
              <h2 className={styles.title} id="like-user-list-title">
                {/* "좋아요 누른 사람" */ message("frontend.like.users.title")}
              </h2>
              <button
                ref={closeButtonRef}
                className={modalControlStyles.roundClose}
                type="button"
                aria-label={/* "닫기" */ message("frontend.common.close")}
                onClick={closeList}
              >
                ×
              </button>
            </header>
            {/* 활성 좋아요 사용자 정보와 추가 조회 영역 */}
            <div
              className={isListScrolling ? styles.listScrolling : styles.list}
              onScroll={handleListScroll}
            >
              {isLoading ? (
                <Loading
                  title={/* "목록 조회 중" */ message("frontend.common.loadingList")}
                  isFullScreen={false}
                  isCompact
                />
              ) : null}
              {!isLoading && users.length === 0 ? (
                <p className={styles.empty}>
                  {/* "아직 좋아요를 누른 사람이 없습니다." */ message("frontend.like.users.empty")}
                </p>
              ) : null}
              {!isLoading ? users.map((user) => (
                /* 좋아요 사용자 개별 항목 영역 */
                <div className={styles.item} key={user.userNumb}>
                  {/* 좋아요 사용자 프로필 정보 영역 */}
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
                    <span className={styles.text}>
                      <strong className={styles.name}>{user.userNick || "-"}</strong>
                      <span className={styles.intro}>
                        {user.intrCntn
                          || /* "한줄 소개를 등록해보세요." */ message("frontend.profile.intro.empty")}
                      </span>
                    </span>
                  </button>
                  {/* 친구 및 맞팔로우 관계 확인과 변경 영역 */}
                  {user.meYsno !== "Y" ? (
                    <button
                      className={styles.statusButton}
                      data-follow-status={user.followStatName}
                      type="button"
                      disabled={updatingUserNumb === user.userNumb}
                      onClick={() => void handleStatusClick(user)}
                    >
                      {user.followStatName}
                    </button>
                  ) : null}
                </div>
              )) : null}
              <InfiniteScrollTrigger
                hasNext={!isLoading && hasNext}
                isLoading={isNextLoading}
                onLoadMore={() => {
                  // 목록 하단에 도달하면 다음 활성 좋아요 사용자 페이지를 조회함
                  if (!isNextLoading) void loadPage(page + 1);
                }}
              />
            </div>
          </section>
        </div>
      ), document.body) : null}
    </>
  );
};

export default LikeUserListButton;
