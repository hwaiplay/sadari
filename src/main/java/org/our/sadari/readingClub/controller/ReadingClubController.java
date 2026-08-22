package org.our.sadari.readingClub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.readingClub.dto.ReadingClubDto;
import org.our.sadari.readingClub.service.ReadingClubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ReadingClubController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 독서 모임 생성, 탐색, 가입, 초대와 승인 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 * 2026-08-14        Hanwon.Jang        모임원·수정·독서 API 추가
 * 2026-08-20        Hanwon.Jang        현재 독서 수정 API 추가
 * 2026-08-22        HanWon.Jang        종료 결과·독후감 조회 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reading-clubs")
@Tag(name = "독서 모임", description = "독서 모임 1차 기능 API")
public class ReadingClubController {

    // 독서 모임 생성과 참여 업무 서비스
    private final ReadingClubService readingClubService;

    /**
     * 모임 독서 회차와 활성 멤버별 읽는 중 독후감을 하나의 요청으로 등록한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 등록을 요청한 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 선택 도서와 목표 독서 기간
     * @return 생성된 회차 번호
     */
    @PostMapping("/{clubNumb}/setBook")
    @Operation(summary = "모임 독서 등록")
    public ResultData setReading(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                , @PathVariable Long clubNumb
                                , @Valid @RequestBody ReadingClubDto.ReadingCreateReqDto request) {
        // 회차와 활성 멤버별 독후감을 같은 트랜잭션으로 생성한 결과를 반환한다
        return readingClubService.setReading(userNumb, clubNumb, request);
    }

    /**
     * 현재 모임 독서의 도서와 목표 기간을 수정한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 수정을 요청한 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param rondNumb 수정할 회차 번호
     * @param request 수정할 도서와 목표 기간
     * @return 수정된 회차 번호
     */
    @PutMapping("/{clubNumb}/{rondNumb}/updateClub")
    @Operation(summary = "현재 모임 독서 수정")
    public ResultData uptReading(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                , @PathVariable Long clubNumb, @PathVariable Long rondNumb
                                , @Valid @RequestBody ReadingClubDto.ReadingUpdateReqDto request) {
        // 모임장 권한과 연결 독후감 작성 여부를 적용한 수정 결과를 반환한다
        return readingClubService.uptReading(userNumb, clubNumb, rondNumb, request);
    }

    /**
     * 로그인 사용자가 활성 회원으로 참여 중인 독서 모임 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 내 모임 목록 조회 결과
     */
    @GetMapping("/mine")
    @Operation(summary = "내 모임 조회")
    public ResultData getMyClubList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자의 활성 모임 목록을 반환한다
        return readingClubService.getMyClubList(userNumb);
    }

    /**
     * 로그인 사용자의 관심분야와 검색어를 반영한 공개 모임 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param keyword 모임명과 소개 검색어
     * @return 공개 모임 목록 조회 결과
     */
    @GetMapping
    @Operation(summary = "공개 모임 찾기")
    public ResultData getFindClubList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                    , @RequestParam(required = false) String keyword) {
        // 관심분야 일치와 검색어를 적용한 공개 모임을 반환한다
        return readingClubService.getFindClubList(userNumb, keyword);
    }

    /**
     * 로그인 사용자의 참여 관계를 포함한 독서 모임 상세 정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 모임 번호
     * @return 모임 상세 조회 결과
     */
    @GetMapping("/{clubNumb}")
    @Operation(summary = "모임 상세 조회")
    public ResultData getClubDtl(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                , @PathVariable Long clubNumb) {
        // 로그인 사용자 관점의 모임 상세를 반환한다
        return readingClubService.getClubDtl(userNumb, clubNumb);
    }

    /**
     * 활성 모임원에게 같은 모임의 공개 가능한 활성 모임원 프로필 목록을 제공한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @return 모임원 프로필 목록 조회 결과
     */
    @GetMapping("/{clubNumb}/members")
    @Operation(summary = "모임원 프로필 목록 조회")
    public ResultData getClubMemberList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                       , @PathVariable Long clubNumb) {
        // 같은 모임의 활성 모임원 프로필 목록을 반환한다
        return readingClubService.getClubMemberList(userNumb, clubNumb);
    }

    /**
     * 활성 모임원에게 종료된 최신 독서 회차의 목표 결과를 제공한다.
     *
     * @author HanWon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @return 종료된 최신 독서 목표 결과
     */
    @GetMapping("/{clubNumb}/reading-result")
    @Operation(summary = "종료된 모임 독서 목표 결과 조회")
    public ResultData getReadingGoalResult(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                          , @PathVariable Long clubNumb) {
        // 현재 활성 모임원에게 공개할 수 있는 종료 회차 결과를 반환한다
        return readingClubService.getReadingGoalResult(userNumb, clubNumb);
    }

    /**
     * 활성 모임원에게 완료된 대상 회차의 완료 독후감을 공개 여부와 무관하게 제공한다.
     *
     * @author HanWon.Jang
     * @param userNumb 조회를 요청한 사용자 번호
     * @param clubNumb 조회할 모임 번호
     * @param rondNumb 조회할 회차 번호
     * @param sortType 독후감 정렬 코드
     * @param page 조회할 페이지 번호
     * @return 회차 도서 정보와 완료 독후감 페이지
     */
    @GetMapping("/{clubNumb}/readings/{rondNumb}/reports")
    @Operation(summary = "모임 독서 회차 완료 독후감 목록 조회")
    public ResultData getReadingRoundReportList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @PathVariable Long clubNumb
          , @PathVariable Long rondNumb
          , @RequestParam(defaultValue = "LATEST_DESC") String sortType
          , @RequestParam(defaultValue = "1") int page) {
        // 현재 활성 모임원에게만 대상 회차의 완료 독후감 목록을 반환한다
        return readingClubService.getReadingRoundReportList(
                userNumb, clubNumb, rondNumb, sortType, page);
    }

    /**
     * 모임 정보와 카테고리 및 가입 질문을 저장하고 개설자를 모임장으로 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param request 모임 생성 입력값
     * @return 생성된 모임 상세 조회 결과
     */
    @PostMapping
    @Operation(summary = "새 모임 생성")
    public ResultData setClub(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                             , @Valid @RequestBody ReadingClubDto.ClubCreateReqDto request) {
        // 모임과 개설자 회원 관계를 한 트랜잭션으로 생성한 결과를 반환한다
        return readingClubService.setClub(userNumb, request);
    }

    /**
     * 현재 모임장이 모임 기본 정보와 운영 설정을 수정한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 수정할 모임 번호
     * @param request 수정할 모임 정보
     * @return 수정된 모임 상세 조회 결과
     */
    @PutMapping("/{clubNumb}")
    @Operation(summary = "모임 수정")
    public ResultData uptClub(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                            , @PathVariable Long clubNumb
                            , @Valid @RequestBody ReadingClubDto.ClubCreateReqDto request) {
        // 현재 모임장 권한과 운영 제약을 적용한 수정 결과를 반환한다
        return readingClubService.uptClub(userNumb, clubNumb, request);
    }

    /**
     * 현재 모임장이 모임과 종속 데이터를 복구 불가능하게 삭제한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 삭제할 모임 번호
     * @return 모임 물리 삭제 결과
     */
    @DeleteMapping("/{clubNumb}")
    @Operation(summary = "모임 삭제")
    public ResultData delClub(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                            , @PathVariable Long clubNumb) {
        // 현재 모임장만 실행할 수 있는 물리 삭제 결과를 반환한다
        return readingClubService.delClub(userNumb, clubNumb);
    }

    /**
     * 공개 모임의 가입 방식에 따라 활성 회원을 등록하거나 승인 신청을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 가입 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 가입 질문 답변
     * @return 가입 또는 신청 처리 결과
     */
    @PostMapping("/{clubNumb}/memberships")
    @Operation(summary = "공개 모임 가입")
    public ResultData setJoin(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                             , @PathVariable Long clubNumb
                             , @Valid @RequestBody ReadingClubDto.JoinReqDto request) {
        // 모임 정책에 맞춰 즉시 가입 또는 승인 신청 결과를 반환한다
        return readingClubService.setJoin(userNumb, clubNumb, request);
    }

    /**
     * 모임 관계가 없는 모임장의 맞팔로우 사용자를 초대 후보로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 맞팔로우 초대 후보 목록 조회 결과
     */
    @GetMapping("/{clubNumb}/invitation-candidates")
    @Operation(summary = "맞팔 초대 후보 조회")
    public ResultData getInviteCandidateList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                             , @PathVariable Long clubNumb) {
        // 아직 모임 관계가 없는 활성 맞팔 후보를 반환한다
        return readingClubService.getInviteCandidateList(userNumb, clubNumb);
    }

    /**
     * 모임장이 활성 회원에게 발송한 유효한 초대 목록을 조회한다.
     *
     * @author Hanwon.Jang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 보낸 초대 목록 조회 결과
     */
    @GetMapping("/{clubNumb}/invitations/sent")
    @Operation(summary = "보낸 모임 초대 조회")
    public ResultData getSentInvitationList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                            , @PathVariable Long clubNumb) {
        // 활성 회원에게 발송한 만료 전 초대 목록을 반환한다
        return readingClubService.getSentInvitationList(userNumb, clubNumb);
    }

    /**
     * 선택한 맞팔로우 사용자에게 모임 초대를 발송하고 정원 내 좌석을 예약한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 초대 대상 목록
     * @return 초대 저장 결과
     */
    @PostMapping("/{clubNumb}/invitations")
    @Operation(summary = "맞팔 모임 초대")
    public ResultData setInvitation(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                   , @PathVariable Long clubNumb
                                   , @Valid @RequestBody ReadingClubDto.InviteReqDto request) {
        // 선택한 모든 맞팔 대상의 좌석 예약 결과를 반환한다
        return readingClubService.setInvitation(userNumb, clubNumb, request);
    }

    /**
     * 로그인 사용자에게 도착한 만료 전 모임 초대 목록을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 받은 초대 목록 조회 결과
     */
    @GetMapping("/invitations/received")
    @Operation(summary = "받은 모임 초대 조회")
    public ResultData getInvitationList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb) {
        // 로그인 사용자의 유효한 받은 초대를 반환한다
        return readingClubService.getInvitationList(userNumb);
    }

    /**
     * 로그인 사용자의 유효한 초대 예약석을 활성 모임원 관계로 전환한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 초대 대상 사용자 번호
     * @param clubNumb 모임 번호
     * @return 초대 수락 처리 결과
     */
    @PutMapping("/{clubNumb}/invitations/received")
    @Operation(summary = "받은 모임 초대 수락")
    public ResultData uptInvitationAccepted(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                            , @PathVariable Long clubNumb) {
        // 예약석을 활성 회원으로 전환한 결과를 반환한다
        return readingClubService.uptInvitationAccepted(userNumb, clubNumb);
    }

    /**
     * 로그인 사용자의 초대 예약석을 이력 없이 삭제하여 초대를 거절한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 초대 대상 사용자 번호
     * @param clubNumb 모임 번호
     * @return 초대 거절 처리 결과
     */
    @DeleteMapping("/{clubNumb}/invitations/received")
    @Operation(summary = "받은 모임 초대 거절")
    public ResultData delInvitation(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                   , @PathVariable Long clubNumb) {
        // 본인의 초대 예약석을 이력 없이 삭제한 결과를 반환한다
        return readingClubService.delInvitation(userNumb, clubNumb);
    }

    /**
     * 모임장이 발송한 특정 사용자의 초대 예약석을 이력 없이 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @param targetUserNumb 초대 대상 사용자 번호
     * @return 초대 취소 처리 결과
     */
    @DeleteMapping("/{clubNumb}/invitations/{targetUserNumb}")
    @Operation(summary = "발송한 모임 초대 취소")
    public ResultData delOwnerInvitation(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                        , @PathVariable Long clubNumb, @PathVariable Long targetUserNumb) {
        // 모임장이 지정한 초대 예약석을 삭제한 결과를 반환한다
        return readingClubService.delOwnerInvitation(userNumb, clubNumb, targetUserNumb);
    }

    /**
     * 모임장이 심사할 처리 중 가입 신청의 질문과 답변을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 모임장 사용자 번호
     * @param clubNumb 모임 번호
     * @return 처리 중 가입 신청 목록 조회 결과
     */
    @GetMapping("/{clubNumb}/applications")
    @Operation(summary = "가입 신청 조회")
    public ResultData getApplicationList(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                        , @PathVariable Long clubNumb) {
        // 모임장이 심사할 질문과 답변 목록을 반환한다
        return readingClubService.getApplicationList(userNumb, clubNumb);
    }

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
    @PutMapping("/{clubNumb}/applications/{applNumb}")
    @Operation(summary = "가입 신청 승인 또는 거절")
    public ResultData uptApplication(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                    , @PathVariable Long clubNumb, @PathVariable Long applNumb
                                    , @Valid @RequestBody ReadingClubDto.ApplicationDecisionReqDto request) {
        // 승인 시 좌석을 검사하고 처리된 답변을 즉시 삭제한 결과를 반환한다
        return readingClubService.uptApplication(userNumb, clubNumb, applNumb, request);
    }
}
