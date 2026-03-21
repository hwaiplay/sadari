import React from "react";
import { Link } from "react-router-dom";
import { header, logo } from "./Header.css";
import { Container } from "../Container/Container";

function Header() {
  return (
    <header>
      <Container className={header}>
        <Link to="/" className={logo}>
          <img src={"/img/common/logo-b.svg"} alt="사다리 로고" width={100} />
        </Link>
      </Container>
    </header>
  );
}

export default Header;
