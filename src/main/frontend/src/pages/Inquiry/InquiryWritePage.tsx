import { getApiErrorMessage } from "@/app/api/resultData";
import CustomSelect, { type CustomSelectOption } from "@/components/Select/CustomSelect";
import { setInquiryApi } from "@/features/Inquiry/api/inquiryApi";
import { type ChangeEvent, type FormEvent, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import * as styles from "./InquiryPage.css";

type InquiryCategory = "GENERAL" | "ACCOUNT" | "SUSPENSION_APPEAL" | "BUG" | "SUGGESTION";

const categories: readonly CustomSelectOption<InquiryCategory>[] = [
  { value: "GENERAL", label: "일반 문의" },
  { value: "ACCOUNT", label: "계정 문의" },
  { value: "SUSPENSION_APPEAL", label: "이용정지 이의제기" },
  { value: "BUG", label: "오류 신고" },
  { value: "SUGGESTION", label: "서비스 제안" },
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
 * 고객문의 카테고리와 제목 및 내용을 입력받아 접수합니다.
 *
 * @author SeungHyeon.Kang
 * @return 고객문의 작성 화면
 */
function InquiryWritePage() {

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const requestedCategory = searchParams.get("category");
  const [inqrCatg, setInqrCatg] = useState<InquiryCategory>(getInitialCategory(requestedCategory));
  const [inqrTitl, setInqrTitl] = useState("");
  const [inqrCntn, setInqrCntn] = useState("");
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");
  const canSubmit = inqrTitl.trim().length > 0 && inqrCntn.trim().length > 0;

  /**
   * 문의 제목을 입력 상태에 반영합니다.
   *
   * @author SeungHyeon.Kang
   * @param event 제목 입력 이벤트
   * @return 반환값이 없습니다
   */
  const handleTitleChange = (event: ChangeEvent<HTMLInputElement>): void => {

    setInqrTitl(event.target.value);
  };

  /**
   * 문의 본문을 입력 상태에 반영합니다.
   *
   * @author SeungHyeon.Kang
   * @param event 본문 입력 이벤트
   * @return 반환값이 없습니다
   */
  const handleContentChange = (event: ChangeEvent<HTMLTextAreaElement>): void => {

    setInqrCntn(event.target.value);
  };

  /**
   * 입력값을 검증한 뒤 고객문의를 접수하고 상세 화면으로 이동합니다.
   *
   * @author SeungHyeon.Kang
   * @param event 문의 작성 폼 제출 이벤트
   * @return 반환값이 없습니다
   */
  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {

    event.preventDefault();

    if (!canSubmit) {
      setError("제목과 문의 내용을 입력해주세요.");
      return;
    }

    setIsSaving(true);
    setError("");

    try {
      const inqrNumb = await setInquiryApi({
        inqrCatg,
        inqrTitl: inqrTitl.trim(),
        inqrCntn: inqrCntn.trim(),
      });

      navigate(`/inquiry/detail/${inqrNumb}`, { replace: true });
    } catch (saveError) {
      setError(getApiErrorMessage(saveError, "고객문의를 접수하지 못했습니다."));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.formIntro} aria-labelledby="inquiry-write-title">
        <h2 id="inquiry-write-title" className={styles.sectionTitle}>무엇을 도와드릴까요?</h2>
        <p className={styles.description}>문의 내용을 자세히 남겨주시면 확인 후 답변드리겠습니다.</p>
      </section>

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.field}>
          <span className={styles.label}>
            문의 유형<span className={styles.required} aria-hidden="true">*</span>
          </span>
          <CustomSelect<InquiryCategory>
            value={inqrCatg}
            options={categories}
            ariaLabel="문의 유형 선택"
            className={styles.categorySelect}
            triggerClassName={styles.categorySelectTrigger}
            optionListClassName={styles.categoryOptionList}
            optionClassName={styles.categoryOption}
            onChange={setInqrCatg}
          />
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="inquiry-title">
            제목<span className={styles.required} aria-hidden="true">*</span>
          </label>
          <input
            id="inquiry-title"
            className={styles.input}
            maxLength={200}
            placeholder="문의 제목을 입력해주세요"
            value={inqrTitl}
            onChange={handleTitleChange}
          />
          <div className={styles.fieldFooter}>
            <span />
            <span className={styles.count}>{inqrTitl.length} / 200</span>
          </div>
        </div>

        <div className={styles.field}>
          <label className={styles.label} htmlFor="inquiry-content">
            문의 내용<span className={styles.required} aria-hidden="true">*</span>
          </label>
          <textarea
            id="inquiry-content"
            className={styles.textarea}
            maxLength={4000}
            placeholder="불편한 점이나 궁금한 내용을 구체적으로 입력해주세요"
            value={inqrCntn}
            onChange={handleContentChange}
          />
          <div className={styles.fieldFooter}>
            <p className={styles.helper}>개인정보나 비밀번호는 입력하지 마세요.</p>
            <span className={styles.count}>{inqrCntn.length} / 4,000</span>
          </div>
        </div>

        {error && <p className={styles.error} aria-live="polite">{error}</p>}

        <button
          className={styles.submitButton}
          type="submit"
          disabled={isSaving || !canSubmit}
        >
          {isSaving ? "접수 중입니다" : "문의 접수"}
        </button>
      </form>
    </div>
  );
}

export default InquiryWritePage;
