package org.our.sadari.readingClub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.readingClub.dto.OwnerElectionDto;
import org.our.sadari.readingClub.service.OwnerElectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : OwnerElectionController
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거 조회와 비밀투표 API를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reading-clubs/{clubNumb}/owner-election")
@Tag(name = "모임장 선거", description = "모임장 승계 선거 API")
public class OwnerElectionController {

    // 모임장 승계 선거 업무 서비스
    private final OwnerElectionService ownerElectionService;

    /**
     * 로그인 사용자가 참여 중인 모임장 선거를 조회함
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 모임 번호
     * @return 진행 중인 선거와 후보 목록
     */
    @GetMapping
    @Operation(summary = "모임장 선거 조회")
    public ResultData getElection(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @PathVariable Long clubNumb) {
        // 시작 시점 유권자 명부에 포함된 사용자의 선거 정보를 반환함
        return ownerElectionService.getElection(userNumb, clubNumb);
    }

    /**
     * 로그인 사용자의 모임장 후보 선택을 등록하거나 변경함
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param clubNumb 모임 번호
     * @param request 선택한 모임장 후보
     * @return 투표 처리 결과
     */
    @PutMapping("/vote")
    @Operation(summary = "모임장 선거 투표")
    public ResultData uptElectionVote(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
          , @PathVariable Long clubNumb
          , @Valid @RequestBody OwnerElectionDto.VoteReqDto request) {
        // 서버에서 유권자와 후보 자격을 재검증한 투표 결과를 반환함
        return ownerElectionService.uptElectionVote(userNumb, clubNumb, request);
    }
}
