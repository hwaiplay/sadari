package org.our.sadari.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * fileName       : ReportAlimDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-21
 * description    : 독후감별 좋아요와 댓글 알림 설정 요청 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "독후감 알림 설정 요청 DTO")
public class ReportAlimDto {

    @Schema(description = "독후감 작성자 사용자 번호", example = "31", hidden = true)
    private Long userNumb;

    @Schema(description = "알림 설정을 변경할 독후감 번호", example = "1", hidden = true)
    private Long reptNumb;

    @Schema(description = "알림 설정 유형", example = "like", allowableValues = {"like", "reply"}, hidden = true)
    private String alimType;

    @Schema(description = "알림 사용 여부", example = "Y", allowableValues = {"Y", "N"})
    @NotBlank
    @Pattern(regexp = "[YN]")
    private String useYsno;
}
