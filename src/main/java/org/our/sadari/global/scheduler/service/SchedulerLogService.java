package org.our.sadari.global.scheduler.service;

import org.our.sadari.global.scheduler.dto.SchedulerLogDto;

/**
 * 스케줄러 실행 요약과 실패 상세 로그의 등록 및 종료 상태 수정을 정의
 *
 * @author Seunghyeon.Kang
 */
public interface SchedulerLogService {

    /**
     * 스케줄러 실행 시작 로그를 등록하고 발급된 실행 번호를 반환
     *
     * @author Seunghyeon.Kang
     * @param schedulerRunDto 실행 시작 정보
     * @return TL_SCLOGX_SEQ로 발급된 실행 번호
     */
    Long setSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto);

    /**
     * 스케줄러 실행 로그에 최종 상태, 처리 건수, 실행 시간을 반영
     *
     * @author Seunghyeon.Kang
     * @param schedulerRunDto 실행 종료 정보
     */
    void uptSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto);

    /**
     * 스케줄러 실행 중 발생한 실패 한 건을 TL_SCFAIL에 등록
     *
     * @author Seunghyeon.Kang
     * @param schedulerFailDto 실패 상세 정보
     */
    void setSchedulerFail(SchedulerLogDto.SchedulerFailDto schedulerFailDto);
}
