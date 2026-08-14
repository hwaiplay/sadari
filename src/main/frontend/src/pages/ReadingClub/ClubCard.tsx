import { message } from "@/app/messages/message";
import type { ReadingClub } from "@/features/ReadingClub/api/readingClubApi";
import { useNavigate } from "react-router-dom";
import * as styles from "./ClubCard.css";

/** 독서 모임 목록의 공통 카드 UI를 구성한다. @author SeungHyeon.Kang @param club 표시할 모임 @return 모임 카드 */
export default function ClubCard({ club }: { club: ReadingClub }) {
  const navigate = useNavigate();
  // 목록에서 상세로 이동할 수 있는 모임 카드를 반환한다
  return (
    <article className={styles.card} role="button" tabIndex={0} onClick={() => navigate(`/reading-clubs/${club.clubNumb}`)} onKeyDown={(event) => {
      // Enter 키로도 상세 이동을 지원한다
      if (event.key === "Enter") navigate(`/reading-clubs/${club.clubNumb}`);
    }}>
      {/* 모임명과 가입 방식 영역 */}
      <div className={styles.cardTop}>
        <h2 className={styles.cardTitle}>{club.clubName}</h2>
        <span className={styles.badge}>
          {club.joinType === "OPEN"
            ? message("frontend.readingClub.common.join.open")
            : club.joinType === "APPROVAL"
              ? message("frontend.readingClub.common.join.approval")
              : message("frontend.readingClub.common.join.invite")}
        </span>
      </div>
      <p className={styles.description}>{club.clubCntn}</p>
      {/* 모임 카테고리 영역 */}
      <div className={styles.chips}>{club.categoryList?.map((category) => <span className={styles.chip} key={category.intrCode}>{category.intrName}</span>)}</div>
      <div className={styles.meta}>
        <span>{message("frontend.readingClub.common.owner", [club.ownrNick ?? "-"])}</span>
        <span>{message("frontend.readingClub.common.memberCapacity", [club.memberCnt, club.maxxMemb])}</span>
        {(club.invitedCnt ?? 0) > 0 && (
          <span>{message("frontend.readingClub.card.inviteWaiting", [club.invitedCnt ?? 0])}</span>
        )}
      </div>
    </article>
  );
}
