import { Link } from "react-router-dom";


interface LinkButtonProps {
  // url
  link: string;
  // style class
  className?: string;
  state?: unknown;
  // 링크에 표시할 텍스트 또는 아이콘
  children: React.ReactNode;
}

/**
 * 지정한 경로로 이동하는 링크 버튼을 표시한다
 * @author HanWon.Jang
 * @param link 이동할 화면 경로
 * @param className 외부에서 전달한 추가 스타일 클래스
 * @param children 링크에 표시할 텍스트 또는 아이콘
 * @return 화면 이동 링크 버튼
 */
const LinkButton = ({ link, className, state, children }: LinkButtonProps) => {

  return (
    <Link
      to={link}
      state={state}
      style={{ display: "flex" }}
      className={className ? className : ""}
    >
      {children}
    </Link>
  );
};

export default LinkButton;
