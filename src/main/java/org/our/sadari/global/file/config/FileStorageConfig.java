package org.our.sadari.global.file.config;

import java.net.URI;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.LocalFileStorage;
import org.our.sadari.global.file.storage.S3FileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * fileName       : FileStorageConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 실행 환경에 맞는 로컬 또는 S3 이미지 저장소를 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    환경변수 장기 자격 증명을 S3 클라이언트에 적용
 */
@Configuration
public class FileStorageConfig {

    /**
     * 로컬 개발 환경에서 사용할 디스크 이미지 저장소를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param rootDirectory 로컬 이미지 저장 루트 디렉터리
     * @return 로컬 디스크 이미지 저장소
     */
    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
    public FileStorage localFileStorage(@Value("${app.storage.local-root}") String rootDirectory) {

        // 설정된 로컬 저장 루트를 사용하는 이미지 저장소를 반환한다
        return new LocalFileStorage(rootDirectory);
    }

    /**
     * 운영 환경에서 사용할 AWS S3 또는 S3 호환 클라이언트를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param region S3 리전 식별값
     * @param endpoint S3 호환 저장소의 선택적 엔드포인트
     * @param pathStyleAccess 경로 방식 주소 사용 여부
     * @param accessKey S3 접근에 사용할 장기 Access Key
     * @param secretKey S3 접근에 사용할 장기 Secret Key
     * @return 설정된 정적 자격 증명을 사용하는 S3 클라이언트
     */
    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
    public S3Client s3Client(@Value("${app.storage.s3.region}") String region
                           , @Value("${app.storage.s3.endpoint:}") String endpoint
                           , @Value("${app.storage.s3.path-style-access:false}") boolean pathStyleAccess
                           , @Value("${app.storage.s3.access-key}") String accessKey
                           , @Value("${app.storage.s3.secret-key}") String secretKey) {

        // 리전과 S3 주소 방식을 지정한 클라이언트 빌더를 생성한다
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleAccess)
                        .build());

        // AWS가 아닌 S3 호환 저장소를 사용할 때만 사용자 지정 엔드포인트를 적용한다
        if (!StringUtil.isEmpty(endpoint)) {
            // 검증된 엔드포인트 문자열을 S3 클라이언트에 적용한다
            builder.endpointOverride(URI.create(endpoint));
        }

        // 환경변수에서 주입한 장기 자격 증명으로 인증하는 S3 클라이언트를 반환한다
        return builder.build();
    }

    /**
     * 운영 S3 클라이언트와 대상 버킷을 사용하는 이미지 저장소를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param s3Client S3 API 호출 클라이언트
     * @param bucket 이미지 객체를 저장할 버킷 이름
     * @return S3 기반 이미지 저장소
     */
    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
    public FileStorage s3FileStorage(S3Client s3Client, @Value("${app.storage.s3.bucket}") String bucket) {

        // 비공개 S3 버킷을 사용하는 이미지 저장소를 반환한다
        return new S3FileStorage(s3Client, bucket);
    }
}
