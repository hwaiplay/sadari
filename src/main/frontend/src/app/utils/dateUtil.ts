/**
 * 숫자를 두 자리 문자열로 변환한다.
 * 월과 일처럼 한 자리 숫자가 들어올 수 있는 값을 yyyy-MM-dd 형식에 맞추기 위해 앞에 0을 채운다.
 * 날짜 포맷 함수들이 동일한 자리수 규칙을 사용하도록 공통화한 함수다.
 * @Author Hanwon.Jang
 * @param value 두 자리 문자열로 변환할 숫자
 * @return 필요하면 앞에 0이 채워진 두 자리 문자열
 */
export function padTwoDigits(value: number) {
  return String(value).padStart(2, "0");
}

/**
 * Date 객체를 폼 전송과 API 처리에 사용하는 yyyy-MM-dd 문자열로 변환한다.
 * 브라우저 지역 시간 기준의 연, 월, 일을 사용해 UTC 변환으로 날짜가 하루 밀리는 문제를 피한다.
 * 날짜 선택기와 독서 기간 비교 로직이 같은 형식을 사용하도록 공통화한 함수다.
 * @Author Hanwon.Jang
 * @param date yyyy-MM-dd 문자열로 변환할 Date 객체
 * @return yyyy-MM-dd 형식의 날짜 문자열
 */
export function formatDateValue(date: Date) {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}-${padTwoDigits(date.getDate())}`;
}

/**
 * Date 객체를 월 단위 API 조회에 사용하는 yyyy-MM 문자열로 변환한다.
 * 독서 캘린더의 월별 조회 파라미터처럼 일 정보가 필요 없는 요청에서 사용한다.
 * 월은 1부터 시작하는 화면/API 형식으로 변환하고 한 자리 월에는 0을 채운다.
 * @Author Hanwon.Jang
 * @param date yyyy-MM 문자열로 변환할 Date 객체
 * @return yyyy-MM 형식의 연월 문자열
 */
export function formatYearMonthValue(date: Date) {
  return `${date.getFullYear()}-${padTwoDigits(date.getMonth() + 1)}`;
}

/**
 * yyyy-MM-dd 문자열을 로컬 시간 기준 Date 객체로 변환한다.
 * new Date 문자열 파싱 방식은 환경에 따라 UTC로 해석될 수 있으므로, 연월일을 직접 분리해 로컬 Date를 생성한다.
 * 독서 시작일과 종료일처럼 서버에서 내려온 날짜 문자열을 캘린더 셀과 비교할 때 사용한다.
 * @Author Hanwon.Jang
 * @param value yyyy-MM-dd 형식의 날짜 문자열
 * @return 로컬 시간 기준으로 생성된 Date 객체
 */
export function parseLocalDate(value: string) {
  const [year, month, date] = value.split("-").map(Number);
  return new Date(year, month - 1, date);
}

/**
 * yyyy-MM-dd 문자열을 Date 객체로 변환하고, 값이 없거나 올바르지 않으면 대체 날짜를 반환한다.
 * 날짜 선택기 초기값처럼 값이 비어 있을 수 있는 상황에서 오늘 날짜 또는 호출부가 지정한 날짜로 안전하게 초기화하기 위해 사용한다.
 * 연, 월, 일 중 하나라도 숫자로 해석되지 않으면 잘못된 날짜로 보고 fallbackDate를 그대로 반환한다.
 * @Author Hanwon.Jang
 * @param value 파싱할 yyyy-MM-dd 형식의 날짜 문자열
 * @param fallbackDate value를 날짜로 변환할 수 없을 때 사용할 대체 Date 객체
 * @return 파싱된 Date 객체 또는 대체 Date 객체
 */
export function parseDateValue(value?: string, fallbackDate = new Date()) {
  if (!value) {
    return fallbackDate;
  }

  const [year, month, day] = value.split("-").map(Number);

  if (!year || !month || !day) {
    return fallbackDate;
  }

  return new Date(year, month - 1, day);
}

/**
 * 두 Date 객체가 같은 로컬 연월일을 가리키는지 비교한다.
 * 시간, 분, 초 값은 무시하고 캘린더에서 같은 날짜 셀인지 판단하는 데 필요한 연, 월, 일만 비교한다.
 * 오늘 표시와 선택 날짜 표시처럼 Date 객체의 시간값이 달라도 같은 날짜로 처리해야 하는 UI에서 사용한다.
 * @Author Hanwon.Jang
 * @param a 비교할 첫 번째 Date 객체
 * @param b 비교할 두 번째 Date 객체
 * @return 두 Date 객체의 연월일이 같으면 true, 다르면 false
 */
export function isSameLocalDate(a: Date, b: Date) {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}
