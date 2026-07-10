import { message } from "@/app/messages/message";
import type { CodeDetail } from "@/features/Common/utils/codeUtil";
import * as styles from "./ColorCodeField.css";

type ColorCodeFieldProps = {
  colors: CodeDetail[];
  value: string;
  onChange: (color: string) => void;
};

/**
 * DB 공통코드로 조회한 책장 색상 코드를 swatch 라디오 목록으로 표시한다.
 * @Author Hanwon.Jang
 * @param colors BOOK_COLR 공통코드의 세부코드 목록
 * @param value 현재 선택된 책장 색상 코드
 * @param onChange 색상 코드 선택 변경 콜백
 * @return 책장 색상 선택 필드 컴포넌트
 */
function ColorCodeField({ colors, value, onChange }: ColorCodeFieldProps) {
  return (
    <div
      className={styles.colorGrid}
      aria-label={message("frontend.report.color.aria")}
    >
      {colors.map((color) => (
        <label className={styles.colorOption} key={color.comdCode}>
          <input
            className={styles.hiddenInput}
            type="radio"
            name="reportColr"
            value={color.comdCode}
            checked={value.toLowerCase() === color.comdCode.toLowerCase()}
            onChange={() => onChange(color.comdCode)}
          />
          <span
            className={styles.colorSwatch}
            style={{ backgroundColor: color.comdName }}
            title={color.comdCode}
          />
        </label>
      ))}
    </div>
  );
}

export default ColorCodeField;
