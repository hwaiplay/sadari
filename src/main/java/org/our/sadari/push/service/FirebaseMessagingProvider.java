package org.our.sadari.push.service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Firebase Admin SDK 초기화와 단건 FCM 발송을 담당합니다.
 * 푸시는 부가 기능이므로 service account 설정이 누락되어도 애플리케이션 기동을 막지 않고 발송만 생략합니다.
 *
 * @author Seunghyeon.Kang
 */
@Slf4j
@Component
public class FirebaseMessagingProvider {

    private final ResourceLoader resourceLoader;
    private final String credentialsPath;
    /*
     * Firebase SDK 타입을 필드/메서드 시그니처에 직접 노출하지 않는다.
     * IntelliJ 실행 classpath가 Gradle 의존성을 늦게 반영하는 경우 Spring의 bean introspection 단계에서
     * NoClassDefFoundError가 발생할 수 있으므로, 실제 SDK 접근은 초기화/발송 시점에 reflection으로만 수행한다.
     */
    private Object firebaseMessaging;
    private Class<?> firebaseMessagingClass;
    private Class<?> messageClass;

    public FirebaseMessagingProvider(
            ResourceLoader resourceLoader,
            @Value("${firebase.admin.credentials-path:}") String credentialsPath
    ) {
        this.resourceLoader = resourceLoader;
        this.credentialsPath = credentialsPath;
    }

    /**
     * 서버 시작 시 Firebase Admin SDK를 초기화합니다.
     * 설정 파일이 없으면 push 기능만 no-op 처리되도록 firebaseMessaging을 null로 유지합니다.
     *
     * @author Seunghyeon.Kang
     */
    @PostConstruct
    public void init() {
        if (StringUtil.isEmpty(credentialsPath)) {
            log.warn("Firebase credentials path is empty. Push sending is disabled.");
            return;
        }

        try {
            String resolvedCredentialsPath = normalizeCredentialsPath(credentialsPath);
            log.info("Firebase push provider initialization started. credentialsPath={}", resolvedCredentialsPath);
            Resource resource = resourceLoader.getResource(resolvedCredentialsPath);

            if (!resource.exists()) {
                log.warn("Firebase credentials file does not exist. path={}", resolvedCredentialsPath);
                return;
            }

            try (InputStream inputStream = resource.getInputStream()) {
                Class<?> googleCredentialsClass = Class.forName("com.google.auth.oauth2.GoogleCredentials");
                Class<?> firebaseOptionsClass = Class.forName("com.google.firebase.FirebaseOptions");
                Class<?> firebaseAppClass = Class.forName("com.google.firebase.FirebaseApp");
                firebaseMessagingClass = Class.forName("com.google.firebase.messaging.FirebaseMessaging");
                messageClass = Class.forName("com.google.firebase.messaging.Message");

                Object credentials = googleCredentialsClass
                        .getMethod("fromStream", InputStream.class)
                        .invoke(null, inputStream);
                Object optionsBuilder = firebaseOptionsClass.getMethod("builder").invoke(null);
                Object options = optionsBuilder.getClass()
                        .getMethod("setCredentials", googleCredentialsClass)
                        .invoke(optionsBuilder, credentials)
                        .getClass()
                        .getMethod("build")
                        .invoke(optionsBuilder);

                List<?> appList = (List<?>) firebaseAppClass.getMethod("getApps").invoke(null);
                Object app = appList.isEmpty()
                        ? firebaseAppClass.getMethod("initializeApp", firebaseOptionsClass).invoke(null, options)
                        : firebaseAppClass.getMethod("getInstance").invoke(null);

                firebaseMessaging = firebaseMessagingClass
                        .getMethod("getInstance", firebaseAppClass)
                        .invoke(null, app);
                log.info("Firebase push provider initialized.");
            }
        } catch (Throwable e) {
            /*
             * Firebase Admin SDK가 런타임 classpath에 없거나 service account json이 잘못되어도
             * 알림함 저장 기능까지 같이 죽으면 안 된다. 푸시는 부가 기능으로 보고 서버 기동은 유지한다.
             */
            log.warn("Firebase initialization failed. Push sending is disabled.", e);
        }
    }

    /**
     * Firebase service account json 경로를 Spring ResourceLoader가 해석할 수 있는 값으로 보정합니다.
     * yml에는 classpath: 접두사를 한 번만 쓰는 것이 정상입니다.
     * 다만 실행 환경변수에 classpath:가 이미 들어간 값을 다시 조합하면 classpath:classpath:... 형태가 될 수 있어
     * 서버 기동 시 파일을 못 찾지 않도록 여기서 한 번만 남겨 정리합니다.
     *
     * @author Seunghyeon.Kang
     * @param path yml 또는 환경변수에서 읽은 Firebase service account json 경로
     * @return ResourceLoader에 전달할 정리된 경로
     */
    private String normalizeCredentialsPath(String path) {
        String normalizedPath = path;

        while (normalizedPath.startsWith("classpath:classpath:")) {
            normalizedPath = normalizedPath.replaceFirst("classpath:classpath:", "classpath:");
        }

        return normalizedPath;
    }

    /**
     * FCM registration token으로 푸시 메시지를 발송합니다.
     * Firebase가 초기화되지 않았거나 token이 없으면 실패가 아니라 발송 생략으로 처리합니다.
     *
     * @author Seunghyeon.Kang
     * @param token FCM registration token
     * @param title 알림 제목
     * @param body 알림 내용
     * @param linkUrlx 알림 클릭 이동 링크
     * @return 실제 발송 성공 여부
     */
    public boolean send(String token, String title, String body, String linkUrlx) {
        if (firebaseMessaging == null || StringUtil.isEmpty(token)) {
            return false;
        }

        try {
            Class<?> notificationClass = Class.forName("com.google.firebase.messaging.Notification");
            Object notificationBuilder = notificationClass.getMethod("builder").invoke(null);
            Object notification = notificationBuilder.getClass()
                    .getMethod("setTitle", String.class)
                    .invoke(notificationBuilder, StringUtil.isEmpty(title) ? "알림" : title)
                    .getClass()
                    .getMethod("setBody", String.class)
                    .invoke(notificationBuilder, StringUtil.isEmpty(body) ? "" : body)
                    .getClass()
                    .getMethod("build")
                    .invoke(notificationBuilder);

            Object messageBuilder = messageClass.getMethod("builder").invoke(null);
            Object message = messageBuilder.getClass()
                    .getMethod("setToken", String.class)
                    .invoke(messageBuilder, token)
                    .getClass()
                    .getMethod("setNotification", notificationClass)
                    .invoke(messageBuilder, notification)
                    .getClass()
                    .getMethod("putData", String.class, String.class)
                    .invoke(messageBuilder, "title", StringUtil.isEmpty(title) ? "알림" : title)
                    .getClass()
                    .getMethod("putData", String.class, String.class)
                    .invoke(messageBuilder, "body", StringUtil.isEmpty(body) ? "" : body)
                    .getClass()
                    .getMethod("putData", String.class, String.class)
                    .invoke(messageBuilder, "linkUrlx", StringUtil.isEmpty(linkUrlx) ? "/alim" : linkUrlx)
                    .getClass()
                    .getMethod("build")
                    .invoke(messageBuilder);

            firebaseMessagingClass.getMethod("send", messageClass).invoke(firebaseMessaging, message);
            return true;
        } catch (Throwable e) {
            /*
             * 토큰 만료, Firebase 장애, 런타임 의존성 미반영 같은 문제는 개별 푸시 발송 실패로만 처리한다.
             * 알림 insert 트랜잭션의 성공 여부와 푸시 전송 성공 여부를 강하게 묶지 않기 위한 분기다.
             */
            log.warn("FCM push send failed.", e);
            return false;
        }

    }
}
