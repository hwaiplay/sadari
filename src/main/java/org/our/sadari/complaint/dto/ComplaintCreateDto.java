package org.our.sadari.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * fileName       : ComplaintCreateDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 사용자가 접수할 신고 대상과 사유를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 * 2026-08-22        SeungHyeon.Kang    상세 내용 500자 제한 반영
 */
@Data
@Schema(description = "신고 접수 요청 DTO")
public class ComplaintCreateDto {

    @Schema(description = "신고 대상 유형 세부코드", example = "CMPL_BOOK_REPORT")
    @NotBlank
    private String tagtType;

    @Schema(description = "신고 대상 번호", example = "1")
    @NotNull
    @Positive
    private Long tagtNumb;

    @Schema(description = "신고 사유 세부코드", example = "CMPL_ABUSE")
    @NotBlank
    private String cmplRson;

    @Schema(description = "신고 상세 내용(최대 500자)", example = "신고 내용을 확인해 주세요.")
    @Size(max = 500)
    private String cmplCntn;
}
