package org.our.sadari.global.common.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * fileName       : UserIdEncryptionService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 공통 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 */
@Service
public class UserIdEncryptionService {

    // 암호화 접두사 설정값
    private static final String ENCRYPTED_PREFIX = "ENC:";
    // 키 알고리즘 설정값
    private static final String KEY_ALGORITHM = "AES";
    // 암호화 도구 변환 방식 설정값
    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // 사용자 식별자 암호화 비밀키
    private final SecretKeySpec secretKeySpec;

    /**
     * 암호화 키는 별도 환경변수 app.crypto.user-id-key가 있으면 그것을 우선 사용하고, 없으면 JWT secret을 재사용한다.
     * 운영에서 JWT secret을 교체하면 기존 USER_IDXX 조회가 불가능하므로 실제 배포 환경에서는 별도 고정 키를 두는 것이 맞다.
     *
     * @author SeungHyeon.Kang
     * @param userIdEncryptionKey USER_IDXX 암호화 전용 키
     */
    public UserIdEncryptionService(@Value("${app.crypto.user-id-key:${jwt.secret_key}}") String userIdEncryptionKey) {
        // 사용자 식별자 암호화에 사용할 비밀키를 담을 객체를 생성한다
        this.secretKeySpec = new SecretKeySpec(createAesKey(userIdEncryptionKey), KEY_ALGORITHM);
    }

    /**
     * 로그인 조회와 신규 회원 저장에 사용할 USER_IDXX 암호문을 생성한다.
     * 이미 ENC: 접두어가 붙은 값은 마이그레이션이나 재호출 과정에서 중복 암호화되지 않도록 그대로 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param plainUserId 외부 OAuth 제공자의 원본 사용자 식별값
     * @return DB 저장 및 조회용 암호문
     */
    public String encryptForStorage(String plainUserId) {
        // plainUserId 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(plainUserId) || plainUserId.startsWith(ENCRYPTED_PREFIX)) {
            // 로그인 조회와 신규 회원 저장에 사용할 USER_IDXX 암호문을 생성 결과를 반환한다
            return plainUserId;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 초기화된 Firebase 서비스 인스턴스를 조회한다
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            // 암호화 또는 복호화 모드와 키로 Cipher를 초기화한다
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            // 로그인 조회와 신규 회원 저장에 사용할 USER_IDXX 암호문을 생성 결과를 반환한다
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(cipher.doFinal(plainUserId.getBytes(StandardCharsets.UTF_8)));
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {

            throw new IllegalStateException("USER_IDXX encryption failed.", e);
        }
    }

    /**
     * 임의 길이의 설정 키를 AES-128 키 길이에 맞게 축약한다.
     * 설정 문자열을 그대로 잘라 쓰지 않고 SHA-256 해시 후 앞 16바이트를 사용해 키 길이 오류를 방지한다.
     *
     * @author SeungHyeon.Kang
     * @param sourceKey 설정으로 주입된 원본 키 문자열
     * @return AES-128 키 바이트
     */
    private byte[] createAesKey(String sourceKey) {
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 초기화된 Firebase 서비스 인스턴스를 조회한다
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 임의 길이의 설정 키를 AES-128 키 길이에 맞게 축약 결과를 반환한다
            return Arrays.copyOf(digest.digest(sourceKey.getBytes(StandardCharsets.UTF_8)), 16);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {

            throw new IllegalStateException("USER_IDXX encryption key initialization failed.", e);
        }
    }
}
