package org.our.sadari.global.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * fileName       : SpaController
 * author         : HanWon.Jang
 * date           : 2026-09-16
 * description    : 브라우저에서 직접 요청한 사용자 화면을 React 진입 문서로 연결함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-16        HanWon.Jang        최초 생성
 */
@Controller
public class SpaController {

    /**
     * API와 서버 자원을 제외한 화면 경로를 React Router가 처리하도록 전달함
     *
     * @author HanWon.Jang
     * @return React 애플리케이션 진입 문서 전달 경로
     */
    @GetMapping({
            "/{path:^(?!api|uploads|swagger-ui|v3|error)[^.]*$}",
            "/{path:^(?!api|uploads|swagger-ui|v3|error)[^.]*$}/**"
    })
    public String getSpaPage() {
        // 새로고침과 OAuth 리다이렉트에서도 React Router가 현재 화면 경로를 해석하도록 진입 문서로 전달함
        return "forward:/index.html";
    }
}
