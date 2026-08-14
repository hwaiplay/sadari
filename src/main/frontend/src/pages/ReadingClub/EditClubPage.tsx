import SetClubPage from "./SetClubPage";

/**
 * 기존 모임 정보를 불러와 수정 폼을 표시한다.
 *
 * @author SeungHyeon.Kang
 * @return 모임 수정 화면
 */
export default function EditClubPage() {
  // 공통 모임 폼을 수정 모드로 구성한다
  return <SetClubPage mode="edit" />;
}
