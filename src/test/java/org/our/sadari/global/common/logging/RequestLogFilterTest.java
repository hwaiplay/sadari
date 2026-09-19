package org.our.sadari.global.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.our.sadari.global.common.exception.CommonExceptionHandler;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.MessageUtils;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : RequestLogFilterTest
 * author         : SeungHyeon.Kang
 * date           : 2026-09-19
 * description    : MVC 업무 실패·인증 거절·요청 문맥과 민감정보 제외 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-19        SeungHyeon.Kang         최초 생성
 */
@ExtendWith(OutputCaptureExtension.class)
class RequestLogFilterTest {

    // 실제 MVC 직렬화와 필터 연결 검증 대상
    private MockMvc mvc;

    /** 테스트용 메시지와 실제 공통 응답 처리기 구성 */
    @BeforeEach
    void setUp() {
        // 운영 메시지 소스 의존성을 제거한 테스트 메시지
        StaticMessageSource source = new StaticMessageSource();
        // 실패 응답 생성에 필요한 테스트 언어 설정
        source.setUseCodeAsDefaultMessage(true);
        // 공통 응답의 메시지 조회 경로 초기화
        new MessageUtils().setMessageSource(source);
        // Spring MVC의 매핑과 ResponseBodyAdvice 실행까지 검증
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new ResultLogAdvice(), new CommonExceptionHandler(source))
                .addFilters(new RequestLogFilter(1000)).build();
    }

    /** 다른 테스트로 진단 문맥이 전파되지 않도록 정리 */
    @AfterEach
    void clearContext() {
        // 단위 테스트가 지정한 요청 식별자 정리
        MDC.clear();
    }

    /** 경로 변수·쿼리·헤더·본문 데이터 없이 요청 결과 기록 */
    @Test
    void logsMappedRoute(CapturedOutput output) throws Exception {
        // 실제 매핑 경로와 외부 요청 식별자 오염 방지 검증
        MvcResult result = mvc.perform(get("/api/log-test/private-path")
                        .queryParam("code", "private-query")
                        .header("Authorization", "private-token")
                        .header("X-Request-ID", "forged-id"))
                .andExpect(status().isOk()).andReturn();
        // 서버 생성 식별자가 응답에 포함되는지 확인
        assertThat(result.getResponse().getHeader("X-Request-ID")).isNotBlank().isNotEqualTo("forged-id");
        // 매핑 패턴과 공통 성공 코드 확인
        assertThat(output.getOut()).contains("event=http_request", "route=/api/log-test/{id}", "status=200", "resultCode=200");
        // 입력값과 반환 데이터의 로그 유출 방지
        assertThat(output.getOut()).doesNotContain("private-path", "private-query", "private-token", "private-body", "forged-id");
        // 요청 종료 후 풀 스레드 문맥 정리 확인
        assertThat(MDC.get("requestId")).isNull();
    }

    /** HTTP 200 안의 업무 실패를 정상 요청과 구분 */
    @Test
    void logsBusinessRejection(CapturedOutput output) throws Exception {
        // 기존 API 응답 HTTP 상태 유지 확인
        mvc.perform(get("/api/log-rejected")).andExpect(status().isOk());
        // 직렬화된 실제 응답의 업무 실패 코드 확인
        assertThat(output.getOut()).contains("WARN", "resultCode=" + ResultEnum.COMMON_INVALID_REQUEST.getCode());
    }

    /** MVC 진입 전 보안 필터에서 끝난 요청도 완료 로그 기록 */
    @Test
    void logsSecurityDenial(CapturedOutput output) throws Exception {
        // 매핑이 없는 민감 경로를 포함한 요청
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/private-path");
        // 보안 거절 응답을 보관할 테스트 객체
        MockHttpServletResponse response = new MockHttpServletResponse();
        // 보안 필터가 하위 MVC를 실행하지 않는 거절 상황
        new RequestLogFilter(1000).doFilter(request, response, (req, res) -> {
            // 보안 경로에서도 응답 식별자와 진단 문맥 연결 확인
            assertThat(MDC.get("requestId")).isEqualTo(response.getHeader("X-Request-ID"));
            // 기존 보안 응답 동작 재현
            response.setStatus(403);
        });
        // 경로 원문 없이 보안 실패 확인
        assertThat(output.getOut()).contains("status=403", "route=unmapped", "WARN").doesNotContain("private-path");
    }

    /** 실제 대기 없이 단조 시계의 지연 기준과 경고 수준 검증 */
    @Test
    void logsSlowRequest(CapturedOutput output) {
        // 지연 경고 기준보다 오래 걸린 요청 재현
        long started = System.nanoTime() - java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
        // 고정된 시간 차이를 사용해 테스트 실행 속도 의존성 제거
        ReflectionTestUtils.invokeMethod(new RequestLogFilter(1000), "setCompletionLog",
                new MockHttpServletRequest("GET", "/api/slow"), new MockHttpServletResponse(), started, null);
        // 정상 HTTP 응답도 지연 기준 초과 시 경고 기록
        assertThat(output.getOut()).contains("WARN", "slow=true", "status=200");
    }

    /** DB 예외가 공통 응답으로 변환되어도 원인과 요청 완료 기록 */
    @Test
    void logsHandledDbFailure(CapturedOutput output) throws Exception {
        // 기존 DB 장애 응답 계약 유지 확인
        mvc.perform(get("/api/log-database")).andExpect(status().isServiceUnavailable());
        // 핸들러와 완료 필터의 역할을 함께 확인
        assertThat(output.getOut()).contains("event=database_failure", "status=503", "CannotGetJdbcConnectionException");
        // SQL과 인증값이 포함될 수 있는 예외 메시지 제외
        assertThat(output.getOut()).doesNotContain("private-sql-password");
    }

    /** 미처리 예외의 전파와 상위 문맥 복원 보장 */
    @Test
    void restoresContextOnFailure(CapturedOutput output) {
        // 중첩 실행 이전의 식별자
        MDC.put("requestId", "outer-request");
        // 미처리 스트림 실패를 재현할 필터 입력
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/failure");
        // 예외 전파 검증 대상 필터
        RequestLogFilter filter = new RequestLogFilter(1000);
        // 기존 IOException 인스턴스와 예외 의미 보존
        IOException expected = new IOException("private-io-data");
        // 원시 메시지를 기록하지 않으면서 기존 예외 전파
        IOException actual = assertThrows(IOException.class, () -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            throw expected;
        }));
        // 업무 예외가 로깅에 의해 교체되지 않도록 검증
        assertThat(actual).isSameAs(expected);
        // 중첩 요청 문맥 복원 확인
        assertThat(MDC.get("requestId")).isEqualTo("outer-request");
        // 코드 위치와 오류 유형만 출력 확인
        assertThat(output.getOut()).contains("ERROR", "java.io.IOException").doesNotContain("private-io-data");
    }

    /** 정적 파일 요청은 API 로그와 응답 헤더에서 제외 */
    @Test
    void skipsStaticResources(CapturedOutput output) throws Exception {
        // 정적 리소스 응답 확인 객체
        MockHttpServletResponse response = new MockHttpServletResponse();
        // 정적 파일은 불필요한 요청 로그 생략
        new RequestLogFilter(1000).doFilter(new MockHttpServletRequest("GET", "/assets/app.js"), response, (req, res) -> {
            // 원래 정적 응답 동작 유지
            response.setStatus(200);
        });
        // 필터 제외 결과 확인
        assertThat(response.getHeader("X-Request-ID")).isNull();
        // 로그 저장량을 늘리는 정적 리소스 로그 제외
        assertThat(output.getOut()).doesNotContain("event=http_request");
    }

    /**
     * fileName       : TestController
     * author         : SeungHyeon.Kang
     * date           : 2026-09-19
     * description    : 실제 MVC 응답 로그 검증용 엔드포인트
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-09-19        SeungHyeon.Kang         최초 생성
     */
    @RestController
    static class TestController {

        /**
         * 공통 성공 응답에 민감한 테스트 문자열을 포함한 반환
         *
         * @author SeungHyeon.Kang
         * @return 로그에 포함되면 안 되는 테스트 데이터 응답
         */
        @GetMapping("/api/log-test/{id}")
        public ResultData getSuccess() {
            // 응답 데이터는 유지하되 로그에는 제외할 테스트 값
            return ResultData.success("private-body");
        }

        /**
         * HTTP 성공 안의 업무 실패 응답 반환
         *
         * @author SeungHyeon.Kang
         * @return 업무 거절 코드 응답
         */
        @GetMapping("/api/log-rejected")
        public ResultData getRejected() {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        /**
         * DB 연결 실패의 전역 예외 처리 경로 재현
         *
         * @author SeungHyeon.Kang
         * @return 정상 응답 없이 예외 처리기로 전달
         * @throws CannotGetJdbcConnectionException 테스트용 DB 연결 실패
         */
        @GetMapping("/api/log-database")
        public ResultData getDatabaseFailure() {

            throw new CannotGetJdbcConnectionException("private-sql-password");
        }
    }
}
