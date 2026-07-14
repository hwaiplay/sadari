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
 * 카카오 로그인, 회원 저장, JWT 발급, Redis refresh token 저장, 로그인 이력 저장을 처리한다.
 * 신규 회원의 카카오 프로필 이미지는 회원을 먼저 생성해 USER_NUMB를 확보한 뒤 파일 등록 시 REGI_USER로 바로 저장한다.
 * @Author Seunghyeon.Kang
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
     * 카카오 인가 코드로 사용자 정보를 조회하고 서비스 JWT를 발급한다.
     * 신규 회원은 사용자 테이블 insert 후 생성된 USER_NUMB로 카카오 프로필 파일을 등록하고 사용자 프로필 파일 번호를 갱신한다.
     * @Author Seunghyeon.Kang
     * @param code 카카오 OAuth 인증 완료 후 전달받은 인가 코드
     * @param lognIpxx 로그인 요청 IP 주소
     * @param userAgnt 로그인 요청 User-Agent 값
     * @return 서비스 access token과 refresh token
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

            if (StringUtil.isEmpty(savedUser)) {
                userMapper.setUser(userDto);
                userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                userMapper.uptUserProfile(userDto);
                log.info("Kakao user created. providerId={}", providerId);
            } else {
                userDto.setUserNumb(savedUser.getUserNumb());
                userDto.setUserRole(savedUser.getUserRole());

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

        // refresh token은 DB가 아닌 Redis에 저장해 rotation과 logout 차단 기준으로 사용한다.
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
        loginHistoryMapper.setLoginHistory(loginHistoryDto);

        log.debug("Kakao login JWT issued. userNumb={}", userDto.getUserNumb());
        return TokenDto.of(accessToken, refreshToken);
    }
}
