import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { logoutApi } from "@/features/Auth/api/authApi";
import {
  getUserSuspensionApi,
  type UserSuspension,
} from "@/features/User/api/suspensionApi";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./SuspensionPage.css";

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
  const [suspension, setSuspension] = useState<UserSuspension | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 정지 안내 화면 진입 시 공개 가능한 정지 정보만 조회합니다
    void (async () => {
      // 정지 정보 조회 실패를 사용자 안내로 전환합니다
      try {
        // 현재 로그인 회원의 정지 유형과 사유 및 기간을 조회합니다
        const result = await getUserSuspensionApi();

        // 조회 중 기간 정지가 만료되면 새 인증 상태를 확인하도록 홈을 다시 엽니다
        if (!result.data) {
          // 기간 만료로 Redis 상태가 복구됐으므로 인증 상태도 새로 조회하도록 전체 화면을 전환합니다
          window.location.replace("/home");
          // 만료된 정지 정보는 화면 상태에 반영하지 않고 조회를 종료합니다
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
  }, [navigate]);

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
        <p className={styles.eyebrow}>서비스 이용 안내</p>
        {/* "계정 이용이 정지되었어요" */}
        <h1 className={styles.heading}>계정 이용이 정지되었어요</h1>
        {/* 이용 정지 중 허용되는 계정 처리 안내 영역 */}
        <p className={styles.description}>
          정지 기간에는 일반 서비스 이용과 계정 비활성화가 제한돼요.
          영구 탈퇴와 로그아웃은 계속 이용할 수 있어요.
        </p>

        {/* 이용 정지 공개 사유와 기간 영역 */}
        <dl className={styles.detailList}>
          <div className={styles.detailItem}>
            <dt className={styles.detailTerm}>정지 사유</dt>
            <dd className={styles.detailDescription}>{suspension?.spndRsonName ?? "운영 정책 위반"}</dd>
          </div>
          <div className={styles.detailItem}>
            <dt className={styles.detailTerm}>정지 시작</dt>
            <dd className={styles.detailDescription}>{formatDateTime(suspension?.strtDate)}</dd>
          </div>
          <div className={styles.detailItem}>
            <dt className={styles.detailTerm}>정지 종료</dt>
            <dd className={styles.detailDescription}>{formatDateTime(suspension?.endxDate)}</dd>
          </div>
        </dl>

        {/* 이용 정지 만료와 내부 메모 공개 범위 안내 영역 */}
        <p className={styles.note}>
          관리자 내부 처리 메모는 공개되지 않아요. 기간 정지는 종료 시각 이후 로그인할 때 자동 해제돼요.
        </p>

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
