package org.our.sadari.global.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.our.sadari.global.common.util.MessageUtils;

/**
 * ResultData 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Getter
@Schema(description = "공통 API 응답 형식")
public class ResultData {

    @Schema(description = "업무 응답 코드. 성공은 200이다.", example = "200")
    private final int code;

    @Schema(description = "응답 메시지. 실패 시 사용자에게 표시할 메시지를 담는다.", example = "success")
    private final String message;

    @Schema(description = "API별 응답 데이터")
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
