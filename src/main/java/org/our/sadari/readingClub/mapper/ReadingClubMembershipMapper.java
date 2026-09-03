package org.our.sadari.readingClub.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * fileName       : ReadingClubMembershipMapper
 * author         : HanWon.Jang
 * date           : 2026-09-01
 * description    : 독서 모임 자진 탈퇴의 회원 상태와 활동 연결 정리를 처리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-01        HanWon.Jang        최초 생성
 */
@Mapper
public interface ReadingClubMembershipMapper {

    /** 활성 일반 모임원을 재가입 차단 없는 탈퇴 상태로 변경함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 변경된 모임원 수 */
    int uptMemberLeave(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /** 탈퇴 회원의 다음 도서 투표를 삭제함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 삭제 수 */
    int delMemberBookVotes(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /** 탈퇴 회원의 다음 도서 추천을 삭제함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 삭제 수 */
    int delMemberBookRecs(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /** 탈퇴 회원의 모임장 투표 자격과 투표를 삭제함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 삭제 수 */
    int delMemberElectionVotes(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /** 탈퇴 회원의 회차 참여 연결을 삭제함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 삭제 수 */
    int delMemberRoundLinks(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /** 탈퇴 회원의 목표 결과 확인 기록을 삭제함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 삭제 수 */
    int delMemberResultHistory(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /** 탈퇴 회원의 모임 가입 신청 기록을 삭제함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 삭제 수 */
    int delMemberApplications(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);
}
