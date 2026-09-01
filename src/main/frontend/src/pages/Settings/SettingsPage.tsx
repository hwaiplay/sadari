import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import {
  getUserMenuChildListApi,
  type UserMenuItem,
} from "@/features/Menu/api/userMenuApi";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./SettingsPage.css";

const SETTINGS_MENU_URL = "/settings";
const WITHDRAWAL_MENU_URL = "/settings/withdrawal";

/**
 * 관리자 사용자 메뉴에 등록된 설정 하위 메뉴를 계층형으로 표시합니다.
 * 2뎁스 메뉴와 그 아래의 3뎁스 메뉴는 서버 정렬 순서를 그대로 사용합니다.
 *
 * @author HanWon.Jang
 * @return 설정 카테고리 및 하위 메뉴 화면
 */
function SettingsPage() {

  const navigate = useNavigate();
  const [menuList, setMenuList] = useState<UserMenuItem[]>([]);
  const [openMenuNumbs, setOpenMenuNumbs] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {

    let ignore = false;
    setIsLoading(true);
    setHasError(false);

    getUserMenuChildListApi(SETTINGS_MENU_URL)
      .then((response) => {

        if (ignore) {
          return;
        }

        setMenuList(response.data ?? []);
      })
      .catch(() => {

        if (!ignore) {
          setMenuList([]);
          setHasError(true);
        }
      })
      .finally(() => {

        if (!ignore) {
          setIsLoading(false);
        }
      });

    return () => {

      ignore = true;
    };
  }, []);

  /**
   * 하위 메뉴가 있는 설정 메뉴의 펼침 상태를 전환합니다.
   *
   * @author HanWon.Jang
   * @param menuNumb 펼치거나 접을 메뉴 번호
   * @return 갱신된 메뉴 번호 목록
   */
  const toggleMenu = (menuNumb: number) => {

    setOpenMenuNumbs((currentMenuNumbs) =>
      currentMenuNumbs.includes(menuNumb)
        ? currentMenuNumbs.filter((currentMenuNumb) => currentMenuNumb !== menuNumb)
        : [...currentMenuNumbs, menuNumb],
    );
  };

  /**
   * 설정 메뉴의 하위 구조 또는 이동 URL에 따라 클릭 동작을 처리합니다.
   *
   * @author HanWon.Jang
   * @param menu 클릭한 설정 메뉴
   */
  const handleMenuClick = (menu: UserMenuItem) => {

    const childList = menu.childList ?? [];
    if (childList.length > 0) {
      toggleMenu(menu.menuNumb);
      return;
    }

    if (menu.menuUrlx?.trim()) {
      navigate(menu.menuUrlx);
    }
  };

  if (isLoading) {
    return <Loading />;
  }

  return (
    /* 사용자 설정 메뉴 전체 영역 */
    <main className={styles.page}>
      {/* 관리자 사용자 메뉴에서 조회한 설정 하위 메뉴 영역 */}
      <section className={styles.menu} aria-label={message("frontend.settings.menuLabel")}>
        <div className={styles.menuItem}>
          <button
            className={styles.primaryMenuButton}
            type="button"
            onClick={() => navigate("/settings/notifications")}
          >
            <span>{message("frontend.settings.notifications.title")}</span>
            <svg className={styles.chevronIcon} viewBox="0 0 24 24" aria-hidden="true">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </button>
        </div>
        <div className={styles.menuItem}>
          <button
            className={styles.primaryMenuButton}
            type="button"
            onClick={() => navigate("/settings/privacy")}
          >
            <span>{message("frontend.settings.privacy.title")}</span>
            <svg className={styles.chevronIcon} viewBox="0 0 24 24" aria-hidden="true">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </button>
        </div>
        {hasError && (
          <p className={styles.statusMessage}>{message("frontend.common.tryAgain")}</p>
        )}

        {!hasError && menuList.map((menu) => {

          const childList = menu.childList ?? [];
          const hasChildMenu = childList.length > 0;
          const isOpen = openMenuNumbs.includes(menu.menuNumb);
          const isWithdrawalMenu = menu.menuUrlx === WITHDRAWAL_MENU_URL;
          const childAreaId = `settings-menu-${menu.menuNumb}`;

          return (
            <div className={styles.menuItem} key={menu.menuNumb}>
              <button
                className={`${
                  isWithdrawalMenu
                    ? styles.withdrawMenuButton
                    : styles.primaryMenuButton
                } ${isOpen ? styles.primaryMenuButtonOpen : ""}`}
                type="button"
                aria-expanded={hasChildMenu ? isOpen : undefined}
                aria-controls={hasChildMenu ? childAreaId : undefined}
                onClick={() => handleMenuClick(menu)}
              >
                <span>{menu.menuName}</span>
                <svg
                  className={`${styles.chevronIcon} ${
                    isOpen ? styles.chevronIconOpen : ""
                  }`}
                  viewBox="0 0 24 24"
                  aria-hidden="true"
                >
                  <path d="m9 18 6-6-6-6" />
                </svg>
              </button>

              {hasChildMenu && (
                <div
                  id={childAreaId}
                  className={`${styles.secondaryMenuWrap} ${
                    isOpen ? styles.secondaryMenuWrapOpen : ""
                  }`}
                >
                  <div className={styles.secondaryMenuInner}>
                    {childList.map((childMenu) => (
                      <button
                        className={styles.secondaryMenuButton}
                        type="button"
                        key={childMenu.menuNumb}
                        onClick={() => handleMenuClick(childMenu)}
                      >
                        {childMenu.menuName}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </section>
    </main>
  );
}

export default SettingsPage;
