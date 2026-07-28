package org.our.sadari.global.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * fileName       : MessageUtils
 * author         : SeungHyeon.Kang
 * date           : 2026-03-25
 * description    : 공통 처리에 필요한 변환과 판정 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-25        SeungHyeon.Kang    최초 생성
 */
@Component
public class MessageUtils {
    // 다국어 메시지 조회 객체
    private static MessageSource messageSource;

    /**
     * 공통 메시지 소스 설정한다.
     *
     * @author SeungHyeon.Kang
     * @param ms 설정할 MessageSource
     */
    @Autowired
    public void setMessageSource(MessageSource ms) {

        messageSource = ms;
    }

    /**
     * 메시지 키와 치환값 기준 공통 문구 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param key 조회할 메시지 프로퍼티 키
     * @return 처리 결과
     */
    public static String getMessage(String key) {
        // messageSource 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (StringUtil.isEmpty(messageSource)) {

            throw new IllegalStateException("MessageSource not initialized");
        }

        // 메시지 키와 치환값 기준 공통 문구 조회 결과를 반환한다
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * 메시지 키와 치환값 기준 공통 문구 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param key 조회할 메시지 프로퍼티 키
     * @param args 메시지 치환에 사용할 인자 목록
     * @return 처리 결과
     */
    public static String getMessage(String key, Object... args) {
        // messageSource 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (StringUtil.isEmpty(messageSource)) {

            throw new IllegalStateException("MessageSource not initialized");
        }

        // 메시지 키와 치환값 기준 공통 문구 조회 결과를 반환한다
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
