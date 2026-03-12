import React, { useState } from "react";
import { Link } from "react-router-dom";
import { header } from "./Header.css";

function Header() {
  const [menuOpen, setMenuOpen] = useState<boolean>(false);
  return (
    <header className={header}>
      <Link to="/">
        <img src={"/img/common/logo-b.svg"} alt="사다리 로고" />
      </Link>
    </header>
  );
}

export default Header;
