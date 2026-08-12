import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import { createClubApi, type ClubCreateParams } from "@/features/ReadingClub/api/readingClubApi";
import InterestSelectModal from "@/features/ReadingClub/components/InterestSelectModal";
import { getUserInterestCatalogApi, type UserInterest } from "@/features/User/api/userApi";
import { type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as styles from "./ReadingClub.css";

/** 모임 공개 범위, 가입 방식, 카테고리와 정원을 입력하는 생성 화면을 구성한다. @author SeungHyeon.Kang @return 새 모임 만들기 화면 */
export default function ClubCreatePage() {
  const navigate = useNavigate();
  const [catalog, setCatalog] = useState<UserInterest[]>([]);
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [form, setForm] = useState<ClubCreateParams>({ clubName: "", clubCntn: "", clubVisb: "PUBLIC", joinType: "OPEN", maxxMemb: 10, categoryList: [], questionList: [] });

  useEffect(() => {
    // 카테고리 선택 팝업의 활성 관심분야를 조회한다
    void getUserInterestCatalogApi().then(setCatalog).catch((error) => void sweetError("조회하지 못했어요", getApiErrorMessage(error, "다시 시도해 주세요.")));
  }, []);

  /** 공개 범위를 변경하고 허용 가입 방식으로 보정한다. @author SeungHyeon.Kang @param clubVisb 공개 범위 @return 반환값이 없다 */
  const selectVisibility = (clubVisb: "PUBLIC" | "PRIVATE"): void => {
    // 비공개 모임은 초대 가입으로 자동 고정한다
    setForm((current) => ({ ...current, clubVisb, joinType: clubVisb === "PRIVATE" ? "INVITE" : current.joinType === "INVITE" ? "OPEN" : current.joinType, questionList: clubVisb === "PRIVATE" ? [] : current.questionList }));
  };

  /** 정원을 허용 범위로 보정해 변경한다. @author SeungHyeon.Kang @param value 새 정원 @return 반환값이 없다 */
  const setCapacity = (value: number): void => {
    // 2명부터 100명 사이 정원만 화면 상태에 반영한다
    setForm((current) => ({ ...current, maxxMemb: Math.min(100, Math.max(2, Number.isFinite(value) ? value : 2)) }));
  };

  /** 승인 질문 한 항목을 변경한다. @author SeungHyeon.Kang @param index 질문 순서 @param value 질문 내용 @return 반환값이 없다 */
  const updateQuestion = (index: number, value: string): void => {
    // 해당 순서만 교체한 새 질문 목록을 설정한다
    setForm((current) => ({ ...current, questionList: current.questionList.map((question, questionIndex) => questionIndex === index ? value : question) }));
  };

  /** 모임 생성 폼을 제출한다. @author SeungHyeon.Kang @param event 폼 제출 이벤트 @return 반환값이 없다 */
  const submitForm = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 제출을 막는다
    event.preventDefault();
    // 화면 필수값을 먼저 안내한다
    if (!form.clubName.trim() || !form.clubCntn.trim() || form.categoryList.length === 0 || (form.joinType === "APPROVAL" && (form.questionList.length === 0 || form.questionList.some((question) => !question.trim())))) {
      // 누락 입력 안내를 노출한다
      void sweetError("입력을 확인해 주세요", "모임명, 소개, 카테고리와 필요한 가입 질문을 모두 입력해 주세요.");
      // 저장 요청을 중단한다
      return;
    }
    // 중복 제출을 막는다
    setIsSaving(true);
    try {
      // 모임과 첫 회원 관계를 생성한다
      const club = await createClubApi(form);
      // 생성된 모임 상세로 이동한다
      navigate(`/reading-clubs/${club.clubNumb}`, { replace: true });
    } catch (error) {
      // 서버 검증 메시지를 안내한다
      void sweetError("모임을 만들지 못했어요", getApiErrorMessage(error, "입력값을 확인해 주세요."));
    } finally {
      // 저장 버튼을 다시 활성화한다
      setIsSaving(false);
    }
  };

  const selectedCategories = catalog.filter((interest) => form.categoryList.includes(interest.intrCode));
  // 기존 탈퇴 선택 카드와 목표 설정 Stepper 스타일을 따른 생성 폼을 반환한다
  return (
    <div className={styles.page}>
      <form className={styles.form} onSubmit={(event) => void submitForm(event)}>
        <label className={styles.field}><span className={styles.label}>모임명</span><input className={styles.input} maxLength={100} value={form.clubName} onChange={(event) => setForm((current) => ({ ...current, clubName: event.target.value }))} /></label>
        <label className={styles.field}><span className={styles.label}>모임 소개</span><textarea className={styles.textarea} maxLength={2000} value={form.clubCntn} onChange={(event) => setForm((current) => ({ ...current, clubCntn: event.target.value }))} /></label>
        <section className={styles.field}><h2 className={styles.label}>카테고리</h2><div className={styles.chips}>{selectedCategories.map((interest) => <span className={styles.chip} key={interest.intrCode}>{interest.intrName}</span>)}</div><button className={styles.button} type="button" onClick={() => setIsCategoryOpen(true)}>카테고리 선택 ({form.categoryList.length}/3)</button></section>
        <section className={styles.field}><h2 className={styles.label}>공개 설정</h2><div className={styles.optionGrid}>
          <button className={styles.option} data-selected={form.clubVisb === "PUBLIC"} type="button" onClick={() => selectVisibility("PUBLIC")}><span className={styles.optionTitle}>공개 모임</span><span className={styles.optionDescription}>모임 찾기에 노출돼요.</span></button>
          <button className={styles.option} data-selected={form.clubVisb === "PRIVATE"} type="button" onClick={() => selectVisibility("PRIVATE")}><span className={styles.optionTitle}>비공개 모임</span><span className={styles.optionDescription}>맞팔 초대로만 참여해요.</span></button>
        </div></section>
        <section className={styles.field}><h2 className={styles.label}>가입 방식</h2><div className={styles.optionGrid}>
          {form.clubVisb === "PRIVATE" ? <button className={styles.option} data-selected="true" type="button"><span className={styles.optionTitle}>초대 가입</span><span className={styles.optionDescription}>모임장이 맞팔 사용자를 초대해요.</span></button> : <>
            <button className={styles.option} data-selected={form.joinType === "OPEN"} type="button" onClick={() => setForm((current) => ({ ...current, joinType: "OPEN", questionList: [] }))}><span className={styles.optionTitle}>즉시 가입</span><span className={styles.optionDescription}>정원 안에서 바로 참여해요.</span></button>
            <button className={styles.option} data-selected={form.joinType === "APPROVAL"} type="button" onClick={() => setForm((current) => ({ ...current, joinType: "APPROVAL", questionList: current.questionList.length ? current.questionList : [""] }))}><span className={styles.optionTitle}>승인 후 가입</span><span className={styles.optionDescription}>질문 답변을 보고 모임장이 결정해요.</span></button>
          </>}
        </div></section>
        {form.joinType === "APPROVAL" && <section className={styles.field}><h2 className={styles.label}>가입 질문 ({form.questionList.length}/5)</h2>{form.questionList.map((question, index) => <div className={styles.questionRow} key={`question-${index + 1}`}><input className={styles.input} maxLength={500} value={question} placeholder={`${index + 1}번 질문`} onChange={(event) => updateQuestion(index, event.target.value)} /><ActionButton className={styles.buttonDanger} variant="danger" size="sm" onClick={() => setForm((current) => ({ ...current, questionList: current.questionList.filter((_, questionIndex) => questionIndex !== index) }))}>{/* "삭제하기" */ message("frontend.common.delete")}</ActionButton></div>)}<button className={styles.button} type="button" disabled={form.questionList.length >= 5} onClick={() => setForm((current) => ({ ...current, questionList: [...current.questionList, ""] }))}>질문 추가</button></section>}
        <section className={styles.field}><h2 className={styles.label}>모임 정원</h2><div className={styles.stepper}><button className={styles.stepperButton} type="button" onClick={() => setCapacity(form.maxxMemb - 1)}>−</button><input className={styles.stepperInput} type="number" min={2} max={100} value={form.maxxMemb} onChange={(event) => setCapacity(Number(event.target.value))} /><button className={styles.stepperButton} type="button" onClick={() => setCapacity(form.maxxMemb + 1)}>＋</button></div></section>
        <button className={styles.button} type="submit" disabled={isSaving}>{isSaving ? "만드는 중" : "모임 만들기"}</button>
      </form>
      {isCategoryOpen && <InterestSelectModal catalog={catalog} initialCodes={form.categoryList} minimum={1} maximum={3} onClose={() => setIsCategoryOpen(false)} onSave={(categoryList) => { setForm((current) => ({ ...current, categoryList })); setIsCategoryOpen(false); }} />}
    </div>
  );
}
