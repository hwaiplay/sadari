package org.our.sadari.global.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLNonTransientConnectionException;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.MessageUtils;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * fileName       : CommonExceptionHandlerTest
 * author         : HanWon.Jang
 * date           : 2026-08-27
 * description    : 공통 예외 처리기의 JDBC 연결 장애 판정 범위를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-27        HanWon.Jang         최초 생성
 */
class CommonExceptionHandlerTest {

    // JDBC 연결 장애 판정 단위 테스트 대상
    private CommonExceptionHandler exceptionHandler;

    /**
     * 공통 예외 처리기 생성자 의존성을 구성함
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 실제 공통 실패 메시지를 사용할 메시지 소스를 생성함
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // 테스트 메시지 프로퍼티 기준을 설정함
        messageSource.setBasename("messages");
        // 한글 메시지 원문이 손상되지 않도록 인코딩을 설정함
        messageSource.setDefaultEncoding("UTF-8");
        // 실패 응답이 실제 메시지 소스를 조회하도록 정적 객체를 초기화함
        new MessageUtils().setMessageSource(messageSource);
        // JDBC 연결 장애 판정 범위를 검증할 공통 예외 처리기를 생성함
        exceptionHandler = new CommonExceptionHandler(messageSource);
    }

    /**
     * 외부 HTTP 연결 실패가 데이터베이스 장애 응답으로 변환되지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void externalConnectIsRethrown() {
        // JDBC 근거 없이 외부 연결 실패만 포함한 런타임 예외를 생성함
        RuntimeException exception = new RuntimeException(new ConnectException("External connection failed."));

        // 외부 연결 실패가 DB 장애 응답으로 변환되지 않고 원래 예외로 전달되는지 확인함
        RuntimeException thrown = assertThrows(RuntimeException.class
                                             , () -> exceptionHandler.handleRuntimeException(exception, Locale.KOREAN));
        // 호출부가 원래 외부 연결 실패를 구분할 수 있도록 같은 예외 객체가 유지되는지 확인함
        assertSame(exception, thrown);
    }

    /**
     * 외부 HTTP 응답 지연이 데이터베이스 장애 응답으로 변환되지 않는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void externalTimeoutIsRethrown() {
        // JDBC 근거 없이 외부 응답 지연만 포함한 런타임 예외를 생성함
        RuntimeException exception = new RuntimeException(new SocketTimeoutException("External request timed out."));

        // 외부 응답 지연이 DB 장애 응답으로 변환되지 않고 원래 예외로 전달되는지 확인함
        RuntimeException thrown = assertThrows(RuntimeException.class
                                             , () -> exceptionHandler.handleRuntimeException(exception, Locale.KOREAN));
        // 호출부가 원래 외부 응답 지연을 구분할 수 있도록 같은 예외 객체가 유지되는지 확인함
        assertSame(exception, thrown);
    }

    /**
     * JDBC 연결 예외가 데이터베이스 장애 코드와 HTTP 503으로 변환되는지 검증함
     *
     * @author HanWon.Jang
     */
    @Test
    void jdbcFailureReturns503() {
        // JDBC 연결 실패 SQLState를 포함한 예외를 생성함
        SQLNonTransientConnectionException sqlException = new SQLNonTransientConnectionException("Database connection failed.", "08001");
        // 서비스 계층에서 JDBC 예외를 감싼 상황을 재현함
        RuntimeException exception = new RuntimeException(sqlException);

        // 공통 예외 처리기에 JDBC 연결 실패를 전달함
        ResponseEntity<ResultData> response = exceptionHandler.handleRuntimeException(exception, Locale.KOREAN);
        // 공통 응답 본문이 생성되었는지 확인함
        assertNotNull(response.getBody());
        // JDBC 연결 장애만 서비스 이용 불가 상태로 반환되는지 확인함
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        // 프론트 전역 장애 화면이 식별하는 DB 연결 실패 코드인지 확인함
        assertEquals(ResultEnum.COMMON_DB_CONNECTION_FAILED.getCode(), response.getBody().getCode());
    }
}
