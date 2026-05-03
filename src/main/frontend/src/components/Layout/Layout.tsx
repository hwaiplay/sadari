import Header from "./Header/Header";
import { Outlet } from "react-router-dom";
import Navigation from "./Navigation/Navigation";
import { vars } from "@/app/styles/tokens.css";

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
  return (
    <div>
      <Header />
      <main style={{ paddingTop: isMainLayout ? vars.headerHeight : 0 }}>
        <Outlet />
      </main>
      <Navigation isMain={isMainLayout} />
    </div>
  );
}

export default Layout;
