import { Link } from "react-router-dom";
import { Button } from "../../Button/Button";
import { useNavigate } from "react-router-dom";
import { navButton, navContainer, navigation, whiteBg } from "./Navigation.css";
import { Container } from "../Container/Container";
import clsx from "clsx";

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
  const navigate = useNavigate();

  //홈 버튼 네비게이트
  const HomeOnClick = () => {
    navigate("/home");
  };
  //기록하기 버튼 네비게이트

  const AddOnClick = () => {
    navigate("/add");
  };
  //마이페이지 버튼 네비게이트

  const MypageOnClick = () => {
    navigate("/mypage");
  };
  return (
    <Container className={clsx(navContainer, isMain && whiteBg)}>
      <nav className={navigation}>
        <Link to="/home">
          <img src={"/img/common/icon-home.svg"} alt="홈 아이콘" />
        </Link>
        {/* <Button onClick={HomeOnClick} className={navButton}>
        </Button> */}
        <Link to="/add">
          {/* <Button onClick={AddOnClick} className={navButton}> */}
          <img src={"/img/common/icon-add.svg"} alt="기록하기 아이콘" />
          {/* </Button> */}
        </Link>
        <Link to="/mypage">
          {/* <Button onClick={MypageOnClick} className={navButton}> */}
          <img src={"/img/common/icon-user.svg"} alt="마이페이지 아이콘" />
          {/* </Button> */}
        </Link>
      </nav>
    </Container>
  );
}

export default Navigation;
