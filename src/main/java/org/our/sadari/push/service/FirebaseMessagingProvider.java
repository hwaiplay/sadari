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
 * fileName       : FirebaseMessagingProvider
 * author         : SeungHyeon.Kang
 * date           : 2026-07-25
 * description    : 푸시 알림 외부 연동 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-25        SeungHyeon.Kang    최초 생성
 */
@Slf4j
@Component
public class FirebaseMessagingProvider {

    // 기본 PUSH TITLE 설정값
    private static final String DEFAULT_PUSH_TITLE = "알림";
    // 기본 PUSH LINK 설정값
    private static final String DEFAULT_PUSH_LINK = "/alim";
    // CLASSPATH DUPLICATED 접두사 설정값
    private static final String CLASSPATH_DUPLICATED_PREFIX = "classpath:classpath:";
    // CLASSPATH 접두사 설정값
    private static final String CLASSPATH_PREFIX = "classpath:";

    // GOOGLE CREDENTIALS CLASS 명칭 설정값
    private static final String GOOGLE_CREDENTIALS_CLASS_NAME = "com.google.auth.oauth2.GoogleCredentials";
    // FIREBASE OPTIONS CLASS 명칭 설정값
    private static final String FIREBASE_OPTIONS_CLASS_NAME = "com.google.firebase.FirebaseOptions";
    // FIREBASE APP CLASS 명칭 설정값
    private static final String FIREBASE_APP_CLASS_NAME = "com.google.firebase.FirebaseApp";
    // FIREBASE MESSAGING CLASS 명칭 설정값
    private static final String FIREBASE_MESSAGING_CLASS_NAME = "com.google.firebase.messaging.FirebaseMessaging";
    // FIREBASE 메시지 CLASS 명칭 설정값
    private static final String FIREBASE_MESSAGE_CLASS_NAME = "com.google.firebase.messaging.Message";
    // FIREBASE NOTIFICATION CLASS 명칭 설정값
    private static final String FIREBASE_NOTIFICATION_CLASS_NAME = "com.google.firebase.messaging.Notification";

    // Firebase 인증 파일 리소스 조회 객체
    private final ResourceLoader resourceLoader;
    // Firebase 서비스 계정 인증 파일 경로
    private final String credentialsPath;

    /*
     * Firebase SDK 타입을 필드/메서드 시그니처에 직접 노출하지 않는다.
     * IntelliJ 실행 classpath가 Gradle 의존성을 늦게 반영하는 경우, Spring bean introspection 단계에서
     * NoClassDefFoundError가 발생할 수 있으므로 실제 SDK 접근은 초기화/발송 시점에 reflection으로만 수행한다.
     */
    // 초기화된 Firebase 메시징 인스턴스
    private Object firebaseMessaging;
    // Firebase 메시징 런타임 클래스
    private Class<?> firebaseMessagingClass;
    // Firebase 메시지 런타임 클래스
    private Class<?> messageClass;

    public FirebaseMessagingProvider(ResourceLoader resourceLoader, @Value("${firebase.admin.credentials-path:}") String credentialsPath) {

        this.resourceLoader = resourceLoader;
        this.credentialsPath = credentialsPath;
    }

    /**
     * 서버 시작 시 Firebase Admin SDK를 초기화한다.
     * credentials 경로 누락, 파일 없음, SDK classpath 누락은 모두 push 비활성화로만 처리한다.
     *
     * @author SeungHyeon.Kang
     */
    @PostConstruct
    public void init() {
        // credentialsPath 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(credentialsPath)) {
            // Firebase service account 경로가 없으면 서버 발송 자체가 불가능하므로 push만 비활성화한다.
            log.warn("Firebase credentials path is empty. Push sending is disabled.");
            // 서버 시작 시 Firebase Admin SDK를 초기화한 결과를 반환한다
            return;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // Firebase 인증정보로 메시징 클라이언트를 초기화한다
            initFirebaseMessaging();
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Throwable e) {
            /*
             * Firebase Admin SDK가 런타임 classpath에 없거나 service account json이 잘못되어도
             * 알림함 저장 기능까지 같이 죽으면 안 된다. 푸시는 부가 기능으로 보고 서버 기동은 유지한다.
             */
            clearFirebaseMessaging();
            // 복구 가능한 예외 상황을 경고 로그로 남긴다
            log.warn("Firebase initialization failed. Push sending is disabled.", e);
        }
    }

    /**
     * FCM registration token으로 푸시 메시지를 발송한다.
     * Firebase가 초기화되지 않았거나 token이 없으면 실패가 아니라 발송 생략으로 처리한다.
     *
     * @author SeungHyeon.Kang
     * @param token FCM registration token
     * @param title 알림 제목
     * @param body 알림 내용
     * @param linkUrlx 알림 클릭 이동 링크
     * @param alimNumb 클릭 시 읽음 처리할 사용자별 알림 번호
     * @return 실제 발송 성공 여부
     */
    public boolean send(String token, String title, String body
                      , String linkUrlx, Long alimNumb) {
        // token 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(token)) {
            // token 없이 호출된 경우는 특정 브라우저 구독을 식별할 수 없어 발송하지 않는다.
            log.debug("FCM push send skipped. token is empty.");
            // FCM registration token으로 푸시 메시지를 발송한다 판정값을 반환한다
            return false;
        }

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (!isFirebaseMessagingReady()) {
            // Firebase 초기화 실패 상태에서도 알림 저장은 성공해야 하므로 푸시 발송만 조용히 생략한다.
            log.debug("FCM push send skipped. Firebase messaging is not initialized.");
            // FCM registration token으로 푸시 메시지를 발송한다 판정값을 반환한다
            return false;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // createMessage 호출로 후속 처리에 필요한 객체를 생성한다
            Object message = createMessage(token, title, body, linkUrlx, alimNumb);
            // 호출할 외부 라이브러리 메서드를 조회한다
            firebaseMessagingClass.getMethod("send", messageClass).invoke(firebaseMessaging, message);
            // FCM registration token으로 푸시 메시지를 발송한다 판정값을 반환한다
            return true;
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Throwable e) {
            /*
             * 토큰 만료, Firebase 장애, 런타임 의존성 미반영 같은 문제는 개별 푸시 발송 실패로만 처리한다.
             * 알림 insert 트랜잭션의 성공 여부와 푸시 전송 성공 여부를 강하게 묶지 않기 위한 분기다.
             */
            log.warn("FCM push send failed.", e);
            // FCM registration token으로 푸시 메시지를 발송한다 판정값을 반환한다
            return false;
        }
    }

    /**
     * Firebase Admin SDK class를 로딩하고 service account json으로 FirebaseMessaging 객체를 준비한다.
     * SDK 타입을 직접 import하지 않는 이유는 devtools/IDE 실행 classpath가 늦게 반영될 때 서버 기동이 막히는 문제를 피하기 위해서이다.
     *
     * @author SeungHyeon.Kang
     */
    private void initFirebaseMessaging() throws Exception {
        // Firebase 인증 파일 경로의 classpath 접두사를 정규화한다
        String resolvedCredentialsPath = normalizeCredentialsPath(credentialsPath);
        // 처리 상태를 정보 로그로 남긴다
        log.info("Firebase push provider initialization started. credentialsPath={}", resolvedCredentialsPath);

        // 설정된 Firebase 인증 리소스를 조회한다
        Resource resource = resourceLoader.getResource(resolvedCredentialsPath);

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (!resource.exists()) {
            // json 파일이 없으면 Firebase 인증을 만들 수 없으므로 push만 비활성화한다.
            log.warn("Firebase credentials file does not exist. path={}", resolvedCredentialsPath);
            // Firebase Admin SDK class를 로딩하고 service account json으로 FirebaseMessaging 객체를 준비한 결과를 반환한다
            return;
        }

        // loadFirebaseSdkClasses 호출로 처리에 사용할 기준 데이터를 적재한다
        FirebaseSdkClasses sdkClasses = loadFirebaseSdkClasses();

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try (InputStream inputStream = resource.getInputStream()) {
            // createFirebaseOptions 호출로 후속 처리에 필요한 객체를 생성한다
            Object options = createFirebaseOptions(sdkClasses, inputStream);
            // getFirebaseApp 조회로 후속 처리에 필요한 데이터를 가져온다
            Object app = getFirebaseApp(sdkClasses, options);

            // Firebase 메시징 클래스의 런타임 타입을 확인한다
            firebaseMessagingClass = sdkClasses.firebaseMessagingClass();
            // Firebase 메시지 클래스의 런타임 타입을 확인한다
            messageClass = sdkClasses.messageClass();
            // Firebase 애플리케이션 클래스의 런타임 타입을 확인한다
            firebaseMessaging = invokeStatic(firebaseMessagingClass, "getInstance", new Class<?>[]{sdkClasses.firebaseAppClass()}, app);
        }

        // 처리 상태를 정보 로그로 남긴다
        log.info("Firebase push provider initialized.");
    }

    /**
     * Firebase Admin SDK에서 필요한 class들을 문자열 이름으로 로딩한다.
     * 이 단계가 실패하면 Gradle 의존성이 실행 classpath에 없다는 뜻이므로 push 기능만 비활성화된다.
     *
     * @author SeungHyeon.Kang
     * @return Firebase Admin SDK class 묶음
     */
    private FirebaseSdkClasses loadFirebaseSdkClasses() throws ClassNotFoundException {
        // 런타임에 조회한 Firebase Admin SDK 클래스를 묶은 객체를 반환한다
        return new FirebaseSdkClasses(Class.forName(GOOGLE_CREDENTIALS_CLASS_NAME), Class.forName(FIREBASE_OPTIONS_CLASS_NAME), Class.forName(FIREBASE_APP_CLASS_NAME), Class.forName(FIREBASE_MESSAGING_CLASS_NAME), Class.forName(FIREBASE_MESSAGE_CLASS_NAME), Class.forName(FIREBASE_NOTIFICATION_CLASS_NAME));
    }

    /**
     * service account json 스트림을 FirebaseOptions 객체로 변환한다.
     * GoogleCredentials.fromStream -> FirebaseOptions.builder().setCredentials(...).build() 순서를 reflection으로 수행한다.
     *
     * @author SeungHyeon.Kang
     * @param sdkClasses Firebase Admin SDK class 묶음
     * @param inputStream service account json 입력 스트림
     * @return FirebaseOptions 객체
     */
    private Object createFirebaseOptions(FirebaseSdkClasses sdkClasses, InputStream inputStream) throws Exception {
        // Google 인증정보 클래스의 런타임 타입을 확인한다
        Object credentials = invokeStatic(sdkClasses.googleCredentialsClass(), "fromStream", new Class<?>[]{InputStream.class}, inputStream);
        // Firebase 옵션 클래스의 런타임 타입을 확인한다
        Object optionsBuilder = invokeStatic(sdkClasses.firebaseOptionsClass(), "builder");

        /*
         * Firebase builder는 setter 호출 후 자기 자신을 반환한다.
         * 반환값을 다시 받아 다음 reflection 호출의 target으로 쓰면 SDK 내부 builder 구현이 바뀌어도 체인을 유지할 수 있다.
         */
        Object configuredBuilder = invoke(optionsBuilder, "setCredentials", new Class<?>[]{sdkClasses.googleCredentialsClass()}, credentials);
        // service account json 스트림을 FirebaseOptions 객체로 변환한 결과를 반환한다
        return invoke(configuredBuilder, "build");
    }

    /**
     * JVM 안에 이미 FirebaseApp이 있으면 재사용하고, 없으면 새로 초기화한다.
     * Devtools 재시작이나 테스트 반복 실행 시 FirebaseApp 중복 초기화를 피하기 위한 분기이다.
     *
     * @author SeungHyeon.Kang
     * @param sdkClasses Firebase Admin SDK class 묶음
     * @param options FirebaseOptions 객체
     * @return FirebaseApp 객체
     */
    private Object getFirebaseApp(FirebaseSdkClasses sdkClasses, Object options) throws Exception {
        // Firebase 애플리케이션 클래스의 런타임 타입을 확인한다
        List<?> appList = (List<?>) invokeStatic(sdkClasses.firebaseAppClass(), "getApps");

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (appList.isEmpty()) {
            // JVM 안에 이미 FirebaseApp이 있으면 재사용하고, 없으면 새로 초기화한 결과를 반환한다
            return invokeStatic(sdkClasses.firebaseAppClass(), "initializeApp", new Class<?>[]{sdkClasses.firebaseOptionsClass()}, options);
        }

        // JVM 안에 이미 FirebaseApp이 있으면 재사용하고, 없으면 새로 초기화한 결과를 반환한다
        return invokeStatic(sdkClasses.firebaseAppClass(), "getInstance");
    }

    /**
     * Firebase Messaging이 발송할 Message 객체를 생성한다.
     * 브라우저 foreground/background 처리 양쪽에서 사용할 수 있도록 notification과 data payload를 함께 넣다.
     *
     * @author SeungHyeon.Kang
     * @param token FCM registration token
     * @param title 알림 제목
     * @param body 알림 내용
     * @param linkUrlx 알림 클릭 이동 링크
     * @param alimNumb 클릭 시 읽음 처리할 사용자별 알림 번호
     * @return Firebase Message 객체
     */
    private Object createMessage(String token, String title, String body
                               , String linkUrlx, Long alimNumb) throws Exception {
        // 런타임에 사용할 Firebase 클래스를 이름으로 조회한다
        Class<?> notificationClass = Class.forName(FIREBASE_NOTIFICATION_CLASS_NAME);
        // createNotification 호출로 후속 처리에 필요한 객체를 생성한다
        Object notification = createNotification(notificationClass, title, body);
        // 정적 팩토리 메서드를 호출해 외부 객체를 생성한다
        Object messageBuilder = invokeStatic(messageClass, "builder");

        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object tokenBuilder = invoke(messageBuilder, "setToken", new Class<?>[]{String.class}, token);
        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object notificationBuilder = invoke(tokenBuilder, "setNotification", new Class<?>[]{notificationClass}, notification);
        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object titleDataBuilder = invoke(notificationBuilder, "putData", new Class<?>[]{String.class, String.class}, "title", getPushTitle(title));
        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object bodyDataBuilder = invoke(titleDataBuilder, "putData", new Class<?>[]{String.class, String.class}, "body", getPushBody(body));
        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object linkDataBuilder = invoke(bodyDataBuilder, "putData", new Class<?>[]{String.class, String.class}, "linkUrlx", getPushLink(linkUrlx));
        Object messageDataBuilder = linkDataBuilder;

        // 알림 번호가 있어야 서비스워커가 사용자가 클릭한 정확한 알림 한 건을 읽음 처리할 수 있다.
        if (!StringUtil.isEmpty(alimNumb)) {
            // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
            messageDataBuilder = invoke(
                    linkDataBuilder
                  , "putData"
                  , new Class<?>[]{String.class, String.class}
                  , "alimNumb"
                  , String.valueOf(alimNumb)
            );
        }

        // Firebase Messaging이 발송할 Message 객체를 생성한 결과를 반환한다
        return invoke(messageDataBuilder, "build");
    }

    /**
     * 브라우저 시스템 알림에 표시할 Notification 객체를 생성한다.
     * title/body가 비어 있어도 Firebase 메시지 생성이 실패하지 않도록 기본값으로 보정한다.
     *
     * @author SeungHyeon.Kang
     * @param notificationClass Firebase Notification class
     * @param title 알림 제목
     * @param body 알림 내용
     * @return Firebase Notification 객체
     */
    private Object createNotification(Class<?> notificationClass, String title, String body) throws Exception {
        // 정적 팩토리 메서드를 호출해 외부 객체를 생성한다
        Object notificationBuilder = invokeStatic(notificationClass, "builder");
        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object titleBuilder = invoke(notificationBuilder, "setTitle", new Class<?>[]{String.class}, getPushTitle(title));
        // 리플렉션으로 확인한 외부 라이브러리 메서드를 실행한다
        Object bodyBuilder = invoke(titleBuilder, "setBody", new Class<?>[]{String.class}, getPushBody(body));
        // 브라우저 시스템 알림에 표시할 Notification 객체를 생성한 결과를 반환한다
        return invoke(bodyBuilder, "build");
    }

    /**
     * Firebase service account json 경로를 Spring ResourceLoader가 해석할 수 있는 값으로 보정한다.
     * yml에는 classpath: 접두사를 한 번만 쓰는 것이 정상이다.
     * 다만 실행 환경변수에 classpath:가 이미 들어간 값을 다시 조합하면 classpath:classpath:... 형태가 될 수 있어
     * 서버 기동 시 파일을 못 찾지 않도록 여기서 한 번만 남겨 정리한다.
     *
     * @author SeungHyeon.Kang
     * @param path yml 또는 환경변수에서 읽은 Firebase service account json 경로
     * @return ResourceLoader에 전달할 정리된 경로
     */
    private String normalizeCredentialsPath(String path) {

        String normalizedPath = path;

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        while (normalizedPath.startsWith(CLASSPATH_DUPLICATED_PREFIX)) {
            // 정규식과 처음 일치하는 문자열을 치환한다
            normalizedPath = normalizedPath.replaceFirst(CLASSPATH_DUPLICATED_PREFIX, CLASSPATH_PREFIX);
        }

        // Firebase service account json 경로를 Spring ResourceLoader가 해석할 수 있는 값으로 보정한 결과를 반환한다
        return normalizedPath;
    }

    /**
     * Firebase 초기화가 완료되어 실제 발송을 시도할 수 있는지 확인한다.
     * 하나라도 비어 있으면 초기화 실패 상태로 보고 발송을 생략한다.
     *
     * @author SeungHyeon.Kang
     * @return Firebase 발송 가능 여부
     */
    private boolean isFirebaseMessagingReady() {
        // Firebase 초기화가 완료되어 실제 발송을 시도할 수 있는지 확인한 결과를 반환한다
        return !StringUtil.hasEmpty(firebaseMessaging, firebaseMessagingClass, messageClass);
    }

    /**
     * 초기화 실패 시 중간까지 채워진 Firebase 객체/class 참조를 비웁니다.
     * 일부 필드만 남아 있으면 다음 발송 시 NPE나 잘못된 상태로 이어질 수 있어 명시적으로 정리한다.
     *
     * @author SeungHyeon.Kang
     */
    private void clearFirebaseMessaging() {

        firebaseMessaging = null;
        firebaseMessagingClass = null;
        messageClass = null;
    }

    /**
     * 비어 있는 푸시 제목을 기본 제목으로 보정한다.
     *
     * @author SeungHyeon.Kang
     * @param title 원본 알림 제목
     * @return 보정된 알림 제목
     */
    private String getPushTitle(String title) {
        // 비어 있는 푸시 제목을 기본 제목으로 보정한 결과를 반환한다
        return StringUtil.isEmpty(title) ? DEFAULT_PUSH_TITLE : title;
    }

    /**
     * 비어 있는 푸시 본문을 빈 문자열로 보정한다.
     *
     * @author SeungHyeon.Kang
     * @param body 원본 알림 내용
     * @return 보정된 알림 내용
     */
    private String getPushBody(String body) {
        // 비어 있는 푸시 본문을 빈 문자열로 보정한 결과를 반환한다
        return StringUtil.isEmpty(body) ? "" : body;
    }

    /**
     * 비어 있는 클릭 링크를 알림 목록 경로로 보정한다.
     *
     * @author SeungHyeon.Kang
     * @param linkUrlx 원본 클릭 이동 링크
     * @return 보정된 클릭 이동 링크
     */
    private String getPushLink(String linkUrlx) {
        // 비어 있는 클릭 링크를 알림 목록 경로로 보정한 결과를 반환한다
        return StringUtil.isEmpty(linkUrlx) ? DEFAULT_PUSH_LINK : linkUrlx;
    }

    /**
     * 파라미터 없는 static 메서드를 reflection으로 호출한다.
     *
     * @author SeungHyeon.Kang
     * @param targetClass 호출 대상 class
     * @param methodName 호출할 메서드명
     * @return 메서드 호출 결과
     */
    private Object invokeStatic(Class<?> targetClass, String methodName) throws Exception {
        // 파라미터 없는 static 메서드를 reflection으로 호출한 결과를 반환한다
        return invokeStatic(targetClass, methodName, new Class<?>[]{});
    }

    /**
     * static 메서드를 reflection으로 호출한다.
     * Firebase SDK 타입을 직접 참조하지 않기 위한 공통 호출 지점이다.
     *
     * @author SeungHyeon.Kang
     * @param targetClass 호출 대상 class
     * @param methodName 호출할 메서드명
     * @param parameterTypes 메서드 파라미터 타입
     * @param args 메서드 인자
     * @return 메서드 호출 결과
     */
    private Object invokeStatic(Class<?> targetClass, String methodName, Class<?>[] parameterTypes
                              , Object... args) throws Exception {
        // 호출할 외부 라이브러리 메서드를 조회한다
        Method method = targetClass.getMethod(methodName, parameterTypes);
        // static 메서드를 reflection으로 호출한 결과를 반환한다
        return method.invoke(null, args);
    }

    /**
     * 파라미터 없는 인스턴스 메서드를 reflection으로 호출한다.
     *
     * @author SeungHyeon.Kang
     * @param target 호출 대상 객체
     * @param methodName 호출할 메서드명
     * @return 메서드 호출 결과
     */
    private Object invoke(Object target, String methodName) throws Exception {
        // 파라미터 없는 인스턴스 메서드를 reflection으로 호출한 결과를 반환한다
        return invoke(target, methodName, new Class<?>[]{});
    }

    /**
     * 인스턴스 메서드를 reflection으로 호출한다.
     * Firebase builder 체인 호출을 한 곳으로 모아 예외 처리와 호출 방식을 일관되게 유지한다.
     *
     * @author SeungHyeon.Kang
     * @param target 호출 대상 객체
     * @param methodName 호출할 메서드명
     * @param parameterTypes 메서드 파라미터 타입
     * @param args 메서드 인자
     * @return 메서드 호출 결과
     */
    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes
                        , Object... args) throws Exception {
        // getClass 조회로 후속 처리에 필요한 데이터를 가져온다
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        // 인스턴스 메서드를 reflection으로 호출한 결과를 반환한다
        return method.invoke(target, args);
    }

    /**
     * reflection으로 로딩한 Firebase Admin SDK class 묶음이다.
     * class 참조를 한 객체로 묶어 초기화 단계의 파라미터가 길어지는 것을 막다.
     *
     * @author SeungHyeon.Kang
     * @param googleCredentialsClass GoogleCredentials class
     * @param firebaseOptionsClass FirebaseOptions class
     * @param firebaseAppClass FirebaseApp class
     * @param firebaseMessagingClass FirebaseMessaging class
     * @param messageClass Message class
     * @param notificationClass Notification class
     */
    private record FirebaseSdkClasses(Class<?> googleCredentialsClass, Class<?> firebaseOptionsClass, Class<?> firebaseAppClass
                                    , Class<?> firebaseMessagingClass, Class<?> messageClass, Class<?> notificationClass) {

    }
}
