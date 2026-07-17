package org.our.sadari.user.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.global.security.jwt.JwtProvider;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.auth.dto.KakaoAccountDto;
import org.our.sadari.user.auth.dto.KakaoTokenDto;
import org.our.sadari.user.auth.provider.KakaoAuthProvider;
import org.our.sadari.user.dto.LoginHistoryDto;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.mapper.LoginHistoryMapper;
import org.our.sadari.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthServiceImpl 클래스의 역할과 책임을 정의한다.
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
     * kakaoLogin 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param code 처리에 필요한 입력값
     * @param lognIpxx 처리에 필요한 입력값
     * @param userAgnt 처리에 필요한 입력값
     * @return 처리 결과
     */
    @Transactional
    @Override
    public ResultData kakaoLogin(String code, String lognIpxx, String userAgnt) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(code)) {
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        KakaoTokenDto kakaoTokenDto;
        KakaoAccountDto kakaoAccountDto;

        try {
            kakaoTokenDto = kakaoAuthProvider.getKakaoToken(code);
            kakaoAccountDto = kakaoAuthProvider.getKakaoAccount(kakaoTokenDto);
        } catch (JsonProcessingException e) {
            log.error("Kakao OAuth response parse failed. message={}", e.getMessage());
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

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

            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (StringUtil.isEmpty(savedUser)) {
                userMapper.setUser(userDto);
                userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                userMapper.uptUserProfile(userDto);
                log.info("Kakao user created. providerId={}", providerId);
            } else {
                userDto.setUserNumb(savedUser.getUserNumb());
                userDto.setUserRole(savedUser.getUserRole());

                // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
                if (StringUtil.isEmpty(savedUser.getProfNumb())) {
                    userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                    userMapper.uptUserProfile(userDto);
                }
            }

            log.info("Kakao login user resolved. userNumb={}", userDto.getUserNumb());
        } catch (Exception e) {
            log.error("Kakao user save failed. providerId={}, message={}", providerId, e.getMessage());
            // 호출한 계층에서 사용할 처리 결과를 반환한다.
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        String accessToken = jwtProvider.createAccessToken(userDto.getUserNumb(), userDto.getUserRole());
        String refreshToken = jwtProvider.createRefreshToken(userDto.getUserNumb());

        // 아래 처리 단계의 업무 목적을 설명한다.
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
        // 아래 처리 단계의 업무 목적을 설명한다.
        loginHistoryMapper.setLoginHistory(loginHistoryDto);

        log.debug("Kakao login JWT issued. userNumb={}", userDto.getUserNumb());
        return ResultData.success(TokenDto.of(accessToken, refreshToken));
    }
}
