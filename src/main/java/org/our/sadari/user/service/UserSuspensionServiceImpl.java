package org.our.sadari.user.service;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.dto.UserStatusEventDto;
import org.our.sadari.global.scheduler.mapper.UserStatusEventMapper;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserSuspensionDto;
import org.our.sadari.user.mapper.UserSuspensionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * fileName       : UserSuspensionServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 정지 안내와 기간 만료 상태 복구를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    기간 만료 상태 변경 Outbox 전달 추가
 * 2026-07-30        SeungHyeon.Kang    정지 이력 부재 시 로그인 상태 캐시 보정
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSuspensionServiceImpl implements UserSuspensionService {

    // 사용자 정지 상태 데이터 접근 객체
    private final UserSuspensionMapper userSuspensionMapper;

    // 로그인 세션 회원 상태 캐시 관리 서비스
    private final TokenRedisService tokenRedisService;

    // 회원 상태 변경 Outbox 전달 데이터 접근 객체
    private final UserStatusEventMapper userStatusEventMapper;

    /**
     * 로그인 회원에게 내부 관리자 메모를 제외한 현재 정지 상태를 제공한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 현재 정지 상태
     */
    @Transactional
    @Override
    public ResultData getUserSuspension(Long userNumb) {

        // 인증되지 않은 요청은 다른 회원의 정지 상태를 조회할 수 없도록 거절한다
        if (StringUtil.isEmpty(userNumb)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 조회 시점에 기간이 끝났다면 사용자 요청을 계기로 즉시 상태를 복구한다
        uptExpiredSuspension(userNumb);
        // 관리자 내부 메모를 포함하지 않는 최신 활성 정지 정보를 조회한다
        UserSuspensionDto suspension = userSuspensionMapper.getLatestActiveSuspension(userNumb);

        // 활성 정지 이력이 없으면 해제 또는 만료된 DB 상태로 남은 정지 캐시를 보정한다
        if (StringUtil.isEmpty(suspension)) {
            // 정지 해제 이후의 현재 회원 상태를 DB에서 조회한다
            String userStat = userSuspensionMapper.getUserStatus(userNumb);

            // DB도 정지 상태라면 이력 불일치이므로 접근 제한을 임의로 해제하지 않는다
            if (!StringUtil.isEmpty(userStat) && !Constant.USER_STAT_SUSPENDED.equals(userStat)) {
                // 정지 화면과 일반 화면 사이의 반복 이동을 막도록 커밋 후 Redis 상태를 보정한다
                syncUserStatusAfterCommit(userNumb, userStat);
            }

        }

        // 정지 해제 여부를 화면이 판단할 수 있도록 Null을 포함한 조회 결과를 반환한다
        return ResultData.success(suspension);
    }

    /**
     * 기간 정지의 종료 예정일이 지났으면 이력과 회원 상태를 함께 복구한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 회원 번호
     * @return 기간 만료로 상태를 변경했으면 true
     */
    @Transactional
    @Override
    public boolean uptExpiredSuspension(Long userNumb) {

        // 회원 번호가 없으면 정지 만료 상태를 변경하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            // 변경 대상이 없음을 반환한다
            return false;
        }

        // 현재 활성 상태로 남아 있는 최신 정지 이력을 조회한다
        UserSuspensionDto suspension = userSuspensionMapper.getLatestActiveSuspension(userNumb);

        // 무기한 정지이거나 종료 예정일 전이면 현재 정지 상태를 유지한다
        if (StringUtil.isEmpty(suspension)
                || !Constant.SUSPENSION_TYPE_PERIOD.equals(suspension.getSpndType())
                || StringUtil.isEmpty(suspension.getEndxDate())
                || suspension.getEndxDate().isAfter(LocalDateTime.now())) {
            // 기간 만료 변경이 없음을 반환한다
            return false;
        }

        // 동시에 실행된 요청 중 한 건만 정지 만료 이력을 변경한다
        int updatedCount = userSuspensionMapper.uptSuspensionExpired(suspension.getSpndNumb());

        // 다른 요청이 먼저 만료 처리했다면 회원 상태를 중복 변경하지 않는다
        if (updatedCount < 1) {
            // 현재 요청에서 처리한 만료가 없음을 반환한다
            return false;
        }

        // 영구 삭제 대기 같은 우선 상태가 없을 때만 정지 직전 상태를 복구한다
        int restoredCount =
                userSuspensionMapper.uptUserStatusAfterSuspension(userNumb, suspension.getPrevStat());
        // 영구 삭제 대기 같은 우선 상태가 없을 때만 로그인 세션도 복구 상태로 갱신한다
        if (restoredCount > 0) {
            // 실제 회원 상태가 복구된 정지 이력을 사용자 서버 반영 대기 상태로 되돌린다
            // 반영 대기 상태를 기록하지 못하면 상태 복구와 전달 이벤트를 함께 롤백한다
            if (userSuspensionMapper.uptSuspensionSyncPending(suspension.getSpndNumb()) != 1) {
                // 관리자 화면에 잘못된 완료 상태가 남지 않도록 현재 처리를 실패시킨다
                throw new IllegalStateException("회원 정지 동기화 대기 상태 변경에 실패했습니다.");
            }
            // 즉시 Redis 반영이 실패해도 스케줄러가 다시 처리하도록 전달 이벤트를 저장한다
            setUserStatusEvent(userNumb, suspension.getSpndNumb());
            // DB 복구가 확정된 뒤 Redis가 먼저 제한 상태를 풀지 않도록 커밋 후 동기화한다
            syncUserStatusAfterCommit(userNumb, suspension.getPrevStat());
        }
        // 기간 정지 만료 처리가 완료됐음을 반환한다
        return true;
    }

    /**
     * 기간 만료 회원 상태를 사용자 서버가 다시 반영하도록 Outbox 이벤트를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 상태가 변경된 회원 번호
     * @param spndNumb 만료된 정지 이력 번호
     */
    private void setUserStatusEvent(Long userNumb, Long spndNumb) {

        // 현재 회원 상태를 다시 조회할 사용자 서버 전달 이벤트를 생성한다
        UserStatusEventDto event = new UserStatusEventDto();
        // 사용자 서버가 지원하는 회원 상태 변경 이벤트 유형을 설정한다
        event.setEvntType(Constant.EVENT_TYPE_USER_STATUS_CHANGED);
        // 처리 시점의 현재 회원 상태를 조회할 회원 번호를 설정한다
        event.setUserNumb(userNumb);
        // 실제 반영 완료 상태를 기록할 정지 이력 번호를 설정한다
        event.setSpndNumb(spndNumb);
        // 상태 복구와 전달 이벤트가 같은 트랜잭션으로 확정되도록 저장한다
        if (userStatusEventMapper.setUserStatusEvent(event) != 1) {
            // 전달 이벤트 없이 회원 상태만 복구되는 일을 막기 위해 현재 트랜잭션을 롤백한다
            throw new IllegalStateException("회원 상태 변경 Outbox 이벤트 등록에 실패했습니다.");
        }

    }

    /**
     * 영구 탈퇴 취소 시 남아 있는 정지 효력을 우선 적용한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 영구 탈퇴를 취소할 회원 번호
     * @return 복구할 회원 상태
     */
    @Transactional
    @Override
    public String getStatusAfterWithdrawalCancel(Long userNumb) {

        // 취소 시점에 기간이 끝난 정지 이력을 먼저 만료 처리한다
        uptExpiredSuspension(userNumb);
        // 아직 효력이 있는 기간 또는 무기한 정지가 남아 있는지 조회한다
        UserSuspensionDto suspension = userSuspensionMapper.getLatestActiveSuspension(userNumb);

        // 유효한 관리자 정지가 남아 있으면 정상 상태보다 정지 상태를 우선한다
        if (!StringUtil.isEmpty(suspension)) {
            // 관리자 정지 상태를 반환한다
            return Constant.USER_STAT_SUSPENDED;
        }

        // 남은 정지가 없으면 정상 이용 상태를 반환한다
        return Constant.USER_STAT_ACTIVE;
    }

    /**
     * 회원 상태 변경 트랜잭션이 커밋된 뒤 로그인 세션 상태를 동기화한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 상태를 동기화할 회원 번호
     * @param userStat 커밋 후 적용할 회원 상태
     */
    private void syncUserStatusAfterCommit(Long userNumb, String userStat) {

        // 트랜잭션 동기화를 사용할 수 있으면 DB 커밋 뒤에만 제한 상태를 해제한다
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 현재 정지 만료 트랜잭션에 커밋 후 Redis 작업을 등록한다
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                /**
                 * DB 회원 상태 복구가 커밋된 뒤 Redis 회원 상태를 같은 값으로 변경한다
                 *
                 * @author SeungHyeon.Kang
                 */
                @Override
                public void afterCommit() {

                    // 커밋된 회원 상태를 현재 로그인 세션에 반영한다
                    tokenRedisService.uptUserStatus(userNumb, userStat);
                }
            });
            // 커밋 후 동기화가 등록됐으므로 즉시 Redis를 변경하지 않고 종료한다
            return;
        }

        // 트랜잭션 밖에서 호출된 경우에는 검증된 회원 상태를 즉시 동기화한다
        tokenRedisService.uptUserStatus(userNumb, userStat);
    }
}
