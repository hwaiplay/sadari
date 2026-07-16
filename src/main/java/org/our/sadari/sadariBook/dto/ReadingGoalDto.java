package org.our.sadari.sadariBook.dto;

import lombok.Data;

/**
 * 월별/연도별 독서 목표 설정과 조회에 사용하는 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class ReadingGoalDto {

    /** 목표를 설정할 회원 번호입니다. */
    private Long userNumb;

    /** 목표 기간입니다. 월별은 YYYYMM, 연도별은 YYYY00 형식으로 저장합니다. */
    private String goalDate;

    /** 목표 구분입니다. 월별은 MONT, 연도별은 YEAR로 저장합니다. */
    private String goalType;

    /** 사용자가 설정한 목표 독서 권수입니다. */
    private Integer goalCnt;

    /** 목표 수정 횟수입니다. 최초 등록 이후 실제 목표 권수를 변경한 횟수를 저장합니다. */
    private Integer updtCntt;

    /** 현재 월 목표 독서 권수입니다. 화면 요청 DTO에서 사용합니다. */
    private Integer monthGoalCnt;

    /** 현재 연도 목표 독서 권수입니다. 화면 요청 DTO에서 사용합니다. */
    /** 현재 주간 목표 독서 권수입니다. 화면 요청 DTO에서 사용합니다. */
    private Integer weekGoalCnt;

    private Integer yearGoalCnt;
}
