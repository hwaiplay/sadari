package org.our.sadari.global.common.result;

/**
 * packageName    : org.our.sadari.global.common.result
 * fileName       : ResultData.java
 * author         : hanwon.Jang
 * date           : 2026-03-24
 * description    : 공통 응답 객체
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-24       hanwon.Jang       최초 생성
 */

import lombok.Getter;

@Getter
public class ResultData<T> {

    private final int code;
    private final String message;
    private final T data;

    /**
     * 성공 응답 생성자
     */
    private ResultData(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ResultData<T> success() {
        return new ResultData<>(200, "success", null);
    }

    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> ResultData<T> success(T data) {
        return new ResultData<>(200, "success", data);
    }

    /**
     * 실패 응답 (messageKey 그대로 - 내부용)
     */
    public static <T> ResultData<T> fail(ResultEnum resultEnum) {
        return new ResultData<>(resultEnum.getCode(), resultEnum.getMessageKey(), null);
    }

    /**
     * 실패 응답 (메시지 변환 완료)
     */
    public static <T> ResultData<T> fail(ResultEnum resultEnum, String message) {
        return new ResultData<>(resultEnum.getCode(), message, null);
    }
}