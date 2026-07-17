package org.our.sadari.global.common.util;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * LocaleUtil 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public class LocaleUtil {

    /**
     * LocaleUtil 객체를 생성한다.
     *
     * @author Seunghyeon.Kang
     */
    private LocaleUtil() {
        throw new UnsupportedOperationException("유틸리티 클래스는 인스턴스를 생성할 수 없습니다.");
    }

    /**
     * getLocale 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @return 처리 결과
     */
    public static String getLocale() {
        Locale currentLocale = LocaleContextHolder.getLocale();

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (currentLocale == null) {
            return "KO";
        }

        String language = currentLocale.getLanguage();

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (language == null || language.trim().isEmpty()) {
            return "KO";
        }

        return language.toUpperCase();
    }
}
