import { Link } from "react-router-dom";


interface LinkButtonProps {
  // url
  link: string;
  // style class
  className?: string;
  state?: unknown;
  // ?띿뒪??or ?꾩씠肄???.
  children: React.ReactNode;
}

/**
 * 吏?뺥븳 ?쇱슦?몃줈 ?대룞?섎뒗 留곹겕??踰꾪듉???뚮뜑留곹븳??
 * @author Hanwon.Jang
 * @param link ?대룞???쇱슦??寃쎈줈
 * @param className ?몃??먯꽌 ?꾨떖?섎뒗 異붽? ?ㅽ????대옒?? * @param children 留곹겕 ?덉뿉 ?쒖떆???띿뒪???먮뒗 ?꾩씠肄? * @return 留곹겕 踰꾪듉 而댄룷?뚰듃
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