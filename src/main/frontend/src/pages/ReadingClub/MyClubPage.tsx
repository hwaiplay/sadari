import {
  acceptClubInvitationApi,
  declineClubInvitationApi,
  getClubInvitationListApi,
  getMyClubListApi,
  type ClubInvitation,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ClubCard from "./ClubCard";
import * as styles from "./ReadingClub.css";

/** 내 모임과 받은 초대를 한 화면에 구성한다. @author SeungHyeon.Kang @return 내 모임 화면 */
export default function MyClubPage() {
  const navigate = useNavigate();
  const [clubs, setClubs] = useState<ReadingClub[]>([]);
  const [invitations, setInvitations] = useState<ClubInvitation[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  /** 내 모임과 받은 초대를 새로 조회한다. @author SeungHyeon.Kang @return 반환값이 없다 */
  const loadPage = async (): Promise<void> => {
    // 두 목록을 동시에 조회해 화면 대기 시간을 줄인다
    const [clubList, invitationList] = await Promise.all([getMyClubListApi(), getClubInvitationListApi()]);
    // 내 모임 목록을 화면 상태에 반영한다
    setClubs(clubList);
    // 받은 초대 목록을 화면 상태에 반영한다
    setInvitations(invitationList);
  };

  useEffect(() => {
    // 최초 진입 데이터를 조회한다
    void loadPage().catch((error) => void sweetError("조회하지 못했어요", getApiErrorMessage(error, "다시 시도해 주세요."))).finally(() => setIsLoading(false));
  }, []);

  /** 받은 초대를 수락한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 반환값이 없다 */
  const acceptInvitation = async (clubNumb: number): Promise<void> => {
    // 예약석을 활성 회원으로 전환한다
    await acceptClubInvitationApi(clubNumb);
    // 변경된 내 모임과 초대 목록을 다시 조회한다
    await loadPage();
  };

  /** 받은 초대를 거절한다. @author SeungHyeon.Kang @param clubNumb 모임 번호 @return 반환값이 없다 */
  const declineInvitation = async (clubNumb: number): Promise<void> => {
    // 초대 예약석을 삭제한다
    await declineClubInvitationApi(clubNumb);
    // 변경된 목록을 다시 조회한다
    await loadPage();
  };

  // 로딩 중에는 단일 안내만 반환한다
  if (isLoading) return <div className={styles.loading}>모임을 불러오고 있어요.</div>;

  // 내 모임, 받은 초대와 새 모임 만들기 영역을 반환한다
  return (
    <div className={styles.page}>
      {invitations.length > 0 && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>받은 초대</h2>
          <div className={styles.list}>{invitations.map((invitation) => (
            <article className={styles.card} key={invitation.clubNumb}>
              <div className={styles.cardTop}><h3 className={styles.cardTitle}>{invitation.clubName}</h3><span className={styles.badge}>3일 내 응답</span></div>
              <p className={styles.description}>{invitation.senderNick ?? "모임장"}님이 모임에 초대했어요.</p>
              <div className={styles.actions}>
                <button className={styles.button} type="button" onClick={() => void acceptInvitation(invitation.clubNumb)}>수락</button>
                <button className={styles.buttonDanger} type="button" onClick={() => void declineInvitation(invitation.clubNumb)}>거절</button>
              </div>
            </article>
          ))}</div>
        </section>
      )}
      <section className={styles.section}>
        <h2 className={styles.sectionTitle}>진행 중인 모임</h2>
        {clubs.length > 0 ? <div className={styles.list}>{clubs.map((club) => <ClubCard club={club} key={club.clubNumb} />)}</div> : <p className={styles.empty}>아직 참여 중인 모임이 없어요.</p>}
      </section>
      {/* 목록 아래 새 모임 만들기 진입 영역 */}
      <section className={styles.createArea}>
        <div className={styles.createCopy}><h2 className={styles.createTitle}>함께 읽을 사람을 모아보세요</h2><p className={styles.createDescription}>공개 범위와 가입 방식을 직접 정할 수 있어요.</p></div>
        <button className={styles.button} type="button" onClick={() => navigate("/reading-clubs/new")}>새 모임 만들기</button>
      </section>
    </div>
  );
}
