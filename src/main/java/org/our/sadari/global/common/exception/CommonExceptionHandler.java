package org.our.sadari.global.common.exception;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 포착하여 일관된 API 응답(ResultData)으로 변환하는 전역 예외 처리 핸들러.
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
    public ResponseEntity<ResultData> handleCustomException(CustomException e, Locale locale) {
        ResultEnum result = e.getResultEnum();

        return ResponseEntity
                .status(e.getStatus())
                .body(ResultData.fail(result));
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
    public ResponseEntity<ResultData> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            Locale locale
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST));
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
    public ResponseEntity<ResultData> handleDataAccessException(
            DataAccessException e,
            Locale locale
    ) {
        SQLException sqlException = findSqlException(e);

        /*
         * DataAccessException은 SQL 문법 오류, 제약조건 오류, 커넥션 획득 실패를 모두 감싼다.
         * 모든 DB 장애를 400으로 내려주면 프론트가 "요청값 오류"로 오해하므로, 커넥션 계열 원인은 먼저 분리한다.
         */
        if (isDatabaseConnectionFailure(e)) {
            return createFailResponse(ResultEnum.COMMON_DB_CONNECTION_FAILED, HttpStatus.SERVICE_UNAVAILABLE);
        }

        // Oracle ORA-01461 (바인딩된 값이 열의 크기보다 큼) 에러 발생 시 신고 내용/입력값 길이 초과 전용 메시지를 응답한다.
        if (!StringUtil.isEmpty(sqlException) && sqlException.getErrorCode() == ORACLE_VALUE_TOO_LARGE_ERROR_CODE) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResultData.fail(ResultEnum.COMMON_REPORT_CONTENT_TOO_LONG, Constant.REPORT_CONTENT_MAX_BYTES));
        }

        // 일반적인 DB 데이터 처리 관련 예외 발생 시 공통 요청 오류 메시지로 응답한다.
        return createFailResponse(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<ResultData> handleMyBatisSystemException(
            MyBatisSystemException e,
            Locale locale
    ) {
        ResultEnum resultEnum = isDatabaseConnectionFailure(e)
                ? ResultEnum.COMMON_DB_CONNECTION_FAILED
                : ResultEnum.COMMON_INVALID_REQUEST;
        HttpStatus status = ResultEnum.COMMON_DB_CONNECTION_FAILED.equals(resultEnum)
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.BAD_REQUEST;

        return createFailResponse(resultEnum, status);
    }

    /**
     * MyBatis 또는 트랜잭션 시작 단계에서 DB 커넥션을 얻지 못한 예외를 공통 DB 연결 실패 응답으로 변환한다.
     * Mapper 호출 전 트랜잭션 생성 시점에 실패하면 DataAccessException까지 내려오지 않을 수 있어 별도로 포착한다.
     *
     * @author Seunghyeon.Kang
     * @param e DB 접근 준비 단계에서 발생한 예외
     * @param locale 사용자 locale
     * @return DB 연결 실패 응답
     */
    @ExceptionHandler({
            CannotCreateTransactionException.class,
            TransactionSystemException.class,
            PersistenceException.class,
            SQLException.class
    })
    public ResponseEntity<ResultData> handleDatabaseConnectionException(Exception e, Locale locale) {
        /*
         * 위 예외들은 DB 연결 실패 외의 SQL 실행 오류도 감쌀 수 있다.
         * 실제 원인 체인을 확인해 연결 장애이면 503, 그 외 DB 오류이면 기존 공통 요청 오류로 응답한다.
         */
        if (isDatabaseConnectionFailure(e)) {
            return createFailResponse(ResultEnum.COMMON_DB_CONNECTION_FAILED, HttpStatus.SERVICE_UNAVAILABLE);
        }

        return createFailResponse(ResultEnum.COMMON_INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    /**
     * 다른 계층에서 RuntimeException으로 한 번 더 감싸져 올라온 DB 연결 실패를 마지막으로 포착한다.
     * 연결 실패가 아닌 일반 런타임 예외는 기존처럼 서버 오류로 남겨 원인을 숨기지 않도록 다시 던진다.
     *
     * @author Seunghyeon.Kang
     * @param e 처리 중 발생한 런타임 예외
     * @param locale 사용자 locale
     * @return DB 연결 실패 응답
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResultData> handleRuntimeException(RuntimeException e, Locale locale) {
        if (isDatabaseConnectionFailure(e)) {
            return createFailResponse(ResultEnum.COMMON_DB_CONNECTION_FAILED, HttpStatus.SERVICE_UNAVAILABLE);
        }

        throw e;
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
     * 예외 원인 체인에서 DB 연결 실패에 해당하는 예외 또는 Oracle JDBC 연결 실패 코드를 찾는다.
     * 각 API에서 직접 try/catch하지 않아도 커넥션풀, 트랜잭션, MyBatis, JDBC 드라이버가 감싼 연결 장애를 같은 ResultData 실패 응답으로 반환하기 위해 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param throwable 확인할 최상위 예외
     * @return DB 연결 실패 여부
     */
    private boolean isDatabaseConnectionFailure(Throwable throwable) {
        SQLException sqlException = findSqlException(throwable);

        if (hasCause(throwable, CannotGetJdbcConnectionException.class)
                || hasCause(throwable, CannotCreateTransactionException.class)
                || hasCause(throwable, SQLRecoverableException.class)
                || hasCause(throwable, SQLTransientConnectionException.class)
                || hasCause(throwable, SQLNonTransientConnectionException.class)
                || hasCause(throwable, SQLTimeoutException.class)
                || hasCause(throwable, ConnectException.class)
                || hasCause(throwable, SocketTimeoutException.class)) {
            return true;
        }

        if (StringUtil.isEmpty(sqlException)) {
            return false;
        }

        /*
         * Oracle JDBC에서 네트워크 어댑터 오류, 닫힌 커넥션, 응답 없음 등은 SQLException으로만 감싸져 올라오는 경우가 있다.
         * 대표적인 연결 장애 errorCode와 SQLState 접두어(08)를 같이 확인해 커넥션 문제를 쿼리 오류와 분리한다.
         */
        return "08".equals(getSqlStateClass(sqlException))
                || sqlException.getErrorCode() == 17002
                || sqlException.getErrorCode() == 17008
                || sqlException.getErrorCode() == 17410;
    }

    /**
     * SQLState 앞 두 자리는 오류 분류를 나타내며, 08 계열은 연결 예외(Connection Exception)를 의미한다.
     * SQLState가 비어 있는 Oracle 예외도 있으므로 비어 있으면 null을 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param sqlException 확인할 SQL 예외
     * @return SQLState 분류 코드
     */
    private String getSqlStateClass(SQLException sqlException) {
        String sqlState = sqlException.getSQLState();

        if (StringUtil.isEmpty(sqlState) || sqlState.length() < 2) {
            return null;
        }

        return sqlState.substring(0, 2);
    }

    /**
     * ResultData.fail 응답을 HTTP 상태와 함께 감싸 반환한다.
     * 전역 예외 처리 응답도 컨트롤러 정상 응답과 같은 code/message/data 구조를 유지하기 위해 사용한다.
     *
     * @author Seunghyeon.Kang
     * @param resultEnum 반환할 업무 실패 코드
     * @param status 반환할 HTTP 상태
     * @return ResultData 실패 응답
     */
    private ResponseEntity<ResultData> createFailResponse(ResultEnum resultEnum, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(ResultData.fail(resultEnum));
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
