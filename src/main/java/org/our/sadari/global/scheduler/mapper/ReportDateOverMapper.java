package org.our.sadari.global.scheduler.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.report.dto.ReportDto;

/**
 * 정기 작업 대상자를 DB에서 제한된 크기로 조회하는 MyBatis Mapper
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface ReportDateOverMapper {

    /**
     * 진행 중이면서 목표 종료일이 지난 독후감 중 아직 기간 초과 알림을 받지 않은 대상을 조회
     *
     * @author Seunghyeon.Kang
     * @param maxSize SQL에서 반환할 최대 행 수
     * @return 목표 독서기간 초과 알림 대상 목록
     */
    List<ReportDto> getReportDateOverTargetList(@Param("maxSize") int maxSize);
}
