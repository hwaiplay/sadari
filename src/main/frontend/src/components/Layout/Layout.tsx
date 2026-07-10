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

/**
 * 공통 Header, 본문 영역, Navigation을 조합해 페이지 레이아웃을 구성한다.
 * @Author Hanwon.Jang
 * @param isMainLayout 본문을 Container로 감쌀지 여부
 * @return 공통 레이아웃 컴포넌트
 */
function Layout({ isMainLayout = true }) {

    return (
    <div>
      <Header   />
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
