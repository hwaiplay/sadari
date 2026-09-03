package org.our.sadari.readingClub.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.readingClub.dto.OwnerElectionDto;
import org.our.sadari.readingClub.mapper.OwnerElectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : OwnerElectionServiceImpl
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거의 후보 고정, 비밀투표, 결선과 권한 변경을 처리함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerElectionServiceImpl implements OwnerElectionService {

    // 한 번의 스케줄 실행에서 잠그고 처리할 최대 모임 수
    private static final int BATCH_SIZE = 100;

    // 모임장 선거 데이터 접근 Mapper
    private final OwnerElectionMapper ownerElectionMapper;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResultData getElection(Long userNumb, Long clubNumb) {
        // 로그인 사용자와 모임 식별값이 없으면 선거 정보를 공개하지 않음
        if (StringUtil.hasEmpty(userNumb, clubNumb)) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 시작 시점 명부에 포함된 진행 중 선거를 조회함
        OwnerElectionDto.ElectionDto election = ownerElectionMapper.getElection(clubNumb, userNumb);
        // 상태 전환 직후 스케줄 실행 전에도 상세 화면에서 선거를 시작할 수 있게 보정함
        if (StringUtil.isEmpty(election)) {
            startPendingElection();
            election = ownerElectionMapper.getElection(clubNumb, userNumb);
        }
        if (StringUtil.isEmpty(election)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 활성 자격을 유지하는 후보만 화면에 제공함
        election.setCandidateList(ownerElectionMapper.getCandidateList(
                clubNumb, election.getVoteNumb(), userNumb));
        // 진행 중인 선거와 후보 정보를 반환함
        return ResultData.success(election);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ResultData uptElectionVote(Long userNumb, Long clubNumb
                                     , OwnerElectionDto.VoteReqDto request) {
        // 유권자와 후보 식별값이 모두 있어야 투표를 처리함
        if (StringUtil.hasEmpty(userNumb, clubNumb, request, request.getUserNumb())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 현재 사용자에게 열린 선거를 조회해 클라이언트의 투표 번호를 신뢰하지 않음
        OwnerElectionDto.ElectionDto election = ownerElectionMapper.getElection(clubNumb, userNumb);
        if (StringUtil.isEmpty(election) || election.isVoted() || !election.isCanVote()
                || ownerElectionMapper.getEligibleVoterCnt(clubNumb, election.getVoteNumb(), userNumb) == 0
                || ownerElectionMapper.getEligibleOptionCnt(
                        clubNumb, election.getVoteNumb(), request.getUserNumb()) == 0) {
            // "올바르지 않은 접근이에요. 다시 시도해주세요."
            return ResultData.fail(ResultEnum.COMMON_ACCESS_REJECTED);
        }

        // 고유키와 중복 무시 INSERT로 동시에 요청해도 최초 한 표만 저장함
        if (ownerElectionMapper.uptBallot(
                clubNumb, election.getVoteNumb(), userNumb, request.getUserNumb()) == 0) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        // 최초 투표 등록 완료 결과를 반환함
        return ResultData.success();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void startPendingElection() {
        // 모임 행 잠금으로 모임별 번호 계산과 중복 선거 생성을 직렬화함
        List<Long> clubList = ownerElectionMapper.getPendingClubList(BATCH_SIZE);
        for (Long clubNumb : clubList) {
            long elctNumb = ownerElectionMapper.getNextElectionNumb(clubNumb);
            long voteNumb = ownerElectionMapper.getNextVoteNumb(clubNumb);
            ownerElectionMapper.setElection(clubNumb, elctNumb);
            ownerElectionMapper.setVote(clubNumb, voteNumb, elctNumb, 1, null);
            ownerElectionMapper.setCandidateList(clubNumb, voteNumb);
            ownerElectionMapper.setVoterList(clubNumb, voteNumb);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void completeDueElection() {
        // 마감 대상 투표를 잠가 중복 당선과 중복 결선을 방지함
        List<OwnerElectionDto.DueVoteDto> dueVoteList = ownerElectionMapper.getDueVoteList(BATCH_SIZE);
        for (OwnerElectionDto.DueVoteDto dueVote : dueVoteList) {
            completeVote(dueVote);
        }
    }

    /**
     * 마감된 한 투표의 당선, 결선 또는 연장 결과를 확정함
     *
     * @author HanWon.Jang
     * @param dueVote 잠긴 마감 투표
     */
    private void completeVote(OwnerElectionDto.DueVoteDto dueVote) {
        // 계정 제한 또는 모임 탈퇴 회원은 마감 집계 자격에서 제외함
        ownerElectionMapper.uptInvalidVoter(dueVote.getClubNumb(), dueVote.getVoteNumb());
        List<OwnerElectionDto.VoteResultDto> winnerList = ownerElectionMapper.getWinnerList(
                dueVote.getClubNumb(), dueVote.getVoteNumb());

        // 유효한 단독 최다 득표자를 새 모임장으로 확정함
        if (winnerList.size() == 1) {
            completeWinner(dueVote, winnerList.get(0).getUserNumb());
            return;
        }

        // 본선 동률은 동률 후보만 참여하는 24시간 결선을 한 번 생성함
        if (dueVote.getVoteRoun() == 1 && winnerList.size() > 1) {
            setRunoffVote(dueVote);
            return;
        }

        // 후보나 유효표가 없거나 결선도 동률이면 선거를 한 번만 연장함
        if ("N".equals(dueVote.getExtnYsno())) {
            ownerElectionMapper.uptElectionExtended(
                    dueVote.getClubNumb(), dueVote.getElctNumb(), dueVote.getVoteNumb());
            return;
        }

        // 한 차례 연장 뒤에도 성립하지 않은 모임은 운영을 일시중지함
        ownerElectionMapper.uptElectionPaused(dueVote.getClubNumb(), dueVote.getElctNumb());
    }

    /**
     * 단독 최다 득표자의 모임장 권한과 선거 결과를 같은 트랜잭션으로 확정함
     *
     * @author HanWon.Jang
     * @param dueVote 당선자를 확정할 투표
     * @param winnerNumb 당선 사용자 번호
     */
    private void completeWinner(OwnerElectionDto.DueVoteDto dueVote, Long winnerNumb) {
        ownerElectionMapper.uptVoteClosed(
                dueVote.getClubNumb(), dueVote.getVoteNumb(), winnerNumb);
        ownerElectionMapper.uptPreviousOwner(dueVote.getClubNumb());
        ownerElectionMapper.uptNextOwner(dueVote.getClubNumb(), winnerNumb);
        ownerElectionMapper.uptClubOwner(dueVote.getClubNumb(), winnerNumb);
        ownerElectionMapper.uptElectionCompleted(
                dueVote.getClubNumb(), dueVote.getElctNumb(), winnerNumb);
    }

    /**
     * 본선 동률 후보와 현재 활성 유권자로 결선 투표를 생성함
     *
     * @author HanWon.Jang
     * @param dueVote 동률로 마감된 본선 투표
     */
    private void setRunoffVote(OwnerElectionDto.DueVoteDto dueVote) {
        ownerElectionMapper.uptVoteClosed(dueVote.getClubNumb(), dueVote.getVoteNumb(), null);
        long voteNumb = ownerElectionMapper.getNextVoteNumb(dueVote.getClubNumb());
        ownerElectionMapper.setVote(
                dueVote.getClubNumb(), voteNumb, dueVote.getElctNumb(), 2, dueVote.getVoteNumb());
        ownerElectionMapper.setRunoffCandidateList(
                dueVote.getClubNumb(), voteNumb, dueVote.getVoteNumb());
        ownerElectionMapper.setVoterList(dueVote.getClubNumb(), voteNumb);
        ownerElectionMapper.uptElectionRunoff(dueVote.getClubNumb(), dueVote.getElctNumb());
    }
}
