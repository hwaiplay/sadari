package org.our.sadari.global.common.result;

import lombok.Getter;
import org.our.sadari.global.common.util.MessageUtils;

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

@Getter
public class ResultData<T> {

    private final ResultEnum resultEnum;
    private final T data;

    /**
     * 성공 응답 생성자
     */
    private ResultData(ResultEnum resultEnum, T data) {
        this.resultEnum = resultEnum;
        this.data = data;
    }

    /**
     * 성공 응답 (데이터 없음)
     */
    public static <T> ResultData<T> success() {
        return new ResultData<>(ResultEnum.COMMON_SUCCESS, null);
    }

    /**
     * 성공 응답 (데이터 있음)
     */
    public static <T> ResultData<T> success(T data) {
        return new ResultData<>(ResultEnum.COMMON_SUCCESS, data);
    }

    /**
     * 실패 응답 (messageKey 그대로 - 내부용)
     */
    public static <T> ResultData<T> fail(ResultEnum resultEnum) {
        // Enum의 키를 번역기에 넣어서 실제 메시지를 뽑아옵니다.
        return new ResultData<>(resultEnum, null);
    }
}