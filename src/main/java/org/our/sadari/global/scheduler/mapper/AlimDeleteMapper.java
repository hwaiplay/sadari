package org.our.sadari.global.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 삭제 상태로 전환된 사용자 알림을 물리 삭제하는 스케줄러 전용 Mapper
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface AlimDeleteMapper {

    /**
     * TB_ALIMXX에서 DELT_YSNO가 Y인 알림을 모두 물리 삭제
     *
     * @author Seunghyeon.Kang
     * @return 물리 삭제된 알림 건수
     */
    int delAlim();
}
