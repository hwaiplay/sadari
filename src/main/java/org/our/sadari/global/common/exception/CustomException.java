package org.our.sadari.global.common.exception;

import lombok.Getter;

import org.our.sadari.global.common.result.ResultEnum;
import org.springframework.http.HttpStatus;

/**
 * CustomException 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Getter
public class CustomException extends RuntimeException {

    private final ResultEnum resultEnum;
    private final HttpStatus status;

    public CustomException(ResultEnum resultEnum, HttpStatus status) {
        this.resultEnum = resultEnum;
        this.status = status;
    }
}