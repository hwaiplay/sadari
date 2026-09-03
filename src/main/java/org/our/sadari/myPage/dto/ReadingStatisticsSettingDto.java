package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * fileName       : ReadingStatisticsSettingDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-14
 * description    : 마이페이지 독서 통계의 공개 범위를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "마이페이지 독서 통계 설정 DTO")
public class ReadingStatisticsSettingDto {

    @Schema(hidden = true)
    private Long userNumb;
    @NotBlank
    @Pattern(regexp = "^[YN]$")
    @Schema(description = "다른 사용자에게 독서 통계를 공개할지 여부", example = "N", allowableValues = {"Y", "N"})
    private String publicYsno;
    @Schema(hidden = true)
    private String userStat;
}
