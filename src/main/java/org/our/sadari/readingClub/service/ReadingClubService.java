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
 * 2026-08-14        SeungHyeon.Kang    모임원 프로필 목록 조회 계약 추가
 */
public interface ReadingClubService {

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
     * 활성 모임원에게 같은 모임의 공개 가능한 활성 모임원 프로필 목록을 제공한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @return 모임원 프로필 목록 조회 결과
     */
    ResultData getClubMemberList(Long userNumb, Long clubNumb);

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
     * 모임 관계가 없는 모임장의 맞팔로우 사용자를 초대 후보로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 맞팔로우 초대 후보 목록 조회 결과
     */
    ResultData getInviteCandidateList(Long userNumb, Long clubNumb);

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
