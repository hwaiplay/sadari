package org.our.sadari.global.common.util;

import java.nio.charset.StandardCharsets;

/**
 * 화면 출력용 문자열 escaping과 UTF-8 byte 길이 계산을 제공하는 유틸 클래스입니다.
 *
 * @author Seunghyeon.Kang
 */
public class XssUtil {

    private XssUtil() {
    }

    /**
     * HTML에서 의미를 가지는 특수문자를 entity로 변환합니다.
     *
     * @author Seunghyeon.Kang
     * @param value 변환할 문자열
     * @return HTML entity로 변환된 문자열, 입력값이 비어 있으면 null
     */
    public static String escape(String value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        return value.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 문자열의 UTF-8 byte 길이를 계산합니다.
     *
     * @author Seunghyeon.Kang
     * @param value byte 길이를 계산할 문자열
     * @return UTF-8 기준 byte 길이
     */
    public static int utf8ByteLength(String value) {
        if (StringUtil.isEmpty(value)) {
            return 0;
        }

        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
