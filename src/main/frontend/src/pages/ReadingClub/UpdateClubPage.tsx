import SetClubPage from "./SetClubPage";

/**
 * fileName       : UpdateClubPage
 * author         : Hanwon.Jang
 * date           : 2026-08-22
 * description    : 모임 수정 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        Hanwon.Jang    주석 추가
 * 2026-08-27        Hanwon.Jang    파일명 변경
 */

/**
 * 기존 모임 정보를 불러와 수정 폼을 표시한다.
 *
 * @author SeungHyeon.Kang
 * @return 모임 수정 화면
 */
export default function UpdateClubPage() {
  // 공통 모임 폼을 수정 모드로 구성한다
  return <SetClubPage mode="edit" />;
}
