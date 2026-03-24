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

import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Login from "../pages/Login/Login";
import MainLayout from "../components/Layout/MainLayout";
import App from "../App";
import KakaoOAuth from "../pages/Login/KakaoOAuth";
import Home from "../pages/Home/Home";
import AddLayout from "../pages/Add/AddLayout";
import Add from "../pages/Add/Add";
import BookDetail from "../pages/BookDetail/BookDetail";
import ProtectedRoute from "./ProtectedRoute";

const Router = () => {
  return (
    <Routes>
      {/* 로그인 */}
      <Route path="/login" element={<Login />} />
      {/* 카카오 로그인 검증 */}
      <Route path="/oauth" element={<KakaoOAuth />} />

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
        <Route path="/detail/:id" element={<BookDetail />} />
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
