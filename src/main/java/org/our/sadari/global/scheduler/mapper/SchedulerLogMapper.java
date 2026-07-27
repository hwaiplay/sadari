package org.our.sadari.global.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.global.scheduler.dto.SchedulerLogDto;

/**
 * 스케줄러 실행 요약과 실패 상세 로그를 TL_SCLOGX, TL_SCFAIL에 기록하는 MyBatis Mapper
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface SchedulerLogMapper {

    /**
     * 스케줄러 실행 시작 정보를 TL_SCLOGX에 등록
     *
     * @author Seunghyeon.Kang
     * @param schedulerRunDto 실행 시작 정보
     * @return 등록된 행 수
     */
    int setSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto);

    /**
     * 스케줄러의 최종 상태와 처리 건수 및 실행 시간을 TL_SCLOGX에 수정
     *
     * @author Seunghyeon.Kang
     * @param schedulerRunDto 실행 종료 정보
     * @return 수정된 행 수
     */
    int uptSchedulerLog(SchedulerLogDto.SchedulerRunDto schedulerRunDto);

    /**
     * 스케줄러 실패 정보를 실행 번호별 다음 실패 순번으로 TL_SCFAIL에 등록
     *
     * @author Seunghyeon.Kang
     * @param schedulerFailDto 실패 상세 정보
     * @return 등록된 행 수
     */
    int setSchedulerFail(SchedulerLogDto.SchedulerFailDto schedulerFailDto);
}
