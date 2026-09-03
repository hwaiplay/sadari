import type { ReactNode } from "react";
import * as styles from "./SearchMatchText.css";

type SearchMatchTextProps = {
  text?: string;
  keyword: string;
};

/**
 * 검색 결과 텍스트에서 현재 검색어와 일치하는 모든 부분을 브랜드 색상으로 강조함
 *
 * @author HanWon.Jang
 * @param text 검색 결과에 표시할 원문
 * @param keyword 현재 입력된 검색어
 * @return 검색어 일치 부분이 강조된 텍스트 요소
 */
const SearchMatchText = ({ text, keyword }: SearchMatchTextProps): ReactNode => {
  // 화면에 표시 중인 검색어의 양끝 공백을 제거함
  const normalizedKeyword = keyword.trim();

  // 원문이나 검색어가 없으면 강조 요소를 만들지 않음
  if (!text || !normalizedKeyword) {
    // 서버 원문을 변경하지 않고 반환함
    return text;
  }

  // 영문 검색도 대소문자와 관계없이 강조할 수 있도록 비교값을 소문자로 변환함
  const normalizedText = text.toLocaleLowerCase();
  // 표시 원문은 유지하고 위치 판정에만 소문자 검색어를 사용함
  const comparableKeyword = normalizedKeyword.toLocaleLowerCase();
  // 원문과 강조 요소를 표시 순서대로 담을 목록을 생성함
  const highlightedParts: ReactNode[] = [];
  // 첫 번째 일치 부분 전까지의 원문 시작 위치를 관리함
  let currentIndex = 0;
  // 현재 검색어가 처음 등장하는 원문 위치를 조회함
  let matchIndex = normalizedText.indexOf(comparableKeyword);

  // 원문에 반복되는 검색어도 빠짐없이 같은 색상으로 강조함
  while (matchIndex >= 0) {
    // 일치 부분 앞의 원문을 기존 색상으로 유지함
    highlightedParts.push(text.slice(currentIndex, matchIndex));
    // 일치 부분만 브랜드 연두색을 적용한 의미 요소로 추가함
    highlightedParts.push(
      <mark className={styles.match} key={matchIndex}>
        {text.slice(matchIndex, matchIndex + normalizedKeyword.length)}
      </mark>,
    );
    // 다음 검색은 현재 일치 부분 뒤에서 시작하도록 위치를 이동함
    currentIndex = matchIndex + normalizedKeyword.length;
    // 같은 원문에 남아 있는 다음 일치 위치를 조회함
    matchIndex = normalizedText.indexOf(comparableKeyword, currentIndex);
  }

  // 마지막 일치 부분 뒤의 원문을 기존 색상으로 유지함
  highlightedParts.push(text.slice(currentIndex));
  // 원문 순서와 접근 가능한 텍스트를 유지한 강조 결과를 반환함
  return highlightedParts;
};

export default SearchMatchText;
