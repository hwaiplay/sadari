package org.our.sadari.user.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.AuthConstant;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.UserIdEncryptionService;
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
import org.our.sadari.user.service.NicknameGenerationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * fileName       : AuthServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-03-15
 * description    : Kakao 계정 기반 회원 등록과 JWT 로그인 업무를 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-15        SeungHyeon.Kang    최초 생성
 * 2026-07-29        SeungHyeon.Kang    최초 로그인 자동 닉네임 발급 적용
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    // USER AGENT 최대 길이 설정값
    private static final int USER_AGENT_MAX_LENGTH = 500;

    // KakaoAuth 외부 연동 제공 객체
    private final KakaoAuthProvider kakaoAuthProvider;
    // Jwt 외부 연동 제공 객체
    private final JwtProvider jwtProvider;
    // TokenRedis 업무 처리 서비스
    private final TokenRedisService tokenRedisService;
    // LoginHistory 데이터 접근 객체
    private final LoginHistoryMapper loginHistoryMapper;
    // User 데이터 접근 객체
    private final UserMapper userMapper;
    // File 업무 처리 서비스
    private final FileService fileService;
    // UserIdEncryption 업무 처리 서비스
    private final UserIdEncryptionService userIdEncryptionService;
    // NicknameGeneration 업무 처리 서비스
    private final NicknameGenerationService nicknameGenerationService;

    /**
     * Kakao 계정으로 신규 회원 등록과 JWT 로그인을 처리한다
     *
     * @author SeungHyeon.Kang
     * @param code Kakao 로그인 인가 코드
     * @param lognIpxx 로그인을 요청한 IP 주소
     * @param userAgnt 로그인 클라이언트의 User-Agent
     * @return 처리 결과
     */
    @Transactional
    @Override
    public ResultData kakaoLogin(String code, String lognIpxx, String userAgnt) {
        // code 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (StringUtil.isEmpty(code)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        KakaoTokenDto kakaoTokenDto;
        KakaoAccountDto kakaoAccountDto;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // getKakaoToken 조회로 후속 처리에 필요한 데이터를 가져온다
            kakaoTokenDto = kakaoAuthProvider.getKakaoToken(code);
            // getKakaoAccount 조회로 후속 처리에 필요한 데이터를 가져온다
            kakaoAccountDto = kakaoAuthProvider.getKakaoAccount(kakaoTokenDto);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (JsonProcessingException e) {
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("Kakao OAuth response parse failed. message={}", e.getMessage());
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 입력값을 문자열 표현으로 변환한다
        String providerId = String.valueOf(kakaoAccountDto.id);
        // encryptForStorage 업무 로직을 userIdEncryptionService에 위임한다
        String encryptedProviderId = userIdEncryptionService.encryptForStorage(providerId);
        String profileImg = kakaoAccountDto.kakao_account.profile.profile_image_url;

        // 카카오 로그인 사용자 정보를 담을 객체를 생성한다
        UserDto userDto = new UserDto();

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // UserByIdxx 데이터를 DB에서 조회한다
            UserDto savedUser = userMapper.getUserByIdxx(encryptedProviderId);

            // UserProv 업무 값을 userDto DTO에 설정한다
            userDto.setUserProv(AuthConstant.PROV_KAKAO);
            // USER_IDXX는 외부 OAuth 제공자의 고유 식별값이라 DB에는 평문을 남기지 않고 결정적 암호문으로 저장한다.
            // 로그인 조회도 같은 암호문으로 수행하므로 별도 복호화 없이 기존 사용자 식별이 가능하다.
            userDto.setUserIdxx(encryptedProviderId);
            // UserRole 업무 값을 userDto DTO에 설정한다
            userDto.setUserRole(AuthConstant.ROLE_USER);
            // savedUser 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
            if (StringUtil.isEmpty(savedUser)) {
                // 카카오 닉네임 대신 서비스 정책에 맞는 중복 없는 최초 닉네임을 발급한다
                userDto.setUserNick(nicknameGenerationService.setGeneratedNickname());
                // 신규 회원이 즉시 정상 이용 상태로 등록되도록 회원 상태를 설정한다
                userDto.setUserStat(Constant.USER_STAT_ACTIVE);
                // User 업무 값을 userMapper DTO에 설정한다
                userMapper.setUser(userDto);
                // ProfNumb 업무 값을 userDto DTO에 설정한다
                userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                // UserProfile 데이터를 DB에서 수정한다
                userMapper.uptUserProfile(userDto);
                // 처리 상태를 정보 로그로 남긴다
                log.info("Kakao user created. userNumb={}", userDto.getUserNumb());
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else {
                /*
                 * 소프트 탈퇴 회원은 Kakao 재로그인을 복구 의사로 판단한다.
                 * 기존 프로필과 관계 데이터는 유지하고 회원 상태와 탈퇴 일시만 정상 상태로 되돌린다.
                 */
                if (Constant.USER_STAT_WITHDRAWN.equals(savedUser.getUserStat())) {
                    // 복구할 회원 번호를 상태 변경 요청에 설정한다
                    savedUser.setUserNumb(savedUser.getUserNumb());
                    // 정상 이용 회원 상태를 설정한다
                    savedUser.setUserStat(Constant.USER_STAT_ACTIVE);
                    // 소프트 탈퇴 요청일을 제거한다
                    savedUser.setWthdDate(null);
                    // 영구 삭제 예정일을 제거한다
                    savedUser.setDeltDate(null);
                    // 로그인 완료 전에 회원 상태를 정상으로 복구한다
                    userMapper.uptUserStatus(savedUser);
                }

                // UserNumb 업무 값을 userDto DTO에 설정한다
                userDto.setUserNumb(savedUser.getUserNumb());
                // UserRole 업무 값을 userDto DTO에 설정한다
                userDto.setUserRole(savedUser.getUserRole());
                // 기존 사용자가 프로필에서 수정한 닉네임이 있으면 Kakao 기본 닉네임으로 다시 덮지 않고 DB 값을 사용한다.
                userDto.setUserNick(savedUser.getUserNick());
                // 로그인 세션에 반영할 현재 회원 상태를 설정한다
                userDto.setUserStat(savedUser.getUserStat());

                // savedUser.getProfNumb( 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
                if (StringUtil.isEmpty(savedUser.getProfNumb())) {
                    // ProfNumb 업무 값을 userDto DTO에 설정한다
                    userDto.setProfNumb(fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                    // UserProfile 데이터를 DB에서 수정한다
                    userMapper.uptUserProfile(userDto);
                }
            }

            // 처리 상태를 정보 로그로 남긴다
            log.info("Kakao login user resolved. userNumb={}", userDto.getUserNumb());
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 사용자 등록 일부만 커밋되지 않도록 로그인 쓰기 트랜잭션 전체를 롤백한다
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 실패 원인과 처리 대상을 오류 로그로 남긴다
            log.error("Kakao user save failed. message={}", e.getMessage());
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // createAccessToken 호출로 후속 처리에 필요한 객체를 생성한다
        String accessToken = jwtProvider.createAccessToken(userDto.getUserNumb(), userDto.getUserRole());
        // createRefreshToken 호출로 후속 처리에 필요한 객체를 생성한다
        String refreshToken = jwtProvider.createRefreshToken(userDto.getUserNumb());

        /*
         * 알림 발송 시 발신자 닉네임을 다시 DB에서 조회하지 않도록 로그인 시점의 최신 닉네임을 Refresh Token과 함께 저장한다.
         * 두 값은 TokenRedisService의 Lua 스크립트에서 같은 TTL로 원자 반영된다.
         */
        tokenRedisService.setLoginUserInfo(
                // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
                userDto.getUserNumb()
              , refreshToken
              , userDto.getUserNick()
              , userDto.getUserStat()
              , jwtProvider.getRefreshTokenValiditySeconds()
        );

        // 로그인 이력 저장 데이터를 담을 객체를 생성한다
        LoginHistoryDto loginHistoryDto = new LoginHistoryDto();
        // UserNumb 업무 값을 loginHistoryDto DTO에 설정한다
        loginHistoryDto.setUserNumb(userDto.getUserNumb());
        // LognDate 업무 값을 loginHistoryDto DTO에 설정한다
        loginHistoryDto.setLognDate(LocalDateTime.now());
        // LognIpxx 업무 값을 loginHistoryDto DTO에 설정한다
        loginHistoryDto.setLognIpxx(lognIpxx);
        // UserAgnt 업무 값을 loginHistoryDto DTO에 설정한다
        loginHistoryDto.setUserAgnt(StringUtil.cutString(userAgnt, USER_AGENT_MAX_LENGTH));
        // ProvCode 업무 값을 loginHistoryDto DTO에 설정한다
        loginHistoryDto.setProvCode(AuthConstant.PROV_KAKAO);
        // 아래 처리 단계의 업무 목적을 설명한다.
        loginHistoryMapper.setLoginHistory(loginHistoryDto);

        // 진단에 필요한 처리 상태를 디버그 로그로 남긴다
        log.debug("Kakao login JWT issued. userNumb={}", userDto.getUserNumb());
        // Kakao 계정 확인과 JWT 로그인 처리 결과를 성공 응답으로 반환한다
        return ResultData.success(TokenDto.of(accessToken, refreshToken));
    }
}
