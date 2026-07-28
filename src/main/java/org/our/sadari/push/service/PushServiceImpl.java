package org.our.sadari.push.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.push.dto.PushDto;
import org.our.sadari.push.mapper.PushMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : PushServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-25
 * description    : 푸시 알림 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-25        SeungHyeon.Kang    최초 생성
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushServiceImpl implements PushService {

    // Push 데이터 접근 객체
    private final PushMapper pushMapper;
    // FirebaseMessaging 외부 연동 제공 객체
    private final FirebaseMessagingProvider firebaseMessagingProvider;
    // Firebase 인증 파일 리소스 조회 객체
    private final ResourceLoader resourceLoader;
    // Object 데이터 접근 객체
    private final ObjectMapper objectMapper;

    // Firebase 웹 API 키
    @Value("${firebase.web.api-key:}")
    private String apiKey;

    // Firebase 웹 인증 도메인
    @Value("${firebase.web.auth-domain:}")
    private String authDomain;

    // Firebase 프로젝트 식별자
    @Value("${firebase.web.project-id:}")
    private String projectId;

    // Firebase Storage 버킷명
    @Value("${firebase.web.storage-bucket:}")
    private String storageBucket;

    // Firebase Cloud Messaging 발신자 식별자
    @Value("${firebase.web.messaging-sender-id:}")
    private String messagingSenderId;

    // Firebase 웹 애플리케이션 식별자
    @Value("${firebase.web.app-id:}")
    private String appId;

    // 웹 푸시 구독에 사용하는 VAPID 공개키
    @Value("${firebase.web.vapid-public-key:}")
    private String vapidPublicKey;

    // Firebase 서비스 계정 인증 파일 경로
    @Value("${firebase.admin.credentials-path:}")
    private String credentialsPath;

    /**
     * 브라우저에서 FCM token을 발급받는 데 필요한 공개 설정만 반환한다.
     * 하나라도 비어 있으면 프론트가 token을 만들 수 없으므로 잘못된 설정으로 응답한다.
     *
     * @author SeungHyeon.Kang
     * @return Firebase Web 공개 설정
     */
    @Override
    public ResultData getFirebaseWebConfig() {

        // 웹 설정 누락값을 Firebase 서비스 계정 정보로 보완한다
        applyFirebaseWebFallbackFromServiceAccount();
        // getMissingFirebaseWebConfigList 조회로 후속 처리에 필요한 데이터를 가져온다
        List<String> missingConfigList = getMissingFirebaseWebConfigList();

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (!missingConfigList.isEmpty()) {

            // 누락된 Firebase 설정 항목을 오류 메시지로 결합한다
            String missingConfigText = String.join(", ", missingConfigList);
            // 복구 가능한 예외 상황을 경고 로그로 남긴다
            log.warn("Firebase Web Push config is missing. fields={}", missingConfigText);
            // "Firebase Web Push \uC124\uC815\uC774 \uB204\uB77D\uB418\uC5C8\uC5B4\uC694.\n\uB204\uB77D\uB41C \uD56D\uBAA9: {0}" 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.PUSH_CONFIG_MISSING, missingConfigText);
        }

        // 프런트에 제공할 Firebase 웹 설정을 담을 객체를 생성한다
        PushDto.FirebaseWebConfigDto res = new PushDto.FirebaseWebConfigDto();
        // ApiKey 업무 값을 res DTO에 설정한다
        res.setApiKey(apiKey);
        // AuthDomain 업무 값을 res DTO에 설정한다
        res.setAuthDomain(authDomain);
        // ProjectId 업무 값을 res DTO에 설정한다
        res.setProjectId(projectId);
        // StorageBucket 업무 값을 res DTO에 설정한다
        res.setStorageBucket(storageBucket);
        // MessagingSenderId 업무 값을 res DTO에 설정한다
        res.setMessagingSenderId(messagingSenderId);
        // AppId 업무 값을 res DTO에 설정한다
        res.setAppId(appId);
        // VapidPublicKey 업무 값을 res DTO에 설정한다
        res.setVapidPublicKey(vapidPublicKey);
        // 브라우저에서 FCM token을 발급받는 데 필요한 공개 설정만 반환한 결과를 성공 응답으로 반환한다
        return ResultData.success(res);
    }

    /**
     * Firebase service account json에서 Web Push 설정 중 보완 가능한 값을 채웁니다.
     * service account는 서버 인증용 파일이라 apiKey, appId, messagingSenderId는 들어 있지 않다.
     * 따라서 여기서는 projectId, authDomain, storageBucket처럼 project_id로 유추 가능한 공개 설정만 fallback 처리한다.
     *
     * @author SeungHyeon.Kang
     */
    private void applyFirebaseWebFallbackFromServiceAccount() {

        // projectId 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (!StringUtil.isEmpty(projectId) || StringUtil.isEmpty(credentialsPath)) {

            // Firebase service account json에서 Web Push 설정 중 보완 가능한 값을 채웁니다 결과를 반환한다
            return;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 설정된 Firebase 인증 리소스를 조회한다
            Resource resource = resourceLoader.getResource(normalizeCredentialsPath(credentialsPath));

            // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
            if (!resource.exists()) {

                // 복구 가능한 예외 상황을 경고 로그로 남긴다
                log.warn("Firebase service account json is not found for web config fallback. path={}", credentialsPath);
                // Firebase service account json에서 Web Push 설정 중 보완 가능한 값을 채웁니다 결과를 반환한다
                return;
            }

            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try (InputStream inputStream = resource.getInputStream()) {

                // Firebase 서비스 계정 JSON을 설정 조회용 트리로 변환한다
                JsonNode serviceAccount = objectMapper.readTree(inputStream);
                // 서비스 계정 JSON에서 필요한 Firebase 설정 항목을 조회한다
                String serviceAccountProjectId = serviceAccount.path("project_id").asText("");

                // serviceAccountProjectId 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
                if (StringUtil.isEmpty(serviceAccountProjectId)) {

                    // Firebase service account json에서 Web Push 설정 중 보완 가능한 값을 채웁니다 결과를 반환한다
                    return;
                }

                /*
                 * project_id는 service account와 Firebase Web app이 같은 Firebase project를 바라보는 경우 동일하게 사용할 수 있다.
                 * 단, apiKey/appId/messagingSenderId는 service account에 없으므로 Firebase Console의 Web app config를 yml에 넣어야 한다.
                 */
                projectId = serviceAccountProjectId;

                // authDomain 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
                if (StringUtil.isEmpty(authDomain)) {

                    authDomain = serviceAccountProjectId + ".firebaseapp.com";
                }

                // storageBucket 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
                if (StringUtil.isEmpty(storageBucket)) {

                    storageBucket = serviceAccountProjectId + ".firebasestorage.app";
                }
            }
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {

            // 복구 가능한 예외 상황을 경고 로그로 남긴다
            log.warn("Firebase service account json could not be used for web config fallback.", e);
        }
    }

    /**
     * Firebase service account json 경로를 Spring ResourceLoader가 읽을 수 있게 보정한다.
     * classpath:가 중복으로 들어온 경우 파일을 못 찾으므로 한 번만 남깁니다.
     *
     * @author SeungHyeon.Kang
     * @param path yml에 등록된 service account json 경로
     * @return 보정된 리소스 경로
     */
    private String normalizeCredentialsPath(String path) {

        String normalizedPath = path;

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        while (normalizedPath.startsWith("classpath:classpath:")) {

            // 정규식과 처음 일치하는 문자열을 치환한다
            normalizedPath = normalizedPath.replaceFirst("classpath:classpath:", "classpath:");
        }
        // Firebase service account json 경로를 Spring ResourceLoader가 읽을 수 있게 보정한 결과를 반환한다
        return normalizedPath;
    }

    /**
     * 브라우저 FCM token 발급에 반드시 필요한 Firebase Web 설정 누락 항목을 계산한다.
     * VAPID public key만으로는 token을 만들 수 없고, Firebase Console의 Web app config 값들이 함께 필요한다.
     * service account json에는 apiKey/appId/messagingSenderId가 없으므로 이 값들은 yml 또는 환경변수에서 반드시 받아야 한다.
     *
     * @author SeungHyeon.Kang
     * @return 누락된 설정 property 이름 목록
     */
    private List<String> getMissingFirebaseWebConfigList() {

        List<String> missingConfigList = new ArrayList<>();

        // apiKey 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(apiKey)) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingConfigList.add("firebase.web.api-key");
        }

        // projectId 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(projectId)) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingConfigList.add("firebase.web.project-id");
        }

        // messagingSenderId 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(messagingSenderId)) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingConfigList.add("firebase.web.messaging-sender-id");
        }

        // appId 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(appId)) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingConfigList.add("firebase.web.app-id");
        }

        // vapidPublicKey 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(vapidPublicKey)) {

            // 처리한 값을 결과 컬렉션에 추가한다
            missingConfigList.add("firebase.web.vapid-public-key");
        }
        // 브라우저 FCM token 발급에 반드시 필요한 Firebase Web 설정 누락 항목을 계산한 결과를 반환한다
        return missingConfigList;
    }

    /**
     * 로그인 사용자의 현재 브라우저 FCM token을 저장한다.
     * TB_PSHSUB는 기존 Web Push 컬럼 구조를 사용하므로 token은 ENDP_URLX에 보관한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req FCM token
     * @return 저장 결과
     */
    @Override
    @Transactional
    public ResultData setPushSub(Long userNumb, PushDto.PushSubDto req) {

        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getEndpUrlx())) {

            // "\uC694\uCCAD\uAC12\uC774 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC544\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // PushSub 업무 값을 pushMapper DTO에 설정한다
        pushMapper.setPushSub(req);
        // 로그인 사용자의 현재 브라우저 FCM token을 저장한 결과를 성공 응답으로 반환한다
        return ResultData.success();
    }

    /**
     * 로그인 사용자의 현재 브라우저 FCM token을 비활성화한다.
     * token을 모르면 어떤 브라우저 구독을 끌지 특정할 수 없으므로 잘못된 요청으로 처리한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req FCM token
     * @return 비활성화 결과
     */
    @Override
    @Transactional
    public ResultData delPushSub(Long userNumb, PushDto.PushSubDto req) {

        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getEndpUrlx())) {

            // "\uC694\uCCAD\uAC12\uC774 \uC62C\uBC14\uB974\uC9C0 \uC54A\uC544\uC694." 실패 응답을 반환한다
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // PushSub 데이터를 DB에서 삭제한다
        pushMapper.delPushSub(req);
        // 로그인 사용자의 현재 브라우저 FCM token을 비활성화한 결과를 성공 응답으로 반환한다
        return ResultData.success();
    }

    /**
     * 알림 수신자의 활성 token 전체로 FCM 푸시를 발송한다.
     * 구독이 없거나 Firebase 설정이 누락된 경우에는 알림 저장 기능을 방해하지 않고 발송만 생략한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 알림 수신 사용자 번호
     * @param title 푸시 제목
     * @param body 푸시 내용
     * @param linkUrlx 클릭 이동 링크
     * @param alimNumb 클릭 시 읽음 처리할 사용자별 알림 번호
     */
    @Override
    public void sendPush(Long userNumb, String title, String body
                       , String linkUrlx, Long alimNumb) {

        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {

            // 알림 수신자의 활성 token 전체로 FCM 푸시를 발송한 결과를 반환한다
            return;
        }

        // ActivePushSubList 데이터를 DB에서 조회한다
        List<PushDto.PushSubDto> pushSubList = pushMapper.getActivePushSubList(userNumb);

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (PushDto.PushSubDto pushSub : pushSubList) {

            // send 호출로 검증된 알림 또는 응답을 전송한다
            firebaseMessagingProvider.send(
                    // getEndpUrlx 조회로 후속 처리에 필요한 데이터를 가져온다
                    pushSub.getEndpUrlx()
                  , title
                  , body
                  , linkUrlx
                  , alimNumb
            );
        }
    }
}
