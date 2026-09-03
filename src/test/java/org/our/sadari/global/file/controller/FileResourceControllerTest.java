package org.our.sadari.global.file.controller;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.file.mapper.FileMapper;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.our.sadari.social.service.UserBlockService;
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
 * 2026-08-26        HanWon.Jang         이미지 재검증 캐시 검증
 * 2026-08-26        HanWon.Jang         배경사진 화면용 파생본 조회 검증
 */
@ExtendWith(MockitoExtension.class)
class FileResourceControllerTest {

    // 공개 이미지 조회에 사용할 파일 저장소 대역
    @Mock
    private FileStorage fileStorage;
    // 공개 파일 참조 상태를 조회할 데이터 접근 대역
    @Mock
    private FileMapper fileMapper;
    // 배경사진 화면용 파생본을 처리할 파일 업무 대역
    @Mock
    private FileService fileService;
    // 이미지 요청자와 소유자의 양방향 차단 관계 조회 대역
    @Mock
    private UserBlockService userBlockService;

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
        when(fileMapper.getActivePublicFileOwner(storedName, "/uploads/profile/260807/" + storedName))
                .thenReturn(20L);
        // 검증된 객체 키 조회에 PNG 이미지가 반환되도록 저장소 응답을 구성한다
        when(fileStorage.getFile("profile/260807/" + storedName))
                .thenReturn(Optional.of(new StoredFile(imageBytes, "image/png")));
        // 실제 공개 이미지 조회 계약을 실행한다
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("profile", "260807", storedName, null, null, 10L);

        // 정상 이미지 응답 상태를 확인한다
        assertEquals(200, response.getStatusCode().value());
        // 저장된 MIME 유형이 응답에 유지되는지 확인한다
        assertEquals("image/png", response.getHeaders().getContentType().toString());
        // 다음 사용 전 서버 재검증이 필요한 비공개 캐시 정책인지 확인한다
        assertTrue(response.getHeaders().getCacheControl().contains("no-cache"));
        // 공유 캐시에 사용자 이미지를 저장하지 않는지 확인한다
        assertTrue(response.getHeaders().getCacheControl().contains("private"));
        // 서버 검증에 실패한 저장본이 사용되지 않는지 확인한다
        assertTrue(response.getHeaders().getCacheControl().contains("must-revalidate"));
        // UUID 파일명이 조건부 요청 식별자로 반환되는지 확인한다
        assertEquals("\"" + storedName + "\"", response.getHeaders().getETag());
        // 저장된 이미지 바이트가 변경 없이 반환되는지 확인한다
        assertArrayEquals(imageBytes, response.getBody());
    }

    /** 활성 회원 이미지의 ETag가 일치하면 S3 원본을 다시 조회하지 않는지 검증한다. */
    @Test
    void getFileReturnsNotModified() throws IOException {

        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        // 브라우저 저장본을 사용하기 전에도 현재 활성 회원 이미지인지 확인하도록 구성한다
        when(fileMapper.getActivePublicFileOwner(storedName, "/uploads/background/260807/" + storedName))
                .thenReturn(20L);

        // 이전 정상 응답에서 받은 ETag로 조건부 이미지 조회를 실행한다
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("background", "260807", storedName, null, "\"" + storedName + "\"", 10L);

        // 변경되지 않은 활성 이미지는 본문 없는 조건부 응답으로 처리되는지 확인한다
        assertEquals(304, response.getStatusCode().value());
        // 조건부 응답에도 다음 사용 전 재검증할 ETag가 유지되는지 확인한다
        assertEquals("\"" + storedName + "\"", response.getHeaders().getETag());
        // 브라우저 저장본 재사용 경로가 S3 원본을 다시 내려받지 않는지 확인한다
        verifyNoInteractions(fileStorage);
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
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("profile", "260807", "..%2Fsecret.png", null, null, 10L);

        // 잘못된 공개 경로가 파일 부재 응답으로 처리되는지 확인한다
        assertEquals(404, response.getStatusCode().value());
        // 검증 실패 경로가 내부 저장소까지 전달되지 않는지 확인한다
        verifyNoInteractions(fileStorage, fileMapper, fileService);
    }

    /** 탈퇴 등으로 활성 회원이 참조하지 않는 이전 이미지는 저장소 조회 전에 차단한다. */
    @Test
    void getFileRejectsInactive() throws IOException {
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        // 활성 회원 참조가 없는 이전 프로필 이미지 조건을 구성한다
        when(fileMapper.getActivePublicFileOwner(storedName, "/uploads/profile/260807/" + storedName))
                .thenReturn(null);

        // 공개 URL과 이전 ETag를 알고 있어도 비활성 회원 이미지를 다시 검증하도록 요청한다
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("profile", "260807", storedName, null, "\"" + storedName + "\"", 10L);

        // 활성 참조가 없는 이미지는 파일 부재와 같은 응답으로 처리되는지 확인한다
        assertEquals(404, response.getStatusCode().value());
        // 권한 없는 파일은 저장소 바이트를 읽지 않는지 확인한다
        verifyNoInteractions(fileStorage);
    }

    /** 화면용 배경사진 요청이 파생본 서비스와 별도 ETag를 사용하는지 검증한다. */
    @Test
    void getFileReturnsDisplay() throws IOException {
        byte[] imageBytes = {4, 5, 6};
        String storedName = "123e4567-e89b-12d3-a456-426614174000.jpg";
        String objectKey = "background/260807/" + storedName;
        // 활성 회원의 현재 배경사진으로 참조되는 공개 파일 조건을 구성한다
        when(fileMapper.getActivePublicFileOwner(storedName, "/uploads/" + objectKey)).thenReturn(20L);
        // 화면용 파생 이미지와 ETag 원문을 파일 서비스 응답으로 구성한다
        when(fileService.getBgDisplayTag(storedName)).thenReturn(storedName + "-display-1600");
        when(fileService.getBgDisplayFile(objectKey))
                .thenReturn(Optional.of(new StoredFile(imageBytes, "image/jpeg")));

        // 일반 화면용 배경사진 조회 계약을 실행한다
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("background", "260807", storedName, "display", null, 10L);

        // 파생 이미지 응답과 원본과 구분되는 캐시 식별자를 확인한다
        assertEquals(200, response.getStatusCode().value());
        assertEquals("\"" + storedName + "-display-1600\"", response.getHeaders().getETag());
        assertArrayEquals(imageBytes, response.getBody());
        // 화면용 요청이 원본 저장소를 컨트롤러에서 직접 조회하지 않는지 확인한다
        verifyNoInteractions(fileStorage);
    }

    /** 요청자와 이미지 소유자가 차단 관계이면 알려진 직접 경로도 파일 부재로 처리하는지 검증한다. */
    @Test
    void getFileRejectsBlockedUser() throws IOException {
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        // 현재 활성 이미지 소유자를 20번 사용자로 조회하도록 설정한다
        when(fileMapper.getActivePublicFileOwner(storedName, "/uploads/profile/260807/" + storedName))
                .thenReturn(20L);
        // 10번 요청자와 20번 이미지 소유자가 양방향 격리 상태가 되도록 설정한다
        when(userBlockService.isBlocked(10L, 20L)).thenReturn(true);
        // 차단 사용자가 알고 있는 현재 이미지 직접 경로로 조회를 요청한다
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("profile", "260807", storedName, null, null, 10L);
        // 차단 방향을 노출하지 않는 파일 부재 응답인지 검증한다
        assertEquals(404, response.getStatusCode().value());
        // 차단된 요청이 실제 이미지 저장소 바이트를 읽지 않는지 검증한다
        verifyNoInteractions(fileStorage);
    }

    /** 프로필 사진과 알 수 없는 variant 요청을 저장소 접근 전에 차단하는지 검증한다. */
    @Test
    void getFileRejectsBadVariant() throws IOException {
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";

        // 프로필 사진에 배경 전용 파생본을 요청한다
        ResponseEntity<byte[]> response = new FileResourceController(
                fileStorage, fileMapper, fileService, userBlockService)
                .getFile("profile", "260807", storedName, "display", null, 10L);

        // 지원하지 않는 조합은 내부 조회 없이 파일 부재로 처리되는지 확인한다
        assertEquals(404, response.getStatusCode().value());
        verifyNoInteractions(fileStorage, fileMapper, fileService);
    }
}
