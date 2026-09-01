package org.our.sadari.readingClub.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.readingClub.dto.ReadingClubDto;

/**
 * fileName       : ReadingClubService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 독서 모임 1차 기능의 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 * 2026-08-14        SeungHyeon.Kang,Hanwon.Jang    모임원·수정·독서 계약 추가
 * 2026-08-20        Hanwon.Jang        현재 독서 수정 계약 추가
 * 2026-08-22        HanWon.Jang        종료 결과·독후감 조회 계약
 * 2026-08-23        HanWon.Jang        이전 독서 기록·회차 결과 조회 계약
 * 2026-08-24        HanWon.Jang        가입 신청 취소·모임원 퇴장 계약 추가
 * 2026-08-29        HanWon.Jang        진행 회차 독후감 조회 계약 확장
 * 2026-08-31        HanWon.Jang        독서 회차 조기 마감 계약 추가
 * 2026-09-01        HanWon.Jang        공개 모임 요약·자진 탈퇴 계약 확장
 */
public interface ReadingClubService {

    /** 활성 모임원의 다음 도서 추천 목록을 조회한다. @param userNumb 사용자 번호 @param clubNumb 모임 번호 @return 추천 목록 */
    ResultData getBookRecommendationList(Long userNumb, Long clubNumb);

    /** 활성 모임원이 다음 도서를 추천한다. @param userNumb 사용자 번호 @param clubNumb 모임 번호 @param request 추천 도서 @return 등록 결과 */
    ResultData setBookRecommendation(Long userNumb, Long clubNumb
                                    , ReadingClubDto.BookRecommendationDto request);

    /** 활성 모임원이 본인 추천을 삭제한다. @param userNumb 사용자 번호 @param clubNumb 모임 번호 @param recmNumb 추천 번호 @return 삭제 결과 */
    ResultData delBookRecommendation(Long userNumb, Long clubNumb, Long recmNumb);

    /** 활성 모임원이 다음 도서에 투표한다. @param userNumb 사용자 번호 @param clubNumb 모임 번호 @param request 투표 대상 @return 투표 결과 */
    ResultData uptBookVote(Long userNumb, Long clubNumb, ReadingClubDto.BookVoteReqDto request);

    /**
     * 모임 독서 회차와 모든 활성 멤버의 읽는 중 독후감을 함께 생성한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 등록을 요청한 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 선택 도서와 목표 독서 기간
     * @return 생성된 회차 번호
     */
    ResultData setReading(Long userNumb, Long clubNumb, ReadingClubDto.ReadingCreateReqDto request);

    /**
     * 현재 모임 독서의 도서와 목표 기간을 연결 독후감에 함께 반영한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 수정을 요청한 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param rondNumb 수정할 회차 번호
     * @param request 수정할 도서와 목표 기간
     * @return 수정된 회차 번호
     */
    ResultData uptReading(Long userNumb, Long clubNumb, Long rondNumb
                         , ReadingClubDto.ReadingUpdateReqDto request);

    /**
     * 활성 모임장이 전원 완독한 진행 회차를 목표 기간 안에 조기 마감한다.
     *
     * @author HanWon.Jang
     * @param userNumb 마감을 요청한 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param rondNumb 마감할 회차 번호
     * @return 완료된 회차 번호
     */
    ResultData uptReadingCompletion(Long userNumb, Long clubNumb, Long rondNumb);

    /**
     * 로그인 사용자가 활성 회원으로 참여 중인 독서 모임 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 내 모임 목록 조회 결과
     */
    ResultData getMyClubList(Long userNumb);

    /**
     * 로그인 사용자의 관심분야와 검색어를 반영한 공개 모임 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param keyword 모임명과 소개 검색어
     * @return 공개 모임 목록 조회 결과
     */
    ResultData getFindClubList(Long userNumb, String keyword);

    /**
     * 로그인 사용자의 참여 관계를 포함한 독서 모임 상세 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 모임 번호
     * @return 모임 상세 조회 결과
     */
    ResultData getClubDtl(Long userNumb, Long clubNumb);

    /**
     * 활성 모임원과 공개 중인 활성 모임 조회자에게 활성 모임원 프로필 목록을 제공한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @return 모임원 프로필 목록 조회 결과
     */
    ResultData getClubMemberList(Long userNumb, Long clubNumb);

    /**
     * 활성 모임원에게 종료된 최신 독서 회차의 목표 결과를 제공한다.
     *
     * @author HanWon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @return 종료된 최신 독서 목표 결과
     */
    ResultData getReadingGoalResult(Long userNumb, Long clubNumb);

    /**
     * 활성 모임원에게 지정한 완료 독서 회차의 목표 결과를 제공한다.
     *
     * @author HanWon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @param rondNumb 조회할 완료 회차 번호
     * @return 지정한 완료 독서 회차의 목표 결과
     */
    ResultData getReadingGoalResult(Long userNumb, Long clubNumb, Long rondNumb);

    /**
     * 활성 모임원이 팝업에서 직접 닫은 독서 회차 결과를 확인 처리한다.
     *
     * @author HanWon.Jang
     * @param userNumb 확인한 사용자 번호
     * @param clubNumb 모임 번호
     * @param rondNumb 확인한 완료 회차 번호
     * @return 결과 확인 처리 결과
     */
    ResultData uptReadingResultConfirm(Long userNumb, Long clubNumb, Long rondNumb);

    /**
     * 활성 모임원과 공개 중인 활성 모임 조회자에게 모든 이전 독서 기록을 제공한다.
     *
     * @author HanWon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @param page 조회할 페이지 번호
     * @return 종료 회차 도서와 목표 달성 집계 페이지
     */
    ResultData getReadingHistoryList(Long userNumb, Long clubNumb, int page);

    /**
     * 활성 모임원에게 진행 또는 완료된 대상 회차의 완료 독후감을 공개 여부와 무관하게 제공한다.
     *
     * @author HanWon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @param rondNumb 조회할 회차 번호
     * @param sortType 독후감 정렬 코드
     * @param page 조회할 페이지 번호
     * @return 회차 도서 정보와 완료 독후감 페이지
     */
    ResultData getReadingRoundReportList(Long userNumb, Long clubNumb, Long rondNumb
                                        , String sortType, int page);

    /**
     * 목표 종료일이 지난 독서 회차의 참여자 달성 여부와 회차 상태를 확정한다.
     * @author HanWon.Jang
     */
    void completeExpiredRound();

    /**
     * 모임 정보와 카테고리 및 가입 질문을 저장하고 개설자를 모임장으로 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param request 모임 생성 입력값
     * @return 생성된 모임 상세 조회 결과
     */
    ResultData setClub(Long userNumb, ReadingClubDto.ClubCreateReqDto request);

    /**
     * 현재 모임장이 모임 정보와 카테고리 및 가입 질문을 수정한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 수정할 모임 번호
     * @param request 수정할 모임 정보
     * @return 수정된 모임 상세 조회 결과
     */
    ResultData uptClub(Long userNumb, Long clubNumb, ReadingClubDto.ClubCreateReqDto request);

    /**
     * 현재 모임장이 모임과 외래키로 연결된 종속 데이터를 물리 삭제한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 삭제할 모임 번호
     * @return 모임 물리 삭제 결과
     */
    ResultData delClub(Long userNumb, Long clubNumb);

    /**
     * 공개 모임의 가입 방식에 따라 활성 회원을 등록하거나 승인 신청을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 가입 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 가입 질문 답변
     * @return 가입 또는 신청 처리 결과
     */
    ResultData setJoin(Long userNumb, Long clubNumb, ReadingClubDto.JoinReqDto request);

    /**
     * 활성 일반 모임원의 활동 연결을 삭제하고 개인 독후감 원본을 보존한다.
     *
     * @author HanWon.Jang
     * @param userNumb 탈퇴를 요청한 사용자 번호
     * @param clubNumb 탈퇴할 모임 번호
     * @return 모임 자진 탈퇴 결과
     */
    ResultData delMembership(Long userNumb, Long clubNumb);

    /**
     * 가입 신청자가 승인 전 자신의 처리 대기 신청과 답변을 삭제한다.
     *
     * @author HanWon.Jang
     * @param userNumb 가입 신청 사용자 번호
     * @param clubNumb 모임 번호
     * @return 가입 신청 취소 결과
     */
    ResultData delApplication(Long userNumb, Long clubNumb);

    /**
     * 현재 모임장이 다른 활성 일반 멤버를 퇴장시키고 재가입을 차단한다.
     *
     * @author HanWon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param targetUserNumb 퇴장 대상 사용자 번호
     * @return 모임원 퇴장 결과
     */
    ResultData delMember(Long userNumb, Long clubNumb, Long targetUserNumb);

    /** 모임장에게 퇴장 내역과 재가입 제한 상태를 제공한다. @author HanWon.Jang @param userNumb 모임장 번호 @param clubNumb 모임 번호 @return 퇴장 내역 */
    ResultData getMemberExitList(Long userNumb, Long clubNumb);

    /** 모임장이 퇴장 회원의 제한 내역을 삭제한다. @author HanWon.Jang @param userNumb 모임장 번호 @param clubNumb 모임 번호 @param targetUserNumb 대상 사용자 번호 @return 내역 삭제 결과 */
    ResultData delMemberRestriction(Long userNumb, Long clubNumb, Long targetUserNumb);

    /**
     * 모임 관계가 없는 모임장의 맞팔로우 사용자를 초대 후보로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 맞팔로우 초대 후보 목록 조회 결과
     */
    ResultData getInviteCandidateList(Long userNumb, Long clubNumb);

    /**
     * 모임장이 활성 회원에게 발송한 유효한 초대 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 보낸 초대 목록 조회 결과
     */
    ResultData getSentInvitationList(Long userNumb, Long clubNumb);

    /**
     * 선택한 맞팔로우 사용자에게 모임 초대를 발송하고 정원 내 좌석을 예약한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 초대 대상 목록
     * @return 초대 저장 결과
     */
    ResultData setInvitation(Long userNumb, Long clubNumb, ReadingClubDto.InviteReqDto request);

    /**
     * 로그인 사용자에게 도착한 만료 전 모임 초대 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 받은 초대 목록 조회 결과
     */
    ResultData getInvitationList(Long userNumb);

    /**
     * 로그인 사용자의 유효한 초대 예약석을 활성 모임원 관계로 전환한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 초대 대상 사용자 번호
     * @param clubNumb 모임 번호
     * @return 초대 수락 처리 결과
     */
    ResultData uptInvitationAccepted(Long userNumb, Long clubNumb);

    /**
     * 로그인 사용자의 초대 예약석을 이력 없이 삭제하여 초대를 거절한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 초대 대상 사용자 번호
     * @param clubNumb 모임 번호
     * @return 초대 거절 처리 결과
     */
    ResultData delInvitation(Long userNumb, Long clubNumb);

    /**
     * 모임장이 발송한 특정 사용자의 초대 예약석을 이력 없이 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param targetUserNumb 초대 대상 사용자 번호
     * @return 초대 취소 처리 결과
     */
    ResultData delOwnerInvitation(Long userNumb, Long clubNumb, Long targetUserNumb);

    /**
     * 모임장이 심사할 처리 중 가입 신청의 질문과 답변을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 처리 중 가입 신청 목록 조회 결과
     */
    ResultData getApplicationList(Long userNumb, Long clubNumb);

    /**
     * 모임장이 가입 신청을 승인 또는 거절하고 신청 답변을 즉시 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param applNumb 모임별 신청 번호
     * @param request 승인 또는 거절 상태
     * @return 가입 신청 처리 결과
     */
    ResultData uptApplication(Long userNumb, Long clubNumb, Long applNumb
                            , ReadingClubDto.ApplicationDecisionReqDto request);
}
