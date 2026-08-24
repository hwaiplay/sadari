import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { runBlockingOperation } from "@/app/navigation/blockingOperation";
import { ActionButton } from "@/components/Button/ActionButton";
import Skeleton from "@/components/Skeleton/Skeleton";
import {
  delMemberRestrictionApi,
  getMemberExitListApi,
  type ClubMemberExit,
} from "@/features/ReadingClub/api/readingClubApi";
import ProfileImage from "@/features/User/components/ProfileImage";
import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import * as styles from "./ClubMemberRestrictionPage.css";

/** 모임장이 퇴장 내역과 재가입 제한 상태를 관리한다. @author HanWon.Jang @return 퇴장 내역 및 제한 페이지 */
const ClubMemberRestrictionPage = () => {
  const { clubNumb: clubNumbParam } = useParams<{ clubNumb: string }>();
  const clubNumb = Number(clubNumbParam);
  const [exitList, setExitList] = useState<ClubMemberExit[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [processingUserNumb, setProcessingUserNumb] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState("");

  /** 최신 퇴장 내역과 제한 상태를 조회한다. @author HanWon.Jang @return 조회 완료 Promise */
  const loadExitList = useCallback(async (): Promise<void> => {
    // 유효한 양의 모임 번호만 서버 조회에 사용한다
    if (!Number.isFinite(clubNumb) || clubNumb <= 0) {
      // "올바르지 않은 접근이에요."
      setErrorMessage(message("frontend.common.invalidAccess"));
      // 잘못된 경로의 로딩 상태를 종료한다
      setIsLoading(false);
      // 서버 호출 없이 조회 흐름을 종료한다
      return;
    }

    // 퇴장 내역 조회 시작 상태를 반영한다
    setIsLoading(true);
    // 이전 조회 오류를 초기화한다
    setErrorMessage("");
    // 조회 실패를 페이지 오류 상태로 격리한다
    try {
      // 모임장 권한이 검증된 퇴장 내역을 조회한다
      const list = await getMemberExitListApi(clubNumb);
      // 최신 퇴장 내역을 화면에 반영한다
      setExitList(list);
    } catch (error) {
      // "다시 시도해주세요."
      setErrorMessage(getApiErrorMessage(error, message("frontend.common.tryAgain")));
    } finally {
      // 성공과 실패에 관계없이 최초 조회 상태를 종료한다
      setIsLoading(false);
    }
  }, [clubNumb]);

  // 경로의 모임 번호가 바뀌면 해당 모임의 내역을 다시 조회한다
  useEffect(() => {
    // 현재 경로에 대응하는 퇴장 내역 조회를 시작한다
    void loadExitList();
  }, [loadExitList]);

  /** 지정한 퇴장 회원의 재가입 제한을 해제한다. @author HanWon.Jang @param userNumb 대상 사용자 번호 @return 제한 해제 완료 Promise */
  const handleRelease = async (userNumb: number): Promise<void> => {
    // 중복 제한 해제를 막기 위해 대상 사용자를 처리 상태로 설정한다
    setProcessingUserNumb(userNumb);
    // 제한 해제 실패를 공통 오류 알림으로 격리한다
    try {
      /** 현재 퇴장 회원의 재가입 제한을 해제한다. @author HanWon.Jang @return 제한 해제 응답 Promise */
      const releaseRestriction = () => delMemberRestrictionApi(clubNumb, userNumb);
      // 제한 해제 중 화면 이동을 막고 같은 모달에서 완료를 안내한다
      await runBlockingOperation(releaseRestriction, {
        title: message("frontend.readingClub.restriction.releasing"),
        success: { title: message("frontend.readingClub.restriction.releaseSuccess") },
      });
      // 변경된 제한 상태를 최신 목록으로 다시 조회한다
      await loadExitList();
    } catch (error) {
      // "제한을 해제하지 못했어요."
      void sweetError(
        message("frontend.readingClub.restriction.releaseError"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      // 다음 회원의 제한을 관리할 수 있도록 처리 상태를 종료한다
      setProcessingUserNumb(null);
    }
  };

  /** 퇴장 내역 한 건을 프로필과 제한 상태 카드로 표시한다. @author HanWon.Jang @param item 퇴장 내역 @return 퇴장 내역 카드 */
  const renderExitItem = (item: ClubMemberExit) => {
    /** 현재 카드 회원의 재가입 제한을 해제한다. @author HanWon.Jang @return 반환값이 없다 */
    const handleItemRelease = (): void => {
      // 카드의 사용자 번호를 제한 해제 처리에 전달한다
      void handleRelease(item.userNumb);
    };

    // 퇴장 프로필과 일시 및 현재 제한 상태를 포함한 카드를 반환한다
    return (
      <li className={styles.item} key={item.userNumb}>
        {/* 퇴장 회원 프로필 영역 */}
        <ProfileImage className={styles.avatar} src={item.porfPath} alt={item.userNick ?? ""} />
        {/* 퇴장 회원 정보와 제한 상태 영역 */}
        <div className={styles.info}>
          <strong className={styles.name}>{item.userNick ?? "-"}</strong>
          <span className={styles.exitDate}>{item.exitDate.replace("T", " ").slice(0, 16)}</span>
          <span className={item.blocYsno === "Y" ? styles.restricted : styles.released}>
            {item.blocYsno === "Y" ? message("frontend.readingClub.restriction.active") : message("frontend.readingClub.restriction.released")}
          </span>
        </div>
        {/* 재가입 제한 해제 버튼 영역 */}
        {item.blocYsno === "Y" ? (
          <ActionButton size="sm" variant="secondary" disabled={processingUserNumb !== null} onClick={handleItemRelease}>
            {/* "제한 해제" */}
            {message("frontend.readingClub.restriction.release")}
          </ActionButton>
        ) : null}
      </li>
    );
  };

  // 최초 조회 중에는 카드 형태의 스켈레톤 화면을 반환한다
  if (isLoading) {
    return <div className={styles.page}><Skeleton width="100%" height={92} borderRadius={20} /></div>;
  }

  // 접근 거절 또는 조회 실패 시 정제된 오류 문구를 반환한다
  if (errorMessage) {
    return <p className={styles.stateMessage}>{errorMessage}</p>;
  }

  // 퇴장 내역과 재가입 제한 관리 화면을 반환한다
  return (
    <main className={styles.page}>
      {/* 페이지 목적 안내 영역 */}
      <p className={styles.description}>
        {/* "퇴장한 회원의 내역을 확인하고 재가입 제한을 해제할 수 있어요." */}
        {message("frontend.readingClub.restriction.description")}
      </p>
      {/* 퇴장 회원 목록 영역 */}
      {exitList.length > 0 ? (
        <ul className={styles.list}>{exitList.map(renderExitItem)}</ul>
      ) : (
        <p className={styles.stateMessage}>
          {/* "퇴장한 회원이 없어요." */}
          {message("frontend.readingClub.restriction.empty")}
        </p>
      )}
    </main>
  );
};

export default ClubMemberRestrictionPage;
