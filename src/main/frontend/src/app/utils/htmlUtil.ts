/**
 * 외부 API 응답이나 사용자 입력 문자열에 포함된 HTML 태그를 제거한다.
 * 네이버 책 검색 API처럼 제목, 저자, 설명에 강조 태그가 포함되어 내려오는 데이터를 화면에 일반 텍스트로 표시하기 위해 사용한다.
 * 값이 없으면 빈 문자열을 반환해 호출부에서 null 또는 undefined 처리를 반복하지 않도록 한다.
 * @Author Hanwon.Jang
 * @param value HTML 태그가 포함되어 있을 수 있는 원본 문자열
 * @return HTML 태그가 제거된 화면 표시용 문자열
 */
export function stripHtmlTags(value?: string) {
  return value?.replace(/<[^>]*>/g, "") ?? "";
}
