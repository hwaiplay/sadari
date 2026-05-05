import { Link } from "react-router-dom";
import { navContainer, navigation, navMenu, whiteBg } from "./Navigation.css";
import { Container } from "../Container/Container";
import clsx from "clsx";
import LinkButton from "@/components/LinkButton/LinkButton";

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
        <LinkButton link="/home" className={navMenu}>
          <img src={"/img/common/icon-home.svg"} alt="홈 아이콘" />
        </LinkButton>
        <LinkButton link="/add" className={navMenu}>
          <img src={"/img/common/icon-add.svg"} alt="기록하기 아이콘" />
        </LinkButton>
        <LinkButton link="/mypage" className={navMenu}>
          <img src={"/img/common/icon-user.svg"} alt="마이페이지" />
        </LinkButton>
      </nav>
    </Container>
  );
}

export default Navigation;
