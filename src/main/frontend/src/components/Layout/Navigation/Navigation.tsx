import { message } from "@/app/messages/message";
import { navContainer, navigation, navIcon, navLink, whiteBg } from "./Navigation.css";
import { Container } from "../Container/Container";
import LinkButton from "@/components/Button/LinkButton/LinkButton";
import { clsx } from "clsx";

/**
 * fileName       : Navigation
 * author         : hanwon.Jang
 * date           : 2026-05-03
 * description    : 하단 네비게이션 바
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       hanwon.Jang       버튼 UI 변경
 */

type NavigationProps = {
  isMain: boolean;
};

function Navigation({ isMain }: NavigationProps) {
  return (
    <Container className={clsx(navContainer, isMain && whiteBg)}>
      <nav className={navigation}>
        <LinkButton link="/home" className={navLink}>
          <img
            className={navIcon}
            src={"/img/common/icon-home.svg"}
            alt={message("frontend.common.homeIconAlt")} // frontend.common.homeIconAlt = 홈 아이콘
          />
        </LinkButton>
        <LinkButton link="/set" className={navLink}>
          <img
            className={navIcon}
            src={"/img/common/icon-add.svg"}
            alt={message("frontend.common.addIconAlt")} // frontend.common.addIconAlt = 기록하기 아이콘
          />
        </LinkButton>
        <LinkButton link="/mypage" className={navLink}>
          <img
            className={navIcon}
            src={"/img/common/icon-user.svg"}
            alt={message("frontend.common.myPageIconAlt")} // frontend.common.myPageIconAlt = 마이페이지 아이콘
          />
        </LinkButton>
      </nav>
    </Container>
  );
}

export default Navigation;
