// App.tsx
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home/Home";
import MainLayout from "./components/Layout/MainLayout/MainLayout";
import BookDetail from "./pages/BookDetail/BookDetail";
import Login from "./pages/Login/Login";
export default function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />

        <Route path="/home" element={<Home />} />
        <Route path="/detail/:id" element={<BookDetail />} />
      </Route>
    </Routes>
  );
}
