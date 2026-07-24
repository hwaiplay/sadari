package org.our.sadari.push.service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * Firebase Admin SDK 초기화와 단건 FCM 발송을 담당합니다.
 * 푸시는 알림함 저장을 보조하는 기능이므로 Firebase 설정이나 런타임 의존성에 문제가 있어도 서버 기동은 막지 않습니다.
 *
 * @author Seunghyeon.Kang
 */
@Slf4j
@Component
public class FirebaseMessagingProvider {

    private static final String DEFAULT_PUSH_TITLE = "알림";
    private static final String DEFAULT_PUSH_LINK = "/alim";
    private static final String CLASSPATH_DUPLICATED_PREFIX = "classpath:classpath:";
    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final String GOOGLE_CREDENTIALS_CLASS_NAME = "com.google.auth.oauth2.GoogleCredentials";
    private static final String FIREBASE_OPTIONS_CLASS_NAME = "com.google.firebase.FirebaseOptions";
    private static final String FIREBASE_APP_CLASS_NAME = "com.google.firebase.FirebaseApp";
    private static final String FIREBASE_MESSAGING_CLASS_NAME = "com.google.firebase.messaging.FirebaseMessaging";
    private static final String FIREBASE_MESSAGE_CLASS_NAME = "com.google.firebase.messaging.Message";
    private static final String FIREBASE_NOTIFICATION_CLASS_NAME = "com.google.firebase.messaging.Notification";

    private final ResourceLoader resourceLoader;
    private final String credentialsPath;

    /*
     * Firebase SDK 타입을 필드/메서드 시그니처에 직접 노출하지 않는다.
     * IntelliJ 실행 classpath가 Gradle 의존성을 늦게 반영하는 경우, Spring bean introspection 단계에서
     * NoClassDefFoundError가 발생할 수 있으므로 실제 SDK 접근은 초기화/발송 시점에 reflection으로만 수행한다.
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
     * credentials 경로 누락, 파일 없음, SDK classpath 누락은 모두 push 비활성화로만 처리합니다.
     *
     * @author Seunghyeon.Kang
     */
    @PostConstruct
    public void init() {
        if (StringUtil.isEmpty(credentialsPath)) {
            // Firebase service account 경로가 없으면 서버 발송 자체가 불가능하므로 push만 비활성화한다.
            log.warn("Firebase credentials path is empty. Push sending is disabled.");
            return;
        }

        try {
            initializeFirebaseMessaging();
        } catch (Throwable e) {
            /*
             * Firebase Admin SDK가 런타임 classpath에 없거나 service account json이 잘못되어도
             * 알림함 저장 기능까지 같이 죽으면 안 된다. 푸시는 부가 기능으로 보고 서버 기동은 유지한다.
             */
            clearFirebaseMessaging();
            log.warn("Firebase initialization failed. Push sending is disabled.", e);
        }
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
        if (StringUtil.isEmpty(token)) {
            // token 없이 호출된 경우는 특정 브라우저 구독을 식별할 수 없어 발송하지 않는다.
            log.debug("FCM push send skipped. token is empty.");
            return false;
        }

        if (!isFirebaseMessagingReady()) {
            // Firebase 초기화 실패 상태에서도 알림 저장은 성공해야 하므로 푸시 발송만 조용히 생략한다.
            log.debug("FCM push send skipped. Firebase messaging is not initialized.");
            return false;
        }

        try {
            Object message = createMessage(token, title, body, linkUrlx);
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

    /**
     * Firebase Admin SDK class를 로딩하고 service account json으로 FirebaseMessaging 객체를 준비합니다.
     * SDK 타입을 직접 import하지 않는 이유는 devtools/IDE 실행 classpath가 늦게 반영될 때 서버 기동이 막히는 문제를 피하기 위해서입니다.
     *
     * @author Seunghyeon.Kang
     */
    private void initializeFirebaseMessaging() throws Exception {
        String resolvedCredentialsPath = normalizeCredentialsPath(credentialsPath);
        log.info("Firebase push provider initialization started. credentialsPath={}", resolvedCredentialsPath);

        Resource resource = resourceLoader.getResource(resolvedCredentialsPath);

        if (!resource.exists()) {
            // json 파일이 없으면 Firebase 인증을 만들 수 없으므로 push만 비활성화한다.
            log.warn("Firebase credentials file does not exist. path={}", resolvedCredentialsPath);
            return;
        }

        FirebaseSdkClasses sdkClasses = loadFirebaseSdkClasses();

        try (InputStream inputStream = resource.getInputStream()) {
            Object options = createFirebaseOptions(sdkClasses, inputStream);
            Object app = getOrInitializeFirebaseApp(sdkClasses, options);

            firebaseMessagingClass = sdkClasses.firebaseMessagingClass();
            messageClass = sdkClasses.messageClass();
            firebaseMessaging = invokeStatic(firebaseMessagingClass, "getInstance", new Class<?>[]{sdkClasses.firebaseAppClass()}, app);
        }

        log.info("Firebase push provider initialized.");
    }

    /**
     * Firebase Admin SDK에서 필요한 class들을 문자열 이름으로 로딩합니다.
     * 이 단계가 실패하면 Gradle 의존성이 실행 classpath에 없다는 뜻이므로 push 기능만 비활성화됩니다.
     *
     * @author Seunghyeon.Kang
     * @return Firebase Admin SDK class 묶음
     */
    private FirebaseSdkClasses loadFirebaseSdkClasses() throws ClassNotFoundException {
        return new FirebaseSdkClasses(
                Class.forName(GOOGLE_CREDENTIALS_CLASS_NAME),
                Class.forName(FIREBASE_OPTIONS_CLASS_NAME),
                Class.forName(FIREBASE_APP_CLASS_NAME),
                Class.forName(FIREBASE_MESSAGING_CLASS_NAME),
                Class.forName(FIREBASE_MESSAGE_CLASS_NAME),
                Class.forName(FIREBASE_NOTIFICATION_CLASS_NAME)
        );
    }

    /**
     * service account json 스트림을 FirebaseOptions 객체로 변환합니다.
     * GoogleCredentials.fromStream -> FirebaseOptions.builder().setCredentials(...).build() 순서를 reflection으로 수행합니다.
     *
     * @author Seunghyeon.Kang
     * @param sdkClasses Firebase Admin SDK class 묶음
     * @param inputStream service account json 입력 스트림
     * @return FirebaseOptions 객체
     */
    private Object createFirebaseOptions(FirebaseSdkClasses sdkClasses, InputStream inputStream) throws Exception {
        Object credentials = invokeStatic(sdkClasses.googleCredentialsClass(), "fromStream", new Class<?>[]{InputStream.class}, inputStream);
        Object optionsBuilder = invokeStatic(sdkClasses.firebaseOptionsClass(), "builder");

        /*
         * Firebase builder는 setter 호출 후 자기 자신을 반환한다.
         * 반환값을 다시 받아 다음 reflection 호출의 target으로 쓰면 SDK 내부 builder 구현이 바뀌어도 체인을 유지할 수 있다.
         */
        Object configuredBuilder = invoke(optionsBuilder, "setCredentials", new Class<?>[]{sdkClasses.googleCredentialsClass()}, credentials);
        return invoke(configuredBuilder, "build");
    }

    /**
     * JVM 안에 이미 FirebaseApp이 있으면 재사용하고, 없으면 새로 초기화합니다.
     * Devtools 재시작이나 테스트 반복 실행 시 FirebaseApp 중복 초기화를 피하기 위한 분기입니다.
     *
     * @author Seunghyeon.Kang
     * @param sdkClasses Firebase Admin SDK class 묶음
     * @param options FirebaseOptions 객체
     * @return FirebaseApp 객체
     */
    private Object getOrInitializeFirebaseApp(FirebaseSdkClasses sdkClasses, Object options) throws Exception {
        List<?> appList = (List<?>) invokeStatic(sdkClasses.firebaseAppClass(), "getApps");

        if (appList.isEmpty()) {
            return invokeStatic(sdkClasses.firebaseAppClass(), "initializeApp", new Class<?>[]{sdkClasses.firebaseOptionsClass()}, options);
        }

        return invokeStatic(sdkClasses.firebaseAppClass(), "getInstance");
    }

    /**
     * Firebase Messaging이 발송할 Message 객체를 생성합니다.
     * 브라우저 foreground/background 처리 양쪽에서 사용할 수 있도록 notification과 data payload를 함께 넣습니다.
     *
     * @author Seunghyeon.Kang
     * @param token FCM registration token
     * @param title 알림 제목
     * @param body 알림 내용
     * @param linkUrlx 알림 클릭 이동 링크
     * @return Firebase Message 객체
     */
    private Object createMessage(String token, String title, String body, String linkUrlx) throws Exception {
        Class<?> notificationClass = Class.forName(FIREBASE_NOTIFICATION_CLASS_NAME);
        Object notification = createNotification(notificationClass, title, body);
        Object messageBuilder = invokeStatic(messageClass, "builder");

        Object tokenBuilder = invoke(messageBuilder, "setToken", new Class<?>[]{String.class}, token);
        Object notificationBuilder = invoke(tokenBuilder, "setNotification", new Class<?>[]{notificationClass}, notification);
        Object titleDataBuilder = invoke(notificationBuilder, "putData", new Class<?>[]{String.class, String.class}, "title", getPushTitle(title));
        Object bodyDataBuilder = invoke(titleDataBuilder, "putData", new Class<?>[]{String.class, String.class}, "body", getPushBody(body));
        Object linkDataBuilder = invoke(bodyDataBuilder, "putData", new Class<?>[]{String.class, String.class}, "linkUrlx", getPushLink(linkUrlx));

        return invoke(linkDataBuilder, "build");
    }

    /**
     * 브라우저 시스템 알림에 표시할 Notification 객체를 생성합니다.
     * title/body가 비어 있어도 Firebase 메시지 생성이 실패하지 않도록 기본값으로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param notificationClass Firebase Notification class
     * @param title 알림 제목
     * @param body 알림 내용
     * @return Firebase Notification 객체
     */
    private Object createNotification(Class<?> notificationClass, String title, String body) throws Exception {
        Object notificationBuilder = invokeStatic(notificationClass, "builder");
        Object titleBuilder = invoke(notificationBuilder, "setTitle", new Class<?>[]{String.class}, getPushTitle(title));
        Object bodyBuilder = invoke(titleBuilder, "setBody", new Class<?>[]{String.class}, getPushBody(body));

        return invoke(bodyBuilder, "build");
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

        while (normalizedPath.startsWith(CLASSPATH_DUPLICATED_PREFIX)) {
            normalizedPath = normalizedPath.replaceFirst(CLASSPATH_DUPLICATED_PREFIX, CLASSPATH_PREFIX);
        }

        return normalizedPath;
    }

    /**
     * Firebase 초기화가 완료되어 실제 발송을 시도할 수 있는지 확인합니다.
     * 하나라도 비어 있으면 초기화 실패 상태로 보고 발송을 생략합니다.
     *
     * @author Seunghyeon.Kang
     * @return Firebase 발송 가능 여부
     */
    private boolean isFirebaseMessagingReady() {
        return firebaseMessaging != null && firebaseMessagingClass != null && messageClass != null;
    }

    /**
     * 초기화 실패 시 중간까지 채워진 Firebase 객체/class 참조를 비웁니다.
     * 일부 필드만 남아 있으면 다음 발송 시 NPE나 잘못된 상태로 이어질 수 있어 명시적으로 정리합니다.
     *
     * @author Seunghyeon.Kang
     */
    private void clearFirebaseMessaging() {
        firebaseMessaging = null;
        firebaseMessagingClass = null;
        messageClass = null;
    }

    /**
     * 비어 있는 푸시 제목을 기본 제목으로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param title 원본 알림 제목
     * @return 보정된 알림 제목
     */
    private String getPushTitle(String title) {
        return StringUtil.isEmpty(title) ? DEFAULT_PUSH_TITLE : title;
    }

    /**
     * 비어 있는 푸시 본문을 빈 문자열로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param body 원본 알림 내용
     * @return 보정된 알림 내용
     */
    private String getPushBody(String body) {
        return StringUtil.isEmpty(body) ? "" : body;
    }

    /**
     * 비어 있는 클릭 링크를 알림 목록 경로로 보정합니다.
     *
     * @author Seunghyeon.Kang
     * @param linkUrlx 원본 클릭 이동 링크
     * @return 보정된 클릭 이동 링크
     */
    private String getPushLink(String linkUrlx) {
        return StringUtil.isEmpty(linkUrlx) ? DEFAULT_PUSH_LINK : linkUrlx;
    }

    /**
     * 파라미터 없는 static 메서드를 reflection으로 호출합니다.
     *
     * @author Seunghyeon.Kang
     * @param targetClass 호출 대상 class
     * @param methodName 호출할 메서드명
     * @return 메서드 호출 결과
     */
    private Object invokeStatic(Class<?> targetClass, String methodName) throws Exception {
        return invokeStatic(targetClass, methodName, new Class<?>[]{});
    }

    /**
     * static 메서드를 reflection으로 호출합니다.
     * Firebase SDK 타입을 직접 참조하지 않기 위한 공통 호출 지점입니다.
     *
     * @author Seunghyeon.Kang
     * @param targetClass 호출 대상 class
     * @param methodName 호출할 메서드명
     * @param parameterTypes 메서드 파라미터 타입
     * @param args 메서드 인자
     * @return 메서드 호출 결과
     */
    private Object invokeStatic(Class<?> targetClass, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = targetClass.getMethod(methodName, parameterTypes);
        return method.invoke(null, args);
    }

    /**
     * 파라미터 없는 인스턴스 메서드를 reflection으로 호출합니다.
     *
     * @author Seunghyeon.Kang
     * @param target 호출 대상 객체
     * @param methodName 호출할 메서드명
     * @return 메서드 호출 결과
     */
    private Object invoke(Object target, String methodName) throws Exception {
        return invoke(target, methodName, new Class<?>[]{});
    }

    /**
     * 인스턴스 메서드를 reflection으로 호출합니다.
     * Firebase builder 체인 호출을 한 곳으로 모아 예외 처리와 호출 방식을 일관되게 유지합니다.
     *
     * @author Seunghyeon.Kang
     * @param target 호출 대상 객체
     * @param methodName 호출할 메서드명
     * @param parameterTypes 메서드 파라미터 타입
     * @param args 메서드 인자
     * @return 메서드 호출 결과
     */
    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        return method.invoke(target, args);
    }

    /**
     * reflection으로 로딩한 Firebase Admin SDK class 묶음입니다.
     * class 참조를 한 객체로 묶어 초기화 단계의 파라미터가 길어지는 것을 막습니다.
     *
     * @author Seunghyeon.Kang
     * @param googleCredentialsClass GoogleCredentials class
     * @param firebaseOptionsClass FirebaseOptions class
     * @param firebaseAppClass FirebaseApp class
     * @param firebaseMessagingClass FirebaseMessaging class
     * @param messageClass Message class
     * @param notificationClass Notification class
     */
    private record FirebaseSdkClasses(
            Class<?> googleCredentialsClass,
            Class<?> firebaseOptionsClass,
            Class<?> firebaseAppClass,
            Class<?> firebaseMessagingClass,
            Class<?> messageClass,
            Class<?> notificationClass
    ) {
    }
}
