package org.our.sadari.global.common.logging;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * fileName       : OutboundLogInterceptor
 * author         : SeungHyeon.Kang
 * date           : 2026-09-19
 * description    : 외부 HTTP 연동의 상태와 응답 헤더 수신 시간 기록
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-19        SeungHyeon.Kang         최초 생성
 */
@Slf4j
public class OutboundLogInterceptor implements ClientHttpRequestInterceptor {

    /**
     * 외부 연동 결과를 본문과 인증정보 없이 기록
     *
     * @author SeungHyeon.Kang
     * @param request 외부 요청
     * @param body 전송할 본문
     * @param execution 실제 HTTP 실행기
     * @return 소비하지 않은 원본 응답
     * @throws IOException 외부 통신 실패
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 단조 시계를 사용한 외부 응답 헤더 수신 시간 측정
        long started = System.nanoTime();
        // 경로·쿼리·사용자 정보가 제외된 공급자 호스트
        String host = request.getURI().getHost();
        ClientHttpResponse response = null;
        // 외부 오류와 정상 응답의 공통 관측 경계
        try {
            // 본문과 헤더를 기록하거나 복제하지 않는 원본 통신
            response = execution.execute(request, body);
            // 응답 헤더 기준 HTTP 상태 확인
            int status = response.getStatusCode().value();
            // 네트워크 연결과 응답 헤더 대기 시간
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            // 공급자 거절과 장애를 정상 연동과 구분
            if (status >= 400) {
                // 공급자 응답 본문 없이 오류 상태 기록
                log.warn("event=outbound_http host={} status={} durationMs={}"
                       , host, status, elapsed);
            }

            // 정상 외부 호출의 비용과 지연 확인
            else {
                // 응답 데이터와 토큰을 제외한 완료 요약
                log.info("event=outbound_http host={} status={} durationMs={}"
                       , host, status, elapsed);
            }

            // 호출자가 기존 방식으로 읽고 닫을 원본 응답
            return response;
        }

        // 외부 URL과 요청 값이 담길 수 있는 원시 예외 출력 금지
        catch (IOException | RuntimeException error) {
            // 상태 조회 중 실패한 응답은 호출자에게 전달되지 않으므로 자원 해제
            if (!StringUtil.isEmpty(response)) {
                // 오류 응답 연결 누수 방지
                response.close();
            }

            // 메시지 없이 원인 유형과 코드 위치 기록
            log.error("event=outbound_http host={} durationMs={} failure={}"
                    , host, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started), LogSafe.getFailure(error));
            throw error;
        }
    }
}
