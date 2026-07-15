/**
 * src/main/frontend/src/components/Layout/Layout.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
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
 * description    : ?덉씠?꾩썐 而댄룷?뚰듃
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang        二쇱꽍 異붽?
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