package org.our.sadari.global.common.exception;

import lombok.Getter;

import org.our.sadari.global.common.result.ResultEnum;
import org.springframework.http.HttpStatus;

/**
 * fileName       : CustomException
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 공통 예외를 표현하고 공통 응답으로 변환한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang    최초 생성
 */
@Getter
public class CustomException extends RuntimeException {
    // 공통 예외 결과 코드
    private final ResultEnum resultEnum;
    // 예외 응답 HTTP 상태
    private final HttpStatus status;

    /**
     * 공통 결과 코드와 HTTP 상태를 포함한 업무 예외를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param resultEnum 사용자에게 반환할 공통 결과 코드
     * @param status 응답에 사용할 HTTP 상태
     */
    public CustomException(ResultEnum resultEnum, HttpStatus status) {

        this.resultEnum = resultEnum;
        this.status = status;
    }
}
