package org.our.sadari.global.file.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * fileName       : S3FileStorageTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : S3 이미지 객체 쓰기와 객체 부재 처리 계약을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class S3FileStorageTest {

    // S3 API 호출 클라이언트 대역
    @Mock
    private S3Client s3Client;

    /**
     * S3 객체 쓰기 요청에 버킷과 MIME 유형 및 비공개 객체 키가 전달되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @throws IOException S3 저장소 계약상 발생 가능
     */
    @Test
    void setFileWritesPrivateObjectMetadata() throws IOException {

        // 테스트 버킷을 사용하는 이미지 저장소를 생성한다
        S3FileStorage fileStorage = new S3FileStorage(s3Client, "test-bucket");
        // 이미지 객체를 S3 저장소에 기록한다
        fileStorage.setFile("profile/260807/image.png", new byte[] {1, 2}, "image/png");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        // S3 클라이언트에 전달된 객체 쓰기 요청을 수집한다
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        // 대상 버킷이 설정값과 일치하는지 확인한다
        assertEquals("test-bucket", requestCaptor.getValue().bucket());
        // 객체 키가 공개 URL의 상대 경로와 일치하는지 확인한다
        assertEquals("profile/260807/image.png", requestCaptor.getValue().key());
        // 서버가 판정한 MIME 유형이 S3 메타정보에 기록되는지 확인한다
        assertEquals("image/png", requestCaptor.getValue().contentType());
    }

    /**
     * S3가 객체 부재를 반환하면 예외 대신 빈 조회 결과로 변환되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @throws IOException S3 저장소 계약상 발생 가능
     */
    @Test
    void getFileReturnsEmptyForMissingObject() throws IOException {

        // 객체 조회 시 S3의 404 응답이 발생하도록 구성한다
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).message("missing").build());
        // 객체 부재 응답을 검증할 이미지 저장소를 생성한다
        S3FileStorage fileStorage = new S3FileStorage(s3Client, "test-bucket");
        // 존재하지 않는 객체 조회 계약을 실행한다
        Optional<StoredFile> storedFile = fileStorage.getFile("profile/260807/missing.png");

        // 객체 부재가 빈 조회 결과로 변환되는지 확인한다
        assertTrue(storedFile.isEmpty());
    }
}
