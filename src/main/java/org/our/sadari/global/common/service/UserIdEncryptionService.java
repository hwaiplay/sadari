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
 * 외부 OAuth 제공자가 내려주는 사용자 식별값을 DB 저장용 암호문으로 변환하는 서비스이다.
 * USER_IDXX는 로그인 조회 조건으로도 사용되므로 같은 평문은 항상 같은 암호문이 나오는 결정적 암호화를 사용한다.
 *
 * @author Seunghyeon.Kang
 */
@Service
public class UserIdEncryptionService {

    private static final String ENCRYPTED_PREFIX = "ENC:";
    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private final SecretKeySpec secretKeySpec;

    /**
     * 암호화 키는 별도 환경변수 app.crypto.user-id-key가 있으면 그것을 우선 사용하고, 없으면 JWT secret을 재사용한다.
     * 운영에서 JWT secret을 교체하면 기존 USER_IDXX 조회가 불가능하므로 실제 배포 환경에서는 별도 고정 키를 두는 것이 맞다.
     *
     * @author Seunghyeon.Kang
     * @param userIdEncryptionKey USER_IDXX 암호화 전용 키
     */
    public UserIdEncryptionService(@Value("${app.crypto.user-id-key:${jwt.secret_key}}") String userIdEncryptionKey) {
        this.secretKeySpec = new SecretKeySpec(createAesKey(userIdEncryptionKey), KEY_ALGORITHM);
    }

    /**
     * 로그인 조회와 신규 회원 저장에 사용할 USER_IDXX 암호문을 생성한다.
     * 이미 ENC: 접두어가 붙은 값은 마이그레이션이나 재호출 과정에서 중복 암호화되지 않도록 그대로 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param plainUserId 외부 OAuth 제공자의 원본 사용자 식별값
     * @return DB 저장 및 조회용 암호문
     */
    public String encryptForStorage(String plainUserId) {
        if (StringUtil.isEmpty(plainUserId) || plainUserId.startsWith(ENCRYPTED_PREFIX)) {
            return plainUserId;
        }

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(cipher.doFinal(plainUserId.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("USER_IDXX encryption failed.", e);
        }
    }

    /**
     * 임의 길이의 설정 키를 AES-128 키 길이에 맞게 축약한다.
     * 설정 문자열을 그대로 잘라 쓰지 않고 SHA-256 해시 후 앞 16바이트를 사용해 키 길이 오류를 방지한다.
     *
     * @author Seunghyeon.Kang
     * @param sourceKey 설정으로 주입된 원본 키 문자열
     * @return AES-128 키 바이트
     */
    private byte[] createAesKey(String sourceKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Arrays.copyOf(digest.digest(sourceKey.getBytes(StandardCharsets.UTF_8)), 16);
        } catch (Exception e) {
            throw new IllegalStateException("USER_IDXX encryption key initialization failed.", e);
        }
    }
}