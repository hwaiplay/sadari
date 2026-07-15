/**
 * src/main/frontend/src/components/Layout/Header/Header.tsx 파일의 프론트엔드 화면, API, 훅 또는 유틸 로직을 담당합니다.
 *
 * @author Hanwon.Jang
 */
import { message } from "@/app/messages/message";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { header_active, backpageBtn, header, logo } from "./Header.css";
import { Container } from "../Container/Container";
import { clsx } from "clsx";

function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const isSubPage = location.pathname !== "/home";

  const backPrev = () => {
    navigate(-1);
  };

  return (
    <header >
      <Container className={clsx(header, isSubPage && "_sub")}>
        {isSubPage && (
          <button
            className={backpageBtn}
            type="button"
            aria-label={message("frontend.common.back")}
            onClick={backPrev}
          >
            <img
              src={"/img/common/icon-backpage.svg"}
              alt={message("frontend.common.backIconAlt")}
            />
          </button>
        )}
        <Link to="/" className={logo}>
          <img
            src={"/img/common/logo-upper.svg"}
            alt={message("frontend.common.logoAlt")}
            width={100}
          />
        </Link>
      </Container>
    </header>
  );
}

export default Header;