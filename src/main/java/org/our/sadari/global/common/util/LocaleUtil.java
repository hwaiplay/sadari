package org.our.sadari.global.common.util;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * fileName       : LocaleUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-15
 * description    : Spring Security 및 HTTP 요청 컨텍스트에서 현재 사용자의 로케일 정보를 추출하여 프로젝트 표준 규격(대문자 2자리)으로 가공해 주는 공통 유틸리티 클래스입니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-15        SeungHyeon.Kang       최초 생성
 */
public class LocaleUtil {

    /**
     * 인스턴스화를 방지하기 위한 프라이빗 생성자입니다.
     *
     * @author SeungHyeon.Kang
     */
    private LocaleUtil() {
        throw new UnsupportedOperationException("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * 현재 스레드 로컬에 저장된 사용자 로케일의 언어 코드를 대문자 2자리 문자열로 반환합니다.
     *
     * @author SeungHyeon.Kang
     * @return 대문자 2자리 언어 코드 (예: "KO", "EN"). 로케일 정보가 유효하지 않을 경우 기본값 "KO" 반환.
     */
    public static String getLocale() {
        Locale currentLocale = LocaleContextHolder.getLocale();

        // 1. 컨텍스트홀더에서 가져온 로케일이 NULL인 경우를 대비해 기본값 "KO"를 안전하게 세팅합니다.
        if (currentLocale == null) {
            return "KO";
        }

        String language = currentLocale.getLanguage();

        // 2. 파싱된 언어 문자열이 빈 값일 경우를 방지하여 디폴트 처리를 수행합니다.
        if (language == null || language.trim().isEmpty()) {
            return "KO";
        }

        return language.toUpperCase();
    }
}
