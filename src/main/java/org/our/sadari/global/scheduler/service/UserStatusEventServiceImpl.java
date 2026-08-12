package org.our.sadari.global.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.dto.UserStatusEventDto;
import org.our.sadari.global.scheduler.mapper.UserStatusEventMapper;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * fileName       : UserStatusEventServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 회원 상태 변경 Outbox를 현재 DB 상태로 사용자 Redis에 반영한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    성공 이벤트를 정지 동기화 완료 후 삭제
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserStatusEventServiceImpl implements UserStatusEventService {

    // 한 실행에서 처리할 Outbox 이벤트 최대 건수
    @Value("${scheduler.max-size:100}")
    private int maxSize;

    // 회원 상태 변경 Outbox 데이터 접근 객체
    private final UserStatusEventMapper userStatusEventMapper;

    // 로그인 세션 회원 상태 캐시 관리 서비스
    private final TokenRedisService tokenRedisService;

    // 스케줄러 실행 결과 안전 기록 객체
    private final SchedulerLogSupport schedulerLogSupport;

    /**
     * Outbox 이벤트별 현재 회원 상태를 Redis에 반영하고 성공한 전달 이벤트를 삭제한다
     *
     * @author SeungHyeon.Kang
     */
    @Transactional
    @Override
    public void syncUserStatusEvents() {

        // 스케줄러 전체 실행 시간을 측정할 시작 시각을 기록한다
        long startNanoTime = System.nanoTime();
        // 스케줄러 실행 로그에 남길 실제 시작 일시를 기록한다
        LocalDateTime startDate = LocalDateTime.now();
        // 등록 순서대로 이번 실행의 처리 대상 이벤트를 조회한다
        List<UserStatusEventDto> eventList = userStatusEventMapper.getUserStatusEventList(maxSize);

        // 처리할 상태 변경이 없으면 운영 로그와 스케줄러 이력을 만들지 않는다
        if (StringUtil.isEmpty(eventList)) {
            // 대상 없음 상태를 정보 로그로 남긴다
            log.info("회원 상태 동기화 Outbox가 비어 있어 스케줄러를 종료합니다.");
            // 처리할 이벤트가 없는 스케줄러 실행을 종료한다
            return;
        }

        int successCnt = 0;
        int failureCnt = 0;
        RuntimeException firstFailure = null;

        // 한 이벤트 실패가 다른 회원의 상태 동기화를 막지 않도록 건별로 처리한다
        for (UserStatusEventDto event : eventList) {
            // Redis 장애와 잘못된 이벤트 유형을 다음 실행 재시도 대상으로 남기기 위한 블록이다
            try {
                // 관리자 서비스가 등록한 회원 상태 변경 이벤트만 처리한다
                if (!Constant.EVENT_TYPE_USER_STATUS_CHANGED.equals(event.getEvntType())) {
                    throw new IllegalArgumentException("지원하지 않는 회원 상태 이벤트 유형입니다.");
                }

                // 영구 삭제로 회원 원본이 없으면 남은 로그인 정보를 제거한다
                if (StringUtil.isEmpty(event.getUserStat())) {
                    // 삭제된 회원의 모든 기기 세션과 상태 캐시를 함께 제거한다
                    tokenRedisService.delAllUserInfo(event.getUserNumb());
                } else {
                    // 처리 시점의 DB 회원 상태를 기존 로그인 세션 TTL로 Redis에 반영한다
                    tokenRedisService.uptUserStatus(event.getUserNumb(), event.getUserStat());
                }

                // 더 최신 전달 이벤트가 없을 때 정지 이력에 실제 사용자 서버 반영 완료를 기록한다
                userStatusEventMapper.uptSuspensionSyncDone(event.getSpndNumb(), event.getEvntNumb());
                // Redis와 정지 이력 처리가 끝난 전달 이벤트만 삭제해 장애 시 다음 주기에 재시도한다
                userStatusEventMapper.delUserStatusEvent(event.getEvntNumb());
                // 정상 처리된 이벤트 수를 누적한다
                successCnt++;
            }

            // 실패 이벤트는 Outbox에 남기고 나머지 회원 처리를 계속한다
            catch (RuntimeException e) {
                // 실패 이벤트 수를 누적한다
                failureCnt++;
                // 실패 상세 로그에 사용할 첫 번째 예외를 보관한다
                if (StringUtil.isEmpty(firstFailure)) {
                    // 대표 실패 원인을 실행 로그에 연결한다
                    firstFailure = e;
                }
                // 사용자 식별값과 이벤트 번호만 남겨 재처리 대상을 추적한다
                log.error("회원 상태 Outbox 처리에 실패했습니다. 이벤트 번호={}, 회원 번호={}", event.getEvntNumb(), event.getUserNumb(), e);
            }
        }

        // 스케줄러 실행 결과를 담을 객체를 생성한다
        SchedulerLogDto.SchedulerRunDto schedulerRun = new SchedulerLogDto.SchedulerRunDto();
        // 회원 상태 동기화 스케줄러 코드를 설정한다
        schedulerRun.setSchdCode(Constant.SCHEDULER_CODE_USER_STATUS_SYNC);
        // 현재 실행 중인 메서드명을 설정한다
        schedulerRun.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        // 스케줄러 시작 시각을 설정한다
        schedulerRun.setStrtDate(startDate);
        // 조회된 Outbox 이벤트 수를 설정한다
        schedulerRun.setTrgtCntt(eventList.size());
        // 정상 처리된 이벤트 수를 설정한다
        schedulerRun.setSuccCntt(successCnt);
        // 다음 주기에 재시도할 이벤트 수를 설정한다
        schedulerRun.setFailCntt(failureCnt);
        // 성공과 실패 건수로 최종 실행 상태를 설정한다
        schedulerRun.setExecStat(schedulerLogSupport.getSchedulerExecStatus(successCnt, failureCnt));
        // 스케줄러 전체 실행 시간을 밀리초 단위로 설정한다
        schedulerRun.setExecMsec(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
        // 처리 대상이 있는 실행의 마스터 로그를 등록한다
        Long runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRun);
        // 등록된 실행 번호를 종료 로그에 설정한다
        schedulerRun.setRunxNumb(runxNumb);

        // 실패가 있으면 첫 번째 원인을 대표 상세 로그로 기록한다
        if (!StringUtil.isEmpty(firstFailure)) {
            // 재시도 대상의 대표 예외를 스케줄러 실패 상세에 연결한다
            schedulerLogSupport.setSchedulerFailSafely(
                    runxNumb
                  , Constant.SCHEDULER_FAIL_EXCEPTION
                  , null
                  , null
                  , firstFailure
            );
        }

        // 최종 성공과 실패 건수를 스케줄러 로그에 반영한다
        schedulerLogSupport.uptSchedulerLogSafely(schedulerRun);
    }
}
