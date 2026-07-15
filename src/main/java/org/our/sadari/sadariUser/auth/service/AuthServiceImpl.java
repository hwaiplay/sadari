package org.our.sadari.sadariUser.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
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
 * Kakao 로그인, 회원 생성/조회, JWT 발급, Redis refreshToken 저장, 로그인 이력 저장을 처리합니다.
 *
 * @author Seunghyeon.Kang
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
    private final FileService fileService;

    /**
     * Kakao OAuth 인가 코드로 로그인 처리를 완료하고 JWT를 발급합니다.
     *
     * @author Seunghyeon.Kang
     * @param code Kakao OAuth callback 인가 코드
     * @param lognIpxx 로그인 이력에 저장할 IP 주소
     * @param userAgnt 로그인 이력에 저장할 User-Agent 값
     * @return 서비스 accessToken과 refreshToken
     * @throws JsonProcessingException Kakao 응답 JSON 변환에 실패한 경우
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

            // 최초 로그인 회원은 회원 테이블을 먼저 생성해야 USER_NUMB로 프로필 파일을 등록할 수 있습니다.
            if (StringUtil.isEmpty(savedUser)) {
                userMapper.setUser(userDto);
                userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                userMapper.uptUserProfile(userDto);
                log.info("Kakao user created. providerId={}", providerId);
            } else {
                userDto.setUserNumb(savedUser.getUserNumb());
                userDto.setUserRole(savedUser.getUserRole());

                // 기존 회원 중 프로필 파일 번호가 없는 사용자는 Kakao 프로필 이미지를 파일 테이블에 보정 저장합니다.
                if (StringUtil.isEmpty(savedUser.getProfNumb())) {
                    userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                    userMapper.uptUserProfile(userDto);
                }
            }

            log.info("Kakao login user resolved. userNumb={}", userDto.getUserNumb());
        } catch (Exception e) {
            log.error("Kakao user save failed. providerId={}, message={}", providerId, e.getMessage());
            throw e;
        }

        String accessToken = jwtProvider.createAccessToken(userDto.getUserNumb(), userDto.getUserRole());
        String refreshToken = jwtProvider.createRefreshToken(userDto.getUserNumb());

        // refreshToken은 Redis에 사용자별 하나만 저장해 재로그인과 재발급 시 이전 토큰을 무효화합니다.
        tokenRedisService.setRefreshToken(
                userDto.getUserNumb(),
                refreshToken,
                jwtProvider.getRefreshTokenValiditySeconds()
        );

        LoginHistoryDto loginHistoryDto = new LoginHistoryDto();
        loginHistoryDto.setUserNumb(userDto.getUserNumb());
        loginHistoryDto.setLognDate(LocalDateTime.now());
        loginHistoryDto.setLognIpxx(lognIpxx);
        loginHistoryDto.setUserAgnt(StringUtil.cutString(userAgnt, USER_AGENT_MAX_LENGTH));
        loginHistoryDto.setProvCode(AuthConstant.PROV_KAKAO);
        // 로그인 이력은 토큰 값이 아닌 접속 환경 정보를 저장해 이후 사용자 활동 추적에 사용합니다.
        loginHistoryMapper.setLoginHistory(loginHistoryDto);

        log.debug("Kakao login JWT issued. userNumb={}", userDto.getUserNumb());
        return TokenDto.of(accessToken, refreshToken);
    }
}
