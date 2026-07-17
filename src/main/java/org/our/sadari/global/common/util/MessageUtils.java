package org.our.sadari.global.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * MessageUtils 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Component
public class MessageUtils {

    private static MessageSource messageSource;

    /**
     * setMessageSource 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param ms 처리에 필요한 입력값
     */
    @Autowired
    public void setMessageSource(MessageSource ms) {
        messageSource = ms;
    }

    /**
     * getMessage 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param key 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String getMessage(String key) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(messageSource)) {
            throw new IllegalStateException("MessageSource not initialized");
        }

        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * getMessage 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param key 처리에 필요한 입력값
     * @param args 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String getMessage(String key, Object... args) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (StringUtil.isEmpty(messageSource)) {
            throw new IllegalStateException("MessageSource not initialized");
        }

        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
