/**
 * 파싱된 팝업 콘텐츠 항목이 화면에 표시할 수 있는 문자열인지 판정한다
 *
 * @author HanWon.Jang
 * @param contentItem 판정할 JSON 배열 항목
 * @return 공백이 아닌 문자열 여부
 */
const isPopupContentText = (contentItem: unknown): contentItem is string => {
  // 문자열이면서 공백을 제외한 내용이 있어야 사용자 안내 항목으로 사용한다
  return typeof contentItem === "string" && contentItem.trim().length > 0;
};

/**
 * DB의 JSON 문자열 목록을 화면 표시 목록으로 변환하고 잘못된 값은 기본 문구로 대체한다
 *
 * @author HanWon.Jang
 * @param contentJson DB에서 조회한 JSON 문자열 목록
 * @param fallbackItems 조회 또는 파싱 실패 시 표시할 기본 문구 목록
 * @return 화면에 표시할 팝업 콘텐츠 목록
 */
export const parsePopupContentList = (
  contentJson: string | undefined,
  fallbackItems: readonly string[],
): string[] => {
  // 비어 있는 DB 콘텐츠는 현재 화면의 기본 정책 문구로 대체한다
  if (!contentJson) {
    // 화면별 기본 정책 문구를 변경할 수 없는 새 배열로 반환한다
    return [...fallbackItems];
  }

  // 잘못 저장된 JSON이 전체 화면 오류로 번지지 않도록 파싱 실패를 기본 문구로 격리한다
  try {
    // DB에 저장된 JSON 문자열을 런타임 검증 전의 값으로 파싱한다
    const parsedContent: unknown = JSON.parse(contentJson);

    // 문자열 목록이 아니거나 빈 목록이면 사용자에게 빈 도움말을 표시하지 않는다
    if (
      !Array.isArray(parsedContent)
      || parsedContent.length === 0
      || !parsedContent.every(isPopupContentText)
    ) {
      // 유효하지 않은 DB 콘텐츠 대신 화면별 기본 정책 문구를 반환한다
      return [...fallbackItems];
    }

    // 안정적인 React key를 사용할 수 있도록 같은 문구가 반복된 항목을 한 번만 유지한다
    const uniqueContent = [...new Set(parsedContent)];

    // 검증과 중복 제거가 끝난 사용자 안내 문구 목록을 반환한다
    return uniqueContent;
  }

  // JSON 문법 오류가 있는 관리자 입력은 사용자 화면에서 현재 기본 문구로 대체한다
  catch {
    // 파싱할 수 없는 DB 콘텐츠 대신 화면별 기본 정책 문구를 반환한다
    return [...fallbackItems];
  }
};
