package org.our.sadari.global.file.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.file.mapper.FileMapper;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.springframework.http.ResponseEntity;

/**
 * fileName       : FileResourceControllerTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 저장소 이미지를 기존 업로드 URL로 제공하는 계약을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class FileResourceControllerTest {

    // 공개 이미지 조회에 사용할 파일 저장소 대역
    @Mock
    private FileStorage fileStorage;
    // 공개 파일 참조 상태를 조회할 데이터 접근 대역
    @Mock
    private FileMapper fileMapper;

    /**
     * 유효한 업로드 경로의 S3 또는 로컬 이미지가 MIME 유형과 함께 반환되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @throws IOException 저장소 조회 대역 호출 중 발생
     */
    @Test
    void getFileReturnsStoredImage() throws IOException {

        byte[] imageBytes = {1, 2, 3};
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        // 활성 회원의 현재 프로필로 참조되는 공개 파일 조건을 구성한다
        when(fileMapper.getActivePublicFileCount(storedName, "/uploads/profile/260807/" + storedName))
                .thenReturn(1);
        // 검증된 객체 키 조회에 PNG 이미지가 반환되도록 저장소 응답을 구성한다
        when(fileStorage.getFile("profile/260807/" + storedName))
                .thenReturn(Optional.of(new StoredFile(imageBytes, "image/png")));
        // 실제 공개 이미지 조회 계약을 실행한다
        ResponseEntity<byte[]> response = new FileResourceController(fileStorage, fileMapper)
                .getFile("profile", "260807", storedName);

        // 정상 이미지 응답 상태를 확인한다
        assertEquals(200, response.getStatusCode().value());
        // 저장된 MIME 유형이 응답에 유지되는지 확인한다
        assertEquals("image/png", response.getHeaders().getContentType().toString());
        // 계정 상태 변경 뒤 이전 이미지가 브라우저 캐시에서 재노출되지 않도록 저장 금지를 확인한다
        assertEquals("no-store", response.getHeaders().getCacheControl());
        // 저장된 이미지 바이트가 변경 없이 반환되는지 확인한다
        assertArrayEquals(imageBytes, response.getBody());
    }

    /**
     * 경로 이동 문자가 포함된 파일명은 저장소 접근 전에 차단되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @throws IOException 컨트롤러 조회 계약상 발생 가능
     */
    @Test
    void getFileRejectsBadName() throws IOException {

        // 허용되지 않은 파일명으로 공개 이미지 조회 계약을 실행한다
        ResponseEntity<byte[]> response = new FileResourceController(fileStorage, fileMapper)
                .getFile("profile", "260807", "..%2Fsecret.png");

        // 잘못된 공개 경로가 파일 부재 응답으로 처리되는지 확인한다
        assertEquals(404, response.getStatusCode().value());
        // 검증 실패 경로가 내부 저장소까지 전달되지 않는지 확인한다
        verifyNoInteractions(fileStorage, fileMapper);
    }

    /** 탈퇴 등으로 활성 회원이 참조하지 않는 이전 이미지는 저장소 조회 전에 차단한다. */
    @Test
    void getFileRejectsInactive() throws IOException {
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        // 활성 회원 참조가 없는 이전 프로필 이미지 조건을 구성한다
        when(fileMapper.getActivePublicFileCount(storedName, "/uploads/profile/260807/" + storedName))
                .thenReturn(0);

        // 공개 URL을 알고 있는 상태에서 이전 프로필 이미지 조회를 요청한다
        ResponseEntity<byte[]> response = new FileResourceController(fileStorage, fileMapper)
                .getFile("profile", "260807", storedName);

        // 활성 참조가 없는 이미지는 파일 부재와 같은 응답으로 처리되는지 확인한다
        assertEquals(404, response.getStatusCode().value());
        // 권한 없는 파일은 저장소 바이트를 읽지 않는지 확인한다
        verifyNoInteractions(fileStorage);
    }
}
