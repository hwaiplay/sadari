package org.our.sadari.global.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * fileName       : ResultResponse
 * author         : SeungHyeon.Kang
 * date           : 2026-03-25
 * description    : 공통 업무에 필요한 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-25        SeungHyeon.Kang    최초 생성
 */
@Getter
@AllArgsConstructor
public class ResultResponse {

    // 공통 응답 결과 코드
    private int code;
    // 공통 응답 메시지
    private String message;


}
