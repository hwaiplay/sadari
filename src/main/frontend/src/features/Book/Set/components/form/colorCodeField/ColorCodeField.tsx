import { message } from "@/app/messages/message";
import type { CodeDetail } from "@/features/Common/utils/codeUtil";
import * as styles from "./ColorCodeField.css";

type ColorCodeFieldProps = {
  colors: CodeDetail[];
  value: string;
  onChange: (color: string) => void;
};

/**
 * DB 怨듯넻肄붾뱶濡?議고쉶??梨낆옣 ?됱긽 肄붾뱶瑜?swatch ?쇰뵒??紐⑸줉?쇰줈 ?쒖떆?쒕떎.
 * @author Hanwon.Jang
 * @param colors BOOK_COLR 怨듯넻肄붾뱶???몃?肄붾뱶 紐⑸줉
 * @param value ?꾩옱 ?좏깮??梨낆옣 ?됱긽 肄붾뱶
 * @param onChange ?됱긽 肄붾뱶 ?좏깮 蹂寃?肄쒕갚
 * @return 梨낆옣 ?됱긽 ?좏깮 ?꾨뱶 而댄룷?뚰듃
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
