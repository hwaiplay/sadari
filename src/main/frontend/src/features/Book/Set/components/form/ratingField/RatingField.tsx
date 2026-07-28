import { message } from "@/app/messages/message";
import {
  REPORT_GRADE_OPTIONS,
  REPORT_GRADE_VALUES,
} from "@/features/Book/constants/reportForm";
import { useRef, useState } from "react";
import type { PointerEvent } from "react";
import * as styles from "./RatingField.css";

type RatingFieldProps = {
  value: number;
  onChange: (value: number) => void;
  disabled?: boolean;
};

/**
 * Rating Field 화면 또는 컴포넌트를 구성한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 구성된 화면 요소
 */
function RatingField({ value, onChange, disabled = false }: RatingFieldProps) {

  const groupRef = useRef<HTMLDivElement | null>(null);
  const [isDragging, setIsDragging] = useState(false);

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

    const group = groupRef.current;

    if (!group) {
      return;
    }

    const rect = group.getBoundingClientRect();
    const position = Math.min(Math.max(clientX - rect.left, 0), rect.width);
    const rawGrade = Math.ceil(
      (position / rect.width) * REPORT_GRADE_OPTIONS.length,
    );
    const nextGrade = Math.min(
      REPORT_GRADE_OPTIONS.length,
      Math.max(0, rawGrade),
    );

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

  return (
    <div
      ref={groupRef}
      className={`${styles.starGroup} ${disabled ? styles.starGroupDisabled : ""}`}
      aria-label={message("frontend.report.gradeAria")}
      onPointerDown={handlePointerDown}
      onPointerMove={handlePointerMove}
      onPointerUp={endDrag}
      onPointerCancel={endDrag}
    >
      {value === 0 && (
        <input
          className={styles.hiddenInput}
          type="hidden"
          name="grade"
          value={REPORT_GRADE_VALUES[0]}
        />
      )}
      {REPORT_GRADE_OPTIONS.map((grade) => (
        <label
          key={grade}
          className={`${styles.starLabel} ${
            grade <= value ? styles.starActive : ""
          }`}
          htmlFor={`grade${grade}`}
        >
          {"\u2605"}
          <input
            className={styles.hiddenInput}
            type="radio"
            name="grade"
            id={`grade${grade}`}
            value={grade}
            checked={value === grade}
            disabled={disabled}
            onChange={() => {

              if (!disabled) {
                onChange(grade);
              }
            }}
          />
        </label>
      ))}
    </div>
  );
}

export default RatingField;
