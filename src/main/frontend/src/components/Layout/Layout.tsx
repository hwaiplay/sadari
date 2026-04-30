import Header from "./Header/Header";
import { Outlet } from "react-router-dom";
import Navigation from "./Navigation/Navigation";
import { vars } from "@/app/styles/tokens.css";

/**
 * fileName       : MainLayout
 * author         : hanwon.Jang
 * date           : 2026-04-26
 * description    : 메인페이지 전용 레이아웃
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang        주석 추가
 */

function Layout({ hasPaddingTop = true }) {
  return (
    <div>
      <Header />
      <main style={{ paddingTop: hasPaddingTop ? vars.headerHeight : 0 }}>
        <Outlet />
      </main>
      <Navigation />
    </div>
  );
}

export default Layout;
