import Header from "./Header/Header";
import { Outlet } from "react-router-dom";
import Navigation from "./Navigation/Navigation";
import { vars } from "@/app/styles/tokens.css";
import { Container } from "./Container/Container";
import {useEffect, useState} from "react";

/**
 * fileName       : Layout
 * author         : hanwon.Jang
 * date           : 2026-04-26
 * description    : 레이아웃 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang        주석 추가
 */

function Layout({ isMainLayout = true }) {
   const [headerActive, setHeaderActive] = useState<boolean>(false)

    useEffect(() => {
        const handleScroll = () => {
            const isActive = window.scrollY > 5;
            setHeaderActive(isActive);
        };

        window.addEventListener("scroll", handleScroll);

        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    return (
    <div>
      <Header headerActive={headerActive}   />
      <main
        style={{
          paddingTop: isMainLayout ? vars.headerHeight : 0,
          paddingBottom: vars.headerHeight,
        }}
      >
        {isMainLayout ? (
          <Container>
            <Outlet />
          </Container>
        ) : (
          <Outlet />
        )}
      </main>
      <Navigation isMain={isMainLayout} />
    </div>
  );
}

export default Layout;
