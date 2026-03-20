import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { header } from "./Header.css";
import { Container } from "../Container/Container";

function FormHeader() {
  const navigate = useNavigate();

  const backPrev = () => {
    navigate(-1); // 이전 페이지로 이동
  };

  return (
    <Container className={header}>
      <header>
        <button type="button" onClick={backPrev}>
          <img
            src={"/img/common/icon-backpage.svg"}
            alt="뒤로가기 화살표 아이콘"
          />
        </button>
        <Link to="/">
          <img src={"/img/common/logo-b.svg"} alt="사다리 로고" width={100} />
        </Link>
      </header>
    </Container>
  );
}

export default FormHeader;
