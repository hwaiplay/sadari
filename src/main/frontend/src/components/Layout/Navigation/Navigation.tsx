import React from "react";
import { Button } from "../../Button/Button";
import { useNavigate } from "react-router-dom";
import { navButton, navigation } from "./Navigation.css";
import clsx from "clsx";
import { container } from "../Container/container.css";
function Navigation() {
  const navigate = useNavigate();
  //홈 버튼 네비게이트
  const HomeOnClick = () => {
    navigate("/");
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
    <nav className={clsx(navigation, container)}>
      <Button onClick={HomeOnClick} variant="secondary" className={navButton}>
        홈
      </Button>
      <Button onClick={AddOnClick} className={navButton}>
        기록하기
      </Button>
      <Button onClick={MypageOnClick} variant="secondary" className={navButton}>
        마이페이지
      </Button>
    </nav>
  );
}

export default Navigation;
