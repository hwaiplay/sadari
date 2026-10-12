package org.our.sadari.global.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
 * 2026-10-13        SeungHyeon.Kang    해시 정적 자원 장기 Cache 적용
 */
@Component
public class SpaDocumentCacheFilter extends OncePerRequestFilter {

    // 브라우저와 중간 Cache가 화면 문서를 저장하지 않도록 적용할 표준 정책
    private static final String SPA_CACHE_CONTROL = "no-store, no-cache, max-age=0, must-revalidate";
    // 내용 해시가 URL에 포함된 빌드 자원을 브라우저와 CDN이 장기간 재사용할 정책
    private static final String IMMUTABLE_ASSET_CACHE_CONTROL = "public, max-age=31536000, immutable";
    /**
     * 해시 정적 자원은 장기 저장하고 나머지 응답은 재사용하지 않도록 설정
     *
     * @author SeungHyeon.Kang
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param filterChain 다음 Servlet Filter 체인
     * @throws ServletException 다음 Filter 또는 Controller 처리에 실패하면 발생
     * @throws IOException 응답 처리 중 입출력에 실패하면 발생
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
                                  , FilterChain filterChain) throws ServletException, IOException {
        // Vite 내용 해시가 바뀔 때 URL도 바뀌는 자원은 브라우저와 Cloudflare가 재사용
        if (isImmutableAssetRequest(request)) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, IMMUTABLE_ASSET_CACHE_CONTROL);
        }
        else {
            // API와 HTML 및 같은 URL로 교체되는 정적 파일은 기존 보안 재사용 금지 정책을 유지
            response.setHeader(HttpHeaders.CACHE_CONTROL, SPA_CACHE_CONTROL);
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setDateHeader(HttpHeaders.EXPIRES, 0);
        }

        // Header 설정 뒤 기존 보안과 Controller 및 정적 자원 처리를 그대로 수행
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 경로가 내용 해시를 사용하는 Vite 빌드 자원인지 판정
     *
     * @author SeungHyeon.Kang
     * @param request 현재 HTTP 요청
     * @return 장기 Cache를 적용할 정적 자원이면 true
     */
    private boolean isImmutableAssetRequest(HttpServletRequest request) {
        // 조회 요청에만 공개 Cache 정책을 허용
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) {
            return false;
        }

        // 애플리케이션 Context Path를 제외한 실제 요청 경로를 조회
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        // Vite가 내용 해시 파일만 출력하는 자원 디렉터리에 장기 Cache를 적용
        return requestPath.startsWith("/assets/");
    }

}
