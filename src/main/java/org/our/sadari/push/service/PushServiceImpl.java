package org.our.sadari.push.service;

import java.util.List;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.push.dto.PushDto;
import org.our.sadari.push.mapper.PushMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FCM Web 설정 조회, TB_PSHSUB 구독 저장, 알림 발생 시 FCM 발송을 처리하는 Service 구현체입니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final PushMapper pushMapper;
    private final FirebaseMessagingProvider firebaseMessagingProvider;

    @Value("${firebase.web.api-key:}")
    private String apiKey;

    @Value("${firebase.web.auth-domain:}")
    private String authDomain;

    @Value("${firebase.web.project-id:}")
    private String projectId;

    @Value("${firebase.web.storage-bucket:}")
    private String storageBucket;

    @Value("${firebase.web.messaging-sender-id:}")
    private String messagingSenderId;

    @Value("${firebase.web.app-id:}")
    private String appId;

    @Value("${firebase.web.vapid-public-key:}")
    private String vapidPublicKey;

    /**
     * 브라우저에서 FCM token을 발급받는 데 필요한 공개 설정만 반환합니다.
     * 하나라도 비어 있으면 프론트가 token을 만들 수 없으므로 잘못된 설정으로 응답합니다.
     *
     * @author Seunghyeon.Kang
     * @return Firebase Web 공개 설정
     */
    @Override
    public ResultData getFirebaseWebConfig() {
        List<String> missingConfigList = getMissingFirebaseWebConfigList();

        if (!missingConfigList.isEmpty()) {
            String missingConfigText = String.join(", ", missingConfigList);
            log.warn("Firebase Web Push config is missing. fields={}", missingConfigText);
            return ResultData.fail(ResultEnum.PUSH_CONFIG_MISSING, missingConfigText);
        }

        PushDto.FirebaseWebConfigDto res = new PushDto.FirebaseWebConfigDto();
        res.setApiKey(apiKey);
        res.setAuthDomain(authDomain);
        res.setProjectId(projectId);
        res.setStorageBucket(storageBucket);
        res.setMessagingSenderId(messagingSenderId);
        res.setAppId(appId);
        res.setVapidPublicKey(vapidPublicKey);
        return ResultData.success(res);
    }

    /**
     * 브라우저 FCM token 발급에 반드시 필요한 Firebase Web 설정 누락 항목을 계산합니다.
     * VAPID public key만으로는 token을 만들 수 없고, Firebase Console의 Web app config 값들이 함께 필요합니다.
     *
     * @author Seunghyeon.Kang
     * @return 누락된 설정 property 이름 목록
     */
    private List<String> getMissingFirebaseWebConfigList() {
        List<String> missingConfigList = new ArrayList<>();

        if (StringUtil.isEmpty(apiKey)) {
            missingConfigList.add("firebase.web.api-key");
        }

        if (StringUtil.isEmpty(projectId)) {
            missingConfigList.add("firebase.web.project-id");
        }

        if (StringUtil.isEmpty(messagingSenderId)) {
            missingConfigList.add("firebase.web.messaging-sender-id");
        }

        if (StringUtil.isEmpty(appId)) {
            missingConfigList.add("firebase.web.app-id");
        }

        if (StringUtil.isEmpty(vapidPublicKey)) {
            missingConfigList.add("firebase.web.vapid-public-key");
        }

        return missingConfigList;
    }

    /**
     * 로그인 사용자의 현재 브라우저 FCM token을 저장합니다.
     * TB_PSHSUB는 기존 Web Push 컬럼 구조를 사용하므로 token은 ENDP_URLX에 보관합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req FCM token
     * @return 저장 결과
     */
    @Override
    @Transactional
    public ResultData setPushSub(Long userNumb, PushDto.PushSubDto req) {
        if (StringUtil.isEmpty(userNumb) || req == null || StringUtil.isEmpty(req.getEndpUrlx())) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        req.setUserNumb(userNumb);
        pushMapper.setPushSub(req);
        return ResultData.success();
    }

    /**
     * 로그인 사용자의 현재 브라우저 FCM token을 비활성화합니다.
     * token을 모르면 어떤 브라우저 구독을 끌지 특정할 수 없으므로 잘못된 요청으로 처리합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req FCM token
     * @return 비활성화 결과
     */
    @Override
    @Transactional
    public ResultData delPushSub(Long userNumb, PushDto.PushSubDto req) {
        if (StringUtil.isEmpty(userNumb) || req == null || StringUtil.isEmpty(req.getEndpUrlx())) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        req.setUserNumb(userNumb);
        pushMapper.delPushSub(req);
        return ResultData.success();
    }

    /**
     * 알림 수신자의 활성 token 전체로 FCM 푸시를 발송합니다.
     * 구독이 없거나 Firebase 설정이 누락된 경우에는 알림 저장 기능을 방해하지 않고 발송만 생략합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 알림 수신 사용자 번호
     * @param title 푸시 제목
     * @param body 푸시 내용
     * @param linkUrlx 클릭 이동 링크
     */
    @Override
    public void sendPush(Long userNumb, String title, String body, String linkUrlx) {
        if (StringUtil.isEmpty(userNumb)) {
            return;
        }

        List<PushDto.PushSubDto> pushSubList = pushMapper.getActivePushSubList(userNumb);

        for (PushDto.PushSubDto pushSub : pushSubList) {
            firebaseMessagingProvider.send(pushSub.getEndpUrlx(), title, body, linkUrlx);
        }
    }
}
