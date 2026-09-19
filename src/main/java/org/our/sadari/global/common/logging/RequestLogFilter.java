package org.our.sadari.global.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.util.StringUtil;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/**
 * fileName       : RequestLogFilter
 * author         : SeungHyeon.Kang
 * date           : 2026-09-19
 * description    : 보안 필터를 포함한 API 요청 결과와 소요 시간 기록
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-19        SeungHyeon.Kang         최초 생성
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    // 공통 응답에서 전달하는 업무 결과 코드의 요청 속성
    public static final String RESULT_CODE = RequestLogFilter.class.getName() + ".resultCode";
    // 인증 거절 원인을 전달하는 요청 속성
    public static final String SECURITY_REASON = RequestLogFilter.class.getName() + ".securityReason";
    // 외부에서 주입한 임의 메서드 문자열의 로그 기록 방지
    private static final Set<String> METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS", "TRACE");
    // 지연 요청 경고 기준 밀리초
    private final long slowRequestMillis;

    /**
     * API 지연 경고 기준 설정
     *
     * @author SeungHyeon.Kang
     * @param slowRequestMillis 지연 경고 기준 밀리초
     */
    public RequestLogFilter(@Value("${app.logging.slow-request-millis:1000}") long slowRequestMillis) {

        this.slowRequestMillis = Math.max(1, slowRequestMillis);
    }

    /**
     * 정적 파일과 화면 문서 요청을 API 로그에서 제외
     *
     * @author SeungHyeon.Kang
     * @param request 현재 HTTP 요청
     * @return API 이외 요청 여부
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 컨텍스트 경로를 제외하여 WAR 배포에서도 동일 범위 적용
        String path = request.getRequestURI().substring(request.getContextPath().length());
        // API와 API 하위 경로만 기록
        return !path.equals("/api") && !path.startsWith("/api/");
    }

    /**
     * 서버 요청 식별자 발급 및 API 완료 결과 기록
     *
     * @author SeungHyeon.Kang
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param chain 보안 및 MVC 필터 체인
     * @throws ServletException 하위 서블릿 실패
     * @throws IOException 요청 또는 응답 스트림 실패
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 외부 요청 헤더를 신뢰하지 않는 서버 생성 식별자
        String requestId = UUID.randomUUID().toString();
        // 같은 스레드의 기존 진단 문맥 보존
        String previousId = MDC.get("requestId");
        // 시스템 시각 변경의 영향을 받지 않는 경과 시간 기준
        long started = System.nanoTime();
        Throwable failure = null;
        // 중첩 로그의 요청 연결
        MDC.put("requestId", requestId);
        // 문의 시 운영 로그를 찾을 응답 식별자
        response.setHeader("X-Request-ID", requestId);
        // 하위 필터와 MVC 실행 중에도 최종 로그와 문맥 복구 보장
        try {
            // 기존 인증과 업무 처리 흐름 유지
            chain.doFilter(request, response);
        }

        // 처리되지 않은 예외도 완료 로그에 포함하고 기존 전파 유지
        catch (IOException | ServletException | RuntimeException error) {
            failure = error;
            throw error;
        }

        // 정상·인증 거절·예외 모두 완료 결과 기록
        finally {
            // 로깅 중 실패가 발생해도 스레드 문맥 복구 보장
            try {
                // HTTP와 업무 응답 코드 및 안전한 예외 정보 기록
                setCompletionLog(request, response, started, failure);
            }

            // 풀 스레드 재사용 시 다른 요청과 식별자가 섞이지 않도록 복원
            finally {
                // 기존 식별자가 없던 요청은 현재 값 제거
                if (StringUtil.isEmpty(previousId)) {
                    // 현재 요청 문맥 해제
                    MDC.remove("requestId");
                }

                // 상위 실행 문맥이 있던 경우 기존 값 복원
                else {
                    // 상위 요청 식별자 복원
                    MDC.put("requestId", previousId);
                }
            }
        }
    }

    private void setCompletionLog(HttpServletRequest request, HttpServletResponse response, long started
                                , Throwable failure) {
        // 사용자가 입력한 경로 대신 서버의 매핑 패턴만 사용
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        // 인증 이전 거절과 미매핑 요청에서 원시 경로 노출 방지
        String route = pattern instanceof String value ? value : "unmapped";
        // 허용된 메서드만 로그로 전달
        String method = METHODS.contains(request.getMethod()) ? request.getMethod() : "OTHER";
        // 현재 응답 상태와 업무 코드 조회
        int status = response.getStatus();
        // 응답 본문을 복제하거나 읽지 않고 공통 응답 코드만 조회
        Object resultCode = request.getAttribute(RESULT_CODE);
        // 서버가 지정한 인증 거절 이유 조회
        Object reason = request.getAttribute(SECURITY_REASON);
        // 요청 경과 시간 계산
        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        // 지연 요청 별도 판정
        boolean slow = elapsed >= slowRequestMillis;
        // 예외·HTTP 오류·업무 실패를 로그 레벨에 반영
        boolean rejected = resultCode instanceof Integer code && code != 200;
        // 모든 결과를 같은 키로 검색할 수 있는 공통 형식
        String format = "event=http_request method={} route={} status={} resultCode={} durationMs={} slow={} reason={} failure={}";
        // 미처리 예외와 서버 HTTP 실패는 장애로 기록
        if (!StringUtil.isEmpty(failure) || status >= 500) {
            // 원시 예외를 제외한 서버 장애 정보
            log.error(format
                    , method, route, status
                    , resultCode, elapsed, slow
                    , reason, LogSafe.getFailure(failure));
        }

        // 업무 거절·클라이언트 오류·지연 응답은 경고로 기록
        else if (status >= 400 || rejected || slow) {
            // 업무 응답 메시지 없이 실패 코드만 기록
            log.warn(format
                   , method, route, status
                   , resultCode, elapsed, slow
                   , reason, "none");
        }

        // 정상 API도 운영 기본 레벨에서 조회 가능
        else {
            // 정상 요청 완료 요약
            log.info(format
                   , method, route, status
                   , resultCode, elapsed, slow
                   , reason, "none");
        }
    }
}
