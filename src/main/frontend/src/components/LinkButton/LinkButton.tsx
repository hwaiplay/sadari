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

const LinkButton = ({ link, className, children }: LinkButtonProps) => {
  return (
    <Link to={link} className={className ? className : ""}>
      {children}
    </Link>
  );
};

export default LinkButton;
