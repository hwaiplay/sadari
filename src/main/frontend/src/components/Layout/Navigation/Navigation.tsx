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
            <img
              className={styles.navIcon}
              src={"/img/common/icon-home.svg"}
              alt={message("frontend.common.homeIconAlt")}
            />
          </LinkButton>
          <LinkButton link="/set" className={styles.navLink}>
            <img
              className={styles.navIcon}
              src={"/img/common/icon-add.svg"}
              alt={message("frontend.common.addIconAlt")}
            />
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
