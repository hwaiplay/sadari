import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { header, saveBtn } from "./Header.css";
import { Container } from "../Container/Container";
import clsx from "clsx";

function FormHeader() {
  const navigate = useNavigate();

  const backPrev = () => {
    navigate(-1); // 이전 페이지로 이동
  };

  return (
    <header>
      <Container className={clsx(header, "_form")}>
        <button
          type="button"
          aria-label="이전 페이지로 돌아가기"
          onClick={backPrev}
        >
          <img
            src={"/img/common/icon-backpage.svg"}
            alt="뒤로가기 화살표 아이콘"
          />
        </button>
        <Link to="/">
          <img src={"/img/common/logo-b.svg"} alt="사다리 로고" width={100} />
        </Link>
        <button type="button" aria-label="저장하기" className={saveBtn}>
          저장
        </button>
      </Container>
    </header>
  );
}

export default FormHeader;
