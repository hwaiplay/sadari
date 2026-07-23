package org.our.sadari.global.common.code.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "공통코드 세부코드 DTO")
public class CodeDto {

    @Schema(description = "공통코드", example = "READ_STAT")
    private String commCode;
    @Schema(description = "세부코드", example = "STOP")
    private String comdCode;
    @Schema(description = "세부코드명", example = "중단했어요")
    private String comdName;
    @Schema(description = "코드 설명")
    private String codeExpl;
    @Schema(description = "옵션1 코드")
    private String opt1Code;
    @Schema(description = "옵션1 명")
    private String opt1Name;
    @Schema(description = "옵션2 코드")
    private String opt2Code;
    @Schema(description = "옵션2 명")
    private String opt2Name;
    @Schema(description = "옵션3 코드")
    private String opt3Code;
    @Schema(description = "옵션3 명")
    private String opt3Name;
    @Schema(description = "옵션4 코드")
    private String opt4Code;
    @Schema(description = "옵션4 명")
    private String opt4Name;
    @Schema(description = "사용 여부", example = "Y")
    private String useeYsno;
    @Schema(description = "정렬 순서", example = "1")
    private Integer sortOrder;
}
