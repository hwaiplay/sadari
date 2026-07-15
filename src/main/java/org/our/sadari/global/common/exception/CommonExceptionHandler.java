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
 * 애플리케이션 공통 예외를 표준 응답 형식으로 변환하는 전역 예외 처리기입니다.
 *
 * @author Seunghyeon.Kang
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonExceptionHandler {

    private final MessageSource messageSource;

    /**
     * 업무 로직에서 명시적으로 발생시킨 CustomException을 처리합니다.
     *
     * @author Seunghyeon.Kang
     * @param e 업무 예외 객체
     * @param locale 현재 요청 Locale
     * @return 공통 오류 응답
     */
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


    /**
     * Bean Validation 실패 예외를 공통 오류 응답으로 변환합니다.
     *
     * @author Seunghyeon.Kang
     * @param e 검증 실패 예외
     * @param locale 현재 요청 Locale
     * @return 공통 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            Locale locale
    ) {
        // 상세 필드별 메시지는 화면 정책이 정해지기 전까지 공통 잘못된 요청 메시지로 통일합니다.
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

        // Oracle VARCHAR2 byte 한도 초과 오류는 독후감 내용 길이 안내로 치환합니다.
        if (!StringUtil.isEmpty(sqlException) && sqlException.getErrorCode() == 1461) {
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
     * 중첩 예외 체인에서 SQLException을 추출합니다.
     *
     * @author Seunghyeon.Kang
     * @param throwable 검색을 시작할 예외 객체
     * @return SQLException이 있으면 해당 객체, 없으면 null
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
