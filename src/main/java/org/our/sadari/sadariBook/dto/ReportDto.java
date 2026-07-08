package org.our.sadari.sadariBook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(regexp = "done|reading|stopped")
    private String reportStat;

    // 독서 시작일은 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportStdt;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private String reportEndt;
    @NotBlank
    @Pattern(regexp = "[1-5]")
    private String reportGrde;
    @NotBlank
    @Pattern(regexp = "#[0-9a-fA-F]{6}")
    private String reportColr;

    // 독후감 내용은 저장과 수정 요청에서 필수 값이다.
    @NotBlank
    @Size(max = 4000)
    private String reportCntn;

    private String readingYn;
}
