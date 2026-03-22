package org.our.sadari.sadariUser.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.our.sadari.global.security.dto.TokenDto;

/**
 * fileName       : AuthService
 * author         : seungHyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 소셜로그인 토큰 발급 선언체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        seungHyeon.Kang   최초 생성
 */
public interface AuthService {

    TokenDto kakaoLogin(String code) throws JsonProcessingException;
}
