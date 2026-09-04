import * as styles from "./Loading.css";
import { message } from "@/app/messages/message";
import { useUserMenuQuery } from "@/features/Menu/hooks/useUserMenuQuery";
import { useLocation } from "react-router-dom";

/**
 * fileName       : Loading
 * author         : HanWon.Jang
 * date           : 2026-04-07
 * description    : 비동기 처리 상태를 스피너와 안내 문구로 표시함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-07        HanWon.Jang        컴포넌트 수정
 * 2026-08-03        HanWon.Jang        인라인 로딩 화면 지원 및 주석 복구
 * 2026-08-15        SeungHyeon.Kang    모달용 소형 링 로딩 상태 추가
 */

type LoadingProps = {
  title?: string;
  isFullScreen?: boolean;
  isCompact?: boolean;
  isInline?: boolean;
};

/**
 * 별도 문구 또는 현재 메뉴명으로 최종 로딩 제목을 결정함
 *
 * @author SeungHyeon.Kang
 * @param title 호출 화면이 지정한 별도 로딩 문구
 * @param menuName TM_URMENU에서 조회한 현재 메뉴명
 * @return 화면에 표시할 로딩 제목
 */
function resolveLoadingTitle(title?: string, menuName?: string) {

  // 로그인이나 등록처럼 호출 화면이 작업 문구를 지정하면 메뉴명보다 우선함
  if (title !== undefined) {
    // 호출 화면이 지정한 작업 문구를 그대로 반환함
    return title;
  }

  // 현재 경로에 메뉴명이 등록되어 있으면 메뉴 대상을 포함한 조회 문구를 사용함
  if (menuName) {
    // "{메뉴명} 조회 중"
    const menuLoadingTitle = message("frontend.common.loadingMenu", [menuName]);

    // TM_URMENU의 메뉴명을 포함한 조회 문구를 반환함
    return menuLoadingTitle;
  }

  // "목록 조회 중"
  const fallbackLoadingTitle = message("frontend.common.loadingList");

  // 등록된 메뉴명이 없거나 메뉴 조회가 실패하면 공통 목록 문구를 반환함
  return fallbackLoadingTitle;
}

/**
 * 비동기 처리 중 스피너와 안내 문구를 표시함
 *
 * @author SeungHyeon.Kang
 * @param title 메뉴명 대신 사용할 별도 로딩 문구
 * @param isFullScreen 화면 전체 높이 사용 여부
 * @param isCompact 문구 없이 소형 회전 링만 표시할지 여부
 * @param isInline 메시지처럼 작은 인라인 영역에 회전 링을 표시할지 여부
 * @return 비동기 처리 상태를 안내하는 로딩 화면
 */
const Loading = ({
  title,
  isFullScreen = true,
  isCompact = false,
  isInline = false,
}: LoadingProps) => {

  // 현재 브라우저 경로에 대응하는 사용자 메뉴를 조회하기 위해 위치 정보를 가져옴
  const location = useLocation();
  const shouldUseMenuTitle = !isCompact && title === undefined;
  // 헤더와 동일한 Query Key로 현재 경로의 메뉴 조회 결과를 공유함
  const { data: userMenuData } = useUserMenuQuery(
    location.pathname,
    shouldUseMenuTitle,
  );
  // 공백만 저장된 메뉴명은 미등록 상태로 처리하기 위해 표시값을 정리함
  const menuName = userMenuData?.currentMenu?.menuName?.trim();
  // 호출 화면의 작업 문구와 현재 메뉴명 우선순위에 따라 최종 제목을 결정함
  const loadingTitle = resolveLoadingTitle(title, menuName);

  // 모달과 페이지 및 내부 영역이 각각 필요한 높이 스타일을 사용하도록 구분함
  const containerClassName = isCompact
    ? isInline
      ? styles.inlineCompactContainer
      : styles.compactContainer
    : isFullScreen
      ? styles.container
      : styles.inlineContainer;

  // 스피너와 전달받은 로딩 안내 문구를 표시하는 화면을 반환함
  return (
    <div
      className={containerClassName}
      role="status"
      aria-label={isCompact ? `${loadingTitle}...` : undefined}
    >
      {/* 모달에서는 공통 회전 링을 감싼 영역만 축소하여 같은 애니메이션을 유지함 */}
      <div className={isCompact && !isInline ? styles.compactSpinner : undefined}>
        <div className={isInline ? styles.inlineSpinner : styles.spinner} aria-hidden="true" />
      </div>
      {!isCompact && (
        /* 현재 경로에 등록된 메뉴명으로 조회 상태를 안내하고 메뉴가 없으면 목록 문구를 사용함 */
        <p className={styles.text}>{loadingTitle}...</p>
      )}
    </div>
  );
};

export default Loading;
