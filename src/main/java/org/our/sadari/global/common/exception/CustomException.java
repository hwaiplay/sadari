package org.our.sadari.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * fileName       : CustomException
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
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