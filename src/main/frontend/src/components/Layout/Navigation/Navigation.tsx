import { message } from "@/app/messages/message";
import api from "@/app/api/axios";
import { Container } from "../Container/Container";
import LinkButton from "@/components/Button/LinkButton/LinkButton";
import { clsx } from "clsx";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./Navigation.css";

type NavigationProps = {
  isMain: boolean;
};

type MyProfile = {
  userNick?: string;
  porfPath?: string;
};

const MENU_ITEMS = [
  { label: "독후감 달력", disabled: false },
  { label: "준비 중", disabled: true },
  { label: "준비 중", disabled: true },
  { label: "준비 중", disabled: true },
];

/**
 * 하단 주요 이동 버튼과 마이페이지 drawer 메뉴를 렌더링한다.
 * @Author Hanwon.Jang
 * @param isMain 메인 레이아웃에서 사용하는 배경 스타일 여부
 * @return 하단 네비게이션 컴포넌트
 */
function Navigation({ isMain }: NavigationProps) {
  const [profile, setProfile] = useState<MyProfile | null>(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const navigate = useNavigate();
  const profileImage = profile?.porfPath || "/img/common/icon-user.svg";
  const profileName = profile?.userNick || "사용자";

  useEffect(() => {
    let ignore = false;

    api
      .get("/user/me")
      .then((response) => {
        if (!ignore && response.data?.code === 200) {
          setProfile(response.data.data);
        }
      })
      .catch(() => {
        if (!ignore) {
          setProfile(null);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <>
      <Container className={clsx(styles.navContainer, isMain && styles.whiteBg)}>
        <nav className={styles.navigation}>
          <LinkButton link="/home" className={styles.navLink}>
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M5.33331 25.3333V13.3333C5.33331 12.9111 5.42798 12.5111 5.61731 12.1333C5.80665 11.7555 6.06754 11.4444 6.39998 11.2L14.4 5.19996C14.8666 4.8444 15.4 4.66663 16 4.66663C16.6 4.66663 17.1333 4.8444 17.6 5.19996L25.6 11.2C25.9333 11.4444 26.1946 11.7555 26.384 12.1333C26.5733 12.5111 26.6675 12.9111 26.6666 13.3333V25.3333C26.6666 26.0666 26.4053 26.6946 25.8826 27.2173C25.36 27.74 24.7324 28.0008 24 28H20C19.6222 28 19.3058 27.872 19.0506 27.616C18.7955 27.36 18.6675 27.0435 18.6666 26.6666V20C18.6666 19.6222 18.5386 19.3057 18.2826 19.0506C18.0266 18.7955 17.7102 18.6675 17.3333 18.6666H14.6666C14.2889 18.6666 13.9724 18.7946 13.7173 19.0506C13.4622 19.3066 13.3342 19.6231 13.3333 20V26.6666C13.3333 27.0444 13.2053 27.3613 12.9493 27.6173C12.6933 27.8733 12.3769 28.0008 12 28H7.99998C7.26665 28 6.63909 27.7391 6.11731 27.2173C5.59553 26.6955 5.3342 26.0675 5.33331 25.3333Z" fill="#333333"/>
            </svg>

          </LinkButton>
          <LinkButton link="/set" className={clsx(styles.navLink, styles.navLink__set)}>
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M36 25.996H26V35.996C26 36.5264 25.7893 37.0351 25.4142 37.4102C25.0391 37.7853 24.5304 37.996 24 37.996C23.4696 37.996 22.9609 37.7853 22.5858 37.4102C22.2107 37.0351 22 36.5264 22 35.996V25.996H12C11.4696 25.996 10.9609 25.7853 10.5858 25.4102C10.2107 25.0351 10 24.5264 10 23.996C10 23.4655 10.2107 22.9568 10.5858 22.5818C10.9609 22.2067 11.4696 21.996 12 21.996H22V11.996C22 11.4655 22.2107 10.9568 22.5858 10.5818C22.9609 10.2067 23.4696 9.99597 24 9.99597C24.5304 9.99597 25.0391 10.2067 25.4142 10.5818C25.7893 10.9568 26 11.4655 26 11.996V21.996H36C36.5304 21.996 37.0391 22.2067 37.4142 22.5818C37.7893 22.9568 38 23.4655 38 23.996C38 24.5264 37.7893 25.0351 37.4142 25.4102C37.0391 25.7853 36.5304 25.996 36 25.996Z" fill="#333333"/>
            </svg>
          </LinkButton>
          <button
            className={styles.navProfileButton}
            type="button"
            aria-label={message("frontend.common.myPageIconAlt")}
            aria-expanded={isDrawerOpen}
            onClick={() => setIsDrawerOpen(true)}
          >
            <img
              className={styles.navProfileImage}
              src={profileImage}
              alt={message("frontend.common.myPageIconAlt")}
            />
          </button>
        </nav>
      </Container>

      <div
        className={clsx(
          styles.drawerOverlay,
          isDrawerOpen && styles.drawerOverlayVisible,
        )}
        aria-hidden={!isDrawerOpen}
      >
        <button
          className={clsx(
            styles.drawerBackdrop,
            isDrawerOpen && styles.drawerBackdropVisible,
          )}
          type="button"
          aria-label={message("frontend.common.close")}
          onClick={() => setIsDrawerOpen(false)}
        />
        <aside
          className={clsx(styles.drawer, isDrawerOpen && styles.drawerOpen)}
          aria-label="마이페이지 메뉴"
        >
          <section className={styles.drawerHeader}>
            <img className={styles.drawerProfileImage} src={profileImage} alt="" />
            <div className={styles.drawerProfileMeta}>
              <strong className={styles.drawerProfileName}>{profileName}</strong>
              <span className={styles.drawerProfileSub}>카카오 프로필</span>
            </div>
          </section>
          <div className={styles.drawerMenu}>
            {MENU_ITEMS.map((item, index) => (
              <button
                className={clsx(
                  styles.drawerMenuButton,
                  item.disabled && styles.drawerMenuDisabled,
                )}
                type="button"
                disabled={item.disabled}
                onClick={() => {
                  if (item.disabled) {
                    return;
                  }

                  setIsDrawerOpen(false);
                  navigate("/mypage/reading-calendar");
                }}
                key={`${item.label}-${index}`}
              >
                {item.label}
              </button>
            ))}
          </div>
        </aside>
      </div>
    </>
  );
}

export default Navigation;
