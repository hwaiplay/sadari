package org.our.sadari.readingClub.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.readingClub.dto.OwnerElectionDto;

/**
 * fileName       : OwnerElectionService
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거의 조회, 투표와 자동 마감 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
public interface OwnerElectionService {

    /** 진행 중인 모임장 선거를 조회함. @param userNumb 사용자 번호 @param clubNumb 모임 번호 @return 선거 정보 */
    ResultData getElection(Long userNumb, Long clubNumb);

    /** 진행 중인 모임장 선거에 투표함. @param userNumb 사용자 번호 @param clubNumb 모임 번호 @param request 후보 선택값 @return 투표 결과 */
    ResultData uptElectionVote(Long userNumb, Long clubNumb, OwnerElectionDto.VoteReqDto request);

    /** 상태 전환 후 생성되지 않은 모임장 선거를 생성함 */
    void startPendingElection();

    /** 마감 시각이 지난 모임장 선거를 확정함 */
    void completeDueElection();
}
