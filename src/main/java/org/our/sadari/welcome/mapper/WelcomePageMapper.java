package org.our.sadari.welcome.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.welcome.dto.WelcomePageDto;

/**
 * fileName       : WelcomePageMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-28
 * description    : 사용자 상태와 현재 배포 중인 웰컴페이지에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface WelcomePageMapper {

    /** 활성 사용자 여부를 조회한다. */
    int getActiveUserCnt(@Param("userNumb") Long userNumb, @Param("userStat") String userStat);

    /** 현재 배포 중인 관리자 웰컴페이지를 노출 순서대로 조회한다. */
    List<WelcomePageDto> getWelcomePageList(@Param("yes") String yes);
}
