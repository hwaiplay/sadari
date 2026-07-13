package org.our.sadari.sadariUser.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.our.sadari.global.security.dto.TokenDto;

/**
 * fileName       : AuthService
 * author         : Seunghyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 로그인과 JWT 발급 기능을 제공하는 인증 서비스 인터페이스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        Seunghyeon.Kang    최초 생성
 * 2026-07-13        Seunghyeon.Kang    로그인 이력 저장을 위한 IP와 User-Agent 전달 값 추가
 */
public interface AuthService {

    /**
     * 카카오 OAuth 인가 코드로 사용자 정보를 조회하고 서비스 JWT를 발급한다.
     * 로그인 성공 이력에는 인증 처리에 사용한 토큰 원문을 저장하지 않고, 추적에 필요한 요청 환경 정보만 저장한다.
     *
     * @Author Seunghyeon.Kang
     * @param code 카카오 OAuth 콜백으로 전달받은 인가 코드
     * @param lognIpxx 로그인 요청 IP 주소
     * @param userAgnt 로그인 요청 User-Agent 값
     * @return 서비스 accessToken과 refreshToken을 담은 토큰 DTO
     * @throws JsonProcessingException 카카오 응답 JSON 파싱 실패 시 발생
     */
    TokenDto kakaoLogin(String code, String lognIpxx, String userAgnt) throws JsonProcessingException;
}
