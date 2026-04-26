import Header from "./Header/Header";
import { Outlet } from "react-router-dom";
import Navigation from "./Navigation/Navigation";

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

function MainLayout() {
  return (
    <div>
      <Header />
      <main>
        <Outlet />
      </main>
      <Navigation />
    </div>
  );
}

export default MainLayout;
