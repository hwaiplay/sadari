/**
 * fileName       : App
 * author         : Hanwon.Jang
 * date           : 2026-03-19
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-19        Hanwon.Jang       Router 분리
 */
import Home from "./pages/Home/Home";
import Login from "./pages/Login/Login";
import { useEffect, useState } from "react";
import { checkLogin } from "./api/user/loginCheck";
import { initAuth } from "./api/user/auth";
import Router from "./Router/Router";

export default function App() {
  return <Router />;
  // const [loadin/g, setLoading] = useState(true);
  // const [isLogin, setIsLogin] = useState<boolean>(false);

  // useEffect(() => {
  //   const init = async () => {
  //     await initAuth();
  //   };
  //   init();
  // }, []);

  // if (isLogin === null) return <div>로딩중...</div>;

  // return isLogin ? <Home /> : <Login />;
}
