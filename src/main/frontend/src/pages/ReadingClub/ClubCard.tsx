import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import type {
  ClubCategory,
  ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { useNavigate } from "react-router-dom";
import * as styles from "./ClubCard.css";

type ClubCardProps = {
  club: ReadingClub;
};

/**
 * 모임 카테고리 한 항목을 카드 상단 칩으로 표시한다
 *
 * @author Hanwon.Jang
 * @param category 표시할 모임 카테고리
 * @return 모임 카테고리 칩
 */
const renderCategory = (category: ClubCategory) => {
  // 서버 카테고리 이름을 표시하는 어두운 칩을 반환한다
  return (
    <span className={styles.categoryChip} key={category.intrCode}>
      {category.intrName}
    </span>
  );
};

/**
 * 모임 가입 방식을 카드 메타 문구로 변환한다
 *
 * @author Hanwon.Jang
 * @param joinType 모임 가입 방식
 * @return 모임 가입 방식 문구
 */
const getJoinTypeLabel = (joinType: ReadingClub["joinType"]): string => {
  // 즉시 가입 모임은 Figma의 바로 가입 문구를 반환한다
  if (joinType === "OPEN") {
    // "바로 가입"
    return message("frontend.readingClub.find.joinType.open");
  }

  // 승인 가입 모임은 Figma의 승인제 문구를 반환한다
  if (joinType === "APPROVAL") {
    // "승인제"
    return message("frontend.readingClub.find.joinType.approval");
  }

  // 초대 전용 모임은 초대제 문구를 반환한다
  // "초대제"
  return message("frontend.readingClub.find.joinType.invite");
};

/**
 * 로그인 사용자의 모임 관계와 가입 방식에 맞는 카드 버튼 문구를 결정한다
 *
 * @author Hanwon.Jang
 * @param club 버튼 상태를 결정할 추천 모임
 * @return 모임 카드 버튼 문구
 */
const getActionLabel = (club: ReadingClub): string => {
  // 이미 가입한 모임은 상세 확인 문구를 반환한다
  if (club.membStat === "ACTIVE") {
    // "모임 보기"
    return message("frontend.readingClub.find.action.view");
  }

  // 승인 대기 중인 모임은 신청 상태 확인 문구를 반환한다
  if (club.joinStat === "PENDING") {
    // "신청 내역 보기"
    return message("frontend.readingClub.find.action.pending");
  }

  // 즉시 가입 모임은 가입 행동 문구를 반환한다
  if (club.joinType === "OPEN") {
    // "가입하기"
    return message("frontend.readingClub.find.action.join");
  }

  // 승인 가입 모임은 신청 행동 문구를 반환한다
  if (club.joinType === "APPROVAL") {
    // "신청하기"
    return message("frontend.readingClub.find.action.apply");
  }

  // 초대 전용 모임은 상세 확인 문구를 반환한다
  // "상세보기"
  return message("frontend.readingClub.find.action.detail");
};

/**
 * 공개 모임의 카테고리와 가입 정보를 Figma 추천 카드로 구성한다
 *
 * @author Hanwon.Jang
 * @param club 표시할 추천 공개 모임
 * @return 추천 모임 카드
 */
export default function ClubCard({ club }: ClubCardProps) {
  // 모임 상세 화면 이동에 사용할 라우터 함수를 조회한다
  const navigate = useNavigate();
  // "공개"
  const visibilityLabel = message("frontend.common.public");
  // 모임 가입 방식 표시 문구를 조회한다
  const joinTypeLabel = getJoinTypeLabel(club.joinType);
  // 로그인 사용자의 모임 관계에 맞는 행동 문구를 조회한다
  const actionLabel = getActionLabel(club);
  // "{0}/{1}명"
  const memberCapacityLabel = message("frontend.readingClub.common.memberCapacity", [
    club.memberCnt,
    club.maxxMemb,
  ]);

  /**
   * 선택한 추천 모임의 상세 화면으로 이동한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleOpenClub = (): void => {
    // 가입 질문과 현재 관계를 확인할 수 있는 모임 상세로 이동한다
    navigate(`/reading-clubs/${club.clubNumb}`);
  };

  // 추천 모임의 요약과 상세 이동 버튼을 포함한 카드를 반환한다
  return (
    <article className={styles.card}>
      {/* 모임 카테고리와 기본 정보 영역 */}
      <div className={styles.summary}>
        <div className={styles.categoryChips}>
          {club.categoryList?.map(renderCategory)}
        </div>
        <div className={styles.clubCopy}>
          <h3 className={styles.clubTitle}>{club.clubName}</h3>
          <p className={styles.clubMeta}>
            {visibilityLabel} · {joinTypeLabel} · {memberCapacityLabel}
          </p>
        </div>
      </div>

      {/* 모임 소개와 가입 진입 영역 */}
      <div className={styles.actionArea}>
        <p className={styles.description}>{club.clubCntn || "-"}</p>
        <ActionButton
          className={styles.actionButton}
          variant="secondary"
          size="md"
          width="full"
          type="button"
          onClick={handleOpenClub}
          icon={<img className={styles.actionIcon} src="/img/icons/icon-book-dark.svg" alt="" />}
        >
          {actionLabel}
        </ActionButton>
      </div>
    </article>
  );
}
