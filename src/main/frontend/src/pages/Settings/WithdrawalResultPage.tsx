import { useSearchParams } from "react-router-dom";
import * as styles from "./WithdrawalStatusPage.css";

/**
 * Kakao 재인증을 거친 회원 탈퇴 처리 결과를 표시합니다.
 *
 * @author HanWon.Jang
 * @return 회원 탈퇴 완료 또는 실패 안내 화면
 */
function WithdrawalResultPage() {

  const [searchParams] = useSearchParams();
  const isSuccess = searchParams.get("success") === "Y";
  const withdrawalType = searchParams.get("type");

  // 회원 탈퇴 처리 결과 안내 화면을 반환합니다
  return (
    <main className={styles.page}>
      {/* 회원 탈퇴 결과 안내 영역 */}
      <section className={styles.panel}>
        <div className={isSuccess ? styles.successMark : styles.failMark}>
          {isSuccess ? "✓" : "!"}
        </div>
        <h1 className={styles.heading}>{isSuccess ? "탈퇴 처리가 완료됐어요" : "탈퇴 요청을 처리하지 못했어요"}</h1>
        <p className={styles.description}>
          {isSuccess
            ? withdrawalType === "HARD"
              ? "30일 동안 영구 삭제를 취소할 수 있어요."
              : "다시 Kakao 로그인을 하면 기존 계정을 복구할 수 있어요."
            : "계정 정보는 변경되지 않았어요. 잠시 후 다시 시도해주세요."}
        </p>
        <a className={styles.primaryLink} href="/login">로그인 화면으로 이동</a>
      </section>
    </main>
  );
}

export default WithdrawalResultPage;
