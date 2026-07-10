import { Link } from "react-router-dom";

/**
 * fileName       : LinkButton
 * author         : Hanwon.Jang
 * date           : 2026-05-06
 * description    : 단순 link 이동 버튼 컴포넌트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-06       Hanwon.Jang       최초 생성
 */

interface LinkButtonProps {
  // url
  link: string;
  // style class
  className?: string;
  // 텍스트 or 아이콘 등..
  children: React.ReactNode;
}

/**
 * 지정한 라우트로 이동하는 링크형 버튼을 렌더링한다.
 * @Author Hanwon.Jang
 * @param link 이동할 라우트 경로
 * @param className 외부에서 전달하는 추가 스타일 클래스
 * @param children 링크 안에 표시할 텍스트 또는 아이콘
 * @return 링크 버튼 컴포넌트
 */
const LinkButton = ({ link, className, children }: LinkButtonProps) => {
  return (
    <Link
      to={link}
      style={{ display: "flex" }}
      className={className ? className : ""}
    >
      {children}
    </Link>
  );
};

export default LinkButton;
