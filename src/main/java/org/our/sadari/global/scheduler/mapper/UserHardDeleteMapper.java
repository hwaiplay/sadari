package org.our.sadari.global.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.user.dto.UserWithdrawalDto;

import java.util.List;

/**
 * fileName       : UserHardDeleteMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 영구 삭제 예정 회원 조회와 회원 연관 데이터 삭제 SQL을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-08-22        SeungHyeon.Kang    피신고자 물리 삭제 전 신고 종결 추가
 */
@Mapper
public interface UserHardDeleteMapper {

    /**
     * 삭제 예정일이 지난 영구 탈퇴 회원을 제한 건수만큼 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param maxSize 한 번에 처리할 최대 회원 수
     * @return 영구 삭제 대상 목록
     */
    List<UserWithdrawalDto> getHardDeleteTargetList(@Param("maxSize") int maxSize);

    /**
     * 피신고자 연결 익명화 전에 해당 회원의 미처리 신고를 시스템 종결한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 영구 삭제할 피신고자 회원 번호
     * @param procCntn 시스템 종결 처리 내용
     * @return 종결한 미처리 신고 건수
     */
    int uptHardDeleteComplaints(@Param("userNumb") Long userNumb, @Param("procCntn") String procCntn);

    /**
     * 로그인 이력을 익명화하고 회원 연관 데이터와 회원 원본을 물리 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 영구 삭제할 회원 번호
     */
    void delHardDeleteUser(@Param("userNumb") Long userNumb);
}
