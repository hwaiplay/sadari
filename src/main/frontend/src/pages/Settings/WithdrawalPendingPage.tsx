import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { queryClient } from "@/app/query/queryClient";
import { queryKeys } from "@/app/query/queryKeys";
import {
  getWithdrawalStatusApi,
  uptWithdrawalCancelApi,
  type WithdrawalStatus,
} from "@/features/User/api/withdrawalApi";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ActionButton } from "@/components/Button/ActionButton";
import * as styles from "./WithdrawalResultPage.css";

/**
 * 영구 삭제 대기 회원에게 삭제 예정일과 취소 기능만 제공함
 *
 * @author HanWon.Jang
 * @return 영구 삭제 대기 전용 화면
 */
const WithdrawalPendingPage = () => {

  const navigate = useNavigate();
  const [status, setStatus] = useState<WithdrawalStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // 화면 진입 시 영구 삭제 예정일을 조회함
  useEffect(() => {
    // 비동기 상태 조회를 화면 생명주기 안에서 실행함
    void (async () => {
      // API 실패를 사용자 안내로 전환하기 위한 처리 블록임
      try {
        // 로그인 회원의 영구 삭제 대기 정보를 조회함
        const result = await getWithdrawalStatusApi();
        // 화면에 표시할 삭제 대기 정보를 설정함
        setStatus(result.data ?? null);
      }

      // 상태 조회 실패 내용을 사용자에게 안내함
      catch (error) {
        // "탈퇴 상태를 확인할 수 없어요."
        await sweetError(
          message("frontend.withdrawal.pending.loadFailedTitle"),
          // "잠시 후 다시 시도해주세요."
          getApiErrorMessage(error, message("frontend.common.tryAgain")),
        );
      }

      // 상태 조회 완료 후 로딩 표시를 종료함
      finally {
        // 삭제 대기 화면의 로딩 상태를 해제함
        setIsLoading(false);
      }
    })();
  }, []);

  /**
   * 영구 삭제 대기를 취소하고 정상 서비스 화면으로 이동함
   *
   * @author HanWon.Jang
   * @return 반환값 없음
   */
  const handleCancel = async (): Promise<void> => {

    // 영구 탈퇴 취소 의사를 다시 확인함
    const confirmed = await sweetConfirm({
      // "영구 탈퇴를 취소하시겠어요?"
      title: message("frontend.withdrawal.pending.cancelConfirmTitle"),
      // "관리자 이용 정지가 남아 있으면 정지 상태로 복구돼요."
      text: message("frontend.withdrawal.pending.cancelConfirmText"),
    });

    // 사용자가 확인하지 않으면 복구 요청을 보내지 않음
    if (!confirmed) {
      // 사용자의 취소 선택을 유지하고 처리를 종료함
      return;
    }

    // API 실패를 사용자 안내로 전환하기 위한 처리 블록임
    try {
      // 영구 삭제 대기 취소를 서버에 요청함
      const result = await runBlockingOperation(uptWithdrawalCancelApi, {
        success: {
          // "영구 탈퇴가 취소됐어요."
          title: message("frontend.withdrawal.pending.cancelSuccess"),
        },
      });
      // 취소 전 삭제 대기 인증 캐시를 서버의 복구된 상태로 갱신한 뒤 화면 이동
      await queryClient.refetchQueries({ queryKey: queryKeys.auth, exact: true }, { throwOnError: true });
      // 관리자 이용 정지가 남아 있으면 정지 안내로, 아니면 정상 서비스 홈으로 이동함
      navigate(result.data === "SUSPENDED" ? "/suspension" : "/home", { replace: true });
    }

    // 복구 실패 내용을 사용자에게 안내함
    catch (error) {
      // "영구 탈퇴를 취소하지 못했어요."
      await sweetError(
        message("frontend.withdrawal.pending.cancelFailedTitle"),
        // "잠시 후 다시 시도해주세요."
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }
  };

  // 영구 삭제 대기 상태를 조회하는 동안 간단한 로딩 화면을 반환함
  if (isLoading) {
    // 삭제 예정일 조회 중 안내 화면을 반환함
    // "탈퇴 상태를 확인하고 있어요."
    return <main className={styles.page}>{message("frontend.withdrawal.pending.loading")}</main>;
  }

  // 서버 삭제 예정일을 완료 페이지와 동일한 연월일 안내에 사용
  const deleteDate = new Date(status?.deltDate ?? "");
  // "삭제 전까지 다시 로그인하면 영구 탈퇴 신청을 취소할 수 있어요."
  let guide = message("frontend.withdrawal.result.hardGuide");
  // 삭제 예정일 조회 실패에는 날짜를 추정하지 않고 기본 안내 유지
  if (!Number.isNaN(deleteDate.getTime())) {
    // "{0}년 {1}월 {2}일 전까지 다시 로그인하면 영구 탈퇴 신청을 취소할 수 있어요."
    guide = message("frontend.withdrawal.result.hardDateGuide", [
      deleteDate.getFullYear(), deleteDate.getMonth() + 1, deleteDate.getDate(),
    ]);
  }

  // 신청 완료 페이지와 동일한 영구 삭제 상태 안내 및 취소 화면 반환
  return (
    /* 영구 삭제 대기 전체 영역 */
    <main className={styles.page}>
      {/* Sadari 로고 영역 */}
      <header className={styles.logoHeader}>
        {/* "Sadari" */}
        <img className={styles.logo} src="/img/common/logo-upper.svg" alt={message("frontend.common.logoAlt")} />
      </header>

      {/* 영구 탈퇴 처리 상태와 안내 영역 */}
      <div className={styles.content}>
        {/* 영구 탈퇴 신청 상태 요약 영역 */}
        <section className={styles.statusSection}>
          <div className={styles.successMark} aria-hidden="true">✓</div>
          <h1 className={styles.heading}>
            {/* "영구 탈퇴 신청이 완료됐어요" */}
            {message("frontend.withdrawal.result.hardTitle")}
          </h1>
          <p className={styles.description}>
            {/* "설정된 유예기간이 지나면 계정과 관련 데이터가 영구 삭제돼요." */}
            {message("frontend.withdrawal.result.hardDescription")}
          </p>
        </section>

        {/* 계정 처리 상세 안내 영역 */}
        <section className={styles.guideSection}>
          <h2 className={styles.sectionTitle}>
            {/* "계정 처리 안내" */}
            {message("frontend.withdrawal.result.guideTitle")}
          </h2>
          {/* 처리 상태와 이용 안내 목록 영역 */}
          <dl className={styles.guideList}>
            <div className={styles.guideRow}>
              <dt className={styles.guideLabel}>
                {/* "처리 상태" */}
                {message("frontend.withdrawal.result.statusLabel")}
              </dt>
              <dd className={`${styles.guideValue} ${styles.successText}`}>
                {/* "영구 탈퇴 신청 완료" */}
                {message("frontend.withdrawal.result.hardStatus")}
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

        {/* 영구 탈퇴 취소 명령 영역 */}
        <ActionButton className={styles.primaryLink} variant="secondary" size="lg" width="full" onClick={handleCancel} disabled={!status}>
          {/* "영구 탈퇴 취소" */}
          {message("frontend.withdrawal.pending.cancel")}
        </ActionButton>
      </div>
    </main>
  );
};

export default WithdrawalPendingPage;
