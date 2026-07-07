import { message } from "@/app/messages/message";
import { useState } from "react";
import {
  BOOK_COLORS,
  isPresetBookColor,
} from "@/features/Book/constants/reportForm";
import * as styles from "./ColorPickerField.css";

type ColorPickerFieldProps = {
  value: string;
  onChange: (color: string) => void;
};

// 추천 색상과 사용자 지정 색상 선택을 하나의 reportColr 값으로 관리한다.
function ColorPickerField({ value, onChange }: ColorPickerFieldProps) {
  const [customColor, setCustomColor] = useState(value);
  const [isColorModalOpen, setIsColorModalOpen] = useState(false);
  const isPreset = isPresetBookColor(value);

  return (
    <>
      <input type="hidden" name="reportColr" value={value} />
      <div className={styles.colorGrid} aria-label={message("frontend.report.color.aria")}> {/* frontend.report.color.aria = 책장 색상 선택 */}
        {BOOK_COLORS.map((color) => (
          <label className={styles.colorOption} key={color}>
            <input
              className={styles.hiddenInput}
              type="radio"
              name="presetReportColr"
              value={color}
              checked={value.toLowerCase() === color.toLowerCase()}
              onChange={() => onChange(color)}
            />
            <span
              className={styles.colorSwatch}
              style={{ backgroundColor: color }}
            />
          </label>
        ))}
        <button
          className={styles.customColorButton}
          type="button"
          onClick={() => {
            // 모달을 다시 열 때 현재 선택 색상에서 이어서 조정할 수 있게 한다.
            setCustomColor(value);
            setIsColorModalOpen(true);
          }}
        >
          {!isPreset && (
            <span
              className={styles.customColorPreview}
              style={{ backgroundColor: value }}
            />
          )}
          {isPreset
            ? message("frontend.report.color.custom") // frontend.report.color.custom = 다른 색상
            : message("frontend.report.color.customWithValue", [value])} {/* frontend.report.color.customWithValue = 다른 색상 {0} */}
        </button>
      </div>

      {isColorModalOpen && (
        <div className={styles.modalBackdrop} role="presentation">
          <div
            className={styles.colorModal}
            role="dialog"
            aria-modal="true"
            aria-labelledby="customColorTitle"
          >
            <h2 className={styles.modalTitle} id="customColorTitle">
              {message("frontend.report.color.modalTitle") /* frontend.report.color.modalTitle = 책장 색상 고르기 */}
            </h2>
            <div className={styles.colorPickerRow}>
              <input
                className={styles.colorPicker}
                type="color"
                value={customColor}
                onChange={(e) => setCustomColor(e.currentTarget.value)}
                aria-label={message("frontend.report.color.aria")} // frontend.report.color.aria = 책장 색상 선택
              />
              <span className={styles.colorValue}>{customColor}</span>
            </div>
            <div className={styles.modalActions}>
              <button
                className={styles.modalButton}
                type="button"
                onClick={() => setIsColorModalOpen(false)}
              >
                {message("frontend.common.cancel") /* frontend.common.cancel = 취소 */}
              </button>
              <button
                className={styles.modalPrimaryButton}
                type="button"
                onClick={() => {
                  // 선택 버튼을 눌렀을 때만 폼에 제출될 색상값을 확정한다.
                  onChange(customColor);
                  setIsColorModalOpen(false);
                }}
              >
                {message("frontend.book.search.select") /* frontend.book.search.select = 선택 */}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ColorPickerField;
