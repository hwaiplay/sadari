import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import Layout from "../components/Layout/Layout";
import PublicRoute from "./PublicRoute";
import ScrollToTop from "@/components/Layout/ScrollTop";
import Loading from "@/components/Loading/Loading";
import { lazy, Suspense } from "react";

const LoginPage = lazy(() => import("@/pages/Login/LoginPage"));
const Oauth = lazy(() => import("@/pages/Oauth/Oauth"));
const Home = lazy(() => import("@/pages/Home/Home"));
const DetailPage = lazy(() => import("@/pages/Book/Detail/DetailPage"));
const BookSearchType = lazy(() => import("@/pages/Book/Search/SearchBookPage"));
const SetReportPage = lazy(() => import("@/pages/Book/Set/SetReportPage"));
const BookInfoPage = lazy(() => import("@/pages/Book/Info/BookInfoPage"));
const SearchBookInfoPage = lazy(() => import("@/pages/Book/Search/SearchBookInfoPage"));
const ReadingCalendarPage = lazy(() => import("@/pages/My/ReadingCalendarPage"));
const PublicReportPage = lazy(() => import("@/pages/Book/PublicReports/PublicReportPage"));
const ProfileEditPage = lazy(() => import("@/pages/My/ProfileEditPage"));
const SocialProfilePage = lazy(() => import("@/pages/Social/SocialProfilePage"));
const AlimPage = lazy(() => import("@/pages/Alim/AlimPage"));
const NotificationTargetPage = lazy(() => import("@/pages/Alim/NotificationTargetPage"));
const SettingsPage = lazy(() => import("@/pages/Settings/SettingsPage"));
const BlockedUsersPage = lazy(() => import("@/pages/Settings/BlockedUsersPage"));
const UserSettingsPage = lazy(() => import("@/pages/Settings/UserSettingsPage"));
const ServiceInfoPage = lazy(() => import("@/pages/Settings/ServiceInfoPage"));
const WithdrawalPage = lazy(() => import("@/pages/Settings/WithdrawalPage"));
const WithdrawalResultPage = lazy(() => import("@/pages/Settings/WithdrawalResultPage"));
const WithdrawalPendingPage = lazy(() => import("@/pages/Settings/WithdrawalPendingPage"));
const WelcomePage = lazy(() => import("@/pages/Welcome/WelcomePage"));
const SuspensionPage = lazy(() => import("@/pages/Settings/SuspensionPage"));
const UserReportPage = lazy(() => import("@/pages/UserReport/UserReportPage"));
const UserReportCompletePage = lazy(() => import("@/pages/UserReport/UserReportCompletePage"));
const ErrorPage = lazy(() => import("@/pages/Error/ErrorPage"));
const MyClubPage = lazy(() => import("@/pages/ReadingClub/MyClubPage"));
const FindClubPage = lazy(() => import("@/pages/ReadingClub/FindClubPage"));
const SetClubPage = lazy(() => import("@/pages/ReadingClub/SetClubPage"));
const UpdateClubPage = lazy(() => import("@/pages/ReadingClub/UpdateClubPage.tsx"));
const ClubDetailPage = lazy(() => import("@/pages/ReadingClub/ClubDetailPage"));
const ClubBookVotePage = lazy(() => import("@/pages/ReadingClub/ClubBookVotePage"));
const ClubMemberManagementPage = lazy(() => import("@/pages/ReadingClub/ClubMemberManagementPage"));
const ClubMemberRestrictionPage = lazy(() => import("@/pages/ReadingClub/ClubMemberRestrictionPage"));
const SetClubReadingPage = lazy(() => import("@/pages/ReadingClub/SetClubReadingPage"));
const ClubRoundReportPage = lazy(() => import("@/pages/ReadingClub/ClubRoundReportPage"));
const ClubReadingHistoryPage = lazy(() => import("@/pages/ReadingClub/ClubReadingHistoryPage"));
const ReadingGoalResultPage = lazy(() => import("@/pages/ReadingClub/ReadingGoalResultPage"));
const NoticeListPage = lazy(() => import("@/pages/Notice/NoticeListPage"));
const NoticeDetailPage = lazy(() => import("@/pages/Notice/NoticeDetailPage"));
const InquiryListPage = lazy(() => import("@/pages/Inquiry/InquiryListPage"));
const InquiryWritePage = lazy(() => import("@/pages/Inquiry/InquiryWritePage"));
const InquiryDetailPage = lazy(() => import("@/pages/Inquiry/InquiryDetailPage"));
const InquiryLayout = lazy(() => import("@/pages/Inquiry/InquiryLayout"));
const ReadingTimerPage = lazy(() => import("@/pages/Timer/ReadingTimerPage"));
const FeedPage = lazy(() => import("@/pages/Feed/FeedPage"));

/**
 * 공개 라우트와 인증 라우트를 분리해 애플리케이션 전체 화면 경로를 구성함
 *
 * @author HanWon.Jang
 * @return 애플리케이션 라우터 컴포넌트
 */
const Router = () => {

  return (
    <Suspense fallback={<Loading />}>
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

        {/* 알림번호 기반 현재 접근 화면 해석 */}
        <Route
          path="/notification-target/:alimNumb"
          element={
            <ProtectedRoute>
              <NotificationTargetPage />
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
          <Route
            path="/report/public-reports/target/:reptNumb"
            element={<PublicReportPage />}
          />

          {/* 책 검색 */}
          <Route path="/book/search" element={<BookSearchType />} />
          <Route path="/book/search/info" element={<SearchBookInfoPage />} />

          {/* 기록하기 */}
          <Route path="/report/set" element={<SetReportPage />} />

          {/* 독후감 달력*/}
          <Route path="/mypage/reading-calendar" element={<ReadingCalendarPage />} />

          {/* 마이페이지 */}
          <Route path="/mypage/profile" element={<ProfileEditPage />} />


          {/* 유저 페이지 */}
          <Route path="/social/profile/:userNumb" element={<SocialProfilePage />} />

          {/* 알림 페이지 */}
          <Route path="/alim" element={<AlimPage />} />

          {/* 독서 타이머와 주간 출석 */}
          <Route path="/timer" element={<ReadingTimerPage />} />

          {/* 팔로잉 사용자의 공개 활동 피드 */}
          <Route path="/feed" element={<FeedPage />} />

          {/* 환경설정 */}
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/settings/alim" element={<Navigate to="/settings/notifications" replace />} />
          <Route path="/settings/notifications" element={<UserSettingsPage section="notifications" />} />
          <Route path="/settings/privacy" element={<UserSettingsPage section="privacy" />} />
          <Route path="/settings/blocked-users" element={<BlockedUsersPage />} />
          <Route path="/settings/withdrawal" element={<WithdrawalPage />} />
          <Route path="/settings/service-info" element={<ServiceInfoPage />} />

          {/* 사용자 메뉴 경로에 연결한 배포 공지사항 목록과 상세 */}
          <Route path="/notice" element={<Navigate to="/notice/list" replace />} />
          <Route path="/notice/list" element={<NoticeListPage />} />
          <Route path="/notice/list/:noticeNumb" element={<NoticeDetailPage />} />

          {/* 독서 모임 1차 기능 */}
          {/* 내 모임 */}
          <Route path="/reading-clubs/mine" element={<MyClubPage />} />
          {/* 모임 찾기 */}
          <Route path="/reading-clubs/find" element={<FindClubPage />} />
          {/* 모임 개설 */}
          <Route path="/reading-clubs/set" element={<SetClubPage />} />
          {/* 모임 수정 */}
          <Route path="/reading-clubs/update/:clubNumb" element={<UpdateClubPage />} />
          {/* 모임 상세보기  */}
          <Route path="/reading-clubs/:clubNumb" element={<ClubDetailPage />} />
          {/* 책 검색 */}
          <Route path="/reading-clubs/books/search/:clubNumb" element={<BookSearchType />} />
          {/* 책 정보 */}
          <Route path="/reading-clubs/books/search/info/:clubNumb" element={<SearchBookInfoPage />} />
          {/* 모임 독서 등록 */}
          <Route path="/reading-clubs/set/book/:clubNumb" element={<SetClubReadingPage />} />
          {/* 모임 독서 수정 */}
          <Route path="/reading-clubs/update/book/:clubNumb/:rondNumb" element={<SetClubReadingPage />} />
          {/* 다음 도서 투표 */}
          <Route path="/reading-clubs/vote/book/:clubNumb" element={<ClubBookVotePage />} />
          {/* 멤버 관리 */}
          <Route path="/reading-clubs/manage/members/:clubNumb" element={<ClubMemberManagementPage />} />
          {/* 멤버 퇴장 및 제한 내역 */}
          <Route path="/reading-clubs/manage/member-restrictions/:clubNumb" element={<ClubMemberRestrictionPage />} />
          {/* 이전 독서 기록 */}
          <Route path="/reading-clubs/history/:clubNumb" element={<ClubReadingHistoryPage />} />
          {/* 독서 목표 결과 */}
          <Route path="/reading-clubs/history/detail/:clubNumb/:rondNumb" element={<ReadingGoalResultPage />} />
          {/* 모임원 독후감 목록 */}
          <Route path="/reading-clubs/history/:clubNumb/:rondNumb/reports" element={<ClubRoundReportPage />} />

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
    </Suspense>
  );
};

export default Router;
