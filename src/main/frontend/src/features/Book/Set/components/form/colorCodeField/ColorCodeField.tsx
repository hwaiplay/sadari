import { message } from "@/app/messages/message";
import type { CodeDetail } from "@/features/Common/utils/codeUtil";
import * as styles from "./ColorCodeField.css";

type ColorCodeFieldProps = {
  colors: CodeDetail[];
  value: string;
  onChange: (color: string) => void;
};

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
