package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * fileName       : ReadingGoalDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 주간, 월간, 연간 독서 목표의 저장 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Data
@Schema(description = "독서 목표 저장 DTO")
public class ReadingGoalDto {

    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;

    @Schema(description = "목표 기준일", example = "2026-07-23")
    private String goalDate;

    @Schema(description = "목표 유형 코드. 주간, 월간, 연간 목표를 구분한다.", example = "WEEK"
    , allowableValues = {"WEEK", "MONT", "YEAR"})
    private String goalType;

    @Schema(description = "목표 집계에 사용할 독서 상태 코드", example = "DONE"
    , allowableValues = {"DONE"})
    private String reptStat;

    @Schema(description = "저장할 목표 권수", example = "2")
    private Integer goalCnt;

    @Schema(description = "목표 수정 가능 횟수", example = "1")
    private Integer updtCntt;

    @Schema(description = "월간 목표 권수", example = "5")
    private Integer monthGoalCnt;

    @Schema(description = "주간 목표 권수", example = "2")
    private Integer weekGoalCnt;

    @Schema(description = "연간 목표 권수", example = "60")
    private Integer yearGoalCnt;
}
