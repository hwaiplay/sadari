import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { confirmUserBlock } from "@/components/UserActionMenu/UserActionMenu";
import type { UserReportLocationState } from "@/components/UserActionMenu/userActionMenu.types";
import { setUserBlockApi } from "@/features/Social/api/socialApi";
import { useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import * as styles from "./UserReportPage.css";

/**
 * 신고 사유 선택 이후 감사 안내와 추가 차단 선택지를 표시함
 *
 * @author HanWon.Jang
 * @return 신고 완료 페이지
 */
const UserReportCompletePage = () => {
  const location = useLocation();
  const reportState = location.state as UserReportLocationState | null;
  const [isBlocked, setIsBlocked] = useState(false);

  // 신고 대상 정보 없이 직접 접근한 경우 안전한 기본 화면으로 이동함
  if (!reportState?.target) {
    return <Navigate to="/home" replace />;
  }

  const { target } = reportState;

  /**
   * 완료 화면에서 차단 여부를 확인하고 선택한 신고 대상의 차단 관계를 등록함
   *
   * @author HanWon.Jang
   * @return 차단 확인과 등록 완료 Promise
   */
  const handleBlock = async (): Promise<void> => {
    try {
      // 공통 차단 정책을 안내하고 사용자가 확인한 경우에만 상태 변경을 시작함
      await confirmUserBlock(target.userNick, async () => {
        // 처리 중 화면과 이동 차단을 유지하며 신고 대상 사용자를 차단함
        await runBlockingOperation(() => setUserBlockApi(target.userNumb), {
          // "사용자를 차단하고 있어요."
          title: message("frontend.userAction.block.processing"),
          success: {
            // "차단했어요."
            title: message("frontend.userAction.block.success"),
          },
        });
        // 완료 화면에서 같은 사용자를 다시 차단하지 않도록 성공 상태를 저장함
        setIsBlocked(true);
      });
    }

    catch (error) {
      // "사용자를 차단하지 못했습니다."
      await sweetError(
        message("frontend.userAction.block.failed"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }
  };

  /**
   * 신고 완료 화면의 차단 버튼 클릭을 비동기 차단 처리와 연결함
   *
   * @author HanWon.Jang
   * @return 반환값이 없음
   */
  const handleBlockClick = (): void => {
    // 차단 확인과 API 오류는 비동기 처리 함수 내부에서 안내함
    void handleBlock();
  };

  return (
    /* 신고 완료 안내 페이지 영역 */
    <section className={styles.completePage}>
      <header className={styles.completeHeading}>
        <div className={styles.completeIcon}>
          <svg width="42" height="42" viewBox="0 0 42 42" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="42" height="42" rx="21" fill="#293038"/>
            <path d="M12 20.9546L18.0155 26.97L30.9579 15" stroke="white" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>

        <h1 className={styles.completeTitle}>
          {/* "소중한 의견 감사합니다" */}
          {message("frontend.userReport.complete.title")}
        </h1>
        <p className={styles.completeDescription}>
          {/* "보내주신 신고는 꼼꼼히 검토해 더 안전한 사다리를 만드는 데 반영하겠습니다." */}
          {message("frontend.userReport.complete.description")}
        </p>
      </header>

      {/* 신고 대상에 대한 기타 옵션 영역 */}
      <div className={styles.otherOptions}>
        <h2 className={styles.otherOptionsTitle}>
          {/* "기타 옵션" */}
          {message("frontend.userReport.complete.otherOptions")}
        </h2>
        <button
          className={styles.blockOptionButton}
          type="button"
          disabled={isBlocked}
          onClick={handleBlockClick}
        >
          <span className={styles.blockOptionButtonBody}>
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 17.0625C4.5525 17.0625 0.9375 13.4475 0.9375 9C0.9375 4.5525 4.5525 0.9375 9 0.9375C13.4475 0.9375 17.0625 4.5525 17.0625 9C17.0625 13.4475 13.4475 17.0625 9 17.0625ZM9 2.0625C5.175 2.0625 2.0625 5.175 2.0625 9C2.0625 12.825 5.175 15.9375 9 15.9375C12.825 15.9375 15.9375 12.825 15.9375 9C15.9375 5.175 12.825 2.0625 9 2.0625Z" fill="#292D32"/>
              <path d="M3.675 14.8125C3.5325 14.8125 3.39 14.76 3.2775 14.6475C3.06 14.43 3.06 14.07 3.2775 13.8525L13.7775 3.3525C13.995 3.135 14.355 3.135 14.5725 3.3525C14.79 3.57 14.79 3.93 14.5725 4.1475L4.0725 14.6475C3.96 14.76 3.8175 14.8125 3.675 14.8125Z" fill="#292D32"/>
            </svg>

            <span>
              {/* "{닉네임} 님 차단" */}
              {isBlocked
                ? (
                    <>
                      {/* "차단했어요" */}
                      {message("frontend.userReport.complete.blocked")}
                    </>
                  )
                : message("frontend.userReport.complete.blockUser", [target.userNick])}
            </span>
          </span>

          <img
            className={styles.arrowIcon}
            src="/img/icons/arrow-bottom.svg"
            alt=""
            aria-hidden="true"
          />
        </button>
      </div>
    </section>
  );
};

export default UserReportCompletePage;
