package org.our.sadari.global.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.our.sadari.global.web.filter.SpaDocumentCacheFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * fileName       : SpaControllerTest
 * author         : HanWon.Jang
 * date           : 2026-09-16
 * description    : React 화면 전달과 정적 파일 경로 분리 정책을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-16        HanWon.Jang        최초 생성
 */
class SpaControllerTest {

    // React 화면 전달 Controller를 검증할 MVC 테스트 도구
    private MockMvc mockMvc;

    /**
     * 실제 Spring MVC 경로 매칭을 사용하는 독립 테스트 환경을 구성함
     *
     * @author HanWon.Jang
     */
    @BeforeEach
    void setUp() {
        // 다른 애플리케이션 의존성 없이 화면 전달 Controller만 등록함
        mockMvc = MockMvcBuilders.standaloneSetup(new SpaController())
                .addFilters(new SpaDocumentCacheFilter())
                .build();
    }

    /**
     * 브라우저에서 직접 요청한 중첩 사용자 화면이 React 진입 문서로 전달되는지 검증함
     *
     * @author HanWon.Jang
     * @throws Exception MVC 요청 처리 중 오류가 발생할 때 전달함
     */
    @Test
    void forwardsSpaScreen() throws Exception {
        // 중첩된 React 화면 경로가 진입 문서로 전달되는지 검증함
        mockMvc.perform(get("/reading-clubs/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, max-age=0, must-revalidate"))
                .andExpect(forwardedUrl("/index.html"));
    }

    /**
     * 첫 접속 진입 문서도 이전 배포 Cache를 사용하지 않고 React 화면으로 전달하는지 검증함
     *
     * @author HanWon.Jang
     * @throws Exception MVC 요청 처리 중 오류가 발생할 때 전달함
     */
    @Test
    void preventsRootDocumentCache() throws Exception {
        // 루트 화면에 재사용 금지 Header를 적용하고 React 진입 문서로 전달하는지 검증함
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, max-age=0, must-revalidate"))
                .andExpect(forwardedUrl("/index.html"));
    }

    /**
     * JavaScript 정적 파일 요청이 React 진입 문서로 바뀌지 않는지 검증함
     *
     * @author HanWon.Jang
     * @throws Exception MVC 요청 처리 중 오류가 발생할 때 전달함
     */
    @Test
    void ignoresStaticAssetPath() throws Exception {
        // 정적 파일은 화면 전달 Controller가 처리하지 않아 실제 Resource Handler에 위임되는지 검증함
        mockMvc.perform(get("/assets/index.js"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist(HttpHeaders.CACHE_CONTROL))
                .andExpect(forwardedUrl(null));
    }
}
