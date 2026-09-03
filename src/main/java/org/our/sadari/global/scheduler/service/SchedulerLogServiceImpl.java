package org.our.sadari.global.scheduler.service;

import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;
import org.our.sadari.global.scheduler.mapper.SchedulerLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : SchedulerLogServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 스케줄러 업무 로직을 구현함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@Service
@Transactional(readOnly = true)
public class SchedulerLogServiceImpl implements SchedulerLogService {

    // 스케줄러 코드 최대 길이 설정값
    private static final int SCHEDULER_CODE_MAX_LENGTH = 50;
    // 메서드 명칭 최대 길이 설정값
    private static final int METHOD_NAME_MAX_LENGTH = 200;
    // 실행 상태 최대 길이 설정값
    private static final int EXECUTION_STATUS_MAX_LENGTH = 30;
    // 실패 유형 최대 길이 설정값
    private static final int FAILURE_TYPE_MAX_LENGTH = 30;
    // 결과 메시지 최대 길이 설정값
    private static final int RESULT_MESSAGE_MAX_LENGTH = 1000;
    // 오류 유형 최대 길이 설정값
    private static final int ERROR_TYPE_MAX_LENGTH = 500;
    // 오류 내용 최대 길이 설정값
    private static final int ERROR_CONTENT_MAX_LENGTH = 4000;

    // SchedulerLog 데이터 접근 객체
    private final SchedulerLogMapper schedulerLogMapper;

    /**
     * 스케줄러 로그 Mapper를 주입받아 로그 서비스 구현체를 생성
     *
     * @author SeungHyeon.Kang
     * @param schedulerLogMapper 스케줄러 로그 등록 및 수정 Mapper
     */
    public SchedulerLogServiceImpl(SchedulerLogMapper schedulerLogMapper) {

        this.schedulerLogMapper = schedulerLogMapper;
    }

    /**
     * 스케줄러 실행 시작 로그를 별도 트랜잭션으로 등록
     *
     * @author SeungHyeon.Kang
     * @param schedulerRunDto 실행 시작 정보
     * @return TL_SCLOGX_SEQ로 발급된 실행 번호
     * @throws IllegalArgumentException 필수 실행 정보가 없을 경우 발생
     * @throws IllegalStateException 로그가 한 건 등록되지 않거나 실행 번호가 발급되지 않은 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long setSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 로그 코드, 메서드명, 실행 상태가 없으면 관리자 화면에서 어떤 실행인지 식별할 수 없어 등록을 중단함
        if (StringUtil.isEmpty(schedulerRunDto) || StringUtil.isEmpty(schedulerRunDto.getSchdCode())
                || StringUtil.isEmpty(schedulerRunDto.getMethName()) || StringUtil.isEmpty(schedulerRunDto.getExecStat())
                || StringUtil.isEmpty(schedulerRunDto.getStrtDate())) {

            throw new IllegalArgumentException("스케줄러 실행 로그의 필수 정보가 없습니다.");
        }

        // SchdCode 업무 값을 schedulerRunDto DTO에 설정함
        schedulerRunDto.setSchdCode(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerRunDto.getSchdCode(), SCHEDULER_CODE_MAX_LENGTH)
        );
        // MethName 업무 값을 schedulerRunDto DTO에 설정함
        schedulerRunDto.setMethName(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerRunDto.getMethName(), METHOD_NAME_MAX_LENGTH)
        );
        // ExecStat 업무 값을 schedulerRunDto DTO에 설정함
        schedulerRunDto.setExecStat(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerRunDto.getExecStat(), EXECUTION_STATUS_MAX_LENGTH)
        );

        // SchedulerLog 업무 값을 schedulerLogMapper DTO에 설정함
        int resultCnt = schedulerLogMapper.setSchedulerLog(schedulerRunDto);

        // selectKey가 실행 번호를 채우고 INSERT가 정확히 한 건 반영돼야 이후 실패 로그와 종료 상태를 연결할 수 있음
        if (resultCnt != 1 || StringUtil.isEmpty(schedulerRunDto.getRunxNumb())) {

            throw new IllegalStateException("스케줄러 실행 로그 등록 결과가 올바르지 않습니다.");
        }

        // 스케줄러 실행 시작 로그를 별도 트랜잭션으로 등록 결과를 반환함
        return schedulerRunDto.getRunxNumb();
    }

    /**
     * 스케줄러 실행의 최종 결과를 별도 트랜잭션으로 수정
     *
     * @author SeungHyeon.Kang
     * @param schedulerRunDto 실행 종료 정보
     * @throws IllegalArgumentException 실행 번호나 최종 상태 및 건수 정보가 올바르지 않을 경우 발생
     * @throws IllegalStateException 수정 대상 실행 로그가 존재하지 않을 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uptSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto) {
        // 음수 건수나 실행 시간은 실제 처리 결과가 아니므로 TL_SCLOGX를 수정하기 전에 차단함
        if (StringUtil.isEmpty(schedulerRunDto) || StringUtil.isEmpty(schedulerRunDto.getRunxNumb())
                || StringUtil.isEmpty(schedulerRunDto.getExecStat()) || schedulerRunDto.getTrgtCntt() < 0
                || schedulerRunDto.getSuccCntt() < 0 || schedulerRunDto.getFailCntt() < 0
                || StringUtil.isEmpty(schedulerRunDto.getExecMsec()) || schedulerRunDto.getExecMsec() < 0) {

            throw new IllegalArgumentException("스케줄러 실행 로그의 종료 정보가 올바르지 않습니다.");
        }

        // ExecStat 업무 값을 schedulerRunDto DTO에 설정함
        schedulerRunDto.setExecStat(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerRunDto.getExecStat(), EXECUTION_STATUS_MAX_LENGTH)
        );

        // SchedulerLog 데이터를 DB에서 수정함
        int resultCnt = schedulerLogMapper.uptSchedulerLog(schedulerRunDto);

        // 시작 로그가 없거나 이미 삭제된 실행 번호라면 종료 상태가 유실되므로 호출부에 실패를 알림
        if (resultCnt != 1) {

            throw new IllegalStateException("수정할 스케줄러 실행 로그가 존재하지 않습니다.");
        }
    }

    /**
     * 스케줄러 실패 한 건을 별도 트랜잭션으로 등록
     *
     * @author SeungHyeon.Kang
     * @param schedulerFailDto 실패 상세 정보
     * @throws IllegalArgumentException 실행 번호나 실패 유형이 없을 경우 발생
     * @throws IllegalStateException 실패 로그가 한 건 등록되지 않은 경우 발생
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setSchedulerFail(SchedulerLogDto.SchedulerFailDto schedulerFailDto) {
        // 실행 번호와 실패 유형이 없으면 복합키 순번과 실패 의미를 결정할 수 없어 저장하지 않음
        if (StringUtil.isEmpty(schedulerFailDto) || StringUtil.isEmpty(schedulerFailDto.getRunxNumb())
                || StringUtil.isEmpty(schedulerFailDto.getFailType())) {

            throw new IllegalArgumentException("스케줄러 실패 로그의 필수 정보가 없습니다.");
        }

        // FailType 업무 값을 schedulerFailDto DTO에 설정함
        schedulerFailDto.setFailType(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerFailDto.getFailType(), FAILURE_TYPE_MAX_LENGTH)
        );
        // RsltMesg 업무 값을 schedulerFailDto DTO에 설정함
        schedulerFailDto.setRsltMesg(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerFailDto.getRsltMesg(), RESULT_MESSAGE_MAX_LENGTH)
        );
        // ErroType 업무 값을 schedulerFailDto DTO에 설정함
        schedulerFailDto.setErroType(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerFailDto.getErroType(), ERROR_TYPE_MAX_LENGTH)
        );
        // ErroCntn 업무 값을 schedulerFailDto DTO에 설정함
        schedulerFailDto.setErroCntn(
                // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화함
                StringUtil.normalizePlainText(schedulerFailDto.getErroCntn(), ERROR_CONTENT_MAX_LENGTH)
        );

        // SchedulerFail 업무 값을 schedulerLogMapper DTO에 설정함
        int resultCnt = schedulerLogMapper.setSchedulerFail(schedulerFailDto);

        // MAX+1로 계산한 복합키를 사용해 정확히 한 건이 저장돼야 실패 건수와 상세 로그가 일치함
        if (resultCnt != 1) {

            throw new IllegalStateException("스케줄러 실패 로그 등록 결과가 올바르지 않습니다.");
        }
    }
}
