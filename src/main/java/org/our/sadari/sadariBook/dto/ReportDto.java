package org.our.sadari.sadariBook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportDto extends BookDto {

    // 사용자 번호는 인증 정보에서 설정한다.
    private Long userNumb;
    // 독후감 번호는 상세, 수정, 삭제 대상 식별에 사용한다.
    private Long reportNumb;

    // 독서 상태는 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    private String reportStat;

    // 독서 시작일은 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    private String reportStdt;

    private String reportEndt;
    private String reportGrde;

    // 독후감 내용은 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    private String reportCntn;
}
