package org.our.sadari.global.common.util;

import java.nio.charset.StandardCharsets;

public class XssUtil {

    private XssUtil() {
    }

    /**
     * XSS 방어용 HTML 엔티티 변환
     * @Author SeungHyeon.Kang
     * @param value
     * @return
     */
    public static String escape(String value) {
        if (StringUtil.isEmpty(value)) {
            // null 값은 필터링 대상 문자열이 아니므로 그대로 반환한다.
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
     * UTF-8 바이트 길이 계산
     * @Author SeungHyeon.Kang
     * @param value
     * @return
     */
    public static int utf8ByteLength(String value) {
        if (StringUtil.isEmpty(value)) {
            // null 값은 DB에 저장될 문자열이 아니므로 길이를 0으로 판단한다.
            return 0;
        }

        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
