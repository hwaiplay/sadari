package org.our.sadari.user.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.UUID;
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
import org.our.sadari.user.mapper.UserWithdrawalMapper;
import org.our.sadari.user.service.NicknameGenerationService;
import org.our.sadari.user.service.UserSuspensionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

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
 * 2026-07-30        SeungHyeon.Kang    온보딩·계정 복귀·정지 처리
 * 2026-08-13        SeungHyeon.Kang    재가입 차단·OAuth 예외 처리
 * 2026-08-22        SeungHyeon.Kang    Kakao 기본 프로필 제외
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
    // 회원 정지 기간 만료와 로그인 상태 동기화 서비스
    private final UserSuspensionService userSuspensionService;
    // 탈퇴 식별 이력과 이용 정지를 연결해 재가입 가능 여부를 조회하는 데이터 접근 객체
    private final UserWithdrawalMapper userWithdrawalMapper;

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

        // 만료되거나 재사용된 인가 코드 등 Kakao가 거절한 인증 요청은 공통 인증 실패로 변환한다
        catch (HttpClientErrorException e) {
            // Kakao 오류 응답 본문과 인가 코드를 제외하고 상태 코드만 경고 로그에 남긴다
            log.warn("Kakao OAuth request rejected. status={}", e.getStatusCode().value());
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // Kakao 서버 오류와 연결 실패도 원시 통신 예외가 컨트롤러 밖으로 전파되지 않도록 변환한다
        catch (RestClientException e) {
            // 외부 응답 본문이나 인증값을 기록하지 않고 장애 종류만 오류 로그에 남긴다
            log.error("Kakao OAuth request failed. type={}", e.getClass().getSimpleName());
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // Kakao 성공 응답을 DTO로 변환할 수 없으면 공통 인증 실패로 변환한다
        catch (JsonProcessingException e) {
            // 외부 응답 원문을 제외하고 파싱 실패 메시지만 오류 로그에 남긴다
            log.error("Kakao OAuth response parse failed. message={}", e.getMessage());
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 입력값을 문자열 표현으로 변환한다
        String providerId = String.valueOf(kakaoAccountDto.id);
        // encryptForStorage 업무 로직을 userIdEncryptionService에 위임한다
        String encryptedProviderId = userIdEncryptionService.encryptForStorage(providerId);
        // Kakao 기본 실루엣은 서비스의 실제 사용자 프로필 사진으로 저장하지 않는다
        String profileImg = kakaoAccountDto.kakao_account.profile.is_default_image
                ? null
                : kakaoAccountDto.kakao_account.profile.profile_image_url;

        // 카카오 로그인 사용자 정보를 담을 객체를 생성한다
        UserDto userDto = new UserDto();
        // 일반 로그인과 비활성화 계정 복귀 로그인을 구분할 상태를 초기화한다
        boolean accountReactivated = false;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // UserByIdxx 데이터를 DB에서 조회한다
            UserDto savedUser = userMapper.getUserByIdxx(encryptedProviderId);

            // 기간 정지 종료 뒤 같은 카카오 계정으로 로그인하면 기존 계정을 복구한 뒤 상태를 다시 조회한다
            if (!StringUtil.isEmpty(savedUser)
                    && Constant.USER_STAT_SUSPENDED.equals(savedUser.getUserStat())
                    && userSuspensionService.uptExpiredSuspension(savedUser.getUserNumb())) {
                // 만료 처리로 변경된 최신 회원 상태를 다시 조회한다
                savedUser = userMapper.getUserByIdxx(encryptedProviderId);
            }

            // UserProv 업무 값을 userDto DTO에 설정한다
            userDto.setUserProv(AuthConstant.PROV_KAKAO);
            // USER_IDXX는 외부 OAuth 제공자의 고유 식별값이라 DB에는 평문을 남기지 않고 결정적 암호문으로 저장한다.
            // 로그인 조회도 같은 암호문으로 수행하므로 별도 복호화 없이 기존 사용자 식별이 가능하다.
            userDto.setUserIdxx(encryptedProviderId);
            // UserRole 업무 값을 userDto DTO에 설정한다
            userDto.setUserRole(AuthConstant.ROLE_USER);
            // savedUser 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
            if (StringUtil.isEmpty(savedUser)) {
                // 과거 탈퇴 계정의 회원 번호 중 하나라도 유효한 정지가 남아 있으면 새 회원 번호를 발급하지 않는다
                String userIdHash = userIdEncryptionService.hashForAudit(providerId);
                if (userWithdrawalMapper.getActiveSuspCntByIdHash(userIdHash) > 0) {
                    // 관리자 해제 전에는 같은 Kakao 계정의 재가입과 로그인을 차단한다
                    return ResultData.fail(ResultEnum.AUTH_WITHDRAWN_SUSPENDED);
                }

                // 카카오 닉네임 대신 서비스 정책에 맞는 중복 없는 최초 닉네임을 발급한다
                userDto.setUserNick(nicknameGenerationService.setGeneratedNickname());
                // 신규 회원이 즉시 정상 이용 상태로 등록되도록 회원 상태를 설정한다
                userDto.setUserStat(Constant.USER_STAT_ACTIVE);
                // 신규 회원이 닉네임을 확정할 때까지 웰컴 화면을 유지하도록 온보딩 상태를 설정한다
                userDto.setOnbdYsno(Constant.COMM_NO);
                // User 업무 값을 userMapper DTO에 설정한다
                userMapper.setUser(userDto);
                // ProfNumb 업무 값을 userDto DTO에 설정한다
                userDto.setProfNumb(StringUtil.isEmpty(profileImg)
                        ? null
                        : fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
                // UserProfile 데이터를 DB에서 수정한다
                userMapper.uptUserProfile(userDto);
                // 처리 상태를 정보 로그로 남긴다
                log.info("Kakao user created. userNumb={}", userDto.getUserNumb());
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else {
                /*
                 * 비활성화 회원은 Kakao 재로그인을 계정 재활성화 의사로 판단한다.
                 * 기존 프로필과 관계 데이터는 유지하고 회원 상태와 탈퇴 일시만 정상 상태로 되돌린다.
                 */
                if (Constant.USER_STAT_WITHDRAWN.equals(savedUser.getUserStat())) {
                    // 복구할 회원 번호를 상태 변경 요청에 설정한다
                    savedUser.setUserNumb(savedUser.getUserNumb());
                    // 정상 이용 회원 상태를 설정한다
                    savedUser.setUserStat(Constant.USER_STAT_ACTIVE);
                    // 비활성화 요청일을 제거한다
                    savedUser.setWthdDate(null);
                    // 영구 삭제 예정일을 제거한다
                    savedUser.setDeltDate(null);
                    // 로그인 완료 전에 회원 상태를 정상으로 복구한다
                    userMapper.uptUserStatus(savedUser);
                    // OAuth 완료 화면이 일반 로그인과 구분해 복귀 정책을 안내하도록 상태를 기록한다
                    accountReactivated = true;
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
                if (!Constant.USER_STAT_SUSPENDED.equals(savedUser.getUserStat())
                        && StringUtil.isEmpty(savedUser.getProfNumb())) {
                    // ProfNumb 업무 값을 userDto DTO에 설정한다
                    userDto.setProfNumb(StringUtil.isEmpty(profileImg)
                            ? null
                            : fileService.setKakaoProfileImage(profileImg, providerId, userDto.getUserNumb()));
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

        // 이번 브라우저 로그인을 다른 기기 세션과 구분할 식별자를 생성한다
        String sessionId = UUID.randomUUID().toString();
        // 동일 기기 세션 식별자를 포함한 Access Token을 생성한다
        String accessToken = jwtProvider.createAccessToken(userDto.getUserNumb(), userDto.getUserRole(), sessionId);
        // 동일 기기 세션 식별자를 포함한 Refresh Token을 생성한다
        String refreshToken = jwtProvider.createRefreshToken(userDto.getUserNumb(), sessionId);

        /*
         * 알림 발송 시 발신자 닉네임을 다시 DB에서 조회하지 않도록 로그인 시점의 최신 닉네임을 Refresh Token과 함께 저장한다.
         * 두 값은 TokenRedisService의 Lua 스크립트에서 같은 TTL로 원자 반영된다.
         */
        tokenRedisService.setLoginUserInfo(
                // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
                userDto.getUserNumb()
              , sessionId
              , refreshToken
              , userDto.getUserNick()
              , userDto.getUserStat()
              , jwtProvider.getRefreshTokenValidSec()
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
        return ResultData.success(TokenDto.of(accessToken, refreshToken, accountReactivated));
    }
}
