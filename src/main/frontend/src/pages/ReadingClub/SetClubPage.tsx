import { message } from "@/app/messages/message";
import { ActionButton } from "@/components/Button/ActionButton";
import Skeleton from "@/components/Skeleton/Skeleton";
import InterestSelectModal from "@/features/ReadingClub/components/InterestSelectModal";
import { createPortal } from "react-dom";
import {
  type SetClubPageMode,
  useSetClubPage,
} from "@/features/ReadingClub/hooks/useSetClubPage.ts";
import * as styles from "./SetClubPage.css";

const SET_CLUB_FORM_ID = "set-club-form";

type SetClubPageProps = {
  mode?: SetClubPageMode;
};

/**
 * 모임 공개 범위, 가입 방식, 카테고리와 정원을 입력하는 저장 화면을 구성한다
 *
 * @author Hanwon.Jang
 * @param props 모임 폼 동작 모드
 * @return 모임 만들기 또는 수정 화면
 */
export default function SetClubPage({ mode = "create" }: SetClubPageProps) {
  // 모임 저장 화면 로직 훅에서 입력 상태와 사용자 이벤트 처리 함수를 가져온다
  const {
    addQuestion,
    catalog,
    closeCategoryModal,
    form,
    handleDescriptionChange,
    handleNameChange,
    isCategoryOpen,
    isEditMode,
    isLoading,
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
  } = useSetClubPage(mode);

  // 수정할 모임 정보를 조회하는 동안 폼 크기의 스켈레톤을 표시한다
  if (isLoading) {
    return (
      <main
        className={styles.page}
        aria-busy="true"
        aria-label={message("frontend.readingClub.set.loading")}
      >
        <Skeleton width="100%" height={620} borderRadius={20} />
      </main>
    );
  }

  // 선택 카드와 삭제 버튼의 공통 상태 스타일을 적용한 모임 생성 폼을 반환한다
  return (
    <>
      <div className={styles.page}>
        <form id={SET_CLUB_FORM_ID} className={styles.form} onSubmit={(event) => void submitForm(event)}>
          {/* 모임명 입력 영역 */}
          <label className={styles.field}>
            <span className={styles.label}>{message("frontend.readingClub.set.nameLabel")}</span>
            <input
              placeholder={message("frontend.readingClub.set.namePlaceholder")}
              className={styles.input}
              maxLength={100}
              value={form.clubName}
              onChange={handleNameChange}
            />
          </label>

          {/* 모임 소개 입력 영역 */}
          <label className={styles.field}>
            <span className={styles.label}>{message("frontend.readingClub.set.descriptionLabel")}</span>
            <textarea
              placeholder={message("frontend.readingClub.set.descriptionPlaceholder")}
              className={styles.textarea}
              maxLength={2000}
              value={form.clubCntn}
              onChange={handleDescriptionChange}
            />
          </label>

          {/* 모임 카테고리 선택 영역 */}
          <section className={styles.field}>
            <h2 className={styles.label}>{message("frontend.readingClub.set.categoryLabel")}</h2>
            <div className={styles.chipsContainer}>
              {/* 관심 카테고리 */}
              <div className={styles.chips}>
                {selectedCategories.map((interest) => (
                  <div className={styles.chip} key={interest.intrCode}>
                    {interest.intrName}
                    <button
                      className={styles.chipDeleteBtn}
                      type="button"
                      aria-label={message("frontend.readingClub.set.removeCategory", [interest.intrName])}
                      onClick={() => removeCategory(interest.intrCode)}
                    >
                      <svg width="8" height="8" viewBox="0 0 8 8" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M7.28018 1.21974L4.7548 3.74512L7.28018 6.2705C7.41414 6.40445 7.48939 6.58613 7.48939 6.77557C7.48939 6.96501 7.41414 7.1467 7.28018 7.28065C7.14623 7.4146 6.96455 7.48986 6.77511 7.48986C6.58567 7.48986 6.40399 7.4146 6.27003 7.28065L3.74465 4.75527L1.21927 7.28065C1.08532 7.41461 0.903634 7.48986 0.714194 7.48986C0.524753 7.48986 0.343072 7.4146 0.209117 7.28065C0.0751627 7.1467 -9.19682e-05 6.96501 -9.19577e-05 6.77557C-9.19577e-05 6.58613 0.0751628 6.40445 0.209117 6.2705L2.7345 3.74512L0.209118 1.21974C0.0751631 1.08578 -9.18734e-05 0.9041 -9.18734e-05 0.71466C-9.18839e-05 0.525219 0.075163 0.343538 0.209117 0.209584C0.343072 0.0756292 0.524753 0.000373966 0.714194 0.000373976C0.903634 0.000373976 1.08532 0.075629 1.21927 0.209584L3.74465 2.73496L6.27003 0.209583C6.40399 0.0756282 6.58567 0.000373807 6.77511 0.00037347C6.96455 0.000373133 7.14623 0.0756289 7.28018 0.209583C7.41414 0.343538 7.48939 0.525218 7.48939 0.714659C7.48939 0.9041 7.41414 1.08578 7.28018 1.21974Z" fill="#2F8F64"/>
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
              {/* 선택하기 버튼 */}
              <button
                className={styles.button}
                type="button"
                onClick={openCategoryModal}>
                <svg width="10" height="10" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M9.28571 5.71429H5.71429V9.28571C5.71429 9.47515 5.63903 9.65684 5.50508 9.79079C5.37112 9.92475 5.18944 10 5 10C4.81056 10 4.62888 9.92475 4.49492 9.79079C4.36097 9.65684 4.28571 9.47515 4.28571 9.28571V5.71429H0.714286C0.524845 5.71429 0.343164 5.63903 0.20921 5.50508C0.075255 5.37112 0 5.18944 0 5C0 4.81056 0.075255 4.62888 0.20921 4.49492C0.343164 4.36097 0.524845 4.28571 0.714286 4.28571H4.28571V0.714286C4.28571 0.524845 4.36097 0.343164 4.49492 0.209209C4.62888 0.0752547 4.81056 0 5 0C5.18944 0 5.37112 0.0752547 5.50508 0.209209C5.63903 0.343164 5.71429 0.524845 5.71429 0.714286V4.28571H9.28571C9.47515 4.28571 9.65684 4.36097 9.79079 4.49492C9.92475 4.62888 10 4.81056 10 5C10 5.18944 9.92475 5.37112 9.79079 5.50508C9.65684 5.63903 9.47515 5.71429 9.28571 5.71429Z"
                    fill="#C1C1C1"/>
                </svg>
                <span>{message("frontend.readingClub.set.selectCategory")}</span>
            </button>
            </div>
            </section>

          {/* 모임 공개 범위 선택 영역 */}
          <section className={styles.field}>
            <h2 className={styles.label}>{message("frontend.readingClub.set.visibilityLabel")}</h2>
            <div className={styles.optionGrid}>
              <button
                className={styles.option}
                data-selected={form.clubVisb === "PUBLIC"}
                type="button"
                onClick={() => selectVisibility("PUBLIC")}
              >
                <span className={styles.optionTitle}>{message("frontend.readingClub.set.publicTitle")}</span>
                <span className={styles.optionDescription}>{message("frontend.readingClub.set.publicDescription")}</span>
              </button>
              <button
                className={styles.option}
                data-selected={form.clubVisb === "PRIVATE"}
                type="button"
                onClick={() => selectVisibility("PRIVATE")}
              >
                <span className={styles.optionTitle}>{message("frontend.readingClub.set.privateTitle")}</span>
                <span className={styles.optionDescription}>{message("frontend.readingClub.set.privateDescription")}</span>
              </button>
            </div>
          </section>

          {/* 모임 가입 방식 선택 영역 */}
          <section className={styles.field}>
            <h2 className={styles.label}>{message("frontend.readingClub.set.joinTypeLabel")}</h2>
            <div className={styles.optionGrid}>

              <button
                className={styles.option}
                data-selected={form.joinType === "OPEN" || form.clubVisb === "PRIVATE"}
                type="button"
                onClick={selectOpenJoin}
              >
                <span className={styles.optionTitle}>{message("frontend.readingClub.set.openTitle")}</span>
                <span className={styles.optionDescription}>{message("frontend.readingClub.set.openDescription")}</span>
              </button>

              {form.clubVisb === "PUBLIC" ? (
                <button
                  className={styles.option}
                  data-selected={form.joinType === "APPROVAL"}
                  type="button"
                  onClick={selectApprovalJoin}
                >
                  <span className={styles.optionTitle}>{message("frontend.readingClub.set.approvalTitle")}</span>
                  <span className={styles.optionDescription}>{message("frontend.readingClub.set.approvalDescription")}</span>
                </button>
              ): null}
            </div>
          </section>

          {/* 승인 가입 질문 관리 영역 */}
          {form.joinType === "APPROVAL" && (
            <section className={styles.field}>
              <h2 className={styles.label}>
                {message("frontend.readingClub.set.questionLabel", [form.questionList.length])}
              </h2>
              <p className={styles.description}>
                {message("frontend.readingClub.set.questionDescription")}
              </p>
              {form.questionList.map((question, index) => (
                <div className={styles.questionRow} key={`question-${index + 1}`}>
                  <div className={styles.questionSubjectContainer}>
                    <h3 className={styles.questionSubjectLabel}>{message(("frontend.readingClub.set.questionSubjectLabel"), [index + 1])}</h3>
                    {/* 삭제버튼 (1번 질문이 아닐 때만 노출) */}
                    {index !== 0 ? (
                      <>
                        <button
                          className={styles.buttonDanger}
                          type="button"
                          onClick={() => removeQuestion(index)}
                        >
                          <svg width="8" height="2" viewBox="0 0 8 2" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M0.75 0.75H6.75" stroke="#FF3747" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                          </svg>

                          {message("frontend.readingClub.set.deleteQuestion")}
                        </button>
                      </>
                    ) : null
                    }
                  </div>
                  <input
                    className={styles.input}
                    maxLength={500}
                    value={question}
                    placeholder={message("frontend.readingClub.set.questionPlaceholder", [index + 1])}
                    onChange={(event) => updateQuestion(index, event.target.value)}
                  />
                </div>
              ))}
              <button
                className={styles.button}
                type="button"
                disabled={form.questionList.length >= 5}
                onClick={addQuestion}
              >
                <svg width="10" height="10" viewBox="0 0 10 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9.28571 5.71429H5.71429V9.28571C5.71429 9.47515 5.63903 9.65684 5.50508 9.79079C5.37112 9.92475 5.18944 10 5 10C4.81056 10 4.62888 9.92475 4.49492 9.79079C4.36097 9.65684 4.28571 9.47515 4.28571 9.28571V5.71429H0.714286C0.524845 5.71429 0.343164 5.63903 0.20921 5.50508C0.075255 5.37112 0 5.18944 0 5C0 4.81056 0.075255 4.62888 0.20921 4.49492C0.343164 4.36097 0.524845 4.28571 0.714286 4.28571H4.28571V0.714286C4.28571 0.524845 4.36097 0.343164 4.49492 0.209209C4.62888 0.0752547 4.81056 0 5 0C5.18944 0 5.37112 0.0752547 5.50508 0.209209C5.63903 0.343164 5.71429 0.524845 5.71429 0.714286V4.28571H9.28571C9.47515 4.28571 9.65684 4.36097 9.79079 4.49492C9.92475 4.62888 10 4.81056 10 5C10 5.18944 9.92475 5.37112 9.79079 5.50508C9.65684 5.63903 9.47515 5.71429 9.28571 5.71429Z" fill="#C1C1C1"/>
                </svg>

                {message("frontend.readingClub.set.addQuestion")}
              </button>
            </section>
          )}

          {/* 모임 정원 설정 영역 */}
          <section className={styles.field}>
            <h2 className={styles.label}>{message("frontend.readingClub.set.capacityLabel")}</h2>
            <div className={styles.stepper}>
              <button
                className={styles.stepperButton}
                type="button"
                onClick={() => setCapacity(form.maxxMemb - 1)}
              >
                <svg width="14" height="2" viewBox="0 0 14 2" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M0.850098 0.849609H12.8501" stroke="#293038" strokeWidth="1.7" strokeLinecap="round"/>
                </svg>
              </button>
              <input
                className={styles.stepperInput}
                type="number"
                min={2}
                max={100}
                value={form.maxxMemb}
                onChange={(event) => setCapacity(Number(event.target.value))}
              />
              <button
                className={styles.stepperButton}
                type="button"
                onClick={() => setCapacity(form.maxxMemb + 1)}
              >
                <svg width="14" height="13" viewBox="0 0 14 13" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M13 7.42857H8V12.0714C8 12.3177 7.89464 12.5539 7.70711 12.728C7.51957 12.9022 7.26522 13 7 13C6.73478 13 6.48043 12.9022 6.29289 12.728C6.10536 12.5539 6 12.3177 6 12.0714V7.42857H1C0.734784 7.42857 0.48043 7.33074 0.292893 7.1566C0.105357 6.98246 0 6.74627 0 6.5C0 6.25373 0.105357 6.01754 0.292893 5.8434C0.48043 5.66926 0.734784 5.57143 1 5.57143H6V0.928571C6 0.682299 6.10536 0.446113 6.29289 0.271972C6.48043 0.0978311 6.73478 0 7 0C7.26522 0 7.51957 0.0978311 7.70711 0.271972C7.89464 0.446113 8 0.682299 8 0.928571V5.57143H13C13.2652 5.57143 13.5196 5.66926 13.7071 5.8434C13.8946 6.01754 14 6.25373 14 6.5C14 6.74627 13.8946 6.98246 13.7071 7.1566C13.5196 7.33074 13.2652 7.42857 13 7.42857Z"
                    fill="#293038"/>
                </svg>
              </button>
            </div>
          </section>
        </form>

        {/* 카테고리 선택 팝업 영역 */}
        {isCategoryOpen && (
          <InterestSelectModal
            catalog={catalog}
            initialCodes={form.categoryList}
            minimum={1}
            maximum={3}
            onClose={closeCategoryModal}
            onSave={saveCategories}
          />
        )}

        {/* 모임 저장 버튼 */}
        <ActionButton
          type="submit"
          form={SET_CLUB_FORM_ID}
          variant="primary"
          size="lg"
          width="full"
          disabled={isSaving}
        >
          {isSaving
            ? (isEditMode
              ? message("frontend.readingClub.set.editSaving")
              : message("frontend.readingClub.set.saving"))
            : (isEditMode
              ? message("frontend.readingClub.set.editSubmit")
              : message("frontend.readingClub.set.submit"))}
        </ActionButton>
      </div>
    </>
  );
}
