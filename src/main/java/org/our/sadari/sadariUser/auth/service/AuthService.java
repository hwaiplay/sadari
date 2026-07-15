package org.our.sadari.sadariUser.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.our.sadari.global.security.dto.TokenDto;

/**
 * 소셜 로그인과 JWT 발급 업무를 정의하는 인증 서비스 인터페이스입니다.
 *
 * @author Seunghyeon.Kang
 */
public interface AuthService {

    /**
     * Kakao OAuth 인가 코드로 회원 정보를 조회하고 서비스 JWT를 발급합니다.
     *
     * @author Seunghyeon.Kang
     * @param code Kakao OAuth callback 인가 코드
     * @param lognIpxx 로그인 요청 IP 주소
     * @param userAgnt 로그인 요청 User-Agent 값
     * @return 서비스 accessToken과 refreshToken
     * @throws JsonProcessingException Kakao 응답 JSON 변환에 실패한 경우
     */
    TokenDto kakaoLogin(String code, String lognIpxx, String userAgnt) throws JsonProcessingException;
}
