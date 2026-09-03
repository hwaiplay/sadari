package org.our.sadari.global.common.util;

import java.nio.charset.StandardCharsets;

/**
 * fileName       : XssUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-07
 * description    : 공통 처리에 필요한 변환과 판정 기능을 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-07        SeungHyeon.Kang    최초 생성
 */
public class XssUtil {

    private XssUtil() {

    }

    /**
     * HTML 특수 문자 이스케이프함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static String escape(String value) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(value)) {
            // 조회하거나 생성할 값이 없음을 반환함
            return null;
        }

        // HTML 특수 문자 이스케이프 결과를 반환함
        return value.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * UTF-8 문자열 바이트 길이 계산함
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 처리 결과
     */
    public static int utf8ByteLength(String value) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(value)) {
            // UTF-8 문자열 바이트 길이 계산 결과를 반환함
            return 0;
        }

        // UTF-8 문자열 바이트 길이 계산 결과를 반환함
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
