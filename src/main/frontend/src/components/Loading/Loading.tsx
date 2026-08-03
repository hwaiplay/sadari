import * as styles from "./Loading.css";

/**
 * fileName       : Loading
 * author         : HanWon.Jang
 * date           : 2026-04-07
 * description    : 비동기 처리 상태를 스피너와 안내 문구로 표시한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07        HanWon.Jang        컴포넌트 수정
 * 2026-08-03        HanWon.Jang        인라인 로딩 화면 지원 및 주석 복구
 */

type LoadingProps = {
  title: string;
  isFullScreen?: boolean;
};

/**
 * 비동기 처리 중 스피너와 안내 문구를 표시한다
 *
 * @author HanWon.Jang
 * @param title 로딩 문구 앞에 표시할 제목
 * @param isFullScreen 화면 전체 높이 사용 여부
 * @return 비동기 처리 상태를 안내하는 로딩 화면
 */
const Loading = ({ title, isFullScreen = true }: LoadingProps) => {
  // 페이지와 내부 영역에서 같은 로딩 화면을 사용할 수 있도록 높이 스타일을 구분한다
  const containerClassName = isFullScreen
    ? styles.container
    : styles.inlineContainer;

  // 스피너와 전달받은 로딩 안내 문구를 표시하는 화면을 반환한다
  return (
    <div className={containerClassName}>
      <div className={styles.spinner} />
      <p className={styles.text}>{title}...</p>
    </div>
  );
};

export default Loading;
