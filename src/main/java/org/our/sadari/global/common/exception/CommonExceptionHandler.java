package org.our.sadari.global.common.exception;

import java.sql.SQLException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.MyBatisSystemException;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.result.ResultResponse;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * CommonExceptionHandler 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonExceptionHandler {

    private static final int ORACLE_VALUE_TOO_LARGE_ERROR_CODE = 1461;

    private final MessageSource messageSource;

    /**
     * handleCustomException 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param e 처리에 필요한 입력값
     * @param locale 처리에 필요한 입력값
     * @return 처리 결과
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
     * handleMethodArgumentNotValidException 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param e 처리에 필요한 입력값
     * @param locale 처리에 필요한 입력값
     * @return 처리 결과
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            Locale locale
    ) {
        // 아래 처리 단계의 업무 목적을 설명한다.
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

    /**
     * handleDataAccessException 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param e 처리에 필요한 입력값
     * @param locale 처리에 필요한 입력값
     * @return 처리 결과
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ResultResponse> handleDataAccessException(
            DataAccessException e,
            Locale locale
    ) {
        SQLException sqlException = findSqlException(e);

        // DB 커넥션 자체를 가져오지 못한 경우에는 쿼리 오류와 분리해 사용자에게 점검 가능한 메시지를 반환한다.
        if (hasCause(e, CannotGetJdbcConnectionException.class)) {
            String message = messageSource.getMessage(
                    ResultEnum.COMMON_DB_CONNECTION_FAILED.getMessageKey(),
                    null,
                    locale
            );

            ResultResponse response = new ResultResponse(
                    ResultEnum.COMMON_DB_CONNECTION_FAILED.getCode(),
                    message
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (!StringUtil.isEmpty(sqlException) && sqlException.getErrorCode() == ORACLE_VALUE_TOO_LARGE_ERROR_CODE) {
            // 아래 처리 단계의 업무 목적을 설명한다.
            String message = messageSource.getMessage(
                    ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG.getMessageKey(),
                    new Object[]{Constant.REPORT_CONTENT_MAX_BYTES},
                    locale
            );

            ResultResponse response = new ResultResponse(
                    ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG.getCode(),
                    message
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        // 아래 처리 단계의 업무 목적을 설명한다.
        // 아래 처리 단계의 업무 목적을 설명한다.
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

    /**
     * MyBatis 실행 중 발생한 시스템 예외를 공통 응답으로 변환한다.
     * 내부 원인이 DB 커넥션 실패라면 사용자에게 DB 연결 실패 메시지를 명확하게 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param e MyBatis 실행 중 발생한 예외
     * @param locale 사용자 locale
     * @return 예외 처리 결과
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public ResponseEntity<ResultResponse> handleMyBatisSystemException(
            MyBatisSystemException e,
            Locale locale
    ) {
        ResultEnum resultEnum = hasCause(e, CannotGetJdbcConnectionException.class)
                ? ResultEnum.COMMON_DB_CONNECTION_FAILED
                : ResultEnum.COMMON_INVALID_REQUEST;
        HttpStatus status = ResultEnum.COMMON_DB_CONNECTION_FAILED.equals(resultEnum)
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.BAD_REQUEST;
        String message = messageSource.getMessage(
                resultEnum.getMessageKey(),
                null,
                locale
        );

        ResultResponse response = new ResultResponse(
                resultEnum.getCode(),
                message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
    /**
     * findSqlException 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param throwable 처리에 필요한 입력값
     * @return 처리 결과
     */
    private SQLException findSqlException(Throwable throwable) {
        Throwable current = throwable;

        while (!StringUtil.isEmpty(current)) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }

            current = current.getCause();
        }

        return null;
    }

    /**
     * 예외 원인 체인 안에 특정 예외 타입이 포함되어 있는지 확인한다.
     * MyBatisSystemException처럼 감싸진 예외도 실제 원인을 기준으로 분기하기 위해 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param throwable 확인할 최상위 예외
     * @param causeType 찾을 원인 예외 타입
     * @return 원인 체인에 해당 타입이 있으면 true
     */
    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;

        // 원인 체인을 끝까지 순회해 MyBatis나 Spring이 감싼 실제 DB 연결 예외를 찾는다.
        while (!StringUtil.isEmpty(current)) {
            if (causeType.isInstance(current)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
