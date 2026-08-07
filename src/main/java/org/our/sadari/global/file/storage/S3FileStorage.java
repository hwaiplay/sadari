package org.our.sadari.global.file.storage;

import java.io.IOException;
import java.util.Optional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * fileName       : S3FileStorage
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : AWS S3 또는 S3 호환 저장소에 이미지 객체를 저장한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
public class S3FileStorage implements FileStorage {

    // 이미지 객체에 적용할 브라우저 캐시 정책
    private static final String CACHE_CONTROL = "public, max-age=31536000, immutable";

    // S3 API 호출 클라이언트
    private final S3Client s3Client;
    // 이미지 객체를 저장할 버킷 이름
    private final String bucket;

    /**
     * S3 클라이언트와 대상 버킷으로 이미지 저장소를 구성한다.
     *
     * @author SeungHyeon.Kang
     * @param s3Client S3 API 호출 클라이언트
     * @param bucket 이미지 객체를 저장할 버킷 이름
     */
    public S3FileStorage(S3Client s3Client, String bucket) {

        // S3 API 호출에 사용할 클라이언트를 보관한다
        this.s3Client = s3Client;
        // 모든 이미지 객체가 저장될 단일 버킷 이름을 보관한다
        this.bucket = bucket;
    }

    /**
     * 검증된 이미지 바이트를 비공개 S3 버킷에 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 저장할 객체 키
     * @param bytes 저장할 이미지 바이트
     * @param contentType 이미지 MIME 유형
     * @throws IOException S3 객체 쓰기 실패 시 발생
     */
    @Override
    public void setFile(String objectKey, byte[] bytes, String contentType) throws IOException {

        // 콘텐츠 유형과 캐시 정책을 포함하는 S3 객체 쓰기 요청을 생성한다
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .cacheControl(CACHE_CONTROL)
                .build();

        // AWS SDK 예외를 기존 파일 서비스의 IOException 계약으로 변환한다
        try {
            // 공개 ACL 없이 버킷 정책과 IAM 역할로만 접근하는 객체를 저장한다
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
        }

        catch (SdkException e) {
            throw new IOException("S3 image object could not be saved.", e);
        }
    }

    /**
     * 비공개 S3 버킷에서 객체 키에 해당하는 이미지를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 조회할 객체 키
     * @return 저장된 파일, 객체가 없으면 빈 값
     * @throws IOException S3 객체 읽기 실패 시 발생
     */
    @Override
    public Optional<StoredFile> getFile(String objectKey) throws IOException {

        // 대상 버킷과 객체 키를 지정한 S3 객체 조회 요청을 생성한다
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        // 객체 부재와 일시적인 S3 오류를 서로 다른 응답으로 처리한다
        try {
            // 응답 본문과 S3 메타정보를 함께 조회한다
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            // 조회한 이미지 바이트와 저장 시 기록한 MIME 유형을 반환한다
            return Optional.of(new StoredFile(response.asByteArray(), response.response().contentType()));
        }

        catch (S3Exception e) {
            // S3가 객체 부재를 반환하면 공개 요청에서 404로 처리할 빈 값을 반환한다
            if (e.statusCode() == 404) {
                // 존재하지 않는 객체 조회 결과를 반환한다
                return Optional.empty();
            }

            throw new IOException("S3 image object could not be loaded.", e);
        }

        catch (SdkException e) {
            throw new IOException("S3 image object could not be loaded.", e);
        }
    }

    /**
     * 비공개 S3 버킷에서 객체 키에 해당하는 이미지를 멱등하게 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 삭제할 객체 키
     * @throws IOException S3 객체 삭제 실패 시 발생
     */
    @Override
    public void delFile(String objectKey) throws IOException {

        // 대상 버킷과 객체 키를 지정한 S3 객체 삭제 요청을 생성한다
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        // AWS SDK 예외를 기존 파일 서비스의 IOException 계약으로 변환한다
        try {
            // 존재하지 않는 객체에도 성공하는 S3 삭제 요청을 실행한다
            s3Client.deleteObject(request);
        }

        catch (SdkException e) {
            throw new IOException("S3 image object could not be deleted.", e);
        }
    }
}
