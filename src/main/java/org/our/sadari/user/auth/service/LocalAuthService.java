package org.our.sadari.user.auth.service;

import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : LocalAuthService
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 로컬 프로필에서 활성 회원의 개발용 로그인 세션을 생성함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@Service
@Profile("loc & !prod")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LocalAuthService {

    // 개발용 로그인에서 허용할 서비스 권한 목록
    private static final Set<String> LOGIN_ROLE_SET = Set.of(AuthConstant.ROLE_USER, AuthConstant.ROLE_ADMIN);

    // JWT 발급 제공 객체
    private final JwtProvider jwtProvider;
    // 로그인 세션 Redis 서비스
    private final TokenRedisService tokenRedisService;
    // 사용자 데이터 접근 객체
    private final UserMapper userMapper;

    /**
     * 활성 회원 번호로 로컬 개발용 Access Token과 Refresh Token을 발급함
     *
     * @author HanWon.Jang
     * @param userNumb 로그인할 회원 번호
     * @return 발급 토큰 또는 공통 인증 실패 결과
     */
    @Transactional
    public ResultData setLocalLogin(Long userNumb) {
        // 회원 번호가 없으면 임의 계정 조회와 불완전한 세션 생성을 차단함
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 요청한 회원 번호의 현재 계정 상태와 권한을 DB 원본에서 조회함
        UserDto savedUser = userMapper.getUserByNumb(userNumb);

        // 활성 회원과 서비스가 허용한 권한만 개발용 로그인 대상으로 인정함
        if (StringUtil.isEmpty(savedUser) || !Constant.USER_STAT_ACTIVE.equals(savedUser.getUserStat())
                || StringUtil.isEmpty(savedUser.getUserRole()) || !LOGIN_ROLE_SET.contains(savedUser.getUserRole())) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 브라우저별 개발 로그인 세션을 다른 기기 세션과 구분할 식별자를 생성함
        String sessionId = UUID.randomUUID().toString();
        // DB에서 확인한 회원 번호와 권한으로 Access Token을 발급함
        String accessToken = jwtProvider.createAccessToken(userNumb, savedUser.getUserRole(), sessionId);
        // Access Token과 같은 세션 식별자로 Refresh Token을 발급함
        String refreshToken = jwtProvider.createRefreshToken(userNumb, sessionId);
        // 발급한 개발용 로그인 세션과 현재 회원 표시 정보를 Redis에 원자적으로 저장함
        tokenRedisService.setLoginUserInfo(
                userNumb
              , sessionId
              , refreshToken
              , savedUser.getUserNick()
              , savedUser.getUserStat()
              , jwtProvider.getRefreshTokenValidSec()
        );

        // 개발용 로그인 사용 사실을 토큰 원문 없이 서버 로그에 남김
        log.info("Local profile login JWT issued. userNumb={}", userNumb);
        // 브라우저 인증 쿠키에 사용할 두 토큰을 응답 데이터로 구성함
        TokenDto token = TokenDto.of(accessToken, refreshToken, false);
        // 계정 상태를 변경하지 않은 로컬 개발용 로그인 결과를 반환함
        return ResultData.success(token);
    }
}
