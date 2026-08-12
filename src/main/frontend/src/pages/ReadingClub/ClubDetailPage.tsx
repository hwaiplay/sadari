import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import ProfileImage from "@/features/User/components/ProfileImage";
import {
  decideClubApplicationApi,
  getClubApplicationListApi,
  getClubDtlApi,
  getInviteCandidateListApi,
  inviteClubUsersApi,
  joinClubApi,
  type ClubApplication,
  type InviteCandidate,
  type ReadingClub,
} from "@/features/ReadingClub/api/readingClubApi";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import * as styles from "./ReadingClub.css";

/** 모임 가입, 맞팔 초대와 가입 승인을 포함한 상세 화면을 구성한다. @author SeungHyeon.Kang @return 모임 상세 화면 */
export default function ClubDetailPage() {
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const [club, setClub] = useState<ReadingClub | null>(null);
  const [answers, setAnswers] = useState<string[]>([]);
  const [candidates, setCandidates] = useState<InviteCandidate[]>([]);
  const [selectedCandidates, setSelectedCandidates] = useState<Set<number>>(new Set());
  const [applications, setApplications] = useState<ClubApplication[]>([]);

  /** 모임 상세와 모임장 전용 데이터를 조회한다. @author SeungHyeon.Kang @return 반환값이 없다 */
  const loadPage = async (): Promise<void> => {
    // 모임 상세를 먼저 조회해 사용자 역할을 판단한다
    const detail = await getClubDtlApi(clubNumb);
    // 상세 질문 수에 맞춰 기존 답변을 유지하거나 초기화한다
    setAnswers((current) => detail.questionList?.map((_, index) => current[index] ?? "") ?? []);
    // 상세 상태를 반영한다
    setClub(detail);
    // 모임장은 맞팔 후보와 가입 신청을 추가 조회한다
    if (detail.membRole === "OWNER") {
      // 두 전용 목록을 동시에 조회한다
      const [nextCandidates, nextApplications] = await Promise.all([getInviteCandidateListApi(clubNumb), getClubApplicationListApi(clubNumb)]);
      // 맞팔 후보를 설정한다
      setCandidates(nextCandidates);
      // 승인 대기 신청을 설정한다
      setApplications(nextApplications);
    }
  };

  useEffect(() => {
    // 유효한 모임 번호만 상세 조회를 시작한다
    if (Number.isFinite(clubNumb)) void loadPage().catch((error) => void sweetError("조회하지 못했어요", getApiErrorMessage(error, "다시 시도해 주세요.")));
  }, [clubNumb]);

  /** 모임 가입 또는 승인 신청을 처리한다. @author SeungHyeon.Kang @return 반환값이 없다 */
  const joinClub = async (): Promise<void> => {
    // 모임 정책에 맞춘 가입 요청을 전달한다
    await joinClubApi(clubNumb, answers);
    // 처리 후 상세 상태를 갱신한다
    await loadPage();
  };

  /** 선택한 맞팔 후보에게 초대를 발송한다. @author SeungHyeon.Kang @return 반환값이 없다 */
  const inviteCandidates = async (): Promise<void> => {
    // 선택한 모든 대상의 좌석을 예약한다
    await inviteClubUsersApi(clubNumb, Array.from(selectedCandidates));
    // 선택 상태를 비운다
    setSelectedCandidates(new Set());
    // 후보와 좌석 수를 다시 조회한다
    await loadPage();
  };

  /** 가입 신청을 승인 또는 거절한다. @author SeungHyeon.Kang @param applNumb 신청 번호 @param joinStat 처리 상태 @return 반환값이 없다 */
  const decideApplication = async (applNumb: number, joinStat: "APPROVED" | "REJECTED"): Promise<void> => {
    // 모임장 결정을 서버에 반영한다
    await decideClubApplicationApi(clubNumb, applNumb, joinStat);
    // 답변이 삭제된 최신 신청 목록과 인원을 다시 조회한다
    await loadPage();
  };

  // 상세 조회 전 로딩 안내를 반환한다
  if (!club) return <div className={styles.loading}>모임을 불러오고 있어요.</div>;

  const canJoin = !club.membStat && !club.joinStat && club.clubVisb === "PUBLIC" && club.joinType !== "INVITE";
  // 모임 정보와 역할별 행동 영역을 반환한다
  return (
    <div className={styles.page}>
      <header className={styles.detailHeader}><div className={styles.chips}>{club.categoryList?.map((category) => <span className={styles.chip} key={category.intrCode}>{category.intrName}</span>)}</div><h1 className={styles.detailTitle}>{club.clubName}</h1><p className={styles.description}>{club.clubCntn}</p><div className={styles.meta}><span>모임장 {club.ownrNick}</span><span>{club.memberCnt}/{club.maxxMemb}명</span><span>{club.clubVisb === "PUBLIC" ? "공개" : "비공개"}</span></div></header>
      {club.membStat === "ACTIVE" && <section className={styles.panel}><h2 className={styles.sectionTitle}>{club.membRole === "OWNER" ? "내가 운영하는 모임이에요" : "참여 중인 모임이에요"}</h2></section>}
      {club.joinStat === "PENDING" && <section className={styles.panel}><h2 className={styles.sectionTitle}>가입 승인을 기다리고 있어요</h2><p className={styles.description}>모임장이 답변을 확인한 뒤 승인하거나 거절해요.</p></section>}
      {canJoin && <section className={styles.panel}><h2 className={styles.sectionTitle}>{club.joinType === "OPEN" ? "바로 참여하기" : "가입 신청하기"}</h2>{club.questionList?.map((question, index) => <label className={styles.field} key={question}><span className={styles.label}>{question}</span><textarea className={styles.textarea} maxLength={2000} value={answers[index] ?? ""} onChange={(event) => setAnswers((current) => current.map((answer, answerIndex) => answerIndex === index ? event.target.value : answer))} /></label>)}<button className={styles.button} type="button" disabled={club.joinType === "APPROVAL" && answers.some((answer) => !answer.trim())} onClick={() => void joinClub().catch((error) => void sweetError("가입하지 못했어요", getApiErrorMessage(error, "다시 시도해 주세요.")))}>{club.joinType === "OPEN" ? "즉시 가입" : "가입 신청"}</button></section>}
      {club.membRole === "OWNER" && <>
      <section className={styles.panel}><h2 className={styles.sectionTitle}>맞팔로워 초대</h2>{candidates.length ? <>{candidates.map((candidate) => <label className={styles.profileRow} key={candidate.userNumb}><ProfileImage className={styles.avatar} src={candidate.porfPath} alt="" /><span><strong className={styles.profileName}>{candidate.userNick}</strong><span className={styles.profileIntro}>{candidate.intrCntn}</span></span><input type="checkbox" checked={selectedCandidates.has(candidate.userNumb)} onChange={() => setSelectedCandidates((current) => { const next = new Set(current); if (next.has(candidate.userNumb)) next.delete(candidate.userNumb); else next.add(candidate.userNumb); return next; })} /></label>)}<button className={styles.button} type="button" disabled={!selectedCandidates.size} onClick={() => void inviteCandidates().catch((error) => void sweetError("초대하지 못했어요", getApiErrorMessage(error, "정원을 확인해 주세요.")))}>선택한 사용자 초대</button></> : <p className={styles.empty}>초대할 수 있는 맞팔로워가 없어요.</p>}</section>
      {club.joinType === "APPROVAL" && <section className={styles.panel}><h2 className={styles.sectionTitle}>가입 승인 대기 ({applications.length})</h2>{applications.length ? applications.map((application) => <article className={styles.application} key={application.applNumb}><div className={styles.profileRow}><ProfileImage className={styles.avatar} src={application.porfPath} alt="" /><strong className={styles.profileName}>{application.userNick}</strong><span /></div>{application.questionList.map((question, index) => <div className={styles.qa} key={question}><strong>Q. {question}</strong><span>A. {application.answerList[index]}</span></div>)}<div className={styles.actions}><button className={styles.button} type="button" onClick={() => void decideApplication(application.applNumb, "APPROVED")}>승인</button><button className={styles.buttonDanger} type="button" onClick={() => void decideApplication(application.applNumb, "REJECTED")}>거절</button></div></article>) : <p className={styles.empty}>대기 중인 가입 신청이 없어요.</p>}</section>}
      </>}
    </div>
  );
}
