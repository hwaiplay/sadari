package org.our.sadari.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.service.UserIdEncryptionService;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.common.util.XssUtil;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.user.auth.dto.KakaoAccountDto;
import org.our.sadari.user.auth.dto.KakaoTokenDto;
import org.our.sadari.user.auth.provider.KakaoAuthProvider;
import org.our.sadari.user.dto.UserDto;
import org.our.sadari.user.dto.UserWithdrawalDto;
import org.our.sadari.user.mapper.UserMapper;
import org.our.sadari.user.mapper.UserWithdrawalMapper;
import org.our.sadari.timer.service.ReadingTimerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * fileName       : UserWithdrawalServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : Kakao 재인증을 거친 계정 비활성화와 영구 삭제 대기 업무를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성 및 삭제 유예기간 적용
 * 2026-07-30        SeungHyeon.Kang    계정 상태·탈퇴 처리 정리
 * 2026-07-31        SeungHyeon.Kang    정지 회원의 계정 처리 요청 차단
 * 2026-08-13        SeungHyeon.Kang    정지 회원의 영구 탈퇴 허용과 식별값 해시 공통화
 * 2026-08-14        SeungHyeon.Kang    계정 상태 변경 전 독서 타이머 종료 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserWithdrawalServiceImpl implements UserWithdrawalService {

    // 탈퇴 재인증 요청 Redis 키 접두사
    private static final String WITHDRAWAL_STATE_PREFIX = "user:withdrawal:state:";
    // 탈퇴 재인증 요청 유효시간
    private static final Duration WITHDRAWAL_STATE_TTL = Duration.ofMinutes(10);
    // 기타 탈퇴 사유 코드
    private static final String WITHDRAWAL_REASON_OTHER = "OTHER";
    // 탈퇴 요청 완료 상태
    private static final String WITHDRAWAL_STATUS_COMPLETED = "COMPLETED";
    // Kakao 연결 해제 대기 상태
    private static final String WITHDRAWAL_STATUS_UNLINK_PENDING = "UNLINK_PENDING";
    // 영구 삭제 대기 상태
    private static final String WITHDRAWAL_STATUS_DELETE_PENDING = "DELETE_PENDING";
    // 영구 삭제 취소 상태
    private static final String WITHDRAWAL_STATUS_RESTORED = "RESTORED";

    // 회원 탈퇴 데이터 접근 객체
    private final UserWithdrawalMapper userWithdrawalMapper;
    // 회원 데이터 접근 객체
    private final UserMapper userMapper;
    // Kakao 인증 연동 제공 객체
    private final KakaoAuthProvider kakaoAuthProvider;
    // 사용자 식별값 암호화 서비스
    private final UserIdEncryptionService userIdEncryptionService;
    // 공통코드 메모리 조회 유틸리티
    private final CodeUtil codeUtil;
    // 로그인 토큰과 사용자 캐시 관리 서비스
    private final TokenRedisService tokenRedisService;
    // 탈퇴 재인증 상태 저장 Redis 연산 객체
    private final StringRedisTemplate redisTemplate;
    // 탈퇴 재인증 요청 직렬화 객체
    private final ObjectMapper objectMapper;
    // 관리자 정지 상태와 기간 만료 처리 서비스
    private final UserSuspensionService userSuspensionService;
    // 계정 상태 변경 시 저장하지 않은 프로필 임시 이미지를 정리할 파일 서비스
    private final FileService fileService;
    // 계정 상태 변경 직전 실행 중인 독서 시간을 확정하는 타이머 서비스
    private final ReadingTimerService readingTimerService;

    // 환경별 영구 삭제 유예기간
    @Value("${withdrawal.hard-delete-wait-days:30}")
    private long hardDeleteWaitDays;

    /**
     * 영구 삭제 유예기간 설정값이 유효한지 애플리케이션 시작 시 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @PostConstruct
    private void validateConfiguration() {

        // 음수 유예기간은 요청 시점보다 과거를 삭제 예정일로 만들기 때문에 실행을 차단한다
        if (hardDeleteWaitDays < 0) {
            // 잘못된 영구 삭제 유예기간이면 애플리케이션 시작을 중단한다
            throw new IllegalStateException("withdrawal.hard-delete-wait-days는 0 이상이어야 합니다.");
        }
    }

    /**
     * 회원 탈퇴 요청을 검증하고 Kakao 재인증 URL을 발급한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴를 요청한 회원 번호
     * @param request 탈퇴 유형과 사유
     * @return Kakao 재인증 URL
     */
    @Override
    public ResultData setWithdrawalRequest(Long userNumb, UserWithdrawalDto request) {

        // 회원 번호와 필수 탈퇴 항목이 없으면 재인증 요청을 생성하지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(request)
                || StringUtil.hasEmpty(request.getWthdType(), request.getWthdRson())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 공통코드에 등록되지 않은 탈퇴 유형이나 사유는 임의 요청으로 판단한다
        if (!codeUtil.existsCode("WTHD_TYPE", request.getWthdType())
                || !codeUtil.existsCode("WTHD_RSON", request.getWthdRson())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 정지 회원은 복구 가능한 비활성화만 제한하고 제재를 보존하는 영구 탈퇴는 허용한다
        UserDto requestUser = userMapper.getUserByNumb(userNumb);
        if (!StringUtil.isEmpty(requestUser)
                && Constant.USER_STAT_SUSPENDED.equals(requestUser.getUserStat())
                && !Constant.WITHDRAWAL_TYPE_HARD.equals(request.getWthdType())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 기타 사유를 선택하고 상세 사유를 입력하지 않은 요청은 저장하지 않는다
        if (WITHDRAWAL_REASON_OTHER.equals(request.getWthdRson())
                && StringUtil.isEmpty(request.getRsonCntn())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 탈퇴 사유 상세 내용이 DB 저장 기준을 넘으면 API를 직접 호출한 요청도 거절한다
        if (!StringUtil.isEmpty(request.getRsonCntn())
                && XssUtil.utf8ByteLength(request.getRsonCntn()) > Constant.WITHDRAWAL_REASON_MAX_BYTES) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // Redis에 저장할 탈퇴 요청에 로그인 회원 번호를 설정한다
        request.setUserNumb(userNumb);
        // OAuth 요청과 콜백을 일회성으로 연결할 예측 불가능한 상태값을 생성한다
        String state = UUID.randomUUID().toString();

        // 재인증 요청을 직렬화하고 제한된 시간 동안만 Redis에 보관한다
        try {
            // 탈퇴 요청 본문을 Redis 저장 문자열로 변환한다
            String requestJson = objectMapper.writeValueAsString(request);
            // 콜백 검증에 사용할 일회성 요청을 10분 동안 저장한다
            redisTemplate.opsForValue().set(getWithdrawalStateKey(state), requestJson, WITHDRAWAL_STATE_TTL);
        }

        // 직렬화나 Redis 저장 실패 시 탈퇴 절차를 시작하지 않는다
        catch (Exception e) {
            // 민감한 요청 내용 없이 저장 실패 원인만 기록한다
            log.error("회원 탈퇴 재인증 상태 저장 실패. userNumb={}, message={}", userNumb, e.getMessage());
            // "저장에 실패했어요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 일회성 상태값을 포함한 Kakao 재인증 URL을 설정한다
        request.setAuthUrl(kakaoAuthProvider.getKakaoAuthorizationUrl(state));
        // Kakao 재인증 화면으로 이동할 URL을 반환한다
        return ResultData.success(request);
    }

    /**
     * Kakao 재인증 결과를 검증하고 회원 탈퇴를 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param code Kakao OAuth 인가 코드
     * @param state 탈퇴 요청 일회성 상태값
     * @return 적용된 탈퇴 유형
     */
    @Transactional
    @Override
    public ResultData setWithdrawalCallback(String code, String state) {

        // 인가 코드나 일회성 상태값이 없으면 탈퇴 콜백을 거부한다
        if (StringUtil.hasEmpty(code, state)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 재사용을 막기 위해 Redis에서 요청을 읽은 직후 상태값을 제거한다
        String stateKey = getWithdrawalStateKey(state);
        String requestJson = redisTemplate.opsForValue().get(stateKey);
        // 사용했거나 만료된 OAuth 상태값을 즉시 제거한다
        redisTemplate.delete(stateKey);

        // Redis에 대응하는 요청이 없으면 위조되거나 만료된 콜백으로 판단한다
        if (StringUtil.isEmpty(requestJson)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // Kakao 계정과 탈퇴 요청을 검증할 객체를 준비한다
        UserWithdrawalDto request;
        KakaoTokenDto kakaoToken;
        KakaoAccountDto kakaoAccount;

        // 저장된 요청 복원과 Kakao 사용자 조회를 하나의 검증 단계로 처리한다
        try {
            // Redis에 저장한 탈퇴 요청을 업무 DTO로 복원한다
            request = objectMapper.readValue(requestJson, UserWithdrawalDto.class);
            // 재인증 인가 코드를 Kakao Access Token으로 교환한다
            kakaoToken = kakaoAuthProvider.getKakaoToken(code);
            // 재인증한 Kakao 계정 정보를 조회한다
            kakaoAccount = kakaoAuthProvider.getKakaoAccount(kakaoToken);
        }

        // 요청 복원이나 Kakao 통신에 실패하면 회원 데이터는 변경하지 않는다
        catch (Exception e) {
            // 개인정보를 제외한 재인증 실패 원인을 기록한다
            log.error("회원 탈퇴 Kakao 재인증 실패. message={}", e.getMessage());
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 재인증한 Kakao 식별값을 기존 DB 조회 형식으로 암호화한다
        String providerId = String.valueOf(kakaoAccount.id);
        String encryptedProviderId = userIdEncryptionService.encryptForStorage(providerId);
        // 탈퇴 요청 회원의 현재 DB 정보를 조회한다
        UserDto savedUser = userMapper.getUserByNumb(request.getUserNumb());

        // 현재 회원과 재인증한 Kakao 계정이 일치하지 않으면 탈퇴를 거부한다
        if (StringUtil.isEmpty(savedUser) || !encryptedProviderId.equals(savedUser.getUserIdxx())) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 재인증 사이에 정지됐으면 제재를 보존하는 영구 탈퇴만 계속 허용한다
        if (Constant.USER_STAT_SUSPENDED.equals(savedUser.getUserStat())
                && !Constant.WITHDRAWAL_TYPE_HARD.equals(request.getWthdType())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 탈퇴 이력에서 원본 OAuth 식별값을 복구할 수 없도록 SHA-256 해시만 저장한다
        request.setUserIdhs(userIdEncryptionService.hashForAudit(providerId));
        // 탈퇴 요청 시각을 회원 상태에 반영한다
        request.setRequDate(LocalDateTime.now());

        // Kakao 연결 해제가 실패하면 회원 데이터는 유지하고 재처리 가능한 이력만 남긴다
        try {
            // 재인증한 Access Token으로 Sadari와 Kakao 계정 연결을 해제한다
            kakaoAuthProvider.unlinkKakaoAccount(kakaoToken);
        }

        // 외부 연결 해제 실패는 회원 탈퇴 적용과 분리해 대기 이력으로 저장한다
        catch (Exception e) {
            // 연결 해제 재처리 대기 상태를 설정한다
            request.setWthdStat(WITHDRAWAL_STATUS_UNLINK_PENDING);
            // 운영 확인에 필요한 오류 내용만 제한 길이로 저장한다
            request.setErroCntn(StringUtil.cutString(e.getMessage(), 1000));
            // 연결 해제 실패 이력을 등록한다
            userWithdrawalMapper.setUserWithdrawal(request);
            // 연결 해제 재시도가 필요한 회원 번호를 기록한다
            log.warn("회원 탈퇴 Kakao 연결 해제 대기. userNumb={}", request.getUserNumb());
            // "저장에 실패했어요."
            return ResultData.fail(ResultEnum.COMMON_SAVE_REJECTED);
        }

        // 계정 비활성화와 영구 삭제 대기는 서로 다른 회원 상태와 이력 상태를 적용한다
        if (Constant.WITHDRAWAL_TYPE_SOFT.equals(request.getWthdType())) {
            // 계정 비활성화 이력을 완료 상태로 설정한다
            request.setWthdStat(WITHDRAWAL_STATUS_COMPLETED);
            // 즉시 처리된 시각을 완료일로 설정한다
            request.setProcDate(LocalDateTime.now());
            // 계정 비활성화 회원 상태를 적용한다
            applyWithdrawalStatus(request, Constant.USER_STAT_WITHDRAWN, null);
        }

        // 영구 탈퇴는 환경별 유예기간 동안 취소할 수 있는 삭제 대기 상태로 전환한다
        else {
            // 영구 삭제 대기 이력 상태를 설정한다
            request.setWthdStat(WITHDRAWAL_STATUS_DELETE_PENDING);
            // 요청일에 환경별 유예기간을 더해 영구 삭제 예정일을 설정한다
            request.setDeltDate(LocalDateTime.now().plusDays(hardDeleteWaitDays));
            // 영구 삭제 대기 회원 상태를 적용한다
            applyWithdrawalStatus(request, Constant.USER_STAT_DELETE_PENDING, request.getDeltDate());
        }

        // 탈퇴 처리 후 기존 Refresh Token과 닉네임 캐시를 제거한다
        tokenRedisService.delLoginUserInfo(request.getUserNumb());
        // 비활성화와 영구 삭제 대기 전환 모두 저장하지 않은 임시 프로필 이미지를 즉시 삭제한다
        fileService.delProfileDraftsOnCommit(request.getUserNumb());
        // 프론트엔드가 완료 화면을 구분할 수 있도록 탈퇴 유형을 반환한다
        return ResultData.success(request.getWthdType());
    }

    /**
     * 로그인 회원의 영구 삭제 대기 상태를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 영구 삭제 예정 정보
     */
    @Override
    public ResultData getWithdrawalStatus(Long userNumb) {

        // 로그인 회원 번호가 없으면 조회를 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 회원의 최신 영구 삭제 대기 이력을 조회한다
        UserWithdrawalDto pendingWithdrawal = userWithdrawalMapper.getPendingWithdrawal(userNumb);
        // 영구 삭제 대기 상태가 없음을 포함해 조회 결과를 반환한다
        return ResultData.success(pendingWithdrawal);
    }

    /**
     * 영구 삭제 대기 상태를 취소하고 회원을 복구한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 복구할 회원 번호
     * @return 복구 처리 결과
     */
    @Transactional
    @Override
    public ResultData uptWithdrawalCancel(Long userNumb) {

        // 취소할 영구 삭제 대기 이력을 조회한다
        UserWithdrawalDto pendingWithdrawal = userWithdrawalMapper.getPendingWithdrawal(userNumb);

        // 영구 삭제 대기 이력이 없으면 임의 취소 요청으로 판단한다
        if (StringUtil.isEmpty(pendingWithdrawal)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 취소 이력에 정상 복구 상태를 설정한다
        pendingWithdrawal.setWthdStat(WITHDRAWAL_STATUS_RESTORED);
        // 영구 삭제 대기 이력을 복구 완료로 변경한다
        userWithdrawalMapper.uptWithdrawalRestored(pendingWithdrawal);

        // 회원 상태 복구 요청 객체를 생성한다
        UserDto user = new UserDto();
        // 복구할 회원 번호를 설정한다
        user.setUserNumb(userNumb);
        // 영구 탈퇴 신청 중에도 남아 있는 관리자 정지를 우선 적용할 복구 상태를 계산한다
        String restoredUserStat = userSuspensionService.getWithdrawalCancelStatus(userNumb);
        // 남은 정지 효력을 반영한 회원 상태를 설정한다
        user.setUserStat(restoredUserStat);
        // 탈퇴 요청일을 제거한다
        user.setWthdDate(null);
        // 영구 삭제 예정일을 제거한다
        user.setDeltDate(null);
        // 회원을 정상 이용 상태로 복구한다
        userMapper.uptUserStatus(user);
        // 현재 로그인 세션도 즉시 정상 이용 상태로 변경한다
        tokenRedisService.uptUserStatus(userNumb, restoredUserStat);

        // 프론트엔드가 정지 안내 또는 정상 화면을 선택할 수 있도록 복구 상태를 반환한다
        return ResultData.success(restoredUserStat);
    }

    /**
     * 탈퇴 유형에 맞는 회원 상태와 연관 데이터 제한을 한 트랜잭션으로 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param request 저장할 탈퇴 이력
     * @param userStat 변경할 회원 상태
     * @param deleteDate 영구 삭제 예정일
     */
    private void applyWithdrawalStatus(UserWithdrawalDto request, String userStat
                                     , LocalDateTime deleteDate) {
        // 탈퇴 이력을 먼저 등록해 상태 변경의 업무 근거를 남긴다
        userWithdrawalMapper.setUserWithdrawal(request);

        // 계정이 비활성 상태가 되기 전에 실행 중인 독서 시간을 확정하고 세션을 완료한다
        readingTimerService.uptTimerWithdrawal(request.getUserNumb());

        // 회원 상태 변경 요청 객체를 생성한다
        UserDto user = new UserDto();
        // 탈퇴 회원 번호를 설정한다
        user.setUserNumb(request.getUserNumb());
        // 탈퇴 유형에 대응하는 회원 상태를 설정한다
        user.setUserStat(userStat);
        // 회원 탈퇴 요청일을 설정한다
        user.setWthdDate(LocalDateTime.now());
        // 영구 삭제 유형에만 삭제 예정일을 설정한다
        user.setDeltDate(deleteDate);
        // 회원 상태와 탈퇴 일시를 변경한다
        userMapper.uptUserStatus(user);
        // 세션 삭제와 분리된 Redis 계정 상태도 즉시 제한 상태로 갱신한다
        tokenRedisService.uptUserStatus(request.getUserNumb(), userStat);
        // 탈퇴 회원이 작성한 댓글을 삭제된 댓글 상태로 변경한다
        userMapper.uptUserReplyDeleted(request.getUserNumb());

        // 탈퇴 회원의 기존 독후감을 모두 비공개로 변경한다
        userWithdrawalMapper.uptUserReportPrivate(request.getUserNumb());
        // 탈퇴 회원의 독서 통계 공개를 해제하며 복귀 후 자동 공개하지 않는다
        userWithdrawalMapper.uptReadingStatsPrivate(request.getUserNumb(), Constant.COMM_NO);
        // 탈퇴 회원이 수신한 알림을 모두 삭제 상태로 변경한다
        userWithdrawalMapper.uptUserAlimDeleted(request.getUserNumb());
        // 탈퇴 회원의 브라우저 푸시 구독을 모두 비활성화한다
        userWithdrawalMapper.uptUserPushDisabled(request.getUserNumb());
        // 탈퇴 회원이 댓글에 등록한 좋아요를 삭제하며 복귀 시 자동 복원하지 않는다
        userWithdrawalMapper.delUserReplyLike(request.getUserNumb());
    }

    /**
     * 탈퇴 재인증 요청을 저장할 Redis 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param state OAuth 일회성 상태값
     * @return Redis 저장 키
     */
    private String getWithdrawalStateKey(String state) {
        // 탈퇴 요청 전용 접두사와 일회성 상태값을 조합해 반환한다
        return WITHDRAWAL_STATE_PREFIX + state;
    }
}
