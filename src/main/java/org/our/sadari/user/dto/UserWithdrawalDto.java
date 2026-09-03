package org.our.sadari.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : UserWithdrawalDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 회원 탈퇴 요청과 처리 상태 데이터를 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 */
@Data
@Schema(description = "회원 탈퇴 DTO")
public class UserWithdrawalDto {

    @Schema(description = "탈퇴 이력 번호")
    private Long wthdNumb;

    @Schema(description = "회원 번호")
    private Long userNumb;

    @Schema(description = "회원 외부 식별값 해시")
    private String userIdhs;

    @NotBlank
    @Schema(description = "탈퇴 유형", example = "SOFT")
    private String wthdType;

    @NotBlank
    @Schema(description = "탈퇴 사유", example = "LOW_USAGE")
    private String wthdRson;

    @Schema(description = "기타 탈퇴 사유")
    private String rsonCntn;

    @Schema(description = "탈퇴 처리 상태")
    private String wthdStat;

    @Schema(description = "탈퇴 요청일시")
    private LocalDateTime requDate;

    @Schema(description = "영구 삭제 예정일시")
    private LocalDateTime deltDate;

    @Schema(description = "처리 완료일시")
    private LocalDateTime procDate;

    @Schema(description = "회원 복구일시")
    private LocalDateTime rcovDate;

    @Schema(description = "처리 오류 내용")
    private String erroCntn;

    @Schema(description = "Kakao 재인증 URL")
    private String authUrl;
}
