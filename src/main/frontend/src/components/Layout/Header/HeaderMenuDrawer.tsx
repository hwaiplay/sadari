import { message } from "@/app/messages/message";
import { FIREBASE_PUSH_ENABLED_EVENT } from "@/app/pwa/pushEvents";
import { getUnreadAlimCntApi } from "@/features/Alim/api/alimApi";
import {
  isUnreadAlimChangeEvent,
  UNREAD_ALIM_CNT_CHANGED_EVENT,
} from "@/features/Alim/lib/alimEvents";
import { runLogout, selectLogoutScope } from "@/features/Auth/lib/logoutFlow";
import { getPushConfigApi } from "@/features/Push/api/pushApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useMyProfileQuery } from "@/features/User/hooks/useMyProfileQuery";
import { clsx } from "clsx";
import { useCallback, useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import {
  headerAlimBadge,
  headerAlimButton,
  hamburgerButton,
} from "./Header.css";
import * as drawerStyles from "../Navigation/Navigation.css";
import type { UserMenuItem } from "@/features/Menu/api/userMenuApi";

type HeaderMenuDrawerProps = {
  menuList?: UserMenuItem[];
};

type DrawerMenuTreeItemProps = {
  menu: UserMenuItem;
  expandedMenuNumbs: number[];
  onToggle: (menuNumb: number) => void;
  onMove: (menuUrlx: string) => void;
};

/**
 * 최대 3단계 사용자 메뉴 항목과 하위 아코디언을 재귀적으로 표시함
 *
 * @author SeungHyeon.Kang
 * @param menu 표시할 사용자 메뉴
 * @param expandedMenuNumbs 펼쳐진 사용자 메뉴 번호 목록
 * @param onToggle 하위 메뉴 펼침 상태 변경 함수
 * @param onMove 사용자 메뉴 경로 이동 함수
 * @return 사용자 메뉴 트리 항목
 */
function DrawerMenuTreeItem({
  menu,
  expandedMenuNumbs,
  onToggle,
  onMove,
}: DrawerMenuTreeItemProps) {
  const childList = menu.childList ?? [];
  const hasChildMenu = childList.length > 0;
  const isExpanded = expandedMenuNumbs.includes(menu.menuNumb);
  const childMenuId = `drawer-child-menu-${menu.menuNumb}`;
  const menuButtonClass = menu.menuLevl === 1
    ? drawerStyles.drawerMenuButton
    : menu.menuLevl === 2
      ? drawerStyles.drawerSecondaryMenuButton
      : drawerStyles.drawerTertiaryMenuButton;

  // 사용자 메뉴 한 항목과 해당 항목의 하위 메뉴 아코디언을 반환함
  return (
    <div className={drawerStyles.drawerMenuGroup}>
      {/* 사용자 메뉴 이동 또는 하위 메뉴 펼침 버튼 영역 */}
      <button
        className={clsx(
          menuButtonClass,
          menu.menuLevl === 1 && isExpanded && drawerStyles.drawerMenuButtonOpen,
          !hasChildMenu && !menu.menuUrlx && drawerStyles.drawerMenuDisabled,
        )}
        type="button"
        disabled={!hasChildMenu && !menu.menuUrlx}
        aria-expanded={hasChildMenu ? isExpanded : undefined}
        aria-controls={hasChildMenu ? childMenuId : undefined}
        onClick={() => {
          // 하위 메뉴가 있으면 현재 메뉴의 아코디언 상태를 변경함
          if (hasChildMenu) {
            onToggle(menu.menuNumb);
            return;
          }

          // 이동 경로가 있는 최하위 메뉴만 해당 사용자 화면으로 이동함
          if (menu.menuUrlx) {
            onMove(menu.menuUrlx);
          }
        }}
      >
        <span>{menu.menuName}</span>
        {hasChildMenu ? (
          <svg
            className={clsx(
              drawerStyles.drawerMenuChevron,
              isExpanded && drawerStyles.drawerMenuChevronOpen,
            )}
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="m9 18 6-6-6-6" />
          </svg>
        ) : null}
      </button>

      {hasChildMenu ? (
        /* 하위 사용자 메뉴 아코디언 영역 */
        <div
          id={childMenuId}
          className={clsx(
            drawerStyles.drawerSecondaryMenuWrap,
            isExpanded && drawerStyles.drawerSecondaryMenuWrapOpen,
          )}
        >
          {/* 하위 사용자 메뉴 목록 영역 */}
          <div className={drawerStyles.drawerSecondaryMenuInner}>
            {childList.map((childMenu) => (
              /* 하위 사용자 메뉴 개별 항목 영역 */
              <DrawerMenuTreeItem
                key={childMenu.menuNumb}
                menu={childMenu}
                expandedMenuNumbs={expandedMenuNumbs}
                onToggle={onToggle}
                onMove={onMove}
              />
            ))}
          </div>
        </div>
      ) : null}
    </div>
  );
}

/**
 * 사용자 프로필과 DB에서 조회한 노출 메뉴를 햄버거 드로어에 표시
 *
 * @author HanWon.Jang
 * @param menuList SHOW_YSNO와 USEE_YSNO가 모두 Y인 사용자 메뉴 목록
 * @return 헤더 알림·햄버거 버튼과 메뉴 드로어
 */
function HeaderMenuDrawer({ menuList = [] }: HeaderMenuDrawerProps) {

  // 헤더와 내비게이션 및 프로필 화면이 공유하는 로그인 사용자 프로필을 조회함
  const myProfileQuery = useMyProfileQuery();
  const profile = myProfileQuery.data ?? null;
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [expandedMenuNumbs, setExpandedMenuNumbs] = useState<number[]>([]);
  const [unreadAlimCnt, setUnreadAlimCnt] = useState(0);
  const navigate = useNavigate();
  // "사용자"
  const profileName = profile?.userNick || message("frontend.common.user");
  const profileIntro =
    profile?.intrCntn || message("frontend.profile.intro.empty");
  const portalTarget = typeof document === "undefined" ? null : document.body;

  /**
   * 사용자 메뉴 아코디언 펼침 상태를 변경함
   *
   * @author SeungHyeon.Kang
   * @param menuNumb 펼침 상태를 변경할 메뉴 번호
   * @return 반환값이 없음
   */
  const handleMenuToggle = (menuNumb: number): void => {
    // 선택한 메뉴 번호의 기존 포함 여부에 따라 펼침 목록에서 추가하거나 제거함
    setExpandedMenuNumbs((currentMenuNumbs) => (
      currentMenuNumbs.includes(menuNumb)
        ? currentMenuNumbs.filter((currentMenuNumb) => currentMenuNumb !== menuNumb)
        : [...currentMenuNumbs, menuNumb]
    ));
  };

  /**
   * 햄버거 메뉴를 닫고 선택한 사용자 화면으로 이동함
   *
   * @author SeungHyeon.Kang
   * @param menuUrlx 이동할 사용자 화면 경로
   * @return 반환값이 없음
   */
  const handleMenuMove = (menuUrlx: string): void => {
    // 메뉴 선택 뒤 배경 클릭 영역이 남지 않도록 햄버거 메뉴를 닫음
    setIsDrawerOpen(false);
    // 선택한 사용자 메뉴 경로로 이동함
    navigate(menuUrlx);
  };

  /**
   * 열린 햄버거 메뉴를 닫음
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없음
   */
  const handleDrawerClose = (): void => {
    // 명시적인 닫기 동작과 배경 클릭이 같은 메뉴 상태를 갱신하게 함
    setIsDrawerOpen(false);
  };

  const refreshUnreadAlimCnt = useCallback(async () => {

    try {
      const response = await getUnreadAlimCntApi();
      setUnreadAlimCnt(response.data?.unreadCnt ?? 0);
    } catch {
      // 알림 배지는 보조 정보이므로 조회 실패 시 기존 숫자를 유지함
    }
  }, []);

  /**
   * handle Logout 사용자 동작을 처리함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   * @throws API 요청 또는 비동기 처리 실패 시 발생
   */
  const handleLogout = async () => {

    // Alert에서 현재 기기 또는 전체 기기 로그아웃 범위를 선택함
    const logoutScope = await selectLogoutScope();

    // 취소하면 현재 인증 상태와 메뉴를 유지함
    if (!logoutScope) {
      return;
    }

    try {
      // 선택한 범위의 서버 세션과 브라우저 푸시 구독을 정리함
      await runLogout(logoutScope);
    } finally {
      setIsDrawerOpen(false);
      navigate("/login", { replace: true });
    }
  };

  useEffect(() => {

    void refreshUnreadAlimCnt();

    /**
     * handle Service Worker Message 사용자 동작을 처리함
     *
     * @author HanWon.Jang
     * @param event event 입력값
     * @return 반환값이 없음
     */
    const handleSwMessage = (event: MessageEvent) => {
      // 푸시 수신뿐 아니라 시스템 알림 클릭으로 읽음 상태가 바뀐 경우에도 배지 수를 다시 조회함
      if (
        event.data?.type === "SADARI_ALIM_RECEIVED"
        || event.data?.type === "SADARI_ALIM_READ"
      ) {
        void refreshUnreadAlimCnt();
      }
    };

    /**
     * handle Window Focus 사용자 동작을 처리함
     *
     * @author HanWon.Jang
     * @return 반환값이 없음
     */
    const handleWindowFocus = () => {

      void refreshUnreadAlimCnt();
    };

    /**
     * handle Unread Alim Cnt Changed 사용자 동작을 처리함
     *
     * @author HanWon.Jang
     * @param event event 입력값
     * @return 반환값이 없음
     */
    const handleUnreadAlimChange = (event: Event) => {

      if (isUnreadAlimChangeEvent(event)) {
        setUnreadAlimCnt(event.detail);
      }
    };

    navigator.serviceWorker?.addEventListener("message", handleSwMessage);
    window.addEventListener("focus", handleWindowFocus);
    window.addEventListener(UNREAD_ALIM_CNT_CHANGED_EVENT, handleUnreadAlimChange);

    return () => {

      navigator.serviceWorker?.removeEventListener("message", handleSwMessage);
      window.removeEventListener("focus", handleWindowFocus);
      window.removeEventListener(UNREAD_ALIM_CNT_CHANGED_EVENT, handleUnreadAlimChange);
    };
  }, [refreshUnreadAlimCnt]);

  useEffect(() => {

    let unsubscribe: (() => void) | undefined;
    let ignore = false;

    /**
     * initialize Foreground Messages 기능을 처리함
     *
     * @author HanWon.Jang
     * @return 처리 결과
     * @throws API 요청 또는 비동기 처리 실패 시 발생
     */
    const initForegroundMessages = async () => {

      if (!("Notification" in window) || Notification.permission !== "granted") {
        return;
      }

      try {
        const response = await getPushConfigApi();
        // 알림 권한이 허용된 사용자에게만 Firebase 메시징 모듈을 지연 로드함
        const { subscribeFirebaseMessages } = await import("@/app/pwa/firebaseMessaging");
        // 지연 로드된 SDK로 포그라운드 메시지 수신을 시작함
        const unsubscribeForegroundMessages = await subscribeFirebaseMessages(
            response.data,
            () => void refreshUnreadAlimCnt(),
          );

        if (ignore) {
          unsubscribeForegroundMessages();
          return;
        }

        unsubscribe?.();
        unsubscribe = unsubscribeForegroundMessages;
      } catch {
        // 포그라운드 리스너 초기화 실패는 기본 화면 사용을 막지 않음
      }
    };

    /**
     * handle Push Enabled 사용자 동작을 처리함
     *
     * @author HanWon.Jang
     * @return 반환값이 없음
     */
    const handlePushEnabled = () => {

      void initForegroundMessages();
    };

    void initForegroundMessages();
    window.addEventListener(FIREBASE_PUSH_ENABLED_EVENT, handlePushEnabled);

    return () => {

      ignore = true;
      unsubscribe?.();
      window.removeEventListener(FIREBASE_PUSH_ENABLED_EVENT, handlePushEnabled);
    };
  }, [refreshUnreadAlimCnt]);

  const drawer = (
    <div
      className={clsx(
        drawerStyles.drawerOverlay,
        isDrawerOpen && drawerStyles.drawerOverlayVisible,
      )}
      aria-hidden={!isDrawerOpen}
    >
      <button
        className={clsx(
          drawerStyles.drawerBackdrop,
          isDrawerOpen && drawerStyles.drawerBackdropVisible,
        )}
        type="button"
        aria-label={/* "닫기" */ message("frontend.common.close")}
        onClick={handleDrawerClose}
      />
      <aside
        className={clsx(drawerStyles.drawer, isDrawerOpen && drawerStyles.drawerOpen)}
        aria-label={message("frontend.header.myPageMenu")}
      >
        {/* 햄버거 메뉴 닫기 버튼 영역 */}
        <button
          className={drawerStyles.drawerCloseButton}
          type="button"
          aria-label={/* "닫기" */ message("frontend.common.close")}
          title={/* "닫기" */ message("frontend.common.close")}
          onClick={handleDrawerClose}
        >
          <svg
            className={drawerStyles.drawerCloseIcon}
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="M6 6l12 12M18 6 6 18" />
          </svg>
        </button>

        {/* 사용자 프로필과 메뉴 닫기 영역 */}
        <section className={drawerStyles.drawerHeader}>
          <button
            className={drawerStyles.drawerProfileSummaryButton}
            type="button"
            onClick={() => {

              setIsDrawerOpen(false);
              navigate("/mypage/profile");
            }}
          >
            <ProfileImage
              className={drawerStyles.drawerProfileImage}
              src={profile?.porfPath}
              alt=""
            />
            <div className={drawerStyles.drawerProfileMeta}>
              <strong className={drawerStyles.drawerProfileName}>{profileName}</strong>
              <span className={drawerStyles.drawerProfileSub}>{profileIntro}</span>
            </div>
          </button>
          <div className={drawerStyles.drawerActionGroup}>
            <button
              className={drawerStyles.drawerSettingButton}
              type="button"
              aria-label={message("frontend.common.settings")}
              title={message("frontend.common.settings")}
              onClick={() => {
                // 햄버거 메뉴를 먼저 닫은 뒤 설정 전용 화면으로 이동해 배경 클릭 영역이 남지 않게 함
                setIsDrawerOpen(false);
                navigate("/settings");
              }}
            >
              <svg
                className={drawerStyles.drawerSettingIcon}
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path d="M12 15.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z" />
                <path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-4v-.08A1.7 1.7 0 0 0 8.96 19.4a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.6 15a1.7 1.7 0 0 0-1.56-1.03H3v-4h.08A1.7 1.7 0 0 0 4.6 8.96a1.7 1.7 0 0 0-.34-1.88l-.06-.06 2.83-2.83.06.06A1.7 1.7 0 0 0 8.96 4.6 1.7 1.7 0 0 0 10 3.08V3h4v.08a1.7 1.7 0 0 0 1.03 1.56 1.7 1.7 0 0 0 1.88-.34l.06-.06 2.83 2.83-.06.06a1.7 1.7 0 0 0-.34 1.88A1.7 1.7 0 0 0 20.92 10H21v4h-.08A1.7 1.7 0 0 0 19.4 15Z" />
              </svg>
            </button>
            <button
              className={drawerStyles.drawerLogoutButton}
              type="button"
              onClick={handleLogout}
            >
              {/* "로그아웃" */}
              {message("frontend.auth.logout")}
            </button>
          </div>
        </section>
        {/* 최대 3단계 사용자 메뉴 트리 영역 */}
        <div className={drawerStyles.drawerMenu}>
          {menuList.map((menu) => (
            /* 최상위 사용자 메뉴 개별 항목 영역 */
            <DrawerMenuTreeItem
              key={menu.menuNumb}
              menu={menu}
              expandedMenuNumbs={expandedMenuNumbs}
              onToggle={handleMenuToggle}
              onMove={handleMenuMove}
            />
          ))}
        </div>
      </aside>
    </div>
  );

  return (
    <>
      <button
        className={headerAlimButton}
        type="button"
        aria-label={message("frontend.alim.title")}
        onClick={() => navigate("/alim")}
      >
        <img src={"/img/icons/icon-notification.svg"} alt={message("frontend.alim.title")}/>
        {/*<svg className={headerAlimIcon} viewBox="0 0 24 24" aria-hidden="true">*/}
        {/*  <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />*/}
        {/*  <path d="M13.73 21a2 2 0 0 1-3.46 0" />*/}
        {/*</svg>*/}
        {unreadAlimCnt > 0 ? (
          <span className={headerAlimBadge}>
            {/*{unreadAlimCnt > 99 ? "99+" : unreadAlimCnt}*/}
          </span>
        ) : null}
      </button>
      <button
        className={hamburgerButton}
        type="button"
        aria-label={message("frontend.header.openMenu")}
        aria-expanded={isDrawerOpen}
        onClick={() => setIsDrawerOpen(true)}
      >
        {/*<svg className={hamburgerIcon} viewBox="0 0 24 24" aria-hidden="true">*/}
        {/*  <path d="M4 7h16M4 12h16M4 17h16" />*/}
        {/*</svg>*/}
        <img src={"/img/icons/icon-hamburger.svg"} alt={message("frontend.header.openMenu")}/>

      </button>
      {portalTarget ? createPortal(drawer, portalTarget) : null}
    </>
  );
}

export default HeaderMenuDrawer;
