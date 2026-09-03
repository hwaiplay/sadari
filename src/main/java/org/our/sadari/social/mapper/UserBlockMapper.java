package org.our.sadari.social.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.social.dto.UserBlockDto;

/**
 * fileName       : UserBlockMapper
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 사용자 차단 관계의 저장과 조회 데이터 접근 메서드를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
@Mapper
public interface UserBlockMapper {

    /**
     * 두 사용자의 원본 행을 일정한 순서로 잠금
     *
     * @author HanWon.Jang
     * @param firstUserNumb 작은 사용자 번호
     * @param secondUserNumb 큰 사용자 번호
     * @return 잠금에 성공한 사용자 수
     */
    List<Long> lockUsers(@Param("firstUserNumb") Long firstUserNumb, @Param("secondUserNumb") Long secondUserNumb);

    /**
     * 두 사용자 사이에 어느 방향이든 차단 관계가 있는지 조회함
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 상대 사용자 번호
     * @return 양방향 차단 관계 수
     */
    int getBlockCnt(@Param("userNumb") Long userNumb, @Param("targetUserNumb") Long targetUserNumb);

    /**
     * 로그인 사용자가 만든 차단 관계를 멱등 등록함
     *
     * @author HanWon.Jang
     * @param blockDto 차단 사용자와 대상 정보
     * @return 반영 건수
     */
    int setBlock(UserBlockDto blockDto);

    /**
     * 로그인 사용자가 만든 한 방향의 차단 관계를 삭제함
     *
     * @author HanWon.Jang
     * @param blockDto 차단 사용자와 대상 정보
     * @return 반영 건수
     */
    int delBlock(UserBlockDto blockDto);

    /**
     * 두 사용자 사이의 팔로우 관계를 양방향 삭제함
     *
     * @author HanWon.Jang
     * @param blockDto 차단 사용자와 대상 정보
     * @return 반영 건수
     */
    int delBlockFollows(UserBlockDto blockDto);

    /**
     * 차단 당사자 사이에서 직접 발송된 수락 전 모임 초대를 삭제함
     *
     * @author HanWon.Jang
     * @param blockDto 차단 사용자와 대상 정보
     * @return 삭제된 초대 수
     */
    int delBlockInvitations(UserBlockDto blockDto);

    /**
     * 한 당사자가 모임장인 모임에 상대가 제출한 처리 중 가입 신청을 삭제함
     *
     * @author HanWon.Jang
     * @param blockDto 차단 사용자와 대상 정보
     * @return 삭제된 가입 신청 수
     */
    int delBlockApplications(UserBlockDto blockDto);

    /**
     * 로그인 사용자가 직접 차단한 사용자 목록을 최신순으로 조회함
     *
     * @author HanWon.Jang
     * @param blockDto 로그인 사용자와 페이지 조건
     * @return 차단 사용자 목록
     */
    List<UserBlockDto> getBlockList(UserBlockDto blockDto);
}
