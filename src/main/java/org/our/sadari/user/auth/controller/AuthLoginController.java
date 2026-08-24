package org.our.sadari.user.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.push.dto.PushDto;
import org.our.sadari.push.service.PushService;
import org.our.sadari.user.auth.dto.AuthLogoutDto;
import org.our.sadari.user.auth.provider.KakaoAuthProvider;
import org.our.sadari.user.auth.service.AuthService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.our.sadari.user.service.UserWithdrawalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : AuthLoginController
 * author         : SeungHyeon.Kang
 * date           : 2026-03-15
 * description    : 사용자 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    온보딩·계정 복귀 상태 응답
 * 2026-08-04        SeungHyeon.Kang       브라우저 CSRF Token 조회 API 추가
 * 2026-08-11        SeungHyeon.Kang    기기별 재발급과 선택형 로그아웃 추가
 * 2026-08-13        SeungHyeon.Kang    탈퇴 뒤 유효 제재가 남은 계정의 로그인 차단 안내 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Slf4j
@Tag(name = "인증", description = "카카오 OAuth 로그인, JWT 검증, 재발급, 로그아웃 API")
public class AuthLoginController {

    // 접근 TOKEN COOKIE 명칭 설정값
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    // REFRESH TOKEN COOKIE 명칭 설정값
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    // 일반 OAuth 로그인 요청과 콜백을 연결할 상태 쿠키명
    private static final String OAUTH_LOGIN_STATE_COOKIE_NAME = "oauthLoginState";
    // 일반 로그인 상태값과 탈퇴 재인증 상태값을 구분할 접두사
    private static final String OAUTH_LOGIN_STATE_PREFIX = "login_";
    // 일반 OAuth 로그인 상태 쿠키 유효 시간
    private static final long OAUTH_LOGIN_STATE_MAX_AGE_SECONDS = 300L;
    // OAuth 상태 쿠키를 콜백에만 전송할 경로
    private static final String OAUTH_CALLBACK_COOKIE_PATH = "/api/oauth/callback/kakao";

    // Auth 업무 처리 서비스
    private final AuthService authService;
    // KakaoAuth 외부 연동 제공 객체
    private final KakaoAuthProvider kakaoAuthProvider;
    // Jwt 외부 연동 제공 객체
    private final JwtProvider jwtProvider;
    // TokenRedis 업무 처리 서비스
    private final TokenRedisService tokenRedisService;
    // User 데이터 접근 객체
    private final UserMapper userMapper;
    // 회원 탈퇴 업무 처리 서비스
    private final UserWithdrawalService userWithdrawalService;
    // 로그아웃 시 사용자 프로필 임시 이미지를 정리할 파일 서비스
    private final FileService fileService;
    // 로그아웃 범위에 따라 브라우저 푸시 구독을 정리할 서비스
    private final PushService pushService;

    // OAuth 완료 후 이동할 프런트엔드 도메인
    @Value("${domain.front}")
    private String frontDomain;

    // Cookie Max-Age는 초 단위를 받으므로 yml의 JWT 유효시간 값을 1000 곱하지 않고 그대로 사용한다.
    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessTokenCookieMaxAgeSeconds;

    // 리프레시 토큰 쿠키 최대 유지 시간
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenCookieMaxAgeSeconds;

    // HTTPS에서만 인증 쿠키를 전송할지 여부
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    // 인증 쿠키의 SameSite 정책
    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    /**
     * 브라우저가 상태 변경 요청 Header에 포함할 CSRF Token을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param csrfToken Spring Security가 현재 브라우저에 발급한 CSRF Token
     * @return CSRF Token 문자열을 담은 성공 응답
     */
    @GetMapping("/csrf")
    @Operation(summary = "CSRF Token 조회", description = "Cookie 인증 상태 변경 요청에 사용할 CSRF Token을 조회한다.")
    public ResultData getCsrfToken(@Parameter(hidden = true) CsrfToken csrfToken) {
        // 브라우저가 공통 요청 Header에 설정할 CSRF Token을 반환한다
        return ResultData.success(csrfToken.getToken());
    }

    /**
     * yml 설정으로 생성한 카카오 OAuth 인가 화면으로 브라우저를 이동시킨다.
     *
     * @author SeungHyeon.Kang
     * @param response 카카오 인가 화면으로 리다이렉트할 HTTP 응답
     * @throws java.io.IOException 리다이렉트 응답 기록 실패
     */
    @GetMapping("/kakao")
    @Operation(summary = "카카오 로그인 시작", description = "서버 설정으로 카카오 OAuth 인가 URL을 생성해 로그인 화면으로 이동한다.")
    public void getKakaoAuthorization(HttpServletResponse response) throws java.io.IOException {
        // 추측하기 어려운 일회성 상태값으로 로그인 시작 브라우저와 콜백을 연결한다
        String loginState = OAUTH_LOGIN_STATE_PREFIX + UUID.randomUUID();
        // 콜백에서 비교할 상태값을 스크립트가 읽을 수 없는 제한 쿠키로 저장한다
        response.addHeader(HttpHeaders.SET_COOKIE, createOauthStateCookie(loginState, OAUTH_LOGIN_STATE_MAX_AGE_SECONDS).toString());
        // 동일한 상태값이 포함된 Kakao 인가 화면으로 브라우저를 이동시킨다
        response.sendRedirect(kakaoAuthProvider.getKakaoLoginUrl(loginState));
    }

    /**
     * Access Token 쿠키 유효성 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param request HTTP 요청 정보
     * @return 처리 결과
     */
    @GetMapping("/tokenCheck")
    @Operation(summary = "Access Token 검증", description = "HttpOnly 쿠키의 Access Token 유효성 및 로그아웃 블랙리스트 여부를 검증한다.")
    public ResultData tokenCheck(@Parameter(hidden = true) HttpServletRequest request) {
        // extractAccessToken 호출로 요청에서 인증 토큰을 추출한다
        String accessToken = extractAccessToken(request);

        // 요청 쿠키에 Access Token이 없으면 인증 처리를 중단한다
        if (StringUtil.isEmpty(accessToken)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // Access Token의 위변조 여부 및 만료 시간을 검증하여 유효하지 않으면 실패 처리한다.
        if (!jwtProvider.validateAccessToken(accessToken)) {
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 로그아웃되어 Redis 블랙리스트에 등록된 Access Token(jti 기준)인지 확인한다.
        if (tokenRedisService.hasAccessTokenBlacklist(jwtProvider.getTokenId(accessToken))) {
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 검증된 토큰에서 현재 로그인 사용자 번호를 조회한다
        Long userNumb = jwtProvider.getUserNumb(accessToken);
        // 토큰에 연결된 현재 기기 세션이 로그아웃되지 않았는지 확인한다
        String sessionId = jwtProvider.getSessionId(accessToken);

        // 전체 또는 현재 기기 로그아웃으로 세션이 제거된 토큰은 인증에 사용할 수 없다
        if (!tokenRedisService.isSessionActive(userNumb, sessionId)) {
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 계정 상태를 Redis 누락값으로 추정하지 않고 DB 원본에서 조회한다
        UserDto savedUser = userMapper.getUserByNumb(userNumb);
        // 토큰의 사용자가 DB에 없으면 정상 인증 상태로 응답하지 않는다
        if (StringUtil.isEmpty(savedUser) || StringUtil.isEmpty(savedUser.getUserStat())
                || StringUtil.isEmpty(savedUser.getOnbdYsno())) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 필터와 다음 요청이 같은 상태를 사용하도록 DB 원본 상태를 Redis에 보정한다
        tokenRedisService.uptUserStatus(userNumb, savedUser.getUserStat());

        // 프론트엔드가 회원 상태와 최초 로그인 화면을 한 번에 선택할 수 있도록 반환한다
        return ResultData.success(Map.of(
                "userStat", savedUser.getUserStat()
              , "onbdYsno", savedUser.getOnbdYsno()
        ));
    }

    /**
     * Kakao 인가 코드 기준 로그인한다.
     *
     * @author SeungHyeon.Kang
     * @param code 인가 코드
     * @param request HTTP 요청 정보
     * @param response HTTP 응답 작성 객체
     */
    @GetMapping("/callback/kakao")
    @Operation(summary = "카카오 로그인 콜백", description = "카카오 인가 코드를 받아 서비스 토큰을 발급하고 프론트 OAuth 처리 화면으로 리다이렉트한다.")
    public void kakaoAuthLogin(@Parameter(description = "카카오 OAuth 인가 코드") @RequestParam("code") String code
                             , @Parameter(description = "탈퇴 재인증 상태값") @RequestParam(value = "state", required = false) String state
                             , @Parameter(hidden = true) HttpServletRequest request, @Parameter(hidden = true) HttpServletResponse response) throws Exception {
        // 일반 로그인 접두사가 없는 상태값은 회원 탈퇴 재인증 콜백으로 처리한다
        if (!StringUtil.isEmpty(state) && !state.startsWith(OAUTH_LOGIN_STATE_PREFIX)) {
            // 재인증한 Kakao 계정으로 회원 탈퇴 상태 변경을 요청한다
            ResultData withdrawalResult = userWithdrawalService.setWithdrawalCallback(code, state);
            // 탈퇴 처리 성공 여부를 완료 화면이 구분할 수 있도록 쿼리값으로 전달한다
            if (withdrawalResult.getCode() == 200) {
                // 실제 탈퇴 처리에 성공한 경우에만 기존 인증 쿠키를 제거한다
                expireTokenCookies(response);
                // 탈퇴 유형과 성공 상태를 포함한 완료 화면으로 이동한다
                response.sendRedirect(frontDomain + "/withdrawal/result?success=Y&type=" + withdrawalResult.getData());
                // 회원 탈퇴 재인증 콜백 처리를 종료한다
                return;
            }

            // 실패 상태를 포함한 탈퇴 결과 화면으로 이동한다
            response.sendRedirect(frontDomain + "/withdrawal/result?success=N");
            // 회원 탈퇴 재인증 콜백 처리를 종료한다
            return;
        }

        // 일반 로그인 콜백은 시작 시 저장한 브라우저 상태 쿠키와 일치해야 한다
        if (!isValidLoginState(request, state)) {
            // 잘못되거나 재사용된 콜백이 기존 로그인 세션을 변경하지 않도록 그대로 복귀시킨다
            response.sendRedirect(frontDomain + "/oauth");
            // 검증되지 않은 인가 코드를 로그인 처리에 전달하지 않는다
            return;
        }

        // 검증을 마친 일반 로그인 상태 쿠키를 즉시 만료시켜 같은 브라우저에서도 재사용하지 못하게 한다
        expireOauthStateCookie(response);

        // kakaoLogin 업무 로직을 authService에 위임한다
        ResultData loginResult = authService.kakaoLogin(code, getLoginIp(request), getUserAgent(request));

        // 카카오 로그인 서비스 처리 실패 시 기존 로그인 세션을 유지하고 로그인 페이지로 리다이렉트한다.
        if (loginResult.getCode() != 200) {
            // sendRedirect 호출로 검증된 알림 또는 응답을 전송한다
            String failureRedirectUrl = frontDomain + "/oauth";

            // 탈퇴 계정에 유효한 정지가 남은 경우 일반 인증 실패와 구분해 정확한 안내를 표시한다
            if (ResultEnum.AUTH_WITHDRAWN_SUSPENDED.getCode() == loginResult.getCode()) {
                failureRedirectUrl += "?blocked=suspension";
            }

            // 실패 사유에 맞는 OAuth 완료 화면으로 이동한다
            response.sendRedirect(failureRedirectUrl);
            // Kakao 인가 코드 기준 로그인 결과를 반환한다
            return;
        }

        // 공통 응답에 포함된 업무 데이터를 조회한다
        TokenDto token = (TokenDto) loginResult.getData();

        // 발급된 토큰을 HttpOnly 쿠키에 담아 응답 헤더에 추가하고 프론트엔드로 리다이렉트한다.
        addTokenCookies(response, token.getAccessToken(), token.getRefreshToken());
        // 일반 로그인은 별도 안내 표시 없이 OAuth 완료 화면으로 이동한다
        String oauthRedirectUrl = frontDomain + "/oauth";

        // 이번 로그인에서 비활성화 계정이 복구된 경우에만 일회성 복귀 안내 표시를 전달한다
        if (token.isAccountReactivated()) {
            // OAuth 완료 화면이 복귀 정책 팝업을 표시할 수 있도록 정해진 쿼리값을 추가한다
            oauthRedirectUrl += "?reactivated=Y";
        }

        // 인증 상태와 복귀 여부를 확인할 프론트엔드 OAuth 완료 화면으로 이동한다
        response.sendRedirect(oauthRedirectUrl);
    }

    /**
     * OAuth 콜백 루트 URL로 직접 접근했을 때 보여줄 HTML 오류 화면을 반환한다.
     * 실제 로그인 처리는 /api/oauth/callback/kakao에서만 수행하므로, 잘못된 콜백 URL에서는 ResultData JSON이 브라우저에 그대로 노출되지 않도록 분리한다.
     *
     * @author SeungHyeon.Kang
     * @return OAuth 콜백 오류 안내 HTML
     */
    @GetMapping(value = {"/callback", "/callback/"}, produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "OAuth 콜백 오류 화면", description = "지원하지 않는 OAuth 콜백 루트 접근 시 브라우저용 오류 화면을 반환한다.")
    public ResponseEntity<String> oauthCallbackErrorPage() {
        // HTTP 응답 상태와 본문을 반환한다
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_HTML)
                .body(createOauthErrorHtml());
    }

    /**
     * Refresh Token 검증 후 Access Token 재발급한다.
     *
     * @author SeungHyeon.Kang
     * @param request HTTP 요청 정보
     * @param response HTTP 응답 작성 객체
     * @return 처리 결과
     */
    @PostMapping("/refresh")
    @Operation(summary = "JWT 재발급", description = "Refresh Token 쿠키를 검증하고 Access Token과 Refresh Token을 재발급한다.")
    public ResultData refresh(@Parameter(hidden = true) HttpServletRequest request, @Parameter(hidden = true) HttpServletResponse response) {
        // extractRefreshToken 호출로 요청에서 인증 토큰을 추출한다
        String refreshToken = extractRefreshToken(request);
        // Refresh Token의 존재 여부 및 위변조/만료 상태를 검증한다.
        if (StringUtil.isEmpty(refreshToken) || !jwtProvider.validateRefreshToken(refreshToken)) {
            // 인증 실패 또는 로그아웃 시 브라우저의 토큰 쿠키를 만료시킨다
            expireTokenCookies(response);
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
        Long userNumb = jwtProvider.getUserNumb(refreshToken);
        // Refresh Token에 연결된 기기별 세션 식별자를 조회한다
        String sessionId = jwtProvider.getSessionId(refreshToken);

        // 세션 식별자가 없거나 이미 로그아웃된 세션이면 재발급을 중단한다
        if (StringUtil.isEmpty(sessionId) || !tokenRedisService.isSessionActive(userNumb, sessionId)) {
            // 인증 실패 또는 로그아웃 시 브라우저의 토큰 쿠키를 만료시킨다
            expireTokenCookies(response);
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // UserByNumb 데이터를 DB에서 조회한다
        UserDto savedUser = userMapper.getUserByNumb(userNumb);

        // Access Token 재발급 시에도 DB에 저장된 현재 권한을 사용해야 ADMIN 사용자가 Swagger 접근 권한을 유지할 수 있다.
        if (StringUtil.isEmpty(savedUser)) {
            // 인증 실패 또는 로그아웃 시 브라우저의 토큰 쿠키를 만료시킨다
            expireTokenCookies(response);
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 같은 기기 세션 식별자를 유지한 Refresh Token 후보를 생성한다
        String proposedRefreshToken = jwtProvider.createRefreshToken(userNumb, sessionId);
        // 다중 탭 동시 요청을 하나의 회전 결과로 합쳐 Redis에 저장된 최신 토큰을 조회한다
        String newRefreshToken = tokenRedisService.rotateRefreshToken(
                userNumb
              , sessionId
              , refreshToken
              , proposedRefreshToken
              , jwtProvider.getRefreshTokenValidSec()
        );

        // 현재 토큰이나 직전 유예 토큰이 아니면 탈취 또는 만료 세션으로 판단한다
        if (StringUtil.isEmpty(newRefreshToken)) {
            // 더 이상 사용할 수 없는 인증 쿠키를 제거한다
            expireTokenCookies(response);
            // "유효하지 않은 토큰이에요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.TOKEN_INVALID);
        }

        // 회전 결과와 같은 세션을 가리키는 Access Token을 발급한다
        String newAccessToken = jwtProvider.createAccessToken(userNumb, savedUser.getUserRole(), sessionId);
        // DB 원본 계정 상태를 별도 Redis 캐시에 반영한다
        tokenRedisService.uptUserStatus(userNumb, savedUser.getUserStat());

        // 발급한 액세스 토큰과 리프레시 토큰을 보안 쿠키에 저장한다
        addTokenCookies(response, newAccessToken, newRefreshToken);
        // Refresh Token 검증 후 Access Token 재발급 결과를 성공 응답으로 반환한다
        return ResultData.success();
    }

    /**
     * Access Token 블랙리스트 등록과 Refresh Token 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그아웃 범위와 현재 브라우저 푸시 token
     * @param request HTTP 요청 정보
     * @param response HTTP 응답 작성 객체
     * @return 처리 결과
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 기기 또는 전체 기기의 로그인 세션과 푸시 구독을 정리한다.")
    public ResultData logout(@Valid @RequestBody(required = false) AuthLogoutDto req
                           , @Parameter(hidden = true) HttpServletRequest request
                           , @Parameter(hidden = true) HttpServletResponse response) {
        // extractAccessToken 호출로 요청에서 인증 토큰을 추출한다
        String accessToken = extractAccessToken(request);
        // extractRefreshToken 호출로 요청에서 인증 토큰을 추출한다
        String refreshToken = extractRefreshToken(request);
        // 유효한 Access 또는 Refresh Token에서 임시 이미지 정리에 사용할 사용자 번호를 복원한다
        Long logoutUserNumb = null;
        // 현재 기기 세션만 제거할 때 사용할 식별자를 복원한다
        String logoutSessionId = null;

        // 유효한 Access Token인 경우 남은 유효시간 동안 재사용하지 못하도록 jti를 Redis 블랙리스트에 등록한다.
        if (!StringUtil.isEmpty(accessToken) && jwtProvider.validateAccessToken(accessToken)) {
            // Refresh Token이 없더라도 로그아웃 사용자의 임시 이미지를 정리할 번호를 보관한다
            logoutUserNumb = jwtProvider.getUserNumb(accessToken);
            // Access Token에 연결된 현재 기기 세션 식별자를 보관한다
            logoutSessionId = jwtProvider.getSessionId(accessToken);
            // AccessTokenBlacklist 업무 값을 tokenRedisService DTO에 설정한다
            tokenRedisService.setAccessTokenBlacklist(
                    // getTokenId 조회로 후속 처리에 필요한 데이터를 가져온다
                    jwtProvider.getTokenId(accessToken),
                    // getRemainingSeconds 조회로 후속 처리에 필요한 데이터를 가져온다
                    jwtProvider.getRemainingSeconds(accessToken)
            );
        }

        // 유효한 Refresh Token인 경우 재발급에 사용되지 못하도록 Redis에서 제거한다.
        if (!StringUtil.isEmpty(refreshToken) && jwtProvider.validateRefreshToken(refreshToken)) {
            // 로그아웃하는 사용자 번호를 토큰 제거와 임시 파일 정리에 함께 사용한다
            Long userNumb = jwtProvider.getUserNumb(refreshToken);
            // Refresh Token의 로그인 사용자 번호를 최종 로그아웃 대상으로 설정한다
            logoutUserNumb = userNumb;
            // Refresh Token에 연결된 현재 기기 세션 식별자를 최종 대상으로 설정한다
            logoutSessionId = jwtProvider.getSessionId(refreshToken);
        }

        // 유효한 인증 토큰에서 사용자를 확인한 경우 저장하지 않은 임시 이미지를 모두 삭제한다
        if (!StringUtil.isEmpty(logoutUserNumb)) {
            // 요청 본문이 없는 이전 클라이언트는 안전한 현재 기기 로그아웃으로 처리한다
            String logoutScope = StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getScope())
                    ? "CURRENT" : req.getScope();

            // 전체 기기 로그아웃은 회원의 모든 세션과 푸시 구독을 비활성화한다
            if ("ALL".equals(logoutScope)) {
                // 모든 기기 Refresh Token 세션을 제거한다
                tokenRedisService.delLoginUserInfo(logoutUserNumb);
                // 모든 기기 FCM token을 비활성화한다
                pushService.delAllPushSub(logoutUserNumb);
            } else {
                // 현재 기기 Refresh Token 세션만 제거한다
                tokenRedisService.delLoginSession(logoutUserNumb, logoutSessionId);
                // 현재 브라우저 FCM token을 확인할 수 있을 때만 해당 구독을 비활성화한다
                if (!StringUtil.isEmpty(req) && !StringUtil.isEmpty(req.getPushToken())) {
                    // 푸시 비활성화 요청 DTO를 생성한다
                    PushDto.PushSubDto pushSub = new PushDto.PushSubDto();
                    // 현재 브라우저에서 조회한 FCM token을 설정한다
                    pushSub.setEndpUrlx(req.getPushToken());
                    // 현재 브라우저의 푸시 구독만 비활성화한다
                    pushService.delPushSub(logoutUserNumb, pushSub);
                }
            }

            // 저장하지 않은 프로필과 배경 임시 원본 및 미리보기를 즉시 삭제한다
            fileService.delAllProfileImageDrafts(logoutUserNumb);
        }

        // 브라우저의 토큰 쿠키를 삭제(만료 처리)한다.
        expireTokenCookies(response);
        // Access Token 블랙리스트 등록과 Refresh Token 삭제 결과를 성공 응답으로 반환한다
        return ResultData.success();
    }

    /**
     * Access Token과 Refresh Token 쿠키 추가한다.
     *
     * @author SeungHyeon.Kang
     * @param response HTTP 응답 작성 객체
     * @param accessToken API 인증에 사용할 Access Token
     * @param refreshToken Access Token 재발급에 사용할 Refresh Token
     */
    private void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // 브라우저 응답에 필요한 보안 또는 이동 헤더를 추가한다
        response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString());
        // 브라우저 응답에 필요한 보안 또는 이동 헤더를 추가한다
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());
    }

    /**
     * OAuth 콜백 오류 화면의 HTML 문자열을 생성한다.
     * API 전용 URL을 사용자가 직접 열었을 때도 빈 화면이나 JSON 원문 대신 로그인 화면으로 돌아갈 수 있는 안내 화면을 제공한다.
     *
     * @author SeungHyeon.Kang
     * @return OAuth 콜백 오류 화면 HTML
     */
    private String createOauthErrorHtml() {
        // OAuth 콜백 오류 화면의 HTML 문자열을 생성 결과를 반환한다
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>로그인 요청을 처리할 수 없어요</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            min-height: 100vh;
                            display: grid;
                            place-items: center;
                            padding: 24px;
                            background: #f5f7fb;
                            color: #191919;
                            font-family: Pretendard, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                        }

                        .page {
                            // 오류 화면이 지나치게 커지지 않도록 표시 크기를 제한한다
                            width: min(100%, 420px);
                            padding: 34px 28px 30px;
                            border: 1px solid #e8edf5;
                            border-radius: 18px;
                            background: #ffffff;
                            text-align: center;
                            // 오류 화면에 사용할 반투명 색상값을 생성한다
                            box-shadow: 0 18px 50px rgba(36, 56, 96, 0.12);
                        }

                        .mark {
                            width: 54px;
                            height: 54px;
                            display: grid;
                            place-items: center;
                            margin: 0 auto 18px;
                            border-radius: 18px;
                            background: #e8f3ff;
                            color: #2f80ed;
                            font-size: 28px;
                            font-weight: 800;
                        }

                        h1 {
                            margin: 0;
                            font-size: 22px;
                            line-height: 1.35;
                            letter-spacing: 0;
                        }

                        p {
                            margin: 12px 0 0;
                            color: #687386;
                            font-size: 14px;
                            line-height: 1.65;
                        }

                        a {
                            display: inline-flex;
                            align-items: center;
                            justify-content: center;
                            width: 100%;
                            height: 46px;
                            margin-top: 24px;
                            border-radius: 12px;
                            background: #2f80ed;
                            color: #ffffff;
                            font-size: 15px;
                            font-weight: 700;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <main class="page">
                        <div class="mark">!</div>
                        <h1>로그인 요청을 처리할 수 없어요</h1>
                        <p>로그인 제공자 정보가 없는 콜백 주소로 접근했어요.<br>다시 로그인 화면에서 시작해주세요.</p>
                        <a href="%s/login">로그인 화면으로 돌아가기</a>
                    </main>
                </body>
                </html>
                // 오류 화면 HTML에 사용자 안내 문구와 이동 경로를 반영한다
                """.formatted(frontDomain);
    }

    /**
     * 로그아웃 토큰 쿠키 만료한다.
     *
     * @author SeungHyeon.Kang
     * @param response HTTP 응답 작성 객체
     */
    private void expireTokenCookies(HttpServletResponse response) {
        // 브라우저 응답에 필요한 보안 또는 이동 헤더를 추가한다
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(ACCESS_TOKEN_COOKIE_NAME).toString());
        // 브라우저 응답에 필요한 보안 또는 이동 헤더를 추가한다
        response.addHeader(HttpHeaders.SET_COOKIE, createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME).toString());
    }

    /**
     * 일반 OAuth 로그인 콜백의 상태값이 시작 브라우저의 쿠키와 일치하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param request OAuth 콜백 요청
     * @param state Kakao가 반환한 상태값
     * @return 상태값 일치 여부
     */
    private boolean isValidLoginState(HttpServletRequest request, String state) {
        // 일반 로그인 상태값과 브라우저 상태 쿠키가 모두 있어야 검증할 수 있다
        String savedState = extractCookieValue(request, OAUTH_LOGIN_STATE_COOKIE_NAME);

        // 누락된 상태값은 일반 OAuth 로그인 콜백으로 허용하지 않는다
        if (StringUtil.isEmpty(state) || StringUtil.isEmpty(savedState)) {
            // 검증할 상태값이 없음을 반환한다
            return false;
        }

        // 상태값 비교 시간 차이로 일치 여부가 드러나지 않도록 고정 시간 비교를 수행한다
        return MessageDigest.isEqual(
                state.getBytes(StandardCharsets.UTF_8)
              , savedState.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * OAuth 로그인 상태값을 콜백 경로에서만 사용할 보안 쿠키로 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param state 저장할 OAuth 상태값
     * @param maxAgeSeconds 쿠키 유효 시간
     * @return OAuth 상태 쿠키
     */
    private ResponseCookie createOauthStateCookie(String state, long maxAgeSeconds) {
        // OAuth 상태 쿠키를 콜백 GET 요청에 필요한 Lax 정책과 제한 경로로 생성한다
        return ResponseCookie.from(OAUTH_LOGIN_STATE_COOKIE_NAME, state)
                .httpOnly(true)
                .sameSite("Lax")
                .secure(cookieSecure)
                .path(OAUTH_CALLBACK_COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * 사용을 마친 일반 OAuth 로그인 상태 쿠키를 만료한다.
     *
     * @author SeungHyeon.Kang
     * @param response OAuth 콜백 응답
     */
    private void expireOauthStateCookie(HttpServletResponse response) {
        // 발급 때와 같은 경로의 빈 쿠키를 내려 브라우저에 남은 상태값을 제거한다
        response.addHeader(HttpHeaders.SET_COOKIE, createOauthStateCookie("", 0).toString());
    }

    /**
     * AccessToken 쿠키를 생성한다.
     *
     * @param accessToken 발급된 AccessToken
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createAccessTokenCookie(String accessToken) {
        // AccessToken 쿠키를 생성 결과를 반환한다
        return createTokenCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessToken,
                accessTokenCookieMaxAgeSeconds
        );
    }

    /**
     * RefreshToken 쿠키를 생성한다.
     *
     * @param refreshToken 발급된 RefreshToken
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        // RefreshToken 쿠키를 생성 결과를 반환한다
        return createTokenCookie(
                REFRESH_TOKEN_COOKIE_NAME,
                refreshToken,
                refreshTokenCookieMaxAgeSeconds
        );
    }

    /**
     * 공통 토큰 쿠키 객체를 생성한다.
     *
     * @param name 쿠키명
     * @param value 쿠키값
     * @param maxAgeSeconds 유효기간(초)
     * @return 생성된 ResponseCookie 객체
     */
    private ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
        // 공통 토큰 쿠키 객체를 생성 결과를 반환한다
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                // 운영 HTTPS와 프론트/백 도메인 분리 여부에 따라 SameSite 값을 yml 환경변수로 조정한다.
                .sameSite(cookieSameSite)
                // 운영 HTTPS 배포에서는 true로 설정해 브라우저가 보안 연결에서만 토큰 쿠키를 전송하게 한다.
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    /**
     * 만료 처리용 빈 쿠키 객체를 생성한다.
     *
     * @param name 쿠키명
     * @return 만료 설정된 ResponseCookie 객체
     */
    private ResponseCookie createExpiredCookie(String name) {
        // 만료 처리용 빈 쿠키 객체를 생성 결과를 반환한다
        return createTokenCookie(name, "", 0);
    }

    /**
     * Request 쿠키에서 RefreshToken을 추출한다.
     *
     * @param request HTTP 요청 정보
     * @return 추출된 RefreshToken (없을 경우 null)
     */
    private String extractRefreshToken(HttpServletRequest request) {
        // Request 쿠키에서 RefreshToken을 추출 결과를 반환한다
        return extractCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Request 쿠키에서 AccessToken을 추출한다.
     *
     * @param request HTTP 요청 정보
     * @return 추출된 AccessToken (없을 경우 null)
     */
    private String extractAccessToken(HttpServletRequest request) {
        // Request 쿠키에서 AccessToken을 추출 결과를 반환한다
        return extractCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * Request 쿠키 목록에서 특정 이름의 쿠키 값을 추출한다.
     *
     * @param request HTTP 요청 정보
     * @param name 쿠키명
     * @return 쿠키 값 (없을 경우 null)
     */
    private String extractCookieValue(HttpServletRequest request, String name) {
        // 요청 헤더에 쿠키가 존재하지 않는 경우 null을 반환한다.
        if (StringUtil.isEmpty(request.getCookies())) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (Cookie cookie : request.getCookies()) {
            // 찾고자 하는 쿠키명과 일치하는 쿠키가 존재하면 해당 값을 반환한다.
            if (name.equals(cookie.getName())) {
                // Request 쿠키 목록에서 특정 이름의 쿠키 값을 추출 결과를 반환한다
                return cookie.getValue();
            }
        }

        // 조회하거나 생성할 값이 없음을 반환한다
        return null;
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출한다.
     *
     * @author SeungHyeon.Kang
     * @param request HTTP 요청 정보
     * @return 클라이언트 IP 주소
     */
    private String getLoginIp(HttpServletRequest request) {
        // getHeader 조회로 후속 처리에 필요한 데이터를 가져온다
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // 프록시/로드밸런서를 거쳐 들어온 경우 원본 클라이언트 IP(X-Forwarded-For)를 우선 추출한다.
        if (!StringUtil.isEmpty(forwardedFor)) {
            // 클라이언트의 실제 IP 주소를 추출 결과를 반환한다
            return forwardedFor.split(",")[0].trim();
        }

        // getHeader 조회로 후속 처리에 필요한 데이터를 가져온다
        String realIp = request.getHeader("X-Real-IP");

        // Nginx 등에서 설정한 X-Real-IP 헤더가 존재하는 경우 해당 IP를 반환한다.
        if (!StringUtil.isEmpty(realIp)) {
            // 클라이언트의 실제 IP 주소를 추출 결과를 반환한다
            return realIp;
        }

        // 클라이언트의 실제 IP 주소를 추출 결과를 반환한다
        return request.getRemoteAddr();
    }

    /**
     * Request 헤더에서 User-Agent(브라우저/디바이스 정보)를 추출한다.
     *
     * @author SeungHyeon.Kang
     * @param request HTTP 요청 정보
     * @return User-Agent 문자열
     */
    private String getUserAgent(HttpServletRequest request) {
        // Request 헤더에서 User-Agent(브라우저/디바이스 정보)를 추출 결과를 반환한다
        return request.getHeader(HttpHeaders.USER_AGENT);
    }
}
