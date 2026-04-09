import * as styles from "./Loading.css";

/**
 * fileName       : Loading
 * author         : hanwon.Jang
 * date           : 2026-04-07
 * description    : 로딩화면 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07       hanwon.Jang       컴포넌트 수정
 */

const Loading = ({ title }: { title: String }) => {
  return (
    <div className={styles.container}>
      <div className={styles.spinner} />
      <p className={styles.text}>{title}...</p>
    </div>
  );
};

export default Loading;
