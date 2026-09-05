package org.our.sadari.global.common.util;

import org.our.sadari.global.common.constant.Constant;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * fileName       : LocaleUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-15
 * description    : 공통 처리에 필요한 변환과 판정 기능을 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-15        SeungHyeon.Kang    최초 생성
 */
public class LocaleUtil {

    /**
     * LocaleUtil 객체를 생성함
     *
     * @author SeungHyeon.Kang
     */
    private LocaleUtil() {

        throw new UnsupportedOperationException("유틸리티 클래스는 인스턴스를 생성할 수 없습니다.");
    }

    /**
     * 현재 요청 언어 환경 조회함
     *
     * @author SeungHyeon.Kang
     * @return 처리 결과
     */
    public static String getLocale() {
        // getLocale 조회로 후속 처리에 필요한 데이터를 가져옴
        Locale currentLocale = LocaleContextHolder.getLocale();

        // currentLocale 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(currentLocale)) {
            // 현재 요청 언어 환경 조회 결과를 반환함
            return "KO";
        }

        // getLanguage 조회로 후속 처리에 필요한 데이터를 가져옴
        String language = currentLocale.getLanguage();

        // language 값이 비어 있으면 후속 참조를 차단하기 위해 분기함
        if (StringUtil.isEmpty(language) || language.trim().isEmpty()) {
            // 현재 요청 언어 환경 조회 결과를 반환함
            return "KO";
        }

        // 현재 요청 언어 환경 조회 결과를 반환함
        return language.toUpperCase();
    }

    /**
     * 현재 요청 언어가 영문인지 여부를 조회함
     *
     * @author SeungHyeon.Kang
     * @return 영문 요청이면 Y, 그 외에는 N
     */
    public static String getEnglishYsno() {
        // 지원 언어가 영어와 한국어뿐이므로 영어 요청만 명시적으로 구분함
        return getLocale().startsWith("EN") ? Constant.COMM_YES : Constant.COMM_NO;
    }
}
