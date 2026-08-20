import * as styles from "./NoticeCategoryBadge.css";

type NoticeCategoryBadgeProps = {
  categoryName: string;
};

/**
 * 공지사항 카테고리명을 공통 배지 스타일로 표시한다
 *
 * @author SeungHyeon.Kang
 * @param props 공지사항 카테고리명 속성
 * @return 공지사항 카테고리 배지
 */
export function NoticeCategoryBadge({ categoryName }: NoticeCategoryBadgeProps) {

  // 서버가 제공한 공지사항 카테고리명을 공통 배지로 반환한다
  return <span className={styles.badge}>{categoryName}</span>;
}
