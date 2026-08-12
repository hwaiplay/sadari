package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.user.dto.UserSuspensionDto;

/**
 * fileName       : UserSuspensionMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 회원 정지 상태 조회와 기간 만료 SQL을 연결한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    사용자 서버 동기화 대기 상태 수정 추가
 * 2026-07-30        SeungHyeon.Kang    정지 이력 부재 시 회원 상태 보정 조회 추가
 */
@Mapper
public interface UserSuspensionMapper {

    /**
     * 회원에게 현재 활성 상태로 남아 있는 최신 정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 최신 활성 정지 이력
     */
    UserSuspensionDto getLatestActiveSuspension(@Param("userNumb") Long userNumb);

    /**
     * 정지 이력이 없는 회원의 현재 DB 상태를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 현재 DB 회원 상태
     */
    String getUserStatus(@Param("userNumb") Long userNumb);

    /**
     * 기간이 끝난 회원 정지를 만료 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param spndNumb 만료할 정지 이력 번호
     * @return 변경된 이력 건수
     */
    int uptSuspensionExpired(@Param("spndNumb") Long spndNumb);

    /**
     * 정지 상태인 회원을 정지 직전 상태로 복구한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 복구할 회원 번호
     * @param prevStat 정지 직전 회원 상태
     * @return 변경된 회원 건수
     */
    int uptUserStatusAfterSuspend(@Param("userNumb") Long userNumb, @Param("prevStat") String prevStat);

    /**
     * 회원 상태가 복구된 정지 이력을 사용자 서버 반영 대기 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param spndNumb 반영을 기다릴 정지 이력 번호
     * @return 반영 대기 상태로 변경된 이력 건수
     */
    int uptSuspensionSyncPending(@Param("spndNumb") Long spndNumb);
}
