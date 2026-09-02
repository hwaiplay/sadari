import { getApiErrorMessage } from "@/app/api/resultData.ts";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert.ts";
import { message } from "@/app/messages/message.ts";
import { runBlockingOperation } from "@/app/navigation/blockingOperation.ts";
import { useCompletedFormGuard } from "@/app/navigation/useCompletedFormGuard.ts";
import {
  createClubApi,
  getClubDtlApi,
  uptClubApi,
} from "@/features/ReadingClub/api/readingClubApi.ts";
import { getUserInterestCatalogApi, type UserInterest } from "@/features/User/api/userApi.ts";
import { type ChangeEvent, type FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {ClubCreateParams} from "@/features/ReadingClub/types/club.type.ts";

/**
 * fileName       : useSetClubPage
 * author         : HanWon.Jang
 * date           : 2026-08-22
 * description    : 모임 생성 및 수정 화면의 입력 상태와 카테고리 조회 및 제출 처리를 관리하는 훅
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        HanWon.Jang    진행 회차 공개 범위 잠금 추가
 */

export type SetClubPageMode = "create" | "edit";

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
 * @param mode 모임 폼 동작 모드
 * @return 모임 생성 및 수정 화면 상태와 이벤트 처리 함수
 */
export function useSetClubPage(mode: SetClubPageMode = "create") {
  const navigate = useNavigate();
  const finishForm = useCompletedFormGuard();
  const { clubNumb: clubNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const [catalog, setCatalog] = useState<UserInterest[]>([]);
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(mode === "edit");
  const [isSaving, setIsSaving] = useState(false);
  const [isVisibilityLocked, setIsVisibilityLocked] = useState(false);
  const [form, setForm] = useState<ClubCreateParams>(INITIAL_CLUB_FORM);

  useEffect(() => {
    // 잘못된 수정 경로에서는 서버에 상세 조회를 요청하지 않는다
    if (mode === "edit" && !Number.isFinite(clubNumb)) {
      // "모임 정보를 불러오지 못했어요"
      void sweetError(
        message("frontend.readingClub.error.fetchTitle"),
        /* "다시 시도해주세요." */ message("frontend.common.tryAgain"),
      );
      // 안전한 내 모임 목록으로 이동한다
      navigate("/reading-clubs/mine", { replace: true });
      // 잘못된 경로 처리를 마치고 로딩 상태를 해제한다
      setIsLoading(false);
      return;
    }

    // 수정 화면은 관심분야와 기존 모임 상세를 함께 조회한다
    const pageRequest = mode === "edit"
      ? Promise.all([getUserInterestCatalogApi(), getClubDtlApi(clubNumb)])
      : Promise.all([getUserInterestCatalogApi(), Promise.resolve(null)]);

    void pageRequest
      .then(([nextCatalog, detail]) => {
        // 카테고리 선택 팝업에 활성 관심분야를 설정한다
        setCatalog(nextCatalog);

        // 생성 화면은 빈 입력 상태를 유지한다
        if (!detail) {
          return;
        }

        // 모임장이 아닌 사용자는 수정 화면에 접근할 수 없다
        if (detail.membRole !== "OWNER") {
          // "모임을 수정할 수 없어요"
          void sweetError(
            message("frontend.readingClub.set.editAccessTitle"),
            message("frontend.readingClub.set.editAccessDescription"),
          );
          // 접근 가능한 모임 상세 화면으로 되돌린다
          navigate(`/reading-clubs/${clubNumb}`, { replace: true });
          return;
        }

        // 서버 상세를 수정 폼 입력값으로 변환한다
        setForm({
          clubName: detail.clubName,
          clubCntn: detail.clubCntn ?? "",
          clubVisb: detail.clubVisb,
          joinType: detail.joinType,
          maxxMemb: detail.maxxMemb,
          categoryList: detail.categoryList?.map((category) => category.intrCode) ?? [],
          questionList: detail.questionList ?? [],
        });
        // 예정 또는 진행 중인 독서 회차가 있으면 기존 공개 범위를 유지한다
        setIsVisibilityLocked(typeof detail.currentRondNumb === "number");
      })
      .catch((error) => {
        // "모임 정보를 불러오지 못했어요"
        void sweetError(
          message("frontend.readingClub.error.fetchTitle"),
          getApiErrorMessage(error, /* "다시 시도해주세요." */ message("frontend.common.tryAgain")),
        );

        // 수정 대상 조회에 실패하면 상세 화면으로 되돌린다
        if (mode === "edit" && Number.isFinite(clubNumb)) {
          navigate(`/reading-clubs/${clubNumb}`, { replace: true });
        }
      })
      .finally(() => {
        // 초기 조회가 끝나면 폼 화면을 표시한다
        setIsLoading(false);
      });
  }, [clubNumb, mode, navigate]);

  /**
   * 모임 이름 입력값을 생성 상태에 반영한다
   *
   * @author HanWon.Jang
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
   * @author Hanwon.Jang
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
   * @author Hanwon.Jang
   * @param clubVisb 공개 범위
   * @return 반환값이 없다
   */
  const selectVisibility = (clubVisb: "PUBLIC" | "PRIVATE"): void => {
    // 진행 회차가 있는 수정 화면에서는 공개 범위 상태를 변경하지 않는다
    if (isVisibilityLocked) {
      // 서버 정책과 동일하게 기존 공개 범위를 유지한다
      return;
    }

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
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const selectOpenJoin = (): void => {
    // 즉시 가입에는 승인 질문이 필요하지 않으므로 빈 목록을 설정한다
    setForm((current) => ({ ...current, joinType: "OPEN", questionList: [] }));
  };

  /**
   * 승인 가입 방식을 선택하고 첫 질문 입력란을 준비한다
   *
   * @author Hanwon.Jang
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
   * @author Hanwon.Jang
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
   * @author Hanwon.Jang
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
   * @author Hanwon.Jang
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
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const addQuestion = (): void => {
    // 기존 질문 뒤에 빈 질문 입력란을 추가한다
    setForm((current) => ({ ...current, questionList: [...current.questionList, ""] }));
  };

  /**
   * 선택한 관심 카테고리를 모임 생성 목록에서 제거한다
   *
   * @author Hanwon.Jang
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
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const openCategoryModal = (): void => {
    // 카테고리 선택 팝업을 표시한다
    setIsCategoryOpen(true);
  };

  /**
   * 관심 카테고리 선택 팝업을 닫는다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const closeCategoryModal = (): void => {
    // 카테고리 선택 팝업을 숨긴다
    setIsCategoryOpen(false);
  };

  /**
   * 팝업에서 선택한 관심 카테고리를 생성 상태에 저장한다
   *
   * @author Hanwon.Jang
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
   * 모임 생성 또는 수정 폼을 제출한다
   *
   * @author Hanwon.Jang
   * @param event 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const submitForm = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 제출을 막는다
    event.preventDefault();

    // 모임 저장에 필요한 필수 입력을 모두 확인한다
    if (!form.clubName.trim() || !form.clubCntn.trim() || form.categoryList.length === 0
        || (form.joinType === "APPROVAL" && (form.questionList.length === 0
        || form.questionList.some((question) => !question.trim())))
        || (mode === "edit" && !Number.isFinite(clubNumb))) {
      // "입력을 확인해 주세요"
      void sweetError(
        message("frontend.readingClub.set.validationTitle"),
        message("frontend.readingClub.set.validationDescription"),
      );
      // 필수 입력이 누락되거나 수정 대상이 잘못된 저장 요청을 중단한다
      return;
    }

    // 모임 저장 요청의 중복 제출을 막는다
    setIsSaving(true);

    try {
      // 입력값을 현재 화면 모드에 맞춰 생성 또는 수정한다
      const club = await runBlockingOperation(
        () => mode === "edit" ? uptClubApi(clubNumb, form) : createClubApi(form),
        {
          title: mode === "edit"
            ? message("frontend.readingClub.set.editSaving")
            : message("frontend.readingClub.set.saving"),
        },
      );
      // 완료된 생성 또는 수정 폼이 뒤로가기로 다시 열리지 않도록 상세 화면으로 교체한다
      finishForm(`/reading-clubs/${club.clubNumb}`);
    } catch (error) {
      // "모임을 저장하지 못했어요"
      void sweetError(
        mode === "edit"
          ? message("frontend.readingClub.set.updateErrorTitle")
          : message("frontend.readingClub.set.createErrorTitle"),
        getApiErrorMessage(
          error,
          mode === "edit"
            ? message("frontend.readingClub.set.updateErrorDescription")
            : message("frontend.readingClub.set.createErrorDescription"),
        ),
      );
    } finally {
      // 모임 저장 요청이 끝나면 제출 버튼을 다시 활성화한다
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
    isLoading,
    isSaving,
    isVisibilityLocked,
    isEditMode: mode === "edit",
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
