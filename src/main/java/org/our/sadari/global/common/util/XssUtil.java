package org.our.sadari.global.common.util;

import java.nio.charset.StandardCharsets;

/**
 * XssUtil 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public class XssUtil {

    private XssUtil() {
    }

    /**
     * escape 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String escape(String value) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
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
     * utf8ByteLength 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param value 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static int utf8ByteLength(String value) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(value)) {
            return 0;
        }

        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
