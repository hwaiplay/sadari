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
import Layout from "../components/Layout/MainLayout";
import Home from "../pages/Home/Home";
import BookDetail from "../pages/Book/Detail/BookDetail";
import PublicRoute from "./PublicRoute";
import AddLayout from "../features/Book/Add/components/AddLayout";
import BookSearchType from "../pages/Book/Search/BookSearch";
import BookAdd from "@/pages/Book/Add/BookAdd";
import SetReport from "@/pages/Book/Set/SetReport";

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
            <Layout hasPaddingTop={false} />
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
        <Route path="/book/detail/:id" element={<BookDetail />} />
        {/* 책 검색 */}
        <Route path="/book/search" element={<BookSearchType />} />
        {/* 독후감 수정 */}
        <Route path="/book/set/:id" element={<SetReport />} />
      </Route>

      {/* 기록하기 */}
      <Route
        element={
          <ProtectedRoute>
            <AddLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/add" element={<BookAdd />} />
      </Route>
    </Routes>
  );
};

export default Router;
