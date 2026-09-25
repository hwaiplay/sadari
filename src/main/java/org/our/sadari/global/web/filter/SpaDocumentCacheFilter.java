package org.our.sadari.global.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * fileName       : SpaDocumentCacheFilter
 * author         : HanWon.Jang
 * date           : 2026-09-16
 * description    : React 진입 문서가 이전 배포 Cache로 재사용되지 않도록 응답 정책을 적용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-16        HanWon.Jang        최초 생성
 */
@Component
public class SpaDocumentCacheFilter extends OncePerRequestFilter {

    // 브라우저와 중간 Cache가 화면 문서를 저장하지 않도록 적용할 표준 정책
    private static final String SPA_CACHE_CONTROL = "no-store, no-cache, max-age=0, must-revalidate";
    // React 화면 전달에서 제외되는 서버 및 정적 자원 경로 접두사
    private static final List<String> EXCLUDED_PATH_PREFIX_LIST = List.of(
            "/api",
            "/uploads",
            "/swagger-ui",
            "/v3",
            "/error",
            "/assets",
            "/favicon",
            "/fonts",
            "/img"
    );

    /**
     * React 화면 문서 응답에 재사용 금지 Header를 설정한 뒤 요청 처리를 계속
     *
     * @author HanWon.Jang
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param filterChain 다음 Servlet Filter 체인
     * @throws ServletException 다음 Filter 또는 Controller 처리에 실패하면 발생
     * @throws IOException 응답 처리 중 입출력에 실패하면 발생
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
                                  , FilterChain filterChain) throws ServletException, IOException {
        // 배포마다 달라지는 HTML 문서만 브라우저 저장 대상에서 제외
        if (isSpaDocumentRequest(request)) {
            // 브라우저와 Cloudflare가 이전 HTML을 다시 사용하지 않도록 표준 Cache Header를 설정
            response.setHeader(HttpHeaders.CACHE_CONTROL, SPA_CACHE_CONTROL);
            // HTTP/1.0 호환 Cache에도 같은 재사용 금지 정책을 전달
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            // 이미 저장된 화면 문서도 즉시 만료된 것으로 처리
            response.setDateHeader(HttpHeaders.EXPIRES, 0);
        }

        // Header 설정 뒤 기존 보안과 Controller 및 정적 자원 처리를 그대로 수행
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 경로가 API나 정적 파일이 아닌 React 화면 문서인지 판정
     *
     * @author HanWon.Jang
     * @param request 현재 HTTP 요청
     * @return React 화면 문서 요청이면 true
     */
    private boolean isSpaDocumentRequest(HttpServletRequest request) {
        // 상태 변경 요청과 화면 문서가 아닌 Method에는 Cache 정책을 적용하지 않음
        if (!"GET".equals(request.getMethod())) {
            // 화면 문서 조회가 아님을 반환
            return false;
        }

        // 애플리케이션 Context Path를 제외한 실제 요청 경로를 조회
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());

        // 정적 진입 문서는 확장자가 있어도 React 실행 문서이므로 명시적으로 포함
        if ("/".equals(requestPath) || "/index.html".equals(requestPath)) {
            // 브라우저가 저장하면 안 되는 React 진입 문서임을 반환
            return true;
        }

        // 확장자가 있는 파일은 해시 또는 파일별 Cache 정책을 유지
        if (requestPath.contains(".")) {
            // React 화면 문서가 아님을 반환
            return false;
        }

        // API와 업로드 및 정적 자원 경로를 화면 문서 정책에서 제외
        for (String excludedPathPrefix : EXCLUDED_PATH_PREFIX_LIST) {
            // 경로 자체 또는 하위 경로가 제외 접두사와 일치하는지 확인
            if (requestPath.equals(excludedPathPrefix) || requestPath.startsWith(excludedPathPrefix + "/")) {
                // 개별 Endpoint 또는 파일 Cache 정책을 유지하도록 제외
                return false;
            }
        }

        // 확장자 없는 나머지 경로는 React Router가 처리할 화면 문서로 판정
        return true;
    }
}
