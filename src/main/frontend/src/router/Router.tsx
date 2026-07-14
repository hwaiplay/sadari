import { Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "../pages/Login/LoginPage";
import Oauth from "../pages/Oauth/Oauth";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../components/Layout/Layout";
import Home from "../pages/Home/Home";
import DetailPage from "../pages/Book/Detail/DetailPage";
import PublicRoute from "./PublicRoute";
import BookSearchType from "../pages/Book/Search/SearchBookPage";
import SetReportPage from "@/pages/Book/Set/SetReportPage";
import UpdateReportPage from "@/pages/Book/Update/UpdateReportPage";
import BookInfoPage from "@/pages/Book/Info/BookInfoPage";
import SearchBookInfoPage from "@/pages/Book/Search/SearchBookInfoPage";
import ScrollToTop from "@/components/Layout/ScrollTop";
import ReadingCalendarPage from "@/pages/My/ReadingCalendarPage";
import PublicReportPage from "@/pages/Book/PublicReports/PublicReportPage";
import ProfileEditPage from "@/pages/My/ProfileEditPage";

/**
 * fileName       : Router
 * author         : hanwon.Jang
 * date           : 2026-03-23
 * description    : Router 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-23       hanwon.Jang       최초 생성
 */

/**
 * 공개 라우트와 인증 라우트를 분리해 애플리케이션 전체 화면 경로를 구성한다.
 * @Author Hanwon.Jang
 * @return 애플리케이션 라우터 컴포넌트
 */
const Router = () => {
  return (
    <>
      <ScrollToTop />
      <Routes>
        {/* 로그인 */}
        <Route
          path="/login"
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />

        {/* 카카오 로그인 검증 */}
        <Route
          path="/oauth"
          element={
            <PublicRoute>
              <Oauth />
            </PublicRoute>
          }
        />

        <Route
          element={
            <ProtectedRoute>
              <Layout isMainLayout={false} />
            </ProtectedRoute>
          }
        >
          {/* 메인 */}
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/home" element={<Home />} />
        </Route>

        <Route
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          {/* 독후감 상세보기 */}
          <Route path="/book/detail/:id" element={<DetailPage />} />

          {/* 도서 정보 상세보기 */}
          <Route path="/book/info/:id" element={<BookInfoPage />} />

          {/* 공개 독후감 목록 */}
          <Route
            path="/book/public-reports/report/:reportNumb"
            element={<PublicReportPage />}
          />
          <Route
            path="/book/public-reports/isbn"
            element={<PublicReportPage />}
          />

          {/* 책 검색 */}
          <Route path="/book/search" element={<BookSearchType />} />
          <Route path="/book/search/info" element={<SearchBookInfoPage />} />

          {/* 기록하기 */}
          <Route path="/set" element={<SetReportPage />} />

          {/* 독후감 수정 */}
          <Route path="/book/upt/:id" element={<UpdateReportPage />} />

          <Route path="/mypage/reading-calendar" element={<ReadingCalendarPage />} />
          <Route path="/mypage/profile" element={<ProfileEditPage />} />
        </Route>
      </Routes>
    </>
  );
};

export default Router;
