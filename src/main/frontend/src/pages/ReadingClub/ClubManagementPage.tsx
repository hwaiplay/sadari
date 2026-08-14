/**
 * fileName       : ClubManagementPage
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 모임장 전용 관리 메뉴 화면을 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */
import { message } from "@/app/messages/message";
import { useNavigate, useParams } from "react-router-dom";
import * as styles from "./ClubManagementPage.css";

/**
 * 모임장에게 멤버 관리와 독서 관리 진입 메뉴를 표시한다
 *
 * @author Hanwon.Jang
 * @return 모임 관리 메뉴 화면
 */
export default function ClubManagementPage() {
  const navigate = useNavigate();
  const { clubNumb } = useParams();

  /**
   * 현재 모임의 멤버와 가입 신청 관리 화면으로 이동한다
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  const handleMemberManagement = (): void => {
    // 현재 모임 번호를 유지하여 멤버와 가입 신청 관리 화면으로 이동한다
    navigate(`/reading-clubs/${clubNumb}/manage/members`);
  };

  // 모임장 전용 관리 메뉴 목록을 반환한다
  return (
    <main className={styles.page}>
      {/* 모임장 전용 관리 메뉴 목록 영역 */}
      <nav className={styles.menuList} aria-label={message("frontend.readingClub.management.title")}>
        {/* 멤버 관리 메뉴 항목 영역 */}
        <button className={styles.menuRow} type="button" onClick={handleMemberManagement}>
          <span>
            {/* "멤버 관리" */}
            {message("frontend.readingClub.management.members")}
          </span>
          <img
            className={styles.chevronIcon}
            src="/img/icons/icon-chevron-right.svg"
            alt="arrow"
          />
        </button>

        {/* 독서 관리 메뉴 항목 영역 */}
        <div className={styles.menuRow}>
          <span>
            {/* "독서 관리" */}
            {message("frontend.readingClub.management.reading")}
          </span>
          <img
            className={styles.chevronIcon}
            src="/img/icons/icon-chevron-right.svg"
            alt="arrow"
          />
        </div>
      </nav>
    </main>
  );
}
