package org.our.sadari.global.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.our.sadari.global.common.util.MessageUtils;

/**
 * fileName       : ResultData
 * author         : SeungHyeon.Kang
 * date           : 2026-03-25
 * description    : 공통 업무에 필요한 기능을 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-25        SeungHyeon.Kang    최초 생성
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
     * ResultData 객체를 생성함
     *
     * @author SeungHyeon.Kang
     * @param code Kakao 로그인 인가 코드
     * @param message 사용자에게 노출할 응답 메시지
     * @param data 성공 응답에 포함할 업무 데이터
     */
    private ResultData(int code, String message, Object data) {

        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 공통 성공 응답 생성함
     *
     * @author SeungHyeon.Kang
     * @return 처리 결과
     */
    public static ResultData success() {
        // 새로 생성한 ResultData 객체를 반환함
        return new ResultData(200, "success", null);
    }

    /**
     * 공통 성공 응답 생성함
     *
     * @author SeungHyeon.Kang
     * @param data 성공 응답에 포함할 업무 데이터
     * @return 처리 결과
     */
    public static ResultData success(Object data) {
        // 새로 생성한 ResultData 객체를 반환함
        return new ResultData(200, "success", data);
    }

    /**
     * 공통 실패 응답 생성함
     *
     * @author SeungHyeon.Kang
     * @param resultEnum 실패 응답에 사용할 공통 결과 코드
     * @return 처리 결과
     */
    public static ResultData fail(ResultEnum resultEnum) {
        // 사용자 응답 또는 로그에 사용할 메시지를 조회함
        String translatedMessage = MessageUtils.getMessage(resultEnum.getMessageKey());
        // 새로 생성한 ResultData 객체를 반환함
        return new ResultData(resultEnum.getCode(), translatedMessage, null);
    }

    /**
     * 공통 실패 응답 생성함
     *
     * @author SeungHyeon.Kang
     * @param resultEnum 실패 응답에 사용할 공통 결과 코드
     * @param args 메시지 치환에 사용할 인자 목록
     * @return 처리 결과
     */
    public static ResultData fail(ResultEnum resultEnum, Object... args) {
        // 사용자 응답 또는 로그에 사용할 메시지를 조회함
        String translatedMessage = MessageUtils.getMessage(resultEnum.getMessageKey(), args);
        // 새로 생성한 ResultData 객체를 반환함
        return new ResultData(resultEnum.getCode(), translatedMessage, null);
    }
}
