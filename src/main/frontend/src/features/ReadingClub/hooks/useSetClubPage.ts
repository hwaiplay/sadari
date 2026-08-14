import { getApiErrorMessage } from "@/app/api/resultData.ts";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert.ts";
import { message } from "@/app/messages/message.ts";
import { createClubApi, type ClubCreateParams } from "@/features/ReadingClub/api/readingClubApi.ts";
import { getUserInterestCatalogApi, type UserInterest } from "@/features/User/api/userApi.ts";
import { type ChangeEvent, type FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const INITIAL_CLUB_FORM: ClubCreateParams = {
  clubName: "",
  clubCntn: "",
  clubVisb: "PUBLIC",
  joinType: "OPEN",
  maxxMemb: 2,
  categoryList: [],
  questionList: [],
};

/**
 * 모임 생성 화면의 입력 상태와 카테고리 조회 및 제출 처리를 관리한다
 *
 * @author SeungHyeon.Kang
 * @return 모임 생성 화면 상태와 이벤트 처리 함수
 */
export function useSetClubPage() {
  const navigate = useNavigate();
  const [catalog, setCatalog] = useState<UserInterest[]>([]);
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [form, setForm] = useState<ClubCreateParams>(INITIAL_CLUB_FORM);

  useEffect(() => {
    // 카테고리 선택 팝업의 활성 관심분야를 조회한다
    void getUserInterestCatalogApi()
      .then(setCatalog)
      .catch((error) => void sweetError(
        message("frontend.readingClub.error.fetchTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.common.retry")),
      ));
  }, []);

  /**
   * 모임 이름 입력값을 생성 상태에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 모임 이름 입력 이벤트
   * @return 반환값이 없다
   */
  const handleNameChange = (event: ChangeEvent<HTMLInputElement>): void => {
    // 새 모임 이름을 기존 생성 상태에 설정한다
    setForm((current) => ({ ...current, clubName: event.target.value }));
  };

  /**
   * 모임 소개 입력값을 생성 상태에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 모임 소개 입력 이벤트
   * @return 반환값이 없다
   */
  const handleDescriptionChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {
    // 새 모임 소개를 기존 생성 상태에 설정한다
    setForm((current) => ({ ...current, clubCntn: event.target.value }));
  };

  /**
   * 공개 범위를 변경하고 허용 가입 방식으로 보정한다
   *
   * @author SeungHyeon.Kang
   * @param clubVisb 공개 범위
   * @return 반환값이 없다
   */
  const selectVisibility = (clubVisb: "PUBLIC" | "PRIVATE"): void => {
    // 비공개 모임은 초대 가입으로 자동 고정한다
    setForm((current) => ({
      ...current,
      clubVisb,
      joinType: clubVisb === "PRIVATE" ? "INVITE" : current.joinType === "INVITE" ? "OPEN" : current.joinType,
      questionList: clubVisb === "PRIVATE" ? [] : current.questionList,
    }));
  };

  /**
   * 즉시 가입 방식을 선택하고 기존 승인 질문을 제거한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const selectOpenJoin = (): void => {
    // 즉시 가입에는 승인 질문이 필요하지 않으므로 빈 목록을 설정한다
    setForm((current) => ({ ...current, joinType: "OPEN", questionList: [] }));
  };

  /**
   * 승인 가입 방식을 선택하고 첫 질문 입력란을 준비한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const selectApprovalJoin = (): void => {
    // 기존 질문이 없으면 첫 승인 질문 입력란을 추가한다
    setForm((current) => ({
      ...current,
      joinType: "APPROVAL",
      questionList: current.questionList.length ? current.questionList : [""],
    }));
  };

  /**
   * 정원을 허용 범위로 보정해 변경한다
   *
   * @author SeungHyeon.Kang
   * @param value 새 정원
   * @return 반환값이 없다
   */
  const setCapacity = (value: number): void => {
    // 2명부터 100명 사이 정원만 화면 상태에 반영한다
    setForm((current) => ({
      ...current,
      maxxMemb: Math.min(100, Math.max(2, Number.isFinite(value) ? value : 2)),
    }));
  };

  /**
   * 승인 질문 한 항목을 변경한다
   *
   * @author SeungHyeon.Kang
   * @param index 질문 순서
   * @param value 질문 내용
   * @return 반환값이 없다
   */
  const updateQuestion = (index: number, value: string): void => {
    // 해당 순서만 교체한 새 질문 목록을 설정한다
    setForm((current) => ({
      ...current,
      questionList: current.questionList.map((question, questionIndex) => (
        questionIndex === index ? value : question
      )),
    }));
  };

  /**
   * 선택한 승인 질문을 생성 상태에서 제거한다
   *
   * @author SeungHyeon.Kang
   * @param index 제거할 질문 순서
   * @return 반환값이 없다
   */
  const removeQuestion = (index: number): void => {
    // 선택한 순서 외의 승인 질문만 남긴 목록을 설정한다
    setForm((current) => ({
      ...current,
      questionList: current.questionList.filter((_, questionIndex) => questionIndex !== index),
    }));
  };

  /**
   * 승인 질문 입력란을 하나 추가한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const addQuestion = (): void => {
    // 기존 질문 뒤에 빈 질문 입력란을 추가한다
    setForm((current) => ({ ...current, questionList: [...current.questionList, ""] }));
  };

  /**
   * 선택한 관심 카테고리를 모임 생성 목록에서 제거한다
   *
   * @author SeungHyeon.Kang
   * @param intrCode 제거할 관심 카테고리 코드
   * @return 반환값이 없다
   */
  const removeCategory = (intrCode: string): void => {
    // 선택한 코드 외의 카테고리만 남긴 새 목록을 설정한다
    setForm((current) => ({
      ...current,
      categoryList: current.categoryList.filter((categoryCode) => categoryCode !== intrCode),
    }));
  };

  /**
   * 관심 카테고리 선택 팝업을 연다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const openCategoryModal = (): void => {
    // 카테고리 선택 팝업을 표시한다
    setIsCategoryOpen(true);
  };

  /**
   * 관심 카테고리 선택 팝업을 닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const closeCategoryModal = (): void => {
    // 카테고리 선택 팝업을 숨긴다
    setIsCategoryOpen(false);
  };

  /**
   * 팝업에서 선택한 관심 카테고리를 생성 상태에 저장한다
   *
   * @author SeungHyeon.Kang
   * @param categoryList 선택한 관심 카테고리 코드 목록
   * @return 반환값이 없다
   */
  const saveCategories = (categoryList: string[]): void => {
    // 팝업에서 확정한 카테고리 코드 목록을 설정한다
    setForm((current) => ({ ...current, categoryList }));
    // 저장이 끝난 카테고리 선택 팝업을 닫는다
    setIsCategoryOpen(false);
  };

  /**
   * 모임 생성 폼을 제출한다
   *
   * @author SeungHyeon.Kang
   * @param event 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const submitForm = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 제출을 막는다
    event.preventDefault();

    // 모임 생성에 필요한 필수 입력을 모두 확인한다
    if (!form.clubName.trim() || !form.clubCntn.trim() || form.categoryList.length === 0
        || (form.joinType === "APPROVAL" && (form.questionList.length === 0
        || form.questionList.some((question) => !question.trim())))) {
      // "입력을 확인해 주세요"
      void sweetError(
        message("frontend.readingClub.set.validationTitle"),
        message("frontend.readingClub.set.validationDescription"),
      );
      // 필수 입력이 누락된 모임 생성 요청을 중단한다
      return;
    }

    // 모임 생성 요청의 중복 제출을 막는다
    setIsSaving(true);

    try {
      // 입력한 모임과 첫 회원 관계를 생성한다
      const club = await createClubApi(form);
      // 생성된 모임 상세 화면으로 이동한다
      navigate(`/reading-clubs/${club.clubNumb}`, { replace: true });
    } catch (error) {
      // "모임을 만들지 못했어요"
      void sweetError(
        message("frontend.readingClub.set.createErrorTitle"),
        getApiErrorMessage(error, message("frontend.readingClub.set.createErrorDescription")),
      );
    } finally {
      // 모임 생성 요청이 끝나면 제출 버튼을 다시 활성화한다
      setIsSaving(false);
    }
  };

  const selectedCategories = catalog.filter((interest) => form.categoryList.includes(interest.intrCode));

  // 모임 생성 화면 렌더링에 필요한 상태와 이벤트 처리 함수를 반환한다
  return {
    addQuestion,
    catalog,
    closeCategoryModal,
    form,
    handleDescriptionChange,
    handleNameChange,
    isCategoryOpen,
    isSaving,
    openCategoryModal,
    removeCategory,
    removeQuestion,
    saveCategories,
    selectApprovalJoin,
    selectedCategories,
    selectOpenJoin,
    selectVisibility,
    setCapacity,
    submitForm,
    updateQuestion,
  };
}
