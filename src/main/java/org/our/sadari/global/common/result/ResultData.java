package org.our.sadari.global.common.result;

import lombok.Getter;
import org.our.sadari.global.common.util.MessageUtils;

/**
 * ResultData 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Getter
public class ResultData {

    private final int code;
    private final String message;
    private Object data;

    /**
     * ResultData 객체를 생성한다.
     *
     * @author Seunghyeon.Kang
     * @param code 처리에 필요한 입력값
     * @param message 처리에 필요한 입력값
     * @param data 처리에 필요한 입력값
     */
    private ResultData(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * success 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @return 처리 결과
     */
    public static ResultData success() {
        return new ResultData(200, "success", null);
    }

    /**
     * success 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param data 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static ResultData success(Object data) {
        return new ResultData(200, "success", data);
    }

    /**
     * fail 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param resultEnum 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static ResultData fail(ResultEnum resultEnum) {
        String translatedMessage = MessageUtils.getMessage(resultEnum.getMessageKey());
        return new ResultData(resultEnum.getCode(), translatedMessage, null);
    }

    /**
     * fail 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param resultEnum 처리에 필요한 입력값
     * @param args 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static ResultData fail(ResultEnum resultEnum, Object... args) {
        String translatedMessage = MessageUtils.getMessage(resultEnum.getMessageKey(), args);
        return new ResultData(resultEnum.getCode(), translatedMessage, null);
    }
}
