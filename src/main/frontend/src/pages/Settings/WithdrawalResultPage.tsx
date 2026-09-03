import { Link, useSearchParams } from "react-router-dom";
import { message } from "@/app/messages/message";
import * as styles from "./WithdrawalResultPage.css";

/**
 * Kakao 재인증을 거친 회원 탈퇴 처리 결과를 서비스 디자인으로 표시함
 *
 * @author HanWon.Jang
 * @return 회원 탈퇴 완료 또는 실패 안내 화면
 */
function WithdrawalResultPage() {

  const [searchParams] = useSearchParams();
  const isSuccess = searchParams.get("success") === "Y";
  const isHardWithdrawal = searchParams.get("type") === "HARD";
  let statusSymbol: string;
  let heading: string;
  let description: string;
  let statusLabel: string;
  let guide: string;

  // 실패 결과는 계정 정보가 변경되지 않았음을 명확히 안내함
  if (!isSuccess) {
    // "!"
    statusSymbol = "!";
    // "탈퇴 요청을 처리하지 못했어요"
    heading = message("frontend.withdrawal.result.failedTitle");
    // "계정 정보는 변경되지 않았어요. 잠시 후 다시 시도해주세요."
    description = message("frontend.withdrawal.result.failedDescription");
    // "처리 실패"
    statusLabel = message("frontend.withdrawal.result.failedStatus");
    // "로그인 후 설정에서 탈퇴 절차를 다시 진행해주세요."
    guide = message("frontend.withdrawal.result.failedGuide");
  }

  // 영구 탈퇴 성공은 설정된 유예기간에 따라 삭제가 진행되는 상태로 안내함
  else if (isHardWithdrawal) {
    // "✓"
    statusSymbol = "✓";
    // "영구 탈퇴 신청이 완료됐어요"
    heading = message("frontend.withdrawal.result.hardTitle");
    // "설정된 유예기간이 지나면 계정과 관련 데이터가 영구 삭제돼요."
    description = message("frontend.withdrawal.result.hardDescription");
    // "영구 탈퇴 신청 완료"
    statusLabel = message("frontend.withdrawal.result.hardStatus");
    // "삭제 전까지 다시 로그인하면 영구 탈퇴 신청을 취소할 수 있어요."
    guide = message("frontend.withdrawal.result.hardGuide");
  }

  // 계정 비활성화 성공은 재로그인 시 기존 계정을 다시 활성화할 수 있음을 안내함
  else {
    // "✓"
    statusSymbol = "✓";
    // "계정 비활성화가 완료됐어요"
    heading = message("frontend.withdrawal.result.softTitle");
    // "Sadari 이용을 멈추고 계정을 안전하게 비활성화했어요."
    description = message("frontend.withdrawal.result.softDescription");
    // "계정 비활성화 완료"
    statusLabel = message("frontend.withdrawal.result.softStatus");
    // "다시 Kakao 로그인을 하면 기존 계정을 활성화할 수 있어요."
    guide = message("frontend.withdrawal.result.softGuide");
  }

  // 회원 탈퇴 처리 결과와 후속 안내 화면을 반환함
  return (
    /* 회원 탈퇴 처리 결과 전체 영역 */
    <main className={styles.page}>
      {/* Sadari 로고 영역 */}
      <header className={styles.logoHeader}>
        {/* "Sadari" */}
        <img
          className={styles.logo}
          src="/img/common/logo-upper.svg"
          alt={message("frontend.common.logoAlt")}
        />
      </header>

      {/* 회원 탈퇴 처리 상태와 안내 영역 */}
      <div className={styles.content}>
        {/* 회원 탈퇴 처리 상태 요약 영역 */}
        <section className={styles.statusSection}>
          <div className={isSuccess ? styles.successMark : styles.failMark} aria-hidden="true">
            {statusSymbol}
          </div>
          <h1 className={styles.heading}>{heading}</h1>
          <p className={styles.description}>{description}</p>
        </section>

        {/* 회원 탈퇴 처리 결과 상세 안내 영역 */}
        <section className={styles.guideSection}>
          <h2 className={styles.sectionTitle}>
            {/* "계정 처리 안내" */}
            {message("frontend.withdrawal.result.guideTitle")}
          </h2>

          {/* 처리 상태와 후속 이용 안내 목록 영역 */}
          <dl className={styles.guideList}>
            <div className={styles.guideRow}>
              <dt className={styles.guideLabel}>
                {/* "처리 상태" */}
                {message("frontend.withdrawal.result.statusLabel")}
              </dt>
              <dd className={`${styles.guideValue} ${isSuccess ? styles.successText : styles.failText}`}>
                {statusLabel}
              </dd>
            </div>
            <div className={styles.guideRow}>
              <dt className={styles.guideLabel}>
                {/* "이용 안내" */}
                {message("frontend.withdrawal.result.usageGuide")}
              </dt>
              <dd className={styles.guideValue}>{guide}</dd>
            </div>
          </dl>
        </section>

        {/* 로그인 화면 이동 영역 */}
        <Link className={styles.primaryLink} to="/login">
          {/* "로그인 화면으로 이동" */}
          {message("frontend.withdrawal.result.login")}
        </Link>
      </div>
    </main>
  );
}

export default WithdrawalResultPage;
