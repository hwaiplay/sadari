import * as styles from "./Loading.css";

/**
 * fileName       : Loading
 * author         : HanWon.Jang
 * date           : 2026-04-07
 * description    : 濡쒕뵫?붾㈃ 而댄룷?뚰듃
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07       HanWon.Jang       而댄룷?뚰듃 ?섏젙
 */

/**
 * 비동기 처리 중 스피너와 안내 문구를 표시한다
 * @author HanWon.Jang
 * @param title 로딩 문구 앞에 표시할 제목
 * @return 濡쒕뵫 ?붾㈃ 而댄룷?뚰듃
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
