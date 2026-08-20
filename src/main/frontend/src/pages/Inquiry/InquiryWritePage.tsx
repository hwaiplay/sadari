import {
  BAD_WORD_INCLUDED_CODE,
  getApiErrorMessage,
  ResultDataError,
} from "@/app/api/resultData";
import { sweetError, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import CustomSelect, { type CustomSelectOption } from "@/components/Select/CustomSelect";
import { useCheckAuth } from "@/features/Auth/hooks/useCheckAuth";
import { setInquiryApi } from "@/features/Inquiry/api/inquiryApi";
import { type ChangeEvent, type FormEvent, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import * as styles from "./InquiryPage.css";

type InquiryCategory = "GENERAL" | "ACCOUNT" | "SUSPENSION_APPEAL" | "BUG" | "SUGGESTION";

const categories: readonly CustomSelectOption<InquiryCategory>[] = [
  { value: "GENERAL", label: /* "일반 문의" */ message("frontend.inquiry.category.general") },
  { value: "ACCOUNT", label: /* "계정 문의" */ message("frontend.inquiry.category.account") },
  { value: "SUSPENSION_APPEAL", label: /* "이용정지 이의제기" */ message("frontend.inquiry.category.suspensionAppeal") },
  { value: "BUG", label: /* "오류 신고" */ message("frontend.inquiry.category.bug") },
  { value: "SUGGESTION", label: /* "서비스 제안" */ message("frontend.inquiry.category.suggestion") },
];

/**
 * 경로로 전달된 문의 유형을 허용된 카테고리로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param requestedCategory 경로에서 전달된 문의 유형
 * @return 허용된 문의 유형 또는 기본 일반 문의 유형
 */
function getInitialCategory(requestedCategory: string | null): InquiryCategory {

  // 이용정지 화면에서 전달된 이의제기 유형을 초기 선택값으로 사용한다
  if (requestedCategory === "SUSPENSION_APPEAL") {
    // 검증된 이용정지 이의제기 유형을 반환한다
    return requestedCategory;
  }

  // 나머지 허용 카테고리가 경로로 전달되면 해당 유형을 초기 선택값으로 사용한다
  if (requestedCategory === "ACCOUNT" || requestedCategory === "BUG"
      || requestedCategory === "SUGGESTION" || requestedCategory === "GENERAL") {
    // 검증된 문의 유형을 반환한다
    return requestedCategory;
  }

  // 허용되지 않은 경로 값은 일반 문의로 대체한다
  return "GENERAL";
}

/**
 * 고객문의 카테고리와 제목 및 내용을 입력받아 접수한다
 *
 * @author SeungHyeon.Kang
 * @return 고객문의 작성 화면
 */
function InquiryWritePage() {

  const navigate = useNavigate();
  const { isSuspended } = useCheckAuth();
  const [searchParams] = useSearchParams();
  const requestedCategory = searchParams.get("category");
  const [inqrCatg, setInqrCatg] = useState<InquiryCategory>(getInitialCategory(requestedCategory));
  const [inqrTitl, setInqrTitl] = useState("");
  const [inqrCntn, setInqrCntn] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const canSubmit = inqrTitl.trim().length > 0 && inqrCntn.trim().length > 0;

  /**
   * 문의 제목을 입력 상태에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 제목 입력 이벤트
   * @return 반환값이 없다
   */
  const handleTitleChange = (event: ChangeEvent<HTMLInputElement>): void => {

    setInqrTitl(event.target.value);
  };

  /**
   * 문의 본문을 입력 상태에 반영한다
   *
   * @author SeungHyeon.Kang
   * @param event 본문 입력 이벤트
   * @return 반환값이 없다
   */
  const handleContentChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {

    setInqrCntn(event.target.value);
  };

  /**
   * 입력값을 검증한 뒤 고객문의를 접수하고 상세 화면으로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param event 문의 작성 폼 제출 이벤트
   * @return 반환값이 없다
   */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {

    // 브라우저 기본 제출로 화면이 새로고침되지 않도록 문의 접수 흐름을 제어한다
    event.preventDefault();

    // 제목이나 문의 내용이 비어 있으면 서버 요청 전에 입력 보완을 안내한다
    if (!canSubmit) {
      // "입력 내용을 확인해주세요."
      await sweetWarning(
        message("frontend.common.checkInput"),
        // "제목과 문의 내용을 입력해주세요."
        message("frontend.inquiry.write.required"),
      );
      // 필수 입력값이 준비될 때까지 문의 접수를 중단한다
      return;
    }

    // 문의 접수 요청이 진행되는 동안 중복 제출을 차단한다
    setIsSaving(true);

    // 문의 접수 결과에 따라 상세 이동 또는 오류 알림을 처리한다
    try {
      // 인증 상태와 입력값을 반영한 고객문의를 서버에 접수한다
      const inqrNumb = await setInquiryApi({
        inqrCatg: isSuspended ? "SUSPENSION_APPEAL" : inqrCatg,
        inqrTitl: inqrTitl.trim(),
        inqrCntn: inqrCntn.trim(),
      });

      // 접수된 문의 상세 화면을 현재 작성 이력 대신 표시한다
      navigate(`/inquiry/detail/${inqrNumb}`, { replace: true });

    }

    // 서버가 반환한 실패 원인에 맞는 공통 알럿을 표시한다
    catch (saveError) {
      // 문의 접수 실패 응답에서 사용자 안내 문구를 안전하게 조회한다
      // "고객문의를 접수하지 못했습니다."
      const saveErrorMessage = getApiErrorMessage(saveError, message("frontend.inquiry.write.saveFailed"));

      // 비속어가 포함된 경우에는 입력란 아래 문구 대신 다른 작성 화면과 같은 경고 알럿을 사용한다
      if (saveError instanceof ResultDataError
          && Number(saveError.result.code) === BAD_WORD_INCLUDED_CODE) {
        // "입력 내용을 확인해주세요."
        await sweetWarning(message("frontend.common.checkInput"), saveErrorMessage);
        // 비속어 안내 후 일반 오류 알럿이 중복 표시되지 않도록 종료한다
        return;
      }

      // "문의 접수에 실패했습니다."
      await sweetError(message("frontend.inquiry.write.saveFailedTitle"), saveErrorMessage);
    }

    // 성공과 실패 모두에서 문의 접수 버튼을 다시 사용할 수 있도록 상태를 해제한다
    finally {
      // 문의 접수 요청이 끝났음을 버튼 상태에 반영한다
      setIsSaving(false);
    }
  };

  // 문의 내용 입력란이 남는 공간을 채우고 접수 버튼이 화면 하단에 배치되는 작성 화면을 반환한다
  return (
    <div className={styles.writePage}>
      {/* 문의 접수 안내 영역 */}
      <section className={styles.formIntro} aria-labelledby="inquiry-write-title">
        <h2 id="inquiry-write-title" className={styles.sectionTitle}>
          {/* "무엇을 도와드릴까요?" */}
          {message("frontend.inquiry.write.title")}
        </h2>
        <p className={styles.description}>
          {/* "문의 내용을 자세히 남겨주시면 확인 후 답변드리겠습니다." */}
          {message("frontend.inquiry.write.description")}
        </p>
      </section>

      {/* 문의 유형과 제목 및 내용 입력 영역 */}
      <form className={styles.form} onSubmit={handleSubmit}>
        {/* 문의 유형 선택 영역 */}
        <div className={styles.field}>
          <span className={styles.label}>
            {/* "문의 유형" */}
            {message("frontend.inquiry.write.categoryLabel")}<span className={styles.required} aria-hidden="true">*</span>
          </span>
          {isSuspended ? (
            <div className={styles.fixedCategory} aria-label={message("frontend.inquiry.write.categoryLabel")}>
              {/* "이용정지 이의제기" */}
              {message("frontend.inquiry.category.suspensionAppeal")}
            </div>
          ) : (
            <CustomSelect<InquiryCategory>
              value={inqrCatg}
              options={categories}
              ariaLabel={message("frontend.inquiry.write.categorySelect")}
              className={styles.categorySelect}
              triggerClassName={styles.categorySelectTrigger}
              optionListClassName={styles.categoryOptionList}
              optionClassName={styles.categoryOption}
              onChange={setInqrCatg}
            />
          )}
        </div>

        {/* 문의 제목 입력 영역 */}
        <div className={styles.field}>
          <label className={styles.label} htmlFor="inquiry-title">
            {/* "제목" */}
            {message("frontend.inquiry.write.titleLabel")}<span className={styles.required} aria-hidden="true">*</span>
          </label>
          <input
            id="inquiry-title"
            className={styles.input}
            maxLength={200}
            placeholder={message("frontend.inquiry.write.titlePlaceholder")}
            value={inqrTitl}
            onChange={handleTitleChange}
          />
          <div className={styles.fieldFooter}>
            <span />
            <span className={styles.count}>{inqrTitl.length} / 200</span>
          </div>
        </div>

        {/* 문의 내용 입력 영역 */}
        <div className={styles.contentField}>
          <label className={styles.label} htmlFor="inquiry-content">
            {/* "문의 내용" */}
            {message("frontend.inquiry.write.contentLabel")}<span className={styles.required} aria-hidden="true">*</span>
          </label>
          <textarea
            id="inquiry-content"
            className={styles.textarea}
            maxLength={4000}
            placeholder={message("frontend.inquiry.write.contentPlaceholder")}
            value={inqrCntn}
            onChange={handleContentChange}
          />
          <div className={styles.fieldFooter}>
            <p className={styles.helper}>
              {/* "개인정보나 비밀번호는 입력하지 마세요." */}
              {message("frontend.inquiry.write.privacyHelp")}
            </p>
            <span className={styles.count}>{inqrCntn.length} / 4,000</span>
          </div>
        </div>

        {/* 문의 접수 버튼 영역 */}
        <button
          className={styles.submitButton}
          type="submit"
          disabled={isSaving || !canSubmit}
        >
          {isSaving ? (
            <>
              {/* "접수 중입니다" */}
              {message("frontend.inquiry.write.saving")}
            </>
          ) : (
            <>
              {/* "문의 접수" */}
              {message("frontend.inquiry.write.submit")}
            </>
          )}
        </button>
      </form>
    </div>
  );
}

export default InquiryWritePage;
