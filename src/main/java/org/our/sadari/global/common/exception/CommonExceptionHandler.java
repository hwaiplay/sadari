package org.our.sadari.global.common.exception;

import lombok.RequiredArgsConstructor;

import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.result.ResultResponse;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResultResponse> handleDataAccessException(DataAccessException e) {
        SQLException sqlException = findSqlException(e);

        if (!StringUtil.isEmpty(sqlException) && sqlException.getErrorCode() == 1461) {
            // ORA-01461은 VARCHAR2(4000 BYTE)보다 큰 문자열을 바인딩할 때 발생한다.
            ResultResponse response = new ResultResponse(
                    ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG.getCode(),
                    "독후감 내용이 4000바이트를 초과했어요. 한글은 글자당 2~3바이트로 계산됩니다."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        ResultResponse response = new ResultResponse(
                ResultEnum.COMMON_INVALID_REQUEST.getCode(),
                "데이터베이스 처리 중 오류가 발생했어요. 입력값을 확인해주세요."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * SQLException 추출
     * @Author SeungHyeon.Kang
     * @param throwable
     * @return
     */
    private SQLException findSqlException(Throwable throwable) {
        Throwable current = throwable;

        while (!StringUtil.isEmpty(current)) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }

            current = current.getCause();
        }

        return null;
    }
}
