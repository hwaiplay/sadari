package org.our.sadari.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * fileName       : UserSettingDto
 * author         : SeungHyeon.Kang
 * date           : 2026-09-01
 * description    : 사용자 알림과 공개 범위 설정을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-01        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "사용자 알림과 공개 범위 설정")
public class UserSettingDto {

    @JsonIgnore
    @Schema(hidden = true)
    private Long userNumb;

    @Pattern(regexp = "[YN]")
    @Schema(description = "독서 통계 공개 여부", allowableValues = {"Y", "N"})
    private String readingStatisticsYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "독서 목표 공개 여부", allowableValues = {"Y", "N"})
    private String readingGoalYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "사진 변경 피드 공개 여부", allowableValues = {"Y", "N"})
    private String imageFeedYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "신규 독후감 공개 기본값", allowableValues = {"Y", "N"})
    private String reportPublicDefaultYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "좋아요 알림 사용 여부", allowableValues = {"Y", "N"})
    private String likeAlimYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "댓글과 답글 알림 사용 여부", allowableValues = {"Y", "N"})
    private String replyAlimYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "팔로우 알림 사용 여부", allowableValues = {"Y", "N"})
    private String followAlimYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "선택형 독서 모임 알림 사용 여부", allowableValues = {"Y", "N"})
    private String clubAlimYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "독후감 목표 종료 알림 사용 여부", allowableValues = {"Y", "N"})
    private String reportDueAlimYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "신규 독후감 좋아요 알림 기본값", allowableValues = {"Y", "N"})
    private String reportLikeDefaultYsno;

    @Pattern(regexp = "[YN]")
    @Schema(description = "신규 독후감 댓글 알림 기본값", allowableValues = {"Y", "N"})
    private String reportReplyDefaultYsno;
}
