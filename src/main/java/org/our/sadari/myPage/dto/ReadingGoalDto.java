package org.our.sadari.myPage.dto;

import lombok.Data;

/**
 * ReadingGoalDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class ReadingGoalDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Long userNumb;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String goalDate;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String goalType;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer goalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer updtCntt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer monthGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer weekGoalCnt;

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Integer yearGoalCnt;
}
