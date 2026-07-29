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
import BookInfoPage from "@/pages/Book/Info/BookInfoPage";
import SearchBookInfoPage from "@/pages/Book/Search/SearchBookInfoPage";
import ScrollToTop from "@/components/Layout/ScrollTop";
import ReadingCalendarPage from "@/pages/My/ReadingCalendarPage";
import PublicReportPage from "@/pages/Book/PublicReports/PublicReportPage";
import ProfileEditPage from "@/pages/My/ProfileEditPage";
import SocialProfilePage from "@/pages/Social/SocialProfilePage";
import AlimPage from "@/pages/Alim/AlimPage";
import SettingsPage from "@/pages/Settings/SettingsPage";
import WithdrawalPage from "@/pages/Settings/WithdrawalPage";
import WithdrawalResultPage from "@/pages/Settings/WithdrawalResultPage";
import WithdrawalPendingPage from "@/pages/Settings/WithdrawalPendingPage";

/**
 * 공개 라우트와 인증 라우트를 분리해 애플리케이션 전체 화면 경로를 구성한다
 *
 * @author HanWon.Jang
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

        {/* Kakao 로그인 검증 */}
        <Route
          path="/oauth"
          element={
            <PublicRoute>
              <Oauth />
            </PublicRoute>
          }
        />

        {/* 회원 탈퇴 처리 결과 */}
        <Route path="/withdrawal/result" element={<WithdrawalResultPage />} />

        {/* 영구 삭제 대기 */}
        <Route
          path="/withdrawal/pending"
          element={
            <ProtectedRoute>
              <WithdrawalPendingPage />
            </ProtectedRoute>
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
            path="/book/public-reports/isbn"
            element={<PublicReportPage />}
          />

          {/* 책 검색 */}
          <Route path="/book/search" element={<BookSearchType />} />
          <Route path="/book/search/info" element={<SearchBookInfoPage />} />

          {/* 기록하기 */}
          <Route path="/set" element={<SetReportPage />} />

          <Route path="/mypage/reading-calendar" element={<ReadingCalendarPage />} />
          <Route path="/mypage/profile" element={<ProfileEditPage />} />
          <Route path="/social/profile/:userNumb" element={<SocialProfilePage />} />
          <Route path="/alim" element={<AlimPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/settings/withdrawal" element={<WithdrawalPage />} />
        </Route>
      </Routes>
    </>
  );
};

export default Router;
