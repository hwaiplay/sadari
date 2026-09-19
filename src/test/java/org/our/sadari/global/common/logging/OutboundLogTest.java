package org.our.sadari.global.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.our.sadari.global.common.config.RestTemplateConfig;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : OutboundLogTest
 * author         : SeungHyeon.Kang
 * date           : 2026-09-19
 * description    : 외부 응답·오류 계약과 민감정보 없는 연동 로그 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-19        SeungHyeon.Kang         최초 생성
 */
@ExtendWith(OutputCaptureExtension.class)
class OutboundLogTest {

    /** 외부 응답 본문 소비 없이 원본 응답과 안전한 메타데이터 유지 */
    @Test
    void preservesResponseBody(CapturedOutput output) {
        // 실제 운영 클라이언트 설정의 인터셉터 연결 검증
        RestTemplate client = new RestTemplateConfig().restTemplate(1000, 1000);
        // 외부 네트워크 없이 공급자 응답을 재현할 서버
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();
        // URI에 포함된 비밀값과 본문 로그 제외 검증
        server.expect(requestTo("https://example.test/private-path?token=private-token"))
                .andRespond(withSuccess("private-body", MediaType.TEXT_PLAIN));
        // 인터셉터가 본문을 선소비하지 않는지 확인
        assertThat(client.getForObject("https://example.test/private-path?token=private-token", String.class)).isEqualTo("private-body");
        // 정해진 외부 호출 횟수 검증
        server.verify();
        // 상태와 호스트 외에 민감 입력을 출력하지 않도록 확인
        assertThat(output.getOut()).contains("event=outbound_http", "host=example.test", "status=200")
                .doesNotContain("private-path", "private-token", "private-body");
    }

    /** 공급자 오류 응답을 기존 RestTemplate 예외로 전달 */
    @Test
    void preservesErrorResponse(CapturedOutput output) {
        // 기존 응답 오류 처리기를 사용하는 외부 클라이언트
        RestTemplate client = new RestTemplateConfig().restTemplate(1000, 1000);
        // 공급자 장애 재현 서버
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();
        // 민감 본문이 있는 서버 장애 응답
        server.expect(requestTo("https://example.test/service"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).body("private-error-body"));
        // 기존 장애 예외 전파 계약 유지
        assertThrows(HttpServerErrorException.class, () -> client.getForObject("https://example.test/service", String.class));
        // HTTP 상태만 경고로 기록
        assertThat(output.getOut()).contains("WARN", "status=503").doesNotContain("private-error-body");
    }

    /** 네트워크 예외의 유형만 기록하며 동일한 예외 전파 */
    @Test
    void logsTransportFailure(CapturedOutput output) throws Exception {
        // 외부 요청 메타데이터 대역
        HttpRequest request = mock(HttpRequest.class);
        // 전송 실패를 재현할 실행기
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        // URI에 인증정보가 있어도 호스트만 기록
        when(request.getURI()).thenReturn(URI.create("https://private-user:private-pass@example.test/private-path"));
        // 요청 본문을 그대로 전달할 입력값
        byte[] body = new byte[0];
        // 민감 메시지가 담긴 네트워크 예외
        IOException error = new IOException("private-transport-message");
        // HTTP 응답이 오기 전 실패 재현
        when(execution.execute(request, body)).thenThrow(error);
        // 원래 네트워크 예외를 유지하는지 확인
        assertThat(assertThrows(IOException.class, () -> new OutboundLogInterceptor().intercept(request, body, execution))).isSameAs(error);
        // 요청 URL과 원문 메시지를 제외한 원인 기록
        assertThat(output.getOut()).contains("ERROR", "java.io.IOException", "host=example.test")
                .doesNotContain("private-user", "private-pass", "private-path", "private-transport-message");
    }

    /** 중첩·suppressed 예외의 원문과 순환 체인 로그 증폭 방지 */
    @Test
    void excludesNestedSecrets() {
        // 원인 체인의 메시지 제외 검증 대상
        RuntimeException cause = new RuntimeException("private-cause");
        // 상위 메시지 제외 검증 대상
        RuntimeException error = new RuntimeException("private-error", cause);
        // suppressed 메시지도 기록 대상에서 제외
        error.addSuppressed(new IOException("private-suppressed"));
        // 순환 체인도 고정된 횟수 안에서 종료
        cause.initCause(error);
        // 원인 유형과 코드 위치를 보존하면서 민감 메시지 제외
        assertThat(LogSafe.getFailure(error)).contains("RuntimeException", "OutboundLogTest").doesNotContain("private-");
        // 예외가 없는 업무 실패를 안전하게 표현
        assertThat(LogSafe.getFailure(null)).isEqualTo("none");
    }

    /** 상태 파싱 실패로 호출자에게 전달되지 않는 응답 연결 해제 */
    @Test
    void closesUnreadableResponse() throws Exception {
        // 외부 요청과 실행기 대역
        HttpRequest request = mock(HttpRequest.class);
        // 원본 응답을 반환할 HTTP 실행기
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        // 상태를 읽을 수 없는 공급자 응답
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        // 안전한 공급자 호스트
        when(request.getURI()).thenReturn(URI.create("https://example.test"));
        // 기존 본문 전달 계약 유지
        byte[] body = new byte[0];
        // 응답 연결 이후 상태 파싱 실패 재현
        when(execution.execute(request, body)).thenReturn(response);
        // 상태 조회 예외 구성
        when(response.getStatusCode()).thenThrow(new IOException("private-status-data"));
        // 기존 통신 예외 전파 확인
        assertThrows(IOException.class, () -> new OutboundLogInterceptor().intercept(request, body, execution));
        // 호출자에게 전달되지 못한 연결 누수 방지 확인
        verify(response).close();
    }
}
