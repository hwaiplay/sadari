package org.our.sadari.global.scheduler.service;

import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.SchedulerLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스케줄러 업무 트랜잭션과 독립적으로 실행 요약 및 실패 상세 로그를 저장합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@Transactional(readOnly = true)
public class SchedulerLogServiceImpl implements SchedulerLogService {

    private static final int SCHEDULER_CODE_MAX_LENGTH = 50;
    private static final int METHOD_NAME_MAX_LENGTH = 200;
    private static final int EXECUTION_STATUS_MAX_LENGTH = 30;
    private static final int FAILURE_TYPE_MAX_LENGTH = 30;
    private static final int RESULT_MESSAGE_MAX_LENGTH = 1000;
    private static final int ERROR_TYPE_MAX_LENGTH = 500;
    private static final int ERROR_CONTENT_MAX_LENGTH = 4000;

    private final SchedulerLogMapper schedulerLogMapper;

    /**
     * 스케줄러 로그 Mapper를 주입받아 로그 서비스 구현체를 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param schedulerLogMapper 스케줄러 로그 등록 및 수정 Mapper
     */
    public SchedulerLogServiceImpl(SchedulerLogMapper schedulerLogMapper) {
        this.schedulerLogMapper = schedulerLogMapper;
    }

    /**
     * 스케줄러 실행 시작 로그를 별도 트랜잭션으로 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param schedulerRunDto 실행 시작 정보
     * @return TL_SCLOGX_SEQ로 발급된 실행 번호
     * @throws IllegalArgumentException 필수 실행 정보가 없을 경우 발생
     * @throws IllegalStateException 로그가 한 건 등록되지 않거나 실행 번호가 발급되지 않은 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long setSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 로그 코드, 메서드명, 실행 상태가 없으면 관리자 화면에서 어떤 실행인지 식별할 수 없어 등록을 중단한다.
        if (schedulerRunDto == null
                || StringUtil.isEmpty(schedulerRunDto.getSchdCode())
                || StringUtil.isEmpty(schedulerRunDto.getMethName())
                || StringUtil.isEmpty(schedulerRunDto.getExecStat())) {
            throw new IllegalArgumentException("스케줄러 실행 로그의 필수 정보가 없습니다.");
        }

        schedulerRunDto.setSchdCode(
                StringUtil.normalizePlainText(schedulerRunDto.getSchdCode(), SCHEDULER_CODE_MAX_LENGTH)
        );
        schedulerRunDto.setMethName(
                StringUtil.normalizePlainText(schedulerRunDto.getMethName(), METHOD_NAME_MAX_LENGTH)
        );
        schedulerRunDto.setExecStat(
                StringUtil.normalizePlainText(schedulerRunDto.getExecStat(), EXECUTION_STATUS_MAX_LENGTH)
        );

        int resultCnt = schedulerLogMapper.setSchedulerLog(schedulerRunDto);

        // selectKey가 실행 번호를 채우고 INSERT가 정확히 한 건 반영돼야 이후 실패 로그와 종료 상태를 연결할 수 있다.
        if (resultCnt != 1 || schedulerRunDto.getRunxNumb() == null) {
            throw new IllegalStateException("스케줄러 실행 로그 등록 결과가 올바르지 않습니다.");
        }

        return schedulerRunDto.getRunxNumb();
    }

    /**
     * 스케줄러 실행의 최종 결과를 별도 트랜잭션으로 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param schedulerRunDto 실행 종료 정보
     * @throws IllegalArgumentException 실행 번호나 최종 상태 및 건수 정보가 올바르지 않을 경우 발생
     * @throws IllegalStateException 수정 대상 실행 로그가 존재하지 않을 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uptSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 음수 건수나 실행 시간은 실제 처리 결과가 아니므로 TL_SCLOGX를 수정하기 전에 차단한다.
        if (schedulerRunDto == null
                || schedulerRunDto.getRunxNumb() == null
                || StringUtil.isEmpty(schedulerRunDto.getExecStat())
                || schedulerRunDto.getTrgtCntt() < 0
                || schedulerRunDto.getSuccCntt() < 0
                || schedulerRunDto.getFailCntt() < 0
                || schedulerRunDto.getExecMsec() == null
                || schedulerRunDto.getExecMsec() < 0) {
            throw new IllegalArgumentException("스케줄러 실행 로그의 종료 정보가 올바르지 않습니다.");
        }

        schedulerRunDto.setExecStat(
                StringUtil.normalizePlainText(schedulerRunDto.getExecStat(), EXECUTION_STATUS_MAX_LENGTH)
        );

        int resultCnt = schedulerLogMapper.uptSchedulerLog(schedulerRunDto);

        // 시작 로그가 없거나 이미 삭제된 실행 번호라면 종료 상태가 유실되므로 호출부에 실패를 알린다.
        if (resultCnt != 1) {
            throw new IllegalStateException("수정할 스케줄러 실행 로그가 존재하지 않습니다.");
        }
    }

    /**
     * 스케줄러 실패 한 건을 별도 트랜잭션으로 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param schedulerFailDto 실패 상세 정보
     * @throws IllegalArgumentException 실행 번호나 실패 유형이 없을 경우 발생
     * @throws IllegalStateException 실패 로그가 한 건 등록되지 않은 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setSchedulerFail(SchedulerLogDto.SchedulerFailDto schedulerFailDto) {
        // 실행 번호와 실패 유형이 없으면 복합키 순번과 실패 의미를 결정할 수 없어 저장하지 않는다.
        if (schedulerFailDto == null
                || schedulerFailDto.getRunxNumb() == null
                || StringUtil.isEmpty(schedulerFailDto.getFailType())) {
            throw new IllegalArgumentException("스케줄러 실패 로그의 필수 정보가 없습니다.");
        }

        schedulerFailDto.setFailType(
                StringUtil.normalizePlainText(schedulerFailDto.getFailType(), FAILURE_TYPE_MAX_LENGTH)
        );
        schedulerFailDto.setRsltMesg(
                StringUtil.normalizePlainText(schedulerFailDto.getRsltMesg(), RESULT_MESSAGE_MAX_LENGTH)
        );
        schedulerFailDto.setErroType(
                StringUtil.normalizePlainText(schedulerFailDto.getErroType(), ERROR_TYPE_MAX_LENGTH)
        );
        schedulerFailDto.setErroCntn(
                StringUtil.normalizePlainText(schedulerFailDto.getErroCntn(), ERROR_CONTENT_MAX_LENGTH)
        );

        int resultCnt = schedulerLogMapper.setSchedulerFail(schedulerFailDto);

        // MAX+1로 계산한 복합키를 사용해 정확히 한 건이 저장돼야 실패 건수와 상세 로그가 일치한다.
        if (resultCnt != 1) {
            throw new IllegalStateException("스케줄러 실패 로그 등록 결과가 올바르지 않습니다.");
        }
    }
}
