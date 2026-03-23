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

const Router = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<MainLayout />}>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/oauth" element={<KakaoOAuth />} />
        <Route path="/home" element={<Home />} />
        <Route path="/detail/:id" element={<BookDetail />} />
      </Route>
      <Route element={<AddLayout />}>
        <Route path="/add" element={<Add />} />
      </Route>
    </Routes>
  );
};

export default Router;
