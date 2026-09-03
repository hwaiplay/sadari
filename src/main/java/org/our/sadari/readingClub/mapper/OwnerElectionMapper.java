package org.our.sadari.readingClub.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.readingClub.dto.OwnerElectionDto;

/**
 * fileName       : OwnerElectionMapper
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거의 생성, 투표와 마감 데이터를 처리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
@Mapper
public interface OwnerElectionMapper {

    /** 진행 중인 모임장 선거를 조회함. @param clubNumb 모임 번호 @param userNumb 사용자 번호 @return 선거 정보 */
    OwnerElectionDto.ElectionDto getElection(@Param("clubNumb") Long clubNumb
                                            , @Param("userNumb") Long userNumb);

    /** 진행 중인 선거 후보를 조회함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @param userNumb 사용자 번호 @return 후보 목록 */
    List<OwnerElectionDto.CandidateDto> getCandidateList(@Param("clubNumb") Long clubNumb
                                                        , @Param("voteNumb") Long voteNumb
                                                        , @Param("userNumb") Long userNumb);

    /** 활성 계정인 활성 유권자 여부를 조회함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @param userNumb 사용자 번호 @return 유권자 수 */
    int getEligibleVoterCnt(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb
                           , @Param("userNumb") Long userNumb);

    /** 유효 후보 여부를 조회함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @param userNumb 후보 사용자 번호 @return 후보 수 */
    int getEligibleOptionCnt(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb
                            , @Param("userNumb") Long userNumb);

    /** 유권자의 투표를 등록하거나 변경함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @param voterNumb 유권자 번호 @param targetNumb 후보 번호 @return 반영 수 */
    int uptBallot(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb
                 , @Param("voterNumb") Long voterNumb, @Param("targetNumb") Long targetNumb);

    /** 선거 상태이지만 선거가 없는 모임을 잠가 조회함. @param maxSize 최대 처리 수 @return 모임 번호 목록 */
    List<Long> getPendingClubList(int maxSize);

    /** 모임의 다음 선거 번호를 조회함. @param clubNumb 모임 번호 @return 다음 선거 번호 */
    long getNextElectionNumb(Long clubNumb);

    /** 모임의 다음 투표 번호를 조회함. @param clubNumb 모임 번호 @return 다음 투표 번호 */
    long getNextVoteNumb(Long clubNumb);

    /** 모임장 선거를 생성함. @param clubNumb 모임 번호 @param elctNumb 선거 번호 @return 등록 수 */
    int setElection(@Param("clubNumb") Long clubNumb, @Param("elctNumb") Long elctNumb);

    /** 모임장 본선 또는 결선 투표를 생성함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @param elctNumb 선거 번호 @param voteRoun 투표 차수 @param prntVote 원 투표 번호 @return 등록 수 */
    int setVote(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb
               , @Param("elctNumb") Long elctNumb, @Param("voteRoun") int voteRoun
               , @Param("prntVote") Long prntVote);

    /** 본선 후보를 활성 회원으로 생성함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @return 등록 수 */
    int setCandidateList(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb);

    /** 결선 후보를 이전 투표의 동률 최다 득표자로 생성함. @param clubNumb 모임 번호 @param voteNumb 새 투표 번호 @param prntVote 원 투표 번호 @return 등록 수 */
    int setRunoffCandidateList(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb
                              , @Param("prntVote") Long prntVote);

    /** 투표 시작 시점의 활성 회원 유권자 명부를 생성함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @return 등록 수 */
    int setVoterList(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb);

    /** 선거를 결선 상태와 새 마감 일시로 변경함. @param clubNumb 모임 번호 @param elctNumb 선거 번호 @return 변경 수 */
    int uptElectionRunoff(@Param("clubNumb") Long clubNumb, @Param("elctNumb") Long elctNumb);

    /** 마감 시각이 지난 투표를 잠가 조회함. @param maxSize 최대 처리 수 @return 마감 투표 목록 */
    List<OwnerElectionDto.DueVoteDto> getDueVoteList(int maxSize);

    /** 제한 계정과 비활성 회원의 투표 자격을 제거함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @return 변경 수 */
    int uptInvalidVoter(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb);

    /** 유효한 최다 득표 후보를 조회함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @return 최다 득표 후보 */
    List<OwnerElectionDto.VoteResultDto> getWinnerList(@Param("clubNumb") Long clubNumb
                                                      , @Param("voteNumb") Long voteNumb);

    /** 투표를 종료함. @param clubNumb 모임 번호 @param voteNumb 투표 번호 @param resultNumb 결과 후보 번호 @return 변경 수 */
    int uptVoteClosed(@Param("clubNumb") Long clubNumb, @Param("voteNumb") Long voteNumb
                     , @Param("resultNumb") Long resultNumb);

    /** 선거와 모임장 권한을 완료 상태로 변경함. @param clubNumb 모임 번호 @param elctNumb 선거 번호 @param winnerNumb 당선자 번호 @return 변경 수 */
    int uptElectionCompleted(@Param("clubNumb") Long clubNumb, @Param("elctNumb") Long elctNumb
                            , @Param("winnerNumb") Long winnerNumb);

    /** 기존 모임장을 일반 회원으로 변경함. @param clubNumb 모임 번호 @return 변경 수 */
    int uptPreviousOwner(Long clubNumb);

    /** 당선자를 모임장 회원으로 변경함. @param clubNumb 모임 번호 @param winnerNumb 당선자 번호 @return 변경 수 */
    int uptNextOwner(@Param("clubNumb") Long clubNumb, @Param("winnerNumb") Long winnerNumb);

    /** 모임 마스터의 모임장을 당선자로 변경함. @param clubNumb 모임 번호 @param winnerNumb 당선자 번호 @return 변경 수 */
    int uptClubOwner(@Param("clubNumb") Long clubNumb, @Param("winnerNumb") Long winnerNumb);

    /** 성립하지 않은 선거를 한 번 연장함. @param clubNumb 모임 번호 @param elctNumb 선거 번호 @param voteNumb 투표 번호 @return 변경 수 */
    int uptElectionExtended(@Param("clubNumb") Long clubNumb, @Param("elctNumb") Long elctNumb
                           , @Param("voteNumb") Long voteNumb);

    /** 재연장할 수 없는 모임과 선거를 일시중지함. @param clubNumb 모임 번호 @param elctNumb 선거 번호 @return 변경 수 */
    int uptElectionPaused(@Param("clubNumb") Long clubNumb, @Param("elctNumb") Long elctNumb);
}
