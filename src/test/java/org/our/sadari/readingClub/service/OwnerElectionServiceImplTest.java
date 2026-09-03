package org.our.sadari.readingClub.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.util.MessageUtils;
import org.our.sadari.readingClub.dto.OwnerElectionDto;
import org.our.sadari.readingClub.mapper.OwnerElectionMapper;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : OwnerElectionServiceImplTest
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 선거의 동률 결선 생성 경로를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
@ExtendWith(MockitoExtension.class)
class OwnerElectionServiceImplTest {

    // 모임장 선거 데이터 접근 객체
    @Mock
    private OwnerElectionMapper ownerElectionMapper;

    // 모임장 선거 서비스 단위 테스트 대상
    private OwnerElectionServiceImpl ownerElectionService;

    /** 각 테스트가 독립된 Mock Mapper를 사용하는 서비스를 구성함 */
    @BeforeEach
    void setUp() {
        // 실패 응답을 검증할 서버 공통 메시지 소스를 초기화함
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        new MessageUtils().setMessageSource(messageSource);
        // 선거 마감 분기만 검증할 서비스 구현체를 생성함
        ownerElectionService = new OwnerElectionServiceImpl(ownerElectionMapper);
    }

    /** 본선 최다 득표자가 둘이면 동률 후보만 참여하는 결선을 생성함 */
    @Test
    void createsRunoff() {
        OwnerElectionDto.DueVoteDto dueVote = new OwnerElectionDto.DueVoteDto();
        dueVote.setClubNumb(1L);
        dueVote.setElctNumb(1L);
        dueVote.setVoteNumb(1L);
        dueVote.setVoteRoun(1);
        dueVote.setExtnYsno("N");

        OwnerElectionDto.VoteResultDto first = new OwnerElectionDto.VoteResultDto();
        first.setUserNumb(2L);
        first.setVoteCnt(1);
        OwnerElectionDto.VoteResultDto second = new OwnerElectionDto.VoteResultDto();
        second.setUserNumb(3L);
        second.setVoteCnt(1);

        when(ownerElectionMapper.getDueVoteList(100)).thenReturn(List.of(dueVote));
        when(ownerElectionMapper.getWinnerList(1L, 1L)).thenReturn(List.of(first, second));
        when(ownerElectionMapper.getNextVoteNumb(1L)).thenReturn(2L);

        ownerElectionService.completeDueElection();

        verify(ownerElectionMapper).uptVoteClosed(1L, 1L, null);
        verify(ownerElectionMapper).setVote(1L, 2L, 1L, 2, 1L);
        verify(ownerElectionMapper).setRunoffCandidateList(1L, 2L, 1L);
        verify(ownerElectionMapper).setVoterList(1L, 2L);
        verify(ownerElectionMapper).uptElectionRunoff(1L, 1L);
    }

    /** 이미 투표한 유권자의 결과 변경 요청을 저장하지 않음 */
    @Test
    void rejectsVoteChange() {
        OwnerElectionDto.ElectionDto election = new OwnerElectionDto.ElectionDto();
        election.setVoteNumb(1L);
        election.setCanVote(true);
        election.setVoted(true);
        OwnerElectionDto.VoteReqDto request = new OwnerElectionDto.VoteReqDto();
        request.setUserNumb(2L);

        when(ownerElectionMapper.getElection(1L, 1L)).thenReturn(election);

        ownerElectionService.uptElectionVote(1L, 1L, request);

        verify(ownerElectionMapper, never()).uptBallot(1L, 1L, 1L, 2L);
    }
}
