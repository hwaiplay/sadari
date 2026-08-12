package org.our.sadari.global.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.dto.FileDto;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.scheduler.common.SchedulerLogSupport;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.UserHardDeleteMapper;
import org.our.sadari.global.security.jwt.TokenRedisService;
import org.our.sadari.user.dto.UserWithdrawalDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * fileName       : UserHardDeleteServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 영구 삭제 유예기간이 끝난 회원을 제한 건수만큼 삭제하고 실행 로그를 기록한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-08-06        SeungHyeon.Kang    영구 탈퇴 회원의 프로필과 배경 물리 파일 삭제 추가
 * 2026-08-11        SeungHyeon.Kang    영구 탈퇴 회원의 Redis 인증 정보 물리 삭제 추가
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserHardDeleteServiceImpl implements UserHardDeleteService {

    // 영구 삭제 대상 조회 최대 건수
    @Value("${scheduler.max-size:100}")
    private int maxSize;

    // 영구 삭제 대상 데이터 접근 객체
    private final UserHardDeleteMapper userHardDeleteMapper;
    // 영구 탈퇴 회원의 물리 파일 정리 서비스
    private final FileService fileService;
    // 영구 탈퇴 회원의 Redis 세션과 상태 캐시 정리 서비스
    private final TokenRedisService tokenRedisService;
    // 스케줄러 로그 안전 처리 객체
    private final SchedulerLogSupport schedulerLogSupport;

    /**
     * 영구 삭제 예정일이 지난 회원과 연관 데이터를 물리 삭제한다.
     *
     * @author SeungHyeon.Kang
     */
    @Transactional
    @Override
    public void delPendingUsers() {

        // 스케줄러 전체 실행 시간을 측정할 시작 시각을 기록한다
        long startNanoTime = System.nanoTime();
        // 삭제 대상 회원을 설정 최대 건수만큼 조회한다
        List<UserWithdrawalDto> targets = userHardDeleteMapper.getHardDeleteTargetList(maxSize);

        // 삭제 대상이 없으면 불필요한 스케줄러 로그를 만들지 않고 종료한다
        if (targets.isEmpty()) {
            // 영구 삭제 대상 없음 상태를 운영 로그에 기록한다
            log.info("영구 삭제 예정 회원이 없어 스케줄러를 종료합니다.");
            // 영구 삭제 대상 처리를 종료한다
            return;
        }

        // 스케줄러 실행 로그를 담을 객체를 생성한다
        SchedulerLogDto.SchedulerRunDto schedulerRun = new SchedulerLogDto.SchedulerRunDto();
        // 회원 영구 삭제 스케줄러 코드를 설정한다
        schedulerRun.setSchdCode(Constant.SCHEDULER_CODE_USER_HARD_DELETE);
        // 현재 실행 중인 메서드명을 설정한다
        schedulerRun.setMethName(Thread.currentThread().getStackTrace()[1].getMethodName());
        // 스케줄러 시작 시각을 설정한다
        schedulerRun.setStrtDate(LocalDateTime.now());
        // 조회된 삭제 대상 수를 설정한다
        schedulerRun.setTrgtCntt(targets.size());
        int successCnt = 0;
        int failureCnt = 0;
        RuntimeException failure = null;

        // FK 참조 순서에 맞춘 영구 삭제를 대상 회원별로 수행한다
        try {
            // 조회된 영구 삭제 대상 회원을 순차 처리한다
            for (UserWithdrawalDto target : targets) {
                // 프로시저가 파일 메타정보를 삭제하기 전에 커밋 후 사용할 물리 파일 경로를 조회한다
                List<FileDto> fileList = fileService.getFileListByRegiUser(target.getUserNumb());
                // 로그인 이력을 익명화하고 회원 연관 데이터와 회원 원본을 삭제한다
                userHardDeleteMapper.delHardDeleteUser(target.getUserNumb());
                // 회원 원본과 함께 모든 기기 세션 및 Redis 인증 캐시를 물리 삭제한다
                tokenRedisService.delAllUserInfo(target.getUserNumb());
                // 회원과 파일 메타정보 삭제가 커밋된 뒤 해당 회원의 로컬 물리 파일을 모두 삭제한다
                fileService.delFilesAfterCommit(fileList);
                // 탈퇴 요청 시 이미 정리된 임시 이미지가 남아 있는 경우를 방어적으로 다시 삭제한다
                fileService.delProfileDraftsOnCommit(target.getUserNumb());
                // 정상 삭제된 회원 수를 누적한다
                successCnt++;
            }
        }

        // 삭제 실패는 트랜잭션 전체 롤백과 스케줄러 실패 로그로 전환한다
        catch (RuntimeException e) {
            // 롤백되는 전체 대상 수를 실패 건수로 설정한다
            failureCnt = targets.size();
            // 롤백되는 성공 건수를 초기화한다
            successCnt = 0;
            // 실패 상세 로그에 전달할 원본 예외를 보관한다
            failure = e;
            // 회원 영구 삭제 실패 원인을 운영 로그에 기록한다
            log.error("영구 삭제 예정 회원 처리 중 오류가 발생했습니다.", e);
        }

        // 최종 성공 건수를 실행 로그에 설정한다
        schedulerRun.setSuccCntt(successCnt);
        // 최종 실패 건수를 실행 로그에 설정한다
        schedulerRun.setFailCntt(failureCnt);
        // 성공과 실패 건수에 따른 최종 실행 상태를 설정한다
        schedulerRun.setExecStat(schedulerLogSupport.getSchedulerExecStatus(successCnt, failureCnt));
        // 나노초 실행 시간을 밀리초 단위로 변환해 설정한다
        schedulerRun.setExecMsec(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanoTime));
        // 대상이 존재한 스케줄러 실행 로그를 등록한다
        Long runxNumb = schedulerLogSupport.setSchedulerLogSafely(schedulerRun);
        // 등록된 실행 번호를 종료 로그에 설정한다
        schedulerRun.setRunxNumb(runxNumb);

        // 영구 삭제 실패가 있으면 실패 상세를 실행 로그와 연결한다
        if (!StringUtil.isEmpty(failure)) {
            // 원본 예외 유형과 내용을 실패 상세 로그에 등록한다
            schedulerLogSupport.setSchedulerFailSafely(
                    runxNumb
                  , Constant.SCHEDULER_FAIL_EXCEPTION
                  , null
                  , null
                  , failure
            );
        }

        // 최종 실행 상태와 건수를 스케줄러 로그에 반영한다
        schedulerLogSupport.uptSchedulerLogSafely(schedulerRun);

        // 실패 시 트랜잭션을 롤백하도록 원본 예외를 다시 전달한다
        if (!StringUtil.isEmpty(failure)) {
            // 영구 삭제 트랜잭션 롤백을 위해 원본 예외를 반환한다
            throw failure;
        }
    }
}
