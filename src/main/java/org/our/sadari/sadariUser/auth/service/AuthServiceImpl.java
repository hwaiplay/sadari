package org.our.sadari.sadariUser.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.sadariUser.auth.dto.KakaoAccountDto;
import org.our.sadari.sadariUser.auth.dto.KakaoTokenDto;
import org.our.sadari.sadariUser.auth.provider.KakaoAuthProvider;
import org.our.sadari.sadariUser.user.dto.LoginHistoryDto;
import org.our.sadari.sadariUser.user.dto.UserDto;
import org.our.sadari.sadariUser.user.mapper.LoginHistoryMapper;
import org.our.sadari.sadariUser.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : AuthServiceImpl
 * author         : Seunghyeon.Kang
 * date           : 2026-03-15
 * description    : 카카오 로그인, 회원 저장, JWT 발급, Redis 토큰 저장, 로그인 이력 저장을 처리하는 서비스 구현체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        Seunghyeon.Kang    최초 생성
 * 2026-07-13        Seunghyeon.Kang    TB_LOGHIS 로그인 이력 저장 구조 적용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final int USER_AGENT_MAX_LENGTH = 500;

    private final KakaoAuthProvider kakaoAuthProvider;
    private final JwtProvider jwtProvider;
    private final TokenRedisService tokenRedisService;
    private final LoginHistoryMapper loginHistoryMapper;
    private final UserMapper userMapper;

    /**
     * 카카오 인가 코드로 카카오 사용자 정보를 조회하고 서비스 JWT를 발급한다.
     * 기존 회원이면 저장된 사용자 번호로 JWT를 발급하고, 신규 회원이면 사용자 정보를 먼저 저장한 뒤 발급된 사용자 번호로 JWT를 만든다.
     * 발급한 refreshToken은 Redis에 저장하여 refresh와 logout 판단 기준으로 사용하고, DB에는 로그인 성공 이력만 저장한다.
     *
     * @Author Seunghyeon.Kang
     * @param code 카카오 OAuth 인증 완료 후 전달받은 인가 코드
     * @param lognIpxx 로그인 요청 IP 주소
     * @param userAgnt 로그인 요청 User-Agent 값
     * @return 서비스에서 발급한 accessToken과 refreshToken을 담은 토큰 DTO
     * @throws JsonProcessingException 카카오 응답 JSON 파싱 실패 시 발생
     */
    @Transactional
    @Override
    public TokenDto kakaoLogin(String code, String lognIpxx, String userAgnt) throws JsonProcessingException {
        KakaoTokenDto kakaoTokenDto = kakaoAuthProvider.getKakaoToken(code);
        KakaoAccountDto kakaoAccountDto = kakaoAuthProvider.getKakaoAccount(kakaoTokenDto);

        String providerId = String.valueOf(kakaoAccountDto.id);
        String nickName = kakaoAccountDto.kakao_account.profile.nickname;
        String profileImg = kakaoAccountDto.kakao_account.profile.profile_image_url;

        UserDto userDto = new UserDto();

        try {
            UserDto savedUser = userMapper.getUserByIdxx(providerId);

            userDto.setUserProv(AuthConstant.PROV_KAKAO);
            userDto.setUserIdxx(providerId);
            userDto.setUserRole(AuthConstant.ROLE_USER);
            userDto.setUserNick(nickName);
            userDto.setPorfPath(profileImg);

            if (savedUser == null) {
                // 신규 카카오 사용자는 먼저 사용자 테이블에 저장하고 selectKey로 생성된 사용자 번호를 그대로 사용한다.
                userMapper.setUser(userDto);
                log.info("Kakao user created. providerId={}", providerId);
            } else {
                // 기존 사용자는 DB에 저장된 사용자 번호를 JWT subject와 로그인 이력 사용자 번호로 사용한다.
                userDto.setUserNumb(savedUser.getUserNumb());
            }

            log.info("Kakao login user resolved. userNumb={}", userDto.getUserNumb());
        } catch (Exception e) {
            log.error("Kakao user save failed. providerId={}, message={}", providerId, e.getMessage());
            throw e;
        }

        String accessToken = jwtProvider.createAccessToken(userDto.getUserNumb(), userDto.getUserRole());
        String refreshToken = jwtProvider.createRefreshToken(userDto.getUserNumb());

        // refreshToken은 DB가 아니라 Redis에 저장하여 refreshToken rotation과 logout 차단 기준으로 사용한다.
        tokenRedisService.setRefreshToken(
                userDto.getUserNumb(),
                refreshToken,
                jwtProvider.getRefreshTokenValiditySeconds()
        );

        //로그인 이력테이블 저장
        LoginHistoryDto loginHistoryDto = new LoginHistoryDto();
        loginHistoryDto.setUserNumb(userDto.getUserNumb());
        loginHistoryDto.setLognDate(LocalDateTime.now());
        loginHistoryDto.setLognIpxx(lognIpxx);
        loginHistoryDto.setUserAgnt(StringUtil.cutString(userAgnt, USER_AGENT_MAX_LENGTH));
        loginHistoryDto.setProvCode(AuthConstant.PROV_KAKAO);

        // 로그인 성공 여부를 사후 확인할 수 있도록 토큰 원문을 제외한 접속 환경 정보를 TB_LOGHIS에 남긴다.
        loginHistoryMapper.setLoginHistory(loginHistoryDto);

        log.debug("Kakao login JWT issued. userNumb={}", userDto.getUserNumb());
        return TokenDto.of(accessToken, refreshToken);
    }
}
