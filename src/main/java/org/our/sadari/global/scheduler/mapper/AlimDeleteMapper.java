package org.our.sadari.global.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * fileName       : AlimDeleteMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 스케줄러 데이터베이스 접근 메서드를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface AlimDeleteMapper {
    /**
     * TB_ALIMXX에서 DELT_YSNO가 Y인 알림을 모두 물리 삭제
     *
     * @author SeungHyeon.Kang
     * @return 물리 삭제된 알림 건수
     */
    int delAlim();
}
