package org.our.sadari.global.common.logging;

import org.our.sadari.global.common.result.ResultData;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * fileName       : ResultLogAdvice
 * author         : SeungHyeon.Kang
 * date           : 2026-09-19
 * description    : HTTP 성공 응답에 포함된 업무 실패 코드의 운영 로그 연결
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-19        SeungHyeon.Kang         최초 생성
 */
@RestControllerAdvice
public class ResultLogAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 응답 선언 타입과 무관하게 실제 공통 응답 확인 허용
     *
     * @author SeungHyeon.Kang
     * @param returnType Controller 반환 타입
     * @param converterType 응답 변환기 타입
     * @return 실제 응답 검사 허용 여부
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseEntity와 Object 선언 응답도 실제 본문으로 판정
        return true;
    }

    /**
     * 본문을 변경하지 않고 공통 업무 결과 코드만 요청 문맥에 저장
     *
     * @author SeungHyeon.Kang
     * @param body 반환 본문
     * @param returnType 반환 선언 타입
     * @param contentType 응답 미디어 타입
     * @param converterType 응답 변환기 타입
     * @param request 현재 요청
     * @param response 현재 응답
     * @return 기존 응답 객체
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType
                                 , Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 업무 결과만 추출하며 파일·문자열·기타 응답은 그대로 유지
        if (body instanceof ResultData result && request instanceof ServletServerHttpRequest servletRequest) {
            // HTTP 200으로 반환된 업무 거절도 완료 로그에 전달
            servletRequest.getServletRequest().setAttribute(RequestLogFilter.RESULT_CODE, result.getCode());
        }

        // 응답 직렬화와 API 계약 유지
        return body;
    }
}
