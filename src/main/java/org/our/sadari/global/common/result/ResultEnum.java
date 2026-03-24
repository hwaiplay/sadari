package org.our.sadari.global.common.result;

import lombok.Getter;

/**
 * fileName       : ResultEnum
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 애플리케이션 전역에서 사용하는 "에러 코드 정의 Enum"
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang       최초 생성
 */
@Getter
public enum ResultEnum {


    /**
     * 인증 실패 (로그인 필요 or 인증 안됨)
     * - accessToken 없음
     * - refreshToken 없음
     * - 기타 인증 불가 상황
     */
    AUTH_FAIL(1001, "auth.common.fail"),   //

    /**
     * 토큰이 유효하지 않은 경우 (위조, 형식 오류 등)
     * - JWT 파싱 실패
     * - 서명 검증 실패
     */
    TOKEN_INVALID(1002, "auth.token.invalid"),  //

    /**
     * 토큰 만료
     * - accessToken 만료
     * - refreshToken 만료
     */
    TOKEN_EXPIRED(1003, "auth.token.expired"),  //

    /**
     * 권한 없음 (인증은 되었지만 접근 불가)
     * - USER가 ADMIN API 호출
     */
    FORBIDDEN(1004, "auth.common.forbidden");   //

    /**
     * 프론트로 내려줄 고유 에러 코드
     * - 숫자 기반으로 정의 (ex: 1001, 1002)
     * - 프론트는 이 값을 기준으로 분기 처리
     */
    private final int code;

    /**
     * message.properties에서 메시지를 찾기 위한 key
     * - ex) auth.token.expired
     * - 다국어 지원 및 메시지 중앙 관리를 위해 사용
     */
    private final String messageKey;

    /**
     * Enum 생성자
     *
     * @param code        프론트 전달용 에러 코드
     * @param messageKey  메시지 프로퍼티 키
     */
    ResultEnum(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

}
