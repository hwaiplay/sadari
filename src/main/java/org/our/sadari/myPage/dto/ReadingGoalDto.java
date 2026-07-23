package org.our.sadari.myPage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ReadingGoalDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
@Schema(description = "독서 목표 저장 DTO")
public class ReadingGoalDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "사용자 번호", example = "31")
    private Long userNumb;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "목표 기준일", example = "2026-07-23")
    private String goalDate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "목표 유형 코드. 주간, 월간, 연간 목표를 구분한다.", example = "WEEK")
    private String goalType;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "목표 집계에 사용할 독서 상태 코드", example = "DONE")
    private String reportStat;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "저장할 목표 권수", example = "2")
    private Integer goalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "목표 수정 가능 횟수", example = "1")
    private Integer updtCntt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "월간 목표 권수", example = "5")
    private Integer monthGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "주간 목표 권수", example = "2")
    private Integer weekGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    @Schema(description = "연간 목표 권수", example = "60")
    private Integer yearGoalCnt;
}
