package org.our.sadari.readingClub.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.readingClub.dto.ReadingClubDto;

/**
 * fileName       : ReadingClubMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 독서 모임 1차 기능의 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface ReadingClubMapper {

    /**
     * 사용자가 저장한 관심분야 수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 저장된 관심분야 수
     */
    int getUserInterestCnt(Long userNumb);

    /**
     * 요청한 코드 중 활성 관심분야 세부코드 수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param categoryList 검증할 관심분야 세부코드 목록
     * @return 활성 세부코드 수
     */
    int getValidCategoryCnt(@Param("categoryList") List<String> categoryList);

    /**
     * 독서 모임 마스터를 생성하고 생성 번호를 요청 DTO에 반영한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param request 생성할 모임 정보
     * @return 생성된 모임 수
     */
    int setClub(@Param("userNumb") Long userNumb
              , @Param("request") ReadingClubDto.ClubCreateReqDto request);

    /**
     * 모임 카테고리 한 건을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param intrCode 관심분야 세부코드
     * @param sortOrdr 모임 내 노출 순서
     * @return 등록된 카테고리 수
     */
    int setClubCategory(@Param("clubNumb") Long clubNumb
                      , @Param("intrCode") String intrCode
                      , @Param("sortOrdr") int sortOrdr);

    /**
     * 모임 개설자를 활성 모임장 회원으로 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 모임장 사용자 번호
     * @return 등록된 회원 수
     */
    int setOwnerMember(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /**
     * 승인형 모임의 현재 가입 질문 한 행을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 질문 등록 사용자 번호
     * @param question 등록할 질문 값
     * @return 등록된 질문 행 수
     */
    int setClubQuestion(@Param("userNumb") Long userNumb
                      , @Param("question") ReadingClubDto.QuestionDto question);

    /**
     * 로그인 사용자가 활성 회원인 모임 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 내 모임 목록
     */
    List<ReadingClubDto.ClubViewDto> getMyClubList(Long userNumb);

    /**
     * 공개 모임을 관심분야 일치 순서로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param keyword 모임명과 소개 검색어
     * @return 공개 모임 목록
     */
    List<ReadingClubDto.ClubViewDto> getFindClubList(@Param("userNumb") Long userNumb
                                                   , @Param("keyword") String keyword);

    /**
     * 모임 상세 상태를 로그인 사용자 관점으로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 로그인 사용자 번호
     * @return 모임 상세 상태
     */
    ReadingClubDto.ClubViewDto getClubDtl(@Param("clubNumb") Long clubNumb
                                        , @Param("userNumb") Long userNumb);

    /**
     * 정원과 권한 변경 전에 모임 마스터 행을 잠가 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 잠긴 모임 정보
     */
    ReadingClubDto.ClubViewDto getClubForUpdate(Long clubNumb);

    /**
     * 모임의 카테고리 목록을 노출 순서대로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 모임 카테고리 목록
     */
    List<ReadingClubDto.CategoryDto> getClubCategoryList(Long clubNumb);

    /**
     * 모임당 한 행인 현재 가입 질문을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 현재 가입 질문
     */
    ReadingClubDto.QuestionDto getClubQuestion(Long clubNumb);

    /**
     * 모임과 사용자의 현재 회원 또는 초대 관계를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 사용자 번호
     * @return 현재 관계
     */
    ReadingClubDto.MemberDto getClubMember(@Param("clubNumb") Long clubNumb
                                          , @Param("userNumb") Long userNumb);

    /**
     * 모임의 만료된 초대 예약석을 물리 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 삭제된 초대 수
     */
    int delExpiredInvitation(Long clubNumb);

    /**
     * 사용자가 받은 만료 초대를 물리 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 사용자 번호
     * @return 삭제된 초대 수
     */
    int delUserExpiredInvitation(Long userNumb);

    /**
     * 활성 회원과 유효한 초대 예약석을 합산한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 현재 점유 좌석 수
     */
    int getOccupiedSeatCnt(Long clubNumb);

    /**
     * 공개 즉시 가입 사용자를 활성 일반 회원으로 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 가입 사용자 번호
     * @return 등록된 회원 수
     */
    int setActiveMember(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /**
     * 모임장의 맞팔 초대를 예약석 회원 행으로 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 초대 대상 사용자 번호
     * @param senderNumb 초대 모임장 사용자 번호
     * @return 등록된 초대 수
     */
    int setInvitation(@Param("clubNumb") Long clubNumb
                    , @Param("userNumb") Long userNumb
                    , @Param("senderNumb") Long senderNumb);

    /**
     * 유효한 맞팔 관계인지 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param ownerNumb 모임장 사용자 번호
     * @param userNumb 초대 대상 사용자 번호
     * @return 상호 팔로우 관계 수
     */
    int getMutualFollowCnt(@Param("ownerNumb") Long ownerNumb, @Param("userNumb") Long userNumb);

    /**
     * 모임장의 맞팔 초대 후보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param ownerNumb 모임장 사용자 번호
     * @return 아직 모임 관계가 없는 맞팔 후보 목록
     */
    List<ReadingClubDto.InviteCandidateDto> getInviteCandidateList(@Param("clubNumb") Long clubNumb
                                                                  , @Param("ownerNumb") Long ownerNumb);

    /**
     * 로그인 사용자에게 도착한 유효한 초대 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 수신 초대 목록
     */
    List<ReadingClubDto.InvitationDto> getInvitationList(Long userNumb);

    /**
     * 초대 예약석을 활성 회원으로 전환한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 초대 수락 사용자 번호
     * @return 전환된 회원 수
     */
    int uptInvitationAccepted(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /**
     * 거절·취소하는 유효 초대 예약석을 물리 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 초대 대상 사용자 번호
     * @return 삭제된 초대 수
     */
    int delInvitation(@Param("clubNumb") Long clubNumb, @Param("userNumb") Long userNumb);

    /**
     * 승인형 모임의 처리 중 신청을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param userNumb 신청 사용자 번호
     * @return 처리 중 신청
     */
    ReadingClubDto.ApplicationDto getPendingApplication(@Param("clubNumb") Long clubNumb
                                                       , @Param("userNumb") Long userNumb);

    /**
     * 질문 사본과 답변을 포함한 승인 가입 신청을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param application 생성할 신청 데이터
     * @return 생성된 신청 수
     */
    int setJoinApplication(ReadingClubDto.ApplicationDto application);

    /**
     * 모임장이 심사할 처리 중 가입 신청을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @return 처리 중 가입 신청 목록
     */
    List<ReadingClubDto.ApplicationDto> getApplicationList(Long clubNumb);

    /**
     * 가입 신청을 승인 또는 거절하고 답변 본문을 즉시 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param applNumb 모임별 신청 번호
     * @param ownerNumb 처리 모임장 사용자 번호
     * @param joinStat 승인 또는 거절 상태
     * @return 처리된 신청 수
     */
    int uptJoinApplication(@Param("clubNumb") Long clubNumb
                         , @Param("applNumb") Long applNumb
                         , @Param("ownerNumb") Long ownerNumb
                         , @Param("joinStat") String joinStat);

    /**
     * 신청 번호로 현재 처리 중 가입 신청을 잠가 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param clubNumb 모임 번호
     * @param applNumb 모임별 신청 번호
     * @return 잠긴 가입 신청
     */
    ReadingClubDto.ApplicationDto getApplicationForUpdate(@Param("clubNumb") Long clubNumb
                                                         , @Param("applNumb") Long applNumb);
}
