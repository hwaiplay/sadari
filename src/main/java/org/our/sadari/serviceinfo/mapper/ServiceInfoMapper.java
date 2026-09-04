package org.our.sadari.serviceinfo.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.serviceinfo.dto.ServiceInfoDto;

/**
 * fileName       : ServiceInfoMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 사용자 계정 상태와 서비스 정보 카테고리 및 현재 배포본에 접근함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 * 2026-09-04        SeungHyeon.Kang    개인정보처리방침 단건 조회 추가
 */
@Mapper
public interface ServiceInfoMapper {

    /**
     * 사용자 번호와 상태에 일치하는 활성 계정 수를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 사용자 번호
     * @param userStat 허용할 사용자 상태
     * @return 조건에 일치하는 사용자 수
     */
    int getActiveUserCnt(@Param("userNumb") Long userNumb, @Param("userStat") String userStat);

    /**
     * 활성 서비스 정보 카테고리와 카테고리별 현재 배포본을 조회함
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param yes 사용 및 배포 상태의 예 값
     * @return 정렬된 서비스 정보 카테고리와 현재 배포본 목록
     */
    List<ServiceInfoDto> getServiceInfoList(@Param("cateCgrp") String cateCgrp
                                          , @Param("yes") String yes);

    /**
     * 지정한 서비스 정보 카테고리의 현재 배포본을 조회함
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 조회할 서비스 정보 카테고리 상세코드
     * @param yes 사용 및 배포 상태의 예 값
     * @return 현재 배포된 서비스 정보 한 건
     */
    ServiceInfoDto getServiceInfoDtl(@Param("cateCgrp") String cateCgrp
                                   , @Param("cateCode") String cateCode
                                   , @Param("yes") String yes);
}
