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

const Router = () => {
  return (
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

        {/* 책 검색 */}
        <Route path="/book/search" element={<BookSearchType />} />

        {/* 기록하기 */}
        <Route path="/add" element={<SetReportPage />} />

        {/* 독후감 수정 */}
        <Route path="/book/set/:id" element={<UpdateReportPage />} />
      </Route>
    </Routes>
  );
};

export default Router;
