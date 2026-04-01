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

import { Navigate, Route, Routes } from "react-router-dom";
import Login from "../pages/Login/Login";
import Oauth from "../pages/Oauth/Oauth";
import ProtectedRoute from "./ProtectedRoute";
import MainLayout from "../components/Layout/MainLayout";
import Home from "../pages/Home/Home";
import BookDetail from "../pages/Book/Detail/BookDetail";
import Add from "../pages/Add/Add";
import PublicRoute from "./PublicRoute";
import AddLayout from "../features/Add/components/AddLayout";
import BookSearch from "../pages/Book/Search/BookSearch";

const Router = () => {
  return (
    <Routes>
      {/* 로그인 */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <Login />
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
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* 메인 */}
        {/* home으로 리디렉션 */}
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route path="/home" element={<Home />} />
        {/* 독후감 상세보기 */}
        <Route path="/book/detail/:id" element={<BookDetail />} />
        <Route path="/book/search" element={<BookSearch />} />
      </Route>

      {/* 기록하기 */}
      <Route
        element={
          <ProtectedRoute>
            <AddLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/add" element={<Add />} />
      </Route>
    </Routes>
  );
};

export default Router;
