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
import ServiceInfoPage from "@/pages/Settings/ServiceInfoPage";
import WithdrawalPage from "@/pages/Settings/WithdrawalPage";
import WithdrawalResultPage from "@/pages/Settings/WithdrawalResultPage";
import WithdrawalPendingPage from "@/pages/Settings/WithdrawalPendingPage";
import WelcomePage from "@/pages/Welcome/WelcomePage";
import SuspensionPage from "@/pages/Settings/SuspensionPage";
import UserReportPage from "@/pages/UserReport/UserReportPage";
import UserReportCompletePage from "@/pages/UserReport/UserReportCompletePage";
import ErrorPage from "@/pages/Error/ErrorPage";
import MyClubPage from "@/pages/ReadingClub/MyClubPage";
import FindClubPage from "@/pages/ReadingClub/FindClubPage";
import SetClubPage from "@/pages/ReadingClub/SetClubPage.tsx";
import ClubDetailPage from "@/pages/ReadingClub/ClubDetailPage";
import NoticeListPage from "@/pages/Notice/NoticeListPage";
import NoticeDetailPage from "@/pages/Notice/NoticeDetailPage";
import InquiryListPage from "@/pages/Inquiry/InquiryListPage";
import InquiryWritePage from "@/pages/Inquiry/InquiryWritePage";
import InquiryDetailPage from "@/pages/Inquiry/InquiryDetailPage";
import InquiryLayout from "@/pages/Inquiry/InquiryLayout";
import ReadingTimerPage from "@/pages/Timer/ReadingTimerPage";

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

        {/* 관리자 이용 정지 안내 */}
        <Route
          path="/suspension"
          element={
            <ProtectedRoute>
              <SuspensionPage />
            </ProtectedRoute>
          }
        />

        {/* 이용 정지 회원의 영구 탈퇴 전용 화면 */}
        <Route
          path="/suspension/withdrawal"
          element={
            <ProtectedRoute>
              <WithdrawalPage hardOnly />
            </ProtectedRoute>
          }
        />

        {/* 정상 및 이용정지 사용자의 고객문의 화면 */}
        <Route
          element={
            <ProtectedRoute>
              <InquiryLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/inquiry/list" element={<InquiryListPage />} />
          <Route path="/inquiry/write" element={<InquiryWritePage />} />
          <Route path="/inquiry/detail/:inqrNumb" element={<InquiryDetailPage />} />
        </Route>

        {/* 최초 로그인 웰컴 */}
        <Route
          path="/welcome"
          element={
            <ProtectedRoute>
              <WelcomePage />
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
          <Route path="/report/detail/:id" element={<DetailPage />} />

          {/* 도서 정보 상세보기 */}
          <Route path="/book/info/:id" element={<BookInfoPage />} />

          {/* 공개 독후감 목록 */}
          <Route
            path="/report/public-reports/isbn"
            element={<PublicReportPage />}
          />

          {/* 책 검색 */}
          <Route path="/book/search" element={<BookSearchType />} />
          <Route path="/book/search/info" element={<SearchBookInfoPage />} />

          {/* 기록하기 */}
          <Route path="/report/set" element={<SetReportPage />} />

          <Route path="/mypage/reading-calendar" element={<ReadingCalendarPage />} />
          <Route path="/mypage/profile" element={<ProfileEditPage />} />
          {/* 독서 타이머와 주간 출석 */}
          <Route path="/timer" element={<ReadingTimerPage />} />
          <Route path="/social/profile/:userNumb" element={<SocialProfilePage />} />
          <Route path="/alim" element={<AlimPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/settings/alim" element={<AlimPage />} />
          <Route path="/settings/withdrawal" element={<WithdrawalPage />} />
          <Route path="/settings/service-info" element={<ServiceInfoPage />} />

          {/* 사용자 메뉴 경로에 연결한 배포 공지사항 목록과 상세 */}
          <Route path="/notice" element={<Navigate to="/notice/list" replace />} />
          <Route path="/notice/list" element={<NoticeListPage />} />
          <Route path="/notice/list/:noticeNumb" element={<NoticeDetailPage />} />

          {/* 독서 모임 1차 기능 */}
          <Route path="/reading-clubs/mine" element={<MyClubPage />} />
          <Route path="/reading-clubs/find" element={<FindClubPage />} />
          <Route path="/reading-clubs/set" element={<SetClubPage />} />
          <Route path="/reading-clubs/:clubNumb" element={<ClubDetailPage />} />

          {/* 사용자 콘텐츠 신고 사유 선택 */}
          <Route path="/user-report" element={<UserReportPage />} />

          {/* 사용자 콘텐츠 신고 완료 */}
          <Route
            path="/user-report/complete"
            element={<UserReportCompletePage />}
          />
        </Route>

        {/* 등록되지 않은 URL 안내 */}
        <Route
          path="*"
          element={
            <ProtectedRoute>
              <ErrorPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </>
  );
};

export default Router;
