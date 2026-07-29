/**
 * 독후감 별점을 0.5점 단위의 클릭과 드래그 및 키보드 입력으로 구성한다
 *
 * @author HanWon.Jang
 */
import { message } from "@/app/messages/message";
import {
  REPORT_GRADE_OPTIONS,
} from "@/features/Book/constants/reportForm";
import { useRef, useState } from "react";
import type { CSSProperties, KeyboardEvent, PointerEvent } from "react";
import * as styles from "./RatingField.css";

type RatingFieldProps = {
  value: number;
  onChange: (value: number) => void;
  disabled?: boolean;
};

/**
 * 숫자 점수와 반쪽 채움이 가능한 다섯 개 별점 입력을 구성한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
 */
function RatingField({ value, onChange, disabled = false }: RatingFieldProps) {

  const starRowRef = useRef<HTMLDivElement | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  // "평점 선택"
  const gradeAriaLabel = message("frontend.report.gradeAria");

  /**
   * update Grade By Pointer 정보를 수정한다
   *
   * @author HanWon.Jang
   * @param clientX client X 입력값
   * @return 반환값이 없다
   */
  const updateGradeByPointer = (clientX: number) => {

    if (disabled) {
      return;
    }

    const starRow = starRowRef.current;

    // 별점 입력 영역이 아직 렌더링되지 않았으면 포인터 위치를 계산하지 않는다
    if (!starRow) {
      return;
    }

    const rect = starRow.getBoundingClientRect();
    const position = Math.min(Math.max(clientX - rect.left, 0), rect.width);
    const rawGrade = (position / rect.width) * REPORT_GRADE_OPTIONS.length;
    const nextGrade = Math.min(
      REPORT_GRADE_OPTIONS.length,
      Math.max(0, Math.round(rawGrade * 2) / 2),
    );

    // 포인터 위치를 가장 가까운 0.5점 단위 별점으로 반영한다
    onChange(nextGrade);
  };

  /**
   * handle Pointer Down 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param event event 입력값
   * @return 반환값이 없다
   */
  const handlePointerDown = (event: PointerEvent<HTMLDivElement>) => {

    if (disabled) {
      return;
    }

    setIsDragging(true);
    event.currentTarget.setPointerCapture(event.pointerId);
    updateGradeByPointer(event.clientX);
  };

  /**
   * handle Pointer Move 사용자 동작을 처리한다
   *
   * @author HanWon.Jang
   * @param event event 입력값
   * @return 반환값이 없다
   */
  const handlePointerMove = (event: PointerEvent<HTMLDivElement>) => {

    if (!isDragging) {
      return;
    }

    updateGradeByPointer(event.clientX);
  };

  /**
   * end Drag 기능을 처리한다
   *
   * @author HanWon.Jang
   * @param event event 입력값
   * @return 처리 결과
   */
  const endDrag = (event: PointerEvent<HTMLDivElement>) => {

    setIsDragging(false);

    if (event.currentTarget.hasPointerCapture(event.pointerId)) {
      event.currentTarget.releasePointerCapture(event.pointerId);
    }
  };

  /**
   * 키보드 방향키로 별점을 0.5점씩 조정한다
   *
   * @author HanWon.Jang
   * @param event 별점 입력 영역의 키보드 이벤트
   * @return 반환값이 없다
   */
  function handleKeyDown(event: KeyboardEvent<HTMLDivElement>) {

    // 비활성화 상태에서는 키보드 입력으로 별점을 변경하지 않는다
    if (disabled) {
      return;
    }

    // 오른쪽과 위쪽 방향키는 현재 별점을 0.5점 높인다
    if (event.key === "ArrowRight" || event.key === "ArrowUp") {
      event.preventDefault();
      // 최대 5점을 넘지 않는 다음 별점을 반영한다
      onChange(Math.min(5, value + 0.5));
      return;
    }

    // 왼쪽과 아래쪽 방향키는 현재 별점을 0.5점 낮춘다
    if (event.key === "ArrowLeft" || event.key === "ArrowDown") {
      event.preventDefault();
      // 최소 0점보다 낮아지지 않는 다음 별점을 반영한다
      onChange(Math.max(0, value - 0.5));
    }
  }

  /**
   * 별점 값에 따라 비어 있는 별 위에 채워진 별의 너비를 계산한다
   *
   * @author HanWon.Jang
   * @param grade 왼쪽부터 시작하는 별의 순번
   * @return 0퍼센트와 50퍼센트 및 100퍼센트 중 하나가 적용된 별 요소
   */
  function renderStar(grade: number) {

    const fillPercentage = Math.min(
      100,
      Math.max(0, (value - (grade - 1)) * 100),
    );
    const fillStyle = {
      "--rating-fill-width": `${fillPercentage}%`,
    } as CSSProperties;

    // 현재 점수에 맞춰 비어 있는 별과 채워진 별을 겹친 요소를 반환한다
    return (
      <span className={styles.star} key={grade} aria-hidden="true">
        <span className={styles.starEmpty}>
          <svg className={styles.starIcon} viewBox="0 0 24 24">
            <path d="m12 3.6 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.6Z" />
          </svg>
        </span>
        <span className={styles.starFill} style={fillStyle}>
          <svg className={styles.starIcon} viewBox="0 0 24 24">
            <path d="m12 3.6 2.55 5.17 5.7.83-4.12 4.02.97 5.68L12 16.52 6.9 19.2l.97-5.68L3.75 9.5l5.7-.83L12 3.6Z" />
          </svg>
        </span>
      </span>
    );
  }

  // 숫자 점수와 0.5점 단위로 채워지는 별점 입력 영역을 반환한다
  return (
    <div
      className={`${styles.starGroup} ${disabled ? styles.starGroupDisabled : ""}`}
      aria-label={gradeAriaLabel}
    >
      <div
        ref={starRowRef}
        className={styles.starRow}
        role="slider"
        tabIndex={disabled ? -1 : 0}
        aria-valuemin={0}
        aria-valuemax={5}
        aria-valuenow={value}
        aria-valuetext={String(value)}
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={endDrag}
        onPointerCancel={endDrag}
        onKeyDown={handleKeyDown}
      >
        {REPORT_GRADE_OPTIONS.map(renderStar)}
      </div>
      <input className={styles.hiddenInput} type="hidden" name="grade" value={value} />
    </div>
  );
}

export default RatingField;
