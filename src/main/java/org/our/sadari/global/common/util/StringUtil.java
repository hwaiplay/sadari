package org.our.sadari.global.common.util;

import java.util.List;
import java.util.Map;

/**
 * fileName       : StringUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-03-21
 * description    : 공통 문자열 정규화와 빈 값 판정 기능을 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21        SeungHyeon.Kang    최초 생성
 * 2026-08-26        SeungHyeon.Kang        사용 문자열 기능 정리
 */
public final class StringUtil {

    // 빈 문자열 기본값
    public static final String EMPTY = "";

    /**
     * 정적 유틸리티 클래스의 외부 생성을 차단함
     *
     * @author SeungHyeon.Kang
     */
    private StringUtil() {

    }

    /**
     * 문자열을 지정한 최대 문자 길이에 맞춰 자르고 접미사를 덧붙임
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param output 문자열을 자른 뒤 덧붙일 문자열
     * @param maxLength 허용할 최대 문자 길이
     * @return 길이가 제한된 문자열, 원본이 비어 있으면 null
     */
    public static String cutString(String source, String output, int maxLength) {
        // 비어 있는 입력은 기존 호출부의 선택 처리 계약을 유지하도록 null로 반환함
        if (isEmpty(source)) {
            // 변환할 원본 문자열이 없음을 반환함
            return null;
        }

        // 허용 길이를 초과한 문자열에만 접미사를 적용함
        if (source.length() > maxLength) {
            // 최대 길이로 자른 문자열과 접미사를 반환함
            return source.substring(0, maxLength) + output;
        }

        // 길이 제한 안에 있는 원본 문자열을 그대로 반환함
        return source;
    }

    /**
     * 문자열을 지정한 최대 문자 길이에 맞춰 자름
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param maxLength 허용할 최대 문자 길이
     * @return 길이가 제한된 문자열, 원본이 비어 있으면 null
     */
    public static String cutString(String source, int maxLength) {
        // 접미사 없이 동일한 길이 제한 계약을 적용한 결과를 반환함
        return cutString(source, EMPTY, maxLength);
    }

    /**
     * 사용자 평문 입력의 앞뒤 공백을 제거함
     *
     * @author SeungHyeon.Kang
     * @param value 정규화할 사용자 평문 입력
     * @return 공백이 제거된 문자열, 입력이 비어 있으면 null
     */
    public static String normalizePlainText(String value) {
        // 비어 있는 입력은 저장 조건에서 누락값으로 판단할 수 있도록 null로 통일함
        if (isEmpty(value)) {
            // 정규화할 사용자 입력이 없음을 반환함
            return null;
        }

        // 앞뒤 공백을 제거한 사용자 입력을 반환함
        return value.trim();
    }

    /**
     * 사용자 평문 입력의 앞뒤 공백을 제거하고 최대 문자 길이를 적용함
     *
     * @author SeungHyeon.Kang
     * @param value 정규화할 사용자 평문 입력
     * @param maxLength 허용할 최대 문자 길이
     * @return 공백과 길이가 정규화된 문자열, 입력이 비어 있으면 null
     */
    public static String normalizePlainText(String value, int maxLength) {
        // 길이 제한 전에 공백 정책을 한 번만 적용함
        String normalizedValue = normalizePlainText(value);

        // 공백 제거 뒤 값이 없으면 길이 계산을 진행하지 않음
        if (isEmpty(normalizedValue)) {
            // 정규화 뒤 남은 사용자 입력이 없음을 반환함
            return null;
        }

        // 공백이 제거된 입력에 최대 문자 길이를 적용한 결과를 반환함
        return cutString(normalizedValue, maxLength);
    }

    /**
     * 문자열, 목록, 맵 및 객체 배열의 null 또는 빈 상태를 판정함
     *
     * @author SeungHyeon.Kang
     * @param value 빈 상태를 검사할 값
     * @return null이거나 지원 타입의 내용이 비어 있으면 true
     */
    public static boolean isEmpty(Object value) {
        // 공통 빈 값 판정 메서드 내부에서는 재귀 호출을 피하기 위해 null 자체를 확인함
        if (value == null) {
            // null 값을 빈 상태로 반환함
            return true;
        }

        // 문자열은 앞뒤 공백을 제외한 내용이 있어야 유효한 값으로 판정함
        if (value instanceof String stringValue) {
            // 공백이 제거된 문자열의 빈 상태를 반환함
            return stringValue.trim().isEmpty();
        }

        // 목록은 포함된 항목 수를 기준으로 빈 상태를 판정함
        if (value instanceof List<?> listValue) {
            // 목록의 빈 상태를 반환함
            return listValue.isEmpty();
        }

        // 맵은 포함된 항목 수를 기준으로 빈 상태를 판정함
        if (value instanceof Map<?, ?> mapValue) {
            // 맵의 빈 상태를 반환함
            return mapValue.isEmpty();
        }

        // 객체 배열은 배열 길이를 기준으로 빈 상태를 판정함
        if (value instanceof Object[] arrayValue) {
            // 객체 배열의 빈 상태를 반환함
            return arrayValue.length == 0;
        }

        // 지원 타입이 아닌 객체는 존재하는 값으로 반환함
        return false;
    }

    /**
     * 전달된 값 중 하나라도 null 또는 빈 상태인지 판정함
     *
     * @author SeungHyeon.Kang
     * @param values 빈 상태 포함 여부를 검사할 값 목록
     * @return 하나 이상의 값이 비어 있으면 true
     */
    public static boolean hasEmpty(Object... values) {
        // 가변 인자 배열 자체가 없으면 필수 값이 누락된 요청으로 판정함
        if (values == null) {
            // 검사할 값 목록이 없으므로 빈 상태를 반환함
            return true;
        }

        // 모든 전달값에 동일한 공통 빈 값 정책을 적용함
        for (Object value : values) {
            // 하나라도 비어 있으면 나머지 값을 검사하지 않음
            if (isEmpty(value)) {
                // 빈 값이 포함된 상태를 반환함
                return true;
            }

        }

        // 모든 값이 존재하는 상태를 반환함
        return false;
    }
}
