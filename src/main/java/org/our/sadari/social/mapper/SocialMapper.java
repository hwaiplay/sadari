package org.our.sadari.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * TB_FOLLOW 테이블과 팔로우 상태 Oracle 함수를 호출하는 Mapper 계약입니다.
 * 팔로우 관계 저장, 삭제, 버튼명 조회를 한 곳에 모아 팔로우 기능의 SQL 접근 지점을 분리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface SocialMapper {

    /**
     * 로그인 사용자와 상대 사용자 번호를 기준으로 화면에 표시할 팔로우 버튼명을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 팔로우 버튼명
     */
    String getFollowStatusName(@Param("userNumb") Long userNumb, @Param("flowNumb") Long flowNumb);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 관계를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 반영 건수
     */
    int setFollow(@Param("userNumb") Long userNumb, @Param("flowNumb") Long flowNumb);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 반영 건수
     */
    int delFollow(@Param("userNumb") Long userNumb, @Param("flowNumb") Long flowNumb);
}
