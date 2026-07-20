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

function RatingField({ value, onChange, disabled = false }: RatingFieldProps) {
  const groupRef = useRef<HTMLDivElement | null>(null);
  const [isDragging, setIsDragging] = useState(false);

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

  const handlePointerDown = (event: PointerEvent<HTMLDivElement>) => {
    if (disabled) {
      return;
    }

    setIsDragging(true);
    event.currentTarget.setPointerCapture(event.pointerId);
    updateGradeByPointer(event.clientX);
  };

  const handlePointerMove = (event: PointerEvent<HTMLDivElement>) => {
    if (!isDragging) {
      return;
    }

    updateGradeByPointer(event.clientX);
  };

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
