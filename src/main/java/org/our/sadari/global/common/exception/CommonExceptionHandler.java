package org.our.sadari.global.common.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RsultResponse> handleCustomException(CustomException e, Locale locale) {

        ResultEnum result = e.getResultEnum();

        String message = messageSource.getMessage(
                result.getMessageKey(),
                null,
                locale
        );

        RsultResponse response = new RsultResponse(
                result.getCode(),
                message
        );

        return ResponseEntity
                .status(e.getStatus())
                .body(response);
    }
}
