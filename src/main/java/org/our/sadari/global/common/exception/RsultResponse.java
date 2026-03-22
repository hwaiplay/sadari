package org.our.sadari.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * fileName       : RsultResponse
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Getter
@AllArgsConstructor
public class RsultResponse {

    private int code;       // 에러 코드
    private String message; // 메시지


}
