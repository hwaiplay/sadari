package org.our.sadari.global.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * fileName       : MessageUtils
 * author         : SeungHyeon.Kang
 * date           : 2026-03-25
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-25        SeungHyeon.Kang       최초 생성
 */
@Component
public class MessageUtils {

    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource ms) {
        messageSource = ms;
    }

    public static String getMessage(String key) {
        if (messageSource == null) {
            throw new IllegalStateException("MessageSource not initialized");
        }
        return messageSource.getMessage(key, null, Locale.KOREA);
    }
}