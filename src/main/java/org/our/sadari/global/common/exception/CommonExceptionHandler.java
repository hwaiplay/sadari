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
 * 애플리케이션 전역에서 발생하는 예외를 포착하여 일관된 API 응답(ResultResponse)으로 변환하는 전역 예외 처리 핸들러.
 *
 * @author Seunghyeon.Kang
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class CommonExceptionHandler {

    private static final int ORACLE_VALUE_TOO_LARGE_ERROR_CODE = 1461;

    private final MessageSource messageSource;

    /**
     * 비즈니스 로직 수행 중 직접 발생시킨 커스텀 예외(CustomException)를 포착하여 지정된 결과 코드와 메시지를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param e 발생한 CustomException
     * @param locale 사용자 언어 환경
     * @return 예외 상태 코드 및 메시지가 포함된 ResponseEntity
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
     * @Valid 또는 @Validated 어노테이션 기반의 DTO 요청 파라미터 유효성 검증 실패 예외를 포착하여 공통 잘못된 요청 응답을 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param e 유효성 검증 예외
     * @param locale 사용자 언어 환경
     * @return 400 BAD_REQUEST 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            Locale locale
    ) {
        // 컨트롤러 파라미터 바인딩 및 유효성 검증 실패 시 공통 잘못된 요청 메시지를 다국어 메세지 소스에서 조회한다.
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
     * Spring의 DataAccessException 및 하위 데이터베이스 접근 예외를 포착하여 세부 원인(커넥션 오류, 오라클 바이트 초과 등)별로 분기 처리한다.
     *
     * @author Seunghyeon.Kang
     * @param e 데이터 접근 예외
     * @param locale 사용자 언어 환경
     * @return 예외 세부 원인에 맞는 ResponseEntity
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

        // Oracle ORA-01461 (바인딩된 값이 열의 크기보다 큼) 에러 발생 시 신고 내용/입력값 길이 초과 전용 메시지를 응답한다.
        if (!StringUtil.isEmpty(sqlException) && sqlException.getErrorCode() == ORACLE_VALUE_TOO_LARGE_ERROR_CODE) {
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

        // 일반적인 DB 데이터 처리 관련 예외 발생 시 공통 요청 오류 메시지로 응답한다.
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
     * 예외 원인 체인(Throwable cause chain)을 순회하여 내부에 래핑된 최하위 SQLException 객체를 탐색하여 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param throwable 탐색을 시작할 최상위 예외 객체
     * @return 추출된 SQLException (존재하지 않을 경우 null)
     */
    private SQLException findSqlException(Throwable throwable) {
        Throwable current = throwable;

        while (!StringUtil.isEmpty(current)) {
            // 원인 체인을 순회하는 도중 SQLException 인스턴스를 발견하면 즉시 해당 예외 객체를 반환한다.
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