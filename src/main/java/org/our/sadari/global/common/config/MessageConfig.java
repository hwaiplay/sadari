package org.our.sadari.global.common.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * fileName       : MessageConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-03-22
 * description    : 메시지 소스 설정
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-22        SeungHyeon.Kang    최초 생성
 * 2026-03-24        Hanwon.Jang        메시지 소스 수정
 */
@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages_kr");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ms.setFallbackToSystemLocale(false);
        return ms;
    }
}