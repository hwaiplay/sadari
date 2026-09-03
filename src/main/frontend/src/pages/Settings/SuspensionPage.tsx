import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { checkAuthApi } from "@/features/Auth/api/authApi";
import { runLogout, selectLogoutScope } from "@/features/Auth/lib/logoutFlow";
import { getSuspInquiryNumbApi } from "@/features/Inquiry/api/inquiryApi";
import {
  getUserSuspensionApi,
  type UserSuspension,
} from "@/features/User/api/suspensionApi";
import { useEffect, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import * as styles from "./SuspensionPage.css";

// 관리자 이용 정지 회원 상태 코드
const SUSPENDED_STATUS = "SUSPENDED";

/**
 * 서버의 이용 정지 일시를 한국어 화면 표시값으로 변환함
 *
 * @author HanWon.Jang
 * @param value 변환할 ISO 일시
 * @return 한국어 일시 또는 무기한 문구
 */
const formatDateTime = (value?: string | null): string => {

  // 종료 일시가 없는 무기한 정지는 기간 대신 유형을 표시함
  if (!value) {
    // "무기한"
    return message("frontend.suspension.indefinite");
  }

  // 브라우저의 한국어 날짜 형식으로 변환한 일시를 반환함
  return new Date(value).toLocaleString("ko-KR");
};

/**
 * 관리자 이용 정지가 적용된 회원에게 공개 사유와 기간 및 허용 동작을 안내함
 *
 * @author HanWon.Jang
 * @return 이용 정지 전용 화면
 */
function SuspensionPage() {

  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [suspension, setSuspension] = useState<UserSuspension | null>(null);
  const [suspInquiryNumb, setSuspInquiryNumb] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 정지 안내 화면 진입 시 공개 가능한 정지 정보만 조회함
    void (async () => {
      // 정지 정보 조회 실패를 사용자 안내로 전환함
      try {
        // 현재 로그인 회원의 정지 유형과 사유 및 기간을 조회함
        const result = await getUserSuspensionApi();

        // 활성 정지 이력이 없으면 DB 상태로 보정된 인증 정보를 다시 확인함
        if (!result.data) {
          // 백엔드가 보정한 회원 상태를 즉시 다시 조회함
          const authResult = await checkAuthApi();
          // 보호 라우트도 같은 최신 인증 결과를 사용하도록 인증 캐시를 교체함
          queryClient.setQueryData(["auth"], authResult);
          // 현재 인증 응답에서 화면 이동 판단에 필요한 회원 상태를 추출함
          const authData = authResult.data as { userStat?: string } | undefined;

          // 정지가 해제 또는 만료된 경우에만 일반 서비스 홈으로 이동함
          if (authData?.userStat !== SUSPENDED_STATUS) {
            // 전체 새로고침 없이 홈으로 이동해 정지 화면 재진입 반복을 차단함
            navigate("/home", { replace: true });
          }

          // DB가 여전히 정지 상태이면 상세 정보가 없어도 현재 제한 화면을 유지함
          return;
        }

        // 활성 정지가 확인된 뒤 해당 정지 이후 접수한 최신 이의제기 번호를 조회함
        const inquiryNumb = await getSuspInquiryNumbApi();
        // 현재 정지 이후 접수한 이의제기 문의 번호를 버튼 이동값으로 보관함
        setSuspInquiryNumb(inquiryNumb);
        // 사용자에게 공개할 정지 정보를 화면 상태에 반영함
        setSuspension(result.data);
      }

      // 정지 정보 조회 오류를 사용자에게 안내함
      catch (error) {
        // "이용 정지 정보를 확인할 수 없어요."
        // "잠시 후 다시 시도해주세요."
        await sweetError(
          message("frontend.suspension.loadFailedTitle"),
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      }

      // 조회 성공 여부와 관계없이 로딩 화면을 종료함
      finally {
        // 정지 정보 조회 완료 상태를 화면에 반영함
        setIsLoading(false);
      }
    })();
  }, [navigate, queryClient]);

  /**
   * 선택한 범위의 로그인 세션을 종료하고 로그인 화면으로 이동함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleLogout = async (): Promise<void> => {

    // 제한 화면에서도 같은 Alert로 현재 기기 또는 전체 기기 로그아웃 범위를 선택함
    const logoutScope = await selectLogoutScope();

    // 사용자가 취소하면 정지 안내 화면을 유지함
    if (!logoutScope) {
      return;
    }

    // 서버 로그아웃 실패가 발생해도 현재 제한 화면에서는 로그인 화면으로 이동함
    try {
      // 선택한 범위의 로그인 세션과 푸시 구독을 정리함
      await runLogout(logoutScope);
    }

    // 로그아웃 요청 결과와 관계없이 로그인 화면으로 이동함
    finally {
      // 인증이 필요한 정지 화면에서 공개 로그인 화면으로 이동함
      navigate("/login", { replace: true });
    }
  };

  // 정지 정보를 조회하는 동안 안내 화면을 반환함
  if (isLoading) {
    // "이용 정지 정보를 확인하고 있어요."
    return <main className={styles.page}>{message("frontend.suspension.loading")}</main>;
  }

  // 공개 정지 정보와 허용된 계정 처리 동작만 포함한 전용 화면을 반환함
  return (
    <main className={styles.page}>
      {/* 이용 정지 상태와 제한 동작 안내 전체 영역 */}
      <section className={styles.panel}>
        {/* 이용 정지 상태 표시 영역 */}
        <div className={styles.mark} aria-hidden="true">!</div>
        {/* "서비스 이용 안내" */}
        {/* "계정 이용이 정지되었어요" */}
        <h1 className={styles.heading}>{message("frontend.suspension.title")}</h1>
        {/* 이용 정지 중 허용되는 계정 처리 안내 영역 */}
        <p className={styles.description}>
          {message("frontend.suspension.description")}
        </p>

        {/* 활성 정지 이력이 있을 때만 공개 사유와 기간을 표시함 */}
        {suspension ? (
          <dl className={styles.detailList}>
            <div className={styles.detailItem}>
              <dt className={styles.detailTerm}>{message("frontend.suspension.reason")}</dt>
              <dd className={styles.detailDescription}>{suspension.spndRsonName}</dd>
            </div>
            <div className={styles.detailItem}>
              <dt className={styles.detailTerm}>{message("frontend.suspension.start")}</dt>
              <dd className={styles.detailDescription}>{formatDateTime(suspension.strtDate)}</dd>
            </div>
            <div className={styles.detailItem}>
              <dt className={styles.detailTerm}>{message("frontend.suspension.end")}</dt>
              <dd className={styles.detailDescription}>{formatDateTime(suspension.endxDate)}</dd>
            </div>
          </dl>
        ) : (
          <p className={styles.note}>
            {message("frontend.suspension.mismatch")}
          </p>
        )}

        {/* 정지 회원에게 허용된 로그아웃 버튼 영역 */}
        <div className={styles.actions}>
          <button
            className={styles.withdrawalButton}
            type="button"
            onClick={() => navigate("/suspension/withdrawal")}
          >
            {/* "영구 탈퇴" */}
            {message("frontend.withdrawal.type.hard")}
          </button>
          <button
            className={styles.inquiryButton}
            type="button"
            onClick={() => navigate(suspInquiryNumb
              ? `/inquiry/detail/${suspInquiryNumb}`
              : "/inquiry/write?category=SUSPENSION_APPEAL")}
          >
            {suspInquiryNumb
              ? /* "문의내역보기" */ message("frontend.suspension.viewInquiry")
              : /* "이용정지 문의하기" */ message("frontend.suspension.writeInquiry")}
          </button>
          <button className={styles.logoutButton} type="button" onClick={handleLogout}>
            {/* "로그아웃" */}
            {message("frontend.auth.logout")}
          </button>
        </div>
      </section>
    </main>
  );
}

export default SuspensionPage;
