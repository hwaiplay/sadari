package org.our.sadari.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * fileName       : ComplaintCreateDto
 * author         : HanWon.Jang
 * date           : 2026-08-21
 * description    : 독후감 또는 댓글 신고 등록값을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-21        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintCreateDto {

    // 신고 대상 유형 세부코드
    @NotBlank
    private String tagtType;
    // 신고 대상 독후감 또는 댓글 번호
    @NotNull
    @Positive
    private Long tagtNumb;
    // 신고 사유 세부코드
    @NotBlank
    private String cmplRson;
    // 신고 상세 내용
    @Size(max = 1000)
    private String cmplCntn;
}
