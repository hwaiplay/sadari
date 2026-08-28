package org.our.sadari.readingClub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * fileName       : OwnerElectionDto
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 모임장 승계 선거의 화면 정보와 투표 요청을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
public class OwnerElectionDto {

    /** 모임장 후보 정보를 전달한다. */
    @Data
    @Schema(description = "모임장 선거 후보")
    public static class CandidateDto {

        @Schema(description = "후보 사용자 번호")
        private Long userNumb;

        @Schema(description = "후보 사용자 닉네임")
        private String userNick;

        @Schema(description = "후보 프로필 이미지 경로")
        private String porfPath;

        @Schema(description = "로그인 사용자의 선택 여부")
        private boolean selected;
    }

    /** 진행 중인 모임장 선거 정보를 전달한다. */
    @Data
    @Schema(description = "모임장 선거 화면 정보")
    public static class ElectionDto {

        @Schema(description = "모임 번호")
        private Long clubNumb;

        @Schema(description = "모임별 선거 번호")
        private Long elctNumb;

        @Schema(description = "모임별 투표 번호")
        private Long voteNumb;

        @Schema(description = "투표 차수")
        private Integer voteRoun;

        @Schema(description = "투표 마감 일시")
        private LocalDateTime endxDate;

        @Schema(description = "투표 가능 여부")
        private boolean canVote;

        @Schema(description = "로그인 사용자의 투표 완료 여부")
        private boolean voted;

        @Schema(description = "모임장 후보 목록")
        private List<CandidateDto> candidateList;
    }

    /** 모임장 후보 선택값을 전달한다. */
    @Data
    @Schema(description = "모임장 선거 투표 요청")
    public static class VoteReqDto {

        @NotNull
        @Schema(description = "후보 사용자 번호")
        private Long userNumb;
    }

    /** 마감 대상 투표 식별값을 전달한다. */
    @Data
    public static class DueVoteDto {

        private Long clubNumb;
        private Long elctNumb;
        private Long voteNumb;
        private Long prntVote;
        private Integer voteRoun;
        private String extnYsno;
    }

    /** 최다 득표 후보와 득표 수를 전달한다. */
    @Data
    public static class VoteResultDto {

        private Long userNumb;
        private Integer voteCnt;
    }
}
