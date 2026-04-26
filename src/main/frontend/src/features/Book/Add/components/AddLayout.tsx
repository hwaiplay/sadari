import { Outlet } from "react-router-dom";
import { Container } from "@/components/Layout/Container/Container";
import { vars } from "@/app/styles/tokens.css";
import FormHeader from "@/components/Layout/Header/FormHeader";

/**
 * fileName       : AddLayout
 * author         : hanwon.Jang
 * date           : 2026-04-26
 * description    : 기록페이지 전용 레이아웃
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       hanwon.Jang       주석 추가
 */

function AddLayout() {
  return (
    <Container>
      <FormHeader />
      <main style={{ marginTop: vars.headerHeight }}>
        <Outlet />
      </main>
    </Container>
  );
}

export default AddLayout;
