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

/**
 * 비동기 처리 중 표시할 스피너와 안내 문구를 렌더링한다.
 * @Author Hanwon.Jang
 * @param title 로딩 문구 앞부분에 표시할 제목
 * @return 로딩 화면 컴포넌트
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
