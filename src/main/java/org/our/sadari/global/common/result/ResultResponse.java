package org.our.sadari.global.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResultResponse 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Getter
@AllArgsConstructor
public class ResultResponse {

    private int code; // 업무 규칙에서 사용하는 고정 설정 값이다.
    private String message; // 업무 규칙에서 사용하는 고정 설정 값이다.


}
