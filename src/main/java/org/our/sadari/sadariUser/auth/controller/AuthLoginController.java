package org.our.sadari.sadariUser.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.exception.CustomException;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.sadariUser.auth.entity.TokenHistoryEntity;
import org.our.sadari.sadariUser.auth.repository.TokenHistoryRepository;
import org.our.sadari.sadariUser.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

/**
 * fileName       : AuthLoginController
 * author         : seungHyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 소셜 로그인
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        seungHyeon.Kang   최초 생성
 * 2026-03-17        hanWon.jang       리팩터리 및 JWT 토큰 발급
 * 2026-03-24        hanWon.jang       ResultData로 응답 통일, 로그인 상태 확인 API 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
public class AuthLoginController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenHistoryRepository tokenHistoryRepository;

    // 로그인 상태 확인 API
    @GetMapping("/tokenCheck")
    public ResultData<?> me(HttpServletRequest request) {

        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElse(null);

        if (tokenEntity == null || tokenEntity.isExpired()) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        return ResultData.success(); // 인증된 상태, 필요한 경우 사용자 정보도 반환 가능
    }

    @GetMapping("/callback/kakao")
    public void kakaoAuthLogin (@RequestParam("code") String code,
                                HttpServletResponse response) throws Exception {

        TokenDto token = authService.kakaoLogin(code);

        //refreshToken 쿠키로 저장
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                .sameSite("Lax") // 배포시 None으로 변경
                // .secure(true) // HTTPS 환경에서만 쿠키가 전송되도록 설정 (개발 환경에서는 주석 처리)
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .build();
                
        //accessToken 쿠키로 저장
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", token.getAccessToken())
                .httpOnly(true)
                .sameSite("Lax") // 배포시 None으로 변경
                // .secure(true) // HTTPS 환경에서만 쿠키가 전송되도록 설정 (개발 환경에서는 주석 처리)
                .path("/")
                .maxAge(60 * 60) // 1시간
                .build();
        

        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        response.sendRedirect(
                "http://localhost:5173/oauth"
        );
    }

    /**
     * 토큰 재발급 API
     * @param request
     * @return
     */
    @PostMapping("/refresh")
    public ResultData<TokenDto> refresh(HttpServletRequest request) {

        // 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            // 인증 자체가 안 된 상태
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 조회 (존재 여부 확인)
        TokenHistoryEntity tokenEntity = tokenHistoryRepository
                .findByRefrTokn(refreshToken)
                .orElseThrow(() ->
                    new CustomException(ResultEnum.TOKEN_INVALID, HttpStatus.UNAUTHORIZED)
                );

        // 만료 체크 (DB 기준)
        if (tokenEntity.isExpired()) {
            return ResultData.fail(ResultEnum.TOKEN_EXPIRED);
        }

        // JWT 자체 검증 (위조 체크)
        if (!jwtProvider.validateToken(refreshToken)) {
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // userNumb 추출
        Long userNumb = jwtProvider.getUserNumb(refreshToken);

        // 새 accessToken 발급
        String newAccessToken = jwtProvider.createAccessToken(userNumb, AuthConstant.ROLE_USER);

        // 응답
        return ResultData.success(new TokenDto(newAccessToken, refreshToken));
    }

    /**
     * 쿠키에서 refreshToken 추출
     * @return refreshToken 값 (없으면 null)
     */
    private String extractRefreshToken(HttpServletRequest request) {

        // 쿠키 자체가 없는 경우
        if (request.getCookies() == null) {
            return null;
        }

        // refreshToken 쿠키 찾기
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}