package org.our.sadari.global.file.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * fileName       : FileStorageConfigTest
 * author         : HanWon.Jang
 * date           : 2026-09-16
 * description    : AWS S3와 S3 호환 저장소의 Checksum 설정을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-16        HanWon.Jang         최초 생성
 */
class FileStorageConfigTest {

    // S3 Client 생성 설정 검증 대상
    private final FileStorageConfig fileStorageConfig = new FileStorageConfig();

    /** S3 호환 저장소가 AWS 전용 자동 Checksum을 사용하지 않는지 검증 */
    @Test
    void usesCompatibleChecksums() {

        // 사용자 지정 Endpoint를 사용하는 S3 호환 Client 구성
        try (S3Client client = fileStorageConfig.s3Client(
                "ap-northeast-2", "http://127.0.0.1:3900", true, "test-access-key", "test-secret-key")) {
            // Garage 요청 본문 서명과 충돌하지 않는 최소 Checksum 계산 확인
            assertEquals(RequestChecksumCalculation.WHEN_REQUIRED
                       , client.serviceClientConfiguration().requestChecksumCalculation());
            // S3 호환 응답에 선택적 Checksum Header가 없어도 허용하는 설정 확인
            assertEquals(ResponseChecksumValidation.WHEN_REQUIRED
                       , client.serviceClientConfiguration().responseChecksumValidation());
        }
    }

    /** AWS S3가 SDK 기본 Checksum 보호를 유지하는지 검증 */
    @Test
    void keepsAwsChecksumDefault() {

        // 사용자 지정 Endpoint가 없는 AWS S3 Client 구성
        try (S3Client client = fileStorageConfig.s3Client(
                "ap-northeast-2", "", false, "test-access-key", "test-secret-key")) {
            // AWS S3의 지원 가능 요청 Checksum 기본값 유지 확인
            assertEquals(RequestChecksumCalculation.WHEN_SUPPORTED
                       , client.serviceClientConfiguration().requestChecksumCalculation());
            // AWS S3의 지원 가능 응답 Checksum 기본값 유지 확인
            assertEquals(ResponseChecksumValidation.WHEN_SUPPORTED
                       , client.serviceClientConfiguration().responseChecksumValidation());
        }
    }
}
