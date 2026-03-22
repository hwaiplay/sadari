/**
 * fileName       : App
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       주석 추가
 */
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home/Home";
import MainLayout from "./components/Layout/MainLayout";
import BookDetail from "./pages/BookDetail/BookDetail";
import Login from "./pages/Login/Login";
import KakaoOAuth from "./pages/Login/KakaoOAuth";
import Add from "./pages/Add/Add";
import AddLayout from "./pages/Add/AddLayout";
export default function App() {
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
}
