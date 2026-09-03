import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import {
  getWithdrawalStatusApi,
  uptWithdrawalCancelApi,
  type WithdrawalStatus,
} from "@/features/User/api/withdrawalApi";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./WithdrawalStatusPage.css";

/**
 * 영구 삭제 대기 회원에게 삭제 예정일과 취소 기능만 제공함
 *
 * @author HanWon.Jang
 * @return 영구 삭제 대기 전용 화면
 */
function WithdrawalPendingPage() {

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

  // 영구 삭제 대기 전용 화면을 반환함
  return (
    <main className={styles.page}>
      {/* 영구 삭제 예정일과 취소 기능 영역 */}
      <section className={styles.panel}>
        <div className={styles.pendingMark}>30</div>
        <h1 className={styles.heading}>
          {/* "계정이 영구 삭제 대기 중이에요" */}
          {message("frontend.withdrawal.pending.title")}
        </h1>
        <p className={styles.description}>
          {/* "영구 삭제 예정일" */}
          {message("frontend.withdrawal.pending.deleteDate")}
          <strong className={styles.date}>
            {status?.deltDate
              ? new Date(status.deltDate).toLocaleString(navigator.language)
              : /* "확인할 수 없음" */ message("frontend.common.unavailable")}
          </strong>
        </p>
        <button className={styles.cancelButton} type="button" onClick={handleCancel}>
          {/* "영구 탈퇴 취소" */}
          {message("frontend.withdrawal.pending.cancel")}
        </button>
      </section>
    </main>
  );
}

export default WithdrawalPendingPage;
