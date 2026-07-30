import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { checkAuthApi, logoutApi } from "@/features/Auth/api/authApi";
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
 * 서버의 이용 정지 일시를 한국어 화면 표시값으로 변환합니다.
 *
 * @author HanWon.Jang
 * @param value 변환할 ISO 일시
 * @return 한국어 일시 또는 무기한 문구
 */
const formatDateTime = (value?: string | null): string => {

  // 종료 일시가 없는 무기한 정지는 기간 대신 유형을 표시합니다
  if (!value) {
    // "무기한"
    return "무기한";
  }

  // 브라우저의 한국어 날짜 형식으로 변환한 일시를 반환합니다
  return new Date(value).toLocaleString("ko-KR");
};

/**
 * 관리자 이용 정지가 적용된 회원에게 공개 사유와 기간 및 허용 동작을 안내합니다.
 *
 * @author HanWon.Jang
 * @return 이용 정지 전용 화면
 */
function SuspensionPage() {

  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [suspension, setSuspension] = useState<UserSuspension | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 정지 안내 화면 진입 시 공개 가능한 정지 정보만 조회합니다
    void (async () => {
      // 정지 정보 조회 실패를 사용자 안내로 전환합니다
      try {
        // 현재 로그인 회원의 정지 유형과 사유 및 기간을 조회합니다
        const result = await getUserSuspensionApi();

        // 활성 정지 이력이 없으면 DB 상태로 보정된 인증 정보를 다시 확인한다
        if (!result.data) {
          // 백엔드가 보정한 회원 상태를 즉시 다시 조회한다
          const authResult = await checkAuthApi();
          // 보호 라우트도 같은 최신 인증 결과를 사용하도록 인증 캐시를 교체한다
          queryClient.setQueryData(["auth"], authResult);
          // 현재 인증 응답에서 화면 이동 판단에 필요한 회원 상태를 추출한다
          const authData = authResult.data as { userStat?: string } | undefined;

          // 정지가 해제 또는 만료된 경우에만 일반 서비스 홈으로 이동한다
          if (authData?.userStat !== SUSPENDED_STATUS) {
            // 전체 새로고침 없이 홈으로 이동해 정지 화면 재진입 반복을 차단한다
            navigate("/home", { replace: true });
          }

          // DB가 여전히 정지 상태이면 상세 정보가 없어도 현재 제한 화면을 유지한다
          return;
        }

        // 사용자에게 공개할 정지 정보를 화면 상태에 반영합니다
        setSuspension(result.data);
      }

      // 정지 정보 조회 오류를 사용자에게 안내합니다
      catch (error) {
        // "이용 정지 정보를 확인할 수 없어요."
        // "잠시 후 다시 시도해주세요."
        await sweetError(
          "이용 정지 정보를 확인할 수 없어요.",
          getApiErrorMessage(error, "잠시 후 다시 시도해주세요."),
        );
      }

      // 조회 성공 여부와 관계없이 로딩 화면을 종료합니다
      finally {
        // 정지 정보 조회 완료 상태를 화면에 반영합니다
        setIsLoading(false);
      }
    })();
  }, [navigate, queryClient]);

  /**
   * 현재 로그인 세션을 종료하고 로그인 화면으로 이동합니다.
   *
   * @author HanWon.Jang
   * @return 반환값이 없습니다
   */
  const handleLogout = async (): Promise<void> => {

    // 서버 로그아웃 실패가 발생해도 현재 제한 화면에서는 로그인 화면으로 이동합니다
    try {
      // Refresh Token과 회원 상태 캐시를 제거하도록 로그아웃을 요청합니다
      await logoutApi();
    }

    // 로그아웃 요청 결과와 관계없이 로그인 화면으로 이동합니다
    finally {
      // 인증이 필요한 정지 화면에서 공개 로그인 화면으로 이동합니다
      navigate("/login", { replace: true });
    }
  };

  // 정지 정보를 조회하는 동안 안내 화면을 반환합니다
  if (isLoading) {
    // "이용 정지 정보를 확인하고 있어요."
    return <main className={styles.page}>이용 정지 정보를 확인하고 있어요.</main>;
  }

  // 공개 정지 정보와 허용된 계정 처리 동작만 포함한 전용 화면을 반환합니다
  return (
    <main className={styles.page}>
      {/* 이용 정지 상태와 제한 동작 안내 전체 영역 */}
      <section className={styles.panel}>
        {/* 이용 정지 상태 표시 영역 */}
        <div className={styles.mark} aria-hidden="true">!</div>
        {/* "서비스 이용 안내" */}
        {/* "계정 이용이 정지되었어요" */}
        <h1 className={styles.heading}>계정 이용이 정지되었어요</h1>
        {/* 이용 정지 중 허용되는 계정 처리 안내 영역 */}
        <p className={styles.description}>
          {`정지 기간에는 일반 서비스 이용과\n계정 비활성화가 제한돼요.`}
        </p>

        {/* 활성 정지 이력이 있을 때만 공개 사유와 기간을 표시한다 */}
        {suspension ? (
          <dl className={styles.detailList}>
            <div className={styles.detailItem}>
              <dt className={styles.detailTerm}>정지 사유</dt>
              <dd className={styles.detailDescription}>{suspension.spndRsonName}</dd>
            </div>
            <div className={styles.detailItem}>
              <dt className={styles.detailTerm}>정지 시작</dt>
              <dd className={styles.detailDescription}>{formatDateTime(suspension.strtDate)}</dd>
            </div>
            <div className={styles.detailItem}>
              <dt className={styles.detailTerm}>정지 종료</dt>
              <dd className={styles.detailDescription}>{formatDateTime(suspension.endxDate)}</dd>
            </div>
          </dl>
        ) : (
          <p className={styles.note}>
            정지 상태와 상세 이력이 일치하지 않아요. 로그아웃 후 다시 로그인하거나 관리자에게 문의해주세요.
          </p>
        )}

        {/* 영구 탈퇴와 로그아웃 버튼 영역 */}
        <div className={styles.actions}>
          <button
            className={styles.withdrawButton}
            type="button"
            onClick={() => navigate("/suspension/withdrawal")}
          >
            {/* "영구 탈퇴" */}
            영구 탈퇴
          </button>
          <button className={styles.logoutButton} type="button" onClick={handleLogout}>
            {/* "로그아웃" */}
            로그아웃
          </button>
        </div>
      </section>
    </main>
  );
}

export default SuspensionPage;
