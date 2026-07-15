import * as styles from "./Loading.css";

/**
 * fileName       : Loading
 * author         : hanwon.Jang
 * date           : 2026-04-07
 * description    : 濡쒕뵫?붾㈃ 而댄룷?뚰듃
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07       hanwon.Jang       而댄룷?뚰듃 ?섏젙
 */

/**
 * 鍮꾨룞湲?泥섎━ 以??쒖떆???ㅽ뵾?덉? ?덈궡 臾멸뎄瑜??뚮뜑留곹븳??
 * @author Hanwon.Jang
 * @param title 濡쒕뵫 臾멸뎄 ?욌?遺꾩뿉 ?쒖떆???쒕ぉ
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
