package org.our.sadari.global.common.exception;

import lombok.RequiredArgsConstructor;

import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.result.ResultResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * fileName       : CommonExceptionHandler
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResultResponse> handleCustomException(CustomException e, Locale locale) {

        ResultEnum result = e.getResultEnum();

        String message = messageSource.getMessage(
                result.getMessageKey(),
                null,
                locale
        );

        ResultResponse response = new ResultResponse(
                result.getCode(),
                message
        );

        return ResponseEntity
                .status(e.getStatus())
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            Locale locale
    ) {
        // 요청 본문 검증 실패는 공통 요청 오류 응답으로 변환한다.
        String message = messageSource.getMessage(
                ResultEnum.COMMON_INVALID_REQUEST.getMessageKey(),
                null,
                locale
        );

        ResultResponse response = new ResultResponse(
                ResultEnum.COMMON_INVALID_REQUEST.getCode(),
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
