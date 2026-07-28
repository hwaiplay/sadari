import { useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./SettingsPage.css";

const USER_MANAGEMENT_ITEMS = ["팔로우 관리", "팔로워 관리", "차단 관리"];

/**
 * 알림과 사용자 관련 기능을 계층형 메뉴로 제공하는 설정 메인 화면입니다.
 * 사용자 관리만 하위 메뉴를 가지며 펼침 상태는 화면 내부에서 관리합니다.
 *
 * @author HanWon.Jang
 * @return 설정 카테고리 및 하위 메뉴 화면
 */
function SettingsPage() {

  const navigate = useNavigate();
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  return (
    /* 사용자 설정 메뉴 전체 영역 */
    <main className={styles.page}>
      {/* 알림과 사용자 관리 설정 영역 */}
      <section className={styles.menu} aria-label="설정 메뉴">
        <button
          className={styles.primaryMenuButton}
          type="button"
          onClick={() => navigate("/alim")}
        >
          <span>알림 설정</span>
          <svg className={styles.chevronIcon} viewBox="0 0 24 24" aria-hidden="true">
            <path d="m9 18 6-6-6-6" />
          </svg>
        </button>

        <button
          className={`${styles.primaryMenuButton} ${
            isUserMenuOpen ? styles.primaryMenuButtonOpen : ""
          }`}
          type="button"
          aria-expanded={isUserMenuOpen}
          aria-controls="settings-user-management"
          onClick={() => setIsUserMenuOpen((isOpen) => !isOpen)}
        >
          <span>사용자 관리</span>
          <svg
            className={`${styles.chevronIcon} ${
              isUserMenuOpen ? styles.chevronIconOpen : ""
            }`}
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path d="m9 18 6-6-6-6" />
          </svg>
        </button>

        <div
          id="settings-user-management"
          className={`${styles.secondaryMenuWrap} ${
            isUserMenuOpen ? styles.secondaryMenuWrapOpen : ""
          }`}
        >
          <div className={styles.secondaryMenuInner}>
            {USER_MANAGEMENT_ITEMS.map((item) => (
              <button className={styles.secondaryMenuButton} type="button" key={item}>
                {item}
              </button>
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}

export default SettingsPage;
