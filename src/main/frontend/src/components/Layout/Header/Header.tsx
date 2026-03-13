import React from "react";
import { Link } from "react-router-dom";
import { header } from "./Header.css";
import { Container } from "../Container/Container";

function Header() {
  return (
    <Container className={header}>
      <header>
        <Link to="/">
          <img src={"/img/common/logo-b.svg"} alt="사다리 로고" width={100} />
        </Link>
      </header>
    </Container>
  );
}

export default Header;
