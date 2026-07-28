/**
 * src/main/frontend/src/features/Book/Set/components/SetLayout.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author HanWon.Jang
 */
import { Outlet } from "react-router-dom";
import { Container } from "@/components/Layout/Container/Container";
import { vars } from "@/app/styles/tokens.css";
import SubPageHeader from "@/components/Layout/Header/FormHeader";
import Navigation from "@/components/Layout/Navigation/Navigation";

/**
 * fileName       : SetLayout
 * author         : HanWon.Jang
 * date           : 2026-04-26
 * description    : 湲곕줉?섍린 ?섏씠吏 ?꾩슜 ?덉씠?꾩썐
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-26       HanWon.Jang       二쇱꽍 異붽?
 */

function SetLayout() {

  return (
    <Container>
      <SubPageHeader />
      {/* 독후감 등록과 수정 화면의 입력 콘텐츠 영역 */}
      <main style={{ marginTop: vars.headerHeight }}>
        <Outlet />
      </main>
      <Navigation isMain={false} />
    </Container>
  );
}

export default SetLayout;
