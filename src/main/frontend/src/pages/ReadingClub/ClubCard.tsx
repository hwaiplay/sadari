/**
 * fileName       : ClubCard
 * author         : Hanwon.Jang
 * date           : 2026-09-01
 * description    : 모임 찾기 페이지에서 보여지는 모임 UI 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-01        Hanwon.Jang    상단 주석 추가
 */

import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import type {
  ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { useNavigate } from "react-router-dom";
import * as styles from "./ClubCard.css";

type ClubCardProps = {
  club: ReadingClub;
};

/**
 * 모임 가입 방식을 카드 메타 문구로 변환
 *
 * @author Hanwon.Jang
 * @param joinType 모임 가입 방식
 * @return 모임 가입 방식 문구
 */
const getJoinTypeLabel = (joinType: ReadingClub["joinType"]): string => {
  // 즉시 가입 모임
  if (joinType === "OPEN") {
    // "바로 가입"
    return message("frontend.readingClub.find.joinType.open");
  }

  // 승인 가입 모임
  if (joinType === "APPROVAL") {
    // "승인제"
    return message("frontend.readingClub.find.joinType.approval");
  }

  // 초대 전용 모임
  // "초대제"
  return message("frontend.readingClub.find.joinType.invite");
};

export default function ClubCard({ club }: ClubCardProps) {
  // 모임 상세 화면 이동에 사용할 라우터 함수를 조회
  const navigate = useNavigate();
  // "공개"
  const visibilityLabel = message("frontend.common.public");
  // 모임 가입 방식 표시 문구 조회
  const joinTypeLabel = getJoinTypeLabel(club.joinType);
  // "{0}/{1}명"
  const memberCapacityLabel = message("frontend.readingClub.common.memberCapacity", [
    club.memberCnt,
    club.maxxMemb,
  ]);

  /**
   * 선택한 추천 모임의 상세 화면으로 이동
   *
   * @author Hanwon.Jang
   * @return
   */
  const handleOpenClub = (): void => {
    navigate(`/reading-clubs/${club.clubNumb}`);
  };

  // 추천 모임의 요약과 상세 이동 버튼을 포함한 카드를 반환함
  return (
    <article className={styles.card}>
      {/* 모임 카테고리와 기본 정보 영역 */}
      <div className={styles.summary}>
        <div className={styles.categoryChips}>
          {club.categoryList?.map((category)=> (
            <span className={styles.categoryChip} key={category.intrCode}>
              {category.intrName}
            </span>
            ))
          }
        </div>
        <div className={styles.clubCopy}>
          <h3 className={styles.clubTitle}>{club.clubName}</h3>
          <p className={styles.clubMeta}>
            {visibilityLabel} · {joinTypeLabel} · {memberCapacityLabel}
          </p>
        </div>
      </div>

      <div className={styles.actionArea}>
        {/* 모임 소개 */}
        <p className={styles.description}>{club.clubCntn || "-"}</p>

        {/* 모임 보기 버튼 */}
        <ActionButton
          className={styles.actionButton}
          variant="secondary"
          size="md"
          width="full"
          type="button"
          onClick={handleOpenClub}
          icon={<img className={styles.actionIcon} src="/img/icons/icon-book-dark.svg" alt="" />}
        >
          {message("frontend.readingClub.find.action.view")}
        </ActionButton>
      </div>
    </article>
  );
}
