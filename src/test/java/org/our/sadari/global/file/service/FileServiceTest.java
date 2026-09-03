package org.our.sadari.global.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.file.dto.FileDto;
import org.our.sadari.global.file.dto.ProfileImageDraftDto;
import org.our.sadari.global.file.exception.InvalidImageFileException;
import org.our.sadari.global.file.mapper.FileMapper;
import org.our.sadari.global.file.storage.LocalFileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;

/**
 * fileName       : FileServiceTest
 * author         : HanWon.Jang
 * date           : 2026-07-26
 * description    : 이미지 파일 로직의 동작을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 * 2026-08-06        SeungHyeon.Kang    이미지 저장·정규화 검증 추가
 * 2026-08-26        HanWon.Jang         공용 HTTP 의존성 반영
 * 2026-08-26        HanWon.Jang         배경사진 화면용 파생본 생성·삭제 검증
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    // File 데이터 접근 객체
    @Mock
    private FileMapper fileMapper;

    // 외부 프로필 이미지 조회용 HTTP 클라이언트
    @Mock
    private RestTemplate restTemplate;

    // File 업무 처리 서비스
    private FileService fileService;

    // 테스트마다 격리된 업로드 루트 디렉터리
    @TempDir
    private Path uploadRootPath;

    /**
     * 각 테스트에서 운영 기본값과 같은 이미지 제한값을 FileService에 설정함
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 파일 검증 서비스 단위 테스트 대상을 담을 객체를 생성함
        fileService = new FileService(fileMapper, new LocalFileStorage(uploadRootPath.toString()), restTemplate);
        // Field 업무 값을 ReflectionTestUtils DTO에 설정함
        ReflectionTestUtils.setField(fileService, "maxImageBytes", 10_485_760L);
        // Field 업무 값을 ReflectionTestUtils DTO에 설정함
        ReflectionTestUtils.setField(fileService, "maxImagePixels", 20_000_000L);
        // Field 업무 값을 ReflectionTestUtils DTO에 설정함
        ReflectionTestUtils.setField(fileService, "maxImageDimension", 8_192);
        // 테스트 파일이 실제 프로젝트 uploads 디렉터리에 생성되지 않도록 임시 루트 경로를 설정함
        // 프로필 임시 이미지도 테스트 전용 경로에만 생성되도록 별도 루트를 설정함
        ReflectionTestUtils.setField(fileService, "profileImageDraftRootPath", uploadRootPath.resolve("drafts"));
    }

    /**
     * 프로필 임시 이미지를 다시 선택하면 이전 물리 파일을 제거하고 서버 미리보기만 반환하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws IOException 테스트 이미지 생성 또는 임시 파일 확인 중 오류가 발생한 경우
     */
    @Test
    void setProfileDraftReplaces() throws IOException {
        MockMultipartFile firstImage = new MockMultipartFile(
                "imageFile",
                "first.png",
                "image/png",
                createPngBytes()
        );
        MockMultipartFile secondImage = new MockMultipartFile(
                "imageFile",
                "second.png",
                "image/png",
                createPngBytes()
        );

        // 첫 번째 프로필 이미지를 사용자 전용 임시 저장소에 보관함
        ProfileImageDraftDto firstDraft = fileService.setProfileImageDraft(
                firstImage,
                Constant.FILE_TYPE_PROFILE,
                31L
        );
        // 같은 사용자가 프로필 이미지를 다시 선택해 이전 임시 선택본을 교체함
        ProfileImageDraftDto secondDraft = fileService.setProfileImageDraft(
                secondImage,
                Constant.FILE_TYPE_PROFILE,
                31L
        );

        Path draftDirectory = uploadRootPath.resolve("drafts").resolve("31").resolve("profile");
        try (Stream<Path> draftFiles = Files.list(draftDirectory)) {
            // 최신 원본과 축소 미리보기 두 파일만 남는지 확인함
            assertEquals(2L, draftFiles.count());
        }
        // 재선택 시 임시 식별값이 새로 발급되는지 확인함
        assertFalse(firstDraft.getDraftToken().equals(secondDraft.getDraftToken()));
        // 공개 임시 URL 대신 서버가 생성한 PNG Data URL을 반환하는지 확인함
        assertTrue(secondDraft.getPreviewDataUrl().startsWith("data:image/png;base64,"));
        // 임시 선택본이 30분 만료 시각을 포함하는지 확인함
        assertTrue(secondDraft.getExpiresAt().isAfter(java.time.Instant.now()));
    }

    /**
     * PNG 파일명과 MIME 타입으로 위장한 일반 텍스트가 저장되지 않는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setImageRejectsFakePng() {
        // 허용하지 않는 이미지 입력을 재현할 테스트 파일을 담을 객체를 생성함
        MockMultipartFile disguisedFile = new MockMultipartFile("profileImage", "profile.png", "image/png", "not-an-image".getBytes(StandardCharsets.UTF_8));

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인함
        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        disguisedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인함
        verifyNoInteractions(fileMapper);
    }

    /**
     * JPEG 시그니처만 붙이고 본문은 손상된 파일이 디코딩 단계에서 거부되는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setImageRejectsBadJpeg() {

        byte[] invalidJpeg = {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02
        };

        // 허용하지 않는 이미지 입력을 재현할 테스트 파일을 담을 객체를 생성함
        MockMultipartFile disguisedFile = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg", invalidJpeg);

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인함
        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        disguisedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인함
        verifyNoInteractions(fileMapper);
    }

    /**
     * 설정된 최대 바이트를 넘긴 파일이 이미지 디코딩이나 파일 생성 전에 거부되는지 검증함
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setImageRejectsOversize() {
        // Field 업무 값을 ReflectionTestUtils DTO에 설정함
        ReflectionTestUtils.setField(fileService, "maxImageBytes", 4L);
        // 허용하지 않는 이미지 입력을 재현할 테스트 파일을 담을 객체를 생성함
        MockMultipartFile oversizedFile = new MockMultipartFile("profileImage", "profile.png", "image/png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x00});

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인함
        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        oversizedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인함
        verifyNoInteractions(fileMapper);
    }

    /**
     * 정상 프로필 이미지가 프로필 유형 아래 yyMMdd 날짜 디렉터리에 저장되는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws IOException 테스트 이미지 생성 또는 파일 확인 중 오류가 발생한 경우
     */
    @Test
    void setImageStoresByDate() throws IOException {
        // 파일 메타정보 등록 성공과 생성 파일 번호를 모의 응답으로 설정함
        doAnswer(invocation -> {
            // 등록 요청에 전달된 파일 메타정보를 가져옴
            FileDto fileDto = invocation.getArgument(0);
            // DB 자동 증가 키를 모의 파일 번호로 설정함
            fileDto.setFileNumb(101L);
            // 파일 메타정보 한 건 등록 결과를 반환함
            return 1;
        }).when(fileMapper).setFile(any(FileDto.class));

        // 이미지 검증과 재인코딩을 통과할 실제 PNG 파일을 생성함
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage"
              , "profile.png"
              , "image/png"
              , createPngBytes()
        );

        // 프로필 이미지를 날짜별 업로드 경로에 저장함
        fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, 31L);

        // DB에 저장된 파일 접근 경로와 저장 파일명을 확인할 캡처 객체를 생성함
        ArgumentCaptor<FileDto> fileCaptor = ArgumentCaptor.forClass(FileDto.class);
        // 파일 메타정보 등록 요청을 캡처함
        verify(fileMapper).setFile(fileCaptor.capture());
        // 캡처한 파일 메타정보를 가져옴
        FileDto savedFile = fileCaptor.getValue();
        // yyMMdd 날짜 디렉터리가 프로필 접근 경로에 포함되는지 확인함
        assertTrue(savedFile.getFilePath().matches("/uploads/profile/[0-9]{6}/.+\\.png"));
        // 접근 경로를 임시 업로드 루트 기준의 실제 저장 경로로 변환함
        Path storedPath = uploadRootPath.resolve(savedFile.getFilePath().substring("/uploads/".length()));
        // 날짜 디렉터리 아래에 실제 이미지 파일이 생성되었는지 확인함
        assertTrue(Files.exists(storedPath));
    }

    /** 신규 배경사진 저장 시 긴 변 1600px의 화면용 파생본이 함께 생성되는지 검증함 */
    @Test
    void setBgImageCreatesDisplay() throws IOException {
        // 파일 메타정보 등록 성공과 생성 파일 번호를 모의 응답으로 설정함
        doAnswer(invocation -> {
            FileDto fileDto = invocation.getArgument(0);
            fileDto.setFileNumb(103L);
            return 1;
        }).when(fileMapper).setFile(any(FileDto.class));
        MockMultipartFile backgroundImage = new MockMultipartFile(
                "backgroundImage",
                "background.png",
                "image/png",
                createPngBytes(2_000, 1_000)
        );

        // 고해상도 배경사진을 영구 저장함
        fileService.setUploadedImage(backgroundImage, Constant.FILE_TYPE_BACKGROUND, 31L);

        // DB에 기록된 원본 경로를 기준으로 원본과 화면용 파생 파일 위치를 계산함
        ArgumentCaptor<FileDto> fileCaptor = ArgumentCaptor.forClass(FileDto.class);
        verify(fileMapper).setFile(fileCaptor.capture());
        Path originalPath = uploadRootPath.resolve(
                fileCaptor.getValue().getFilePath().substring("/uploads/".length())
        );
        Path displayPath = originalPath.getParent().resolve("display").resolve(originalPath.getFileName());
        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        BufferedImage displayImage = ImageIO.read(displayPath.toFile());

        // 전체 화면 보기에 사용할 원본 해상도는 유지되는지 확인함
        assertEquals(2_000, originalImage.getWidth());
        assertEquals(1_000, originalImage.getHeight());
        // 일반 화면용 파생본은 비율을 유지한 채 긴 변이 1600px로 제한되는지 확인함
        assertEquals(1_600, displayImage.getWidth());
        assertEquals(800, displayImage.getHeight());
    }

    /** 기존 배경사진의 첫 화면용 요청에서 파생본을 생성하고 이후 저장소에 유지하는지 검증함 */
    @Test
    void getBgDisplayCreatesLazy() throws IOException {
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        String objectKey = "background/260807/" + storedName;
        Path originalPath = uploadRootPath.resolve(objectKey);
        Files.createDirectories(originalPath.getParent());
        Files.write(originalPath, createPngBytes(2_000, 1_000));

        // 배포 전에 저장된 원본의 화면용 파생본을 처음 조회함
        Optional<StoredFile> displayFile = fileService.getBgDisplayFile(objectKey);

        // 응답 이미지와 저장된 파생 이미지가 모두 화면 제한 크기를 따르는지 확인함
        assertTrue(displayFile.isPresent());
        BufferedImage responseImage = ImageIO.read(new ByteArrayInputStream(displayFile.get().bytes()));
        Path displayPath = originalPath.getParent().resolve("display").resolve(storedName);
        BufferedImage storedImage = ImageIO.read(displayPath.toFile());
        assertEquals(1_600, responseImage.getWidth());
        assertEquals(800, responseImage.getHeight());
        assertEquals(1_600, storedImage.getWidth());
        assertEquals(800, storedImage.getHeight());
    }

    /**
     * 시계 방향 90도 EXIF 방향이 있는 JPEG를 표시 방향과 같은 픽셀로 저장하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws IOException 테스트 이미지 생성 또는 저장 파일 확인 중 오류가 발생한 경우
     */
    @Test
    void setImageAppliesExif() throws IOException {
        // 파일 메타정보 등록 성공과 생성 파일 번호를 모의 응답으로 설정함
        doAnswer(invocation -> {
            // 등록 요청에 전달된 파일 메타정보를 가져옴
            FileDto fileDto = invocation.getArgument(0);
            // DB 자동 증가 키를 모의 파일 번호로 설정함
            fileDto.setFileNumb(102L);
            // 파일 메타정보 한 건 등록 결과를 반환함
            return 1;
        }).when(fileMapper).setFile(any(FileDto.class));

        // 좌우 색상으로 회전 방향을 구분할 수 있는 Orientation 6 JPEG를 생성함
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage"
              , "portrait.jpg"
              , "image/jpeg"
              , createExifJpegBytes(6)
        );

        // EXIF 표시 방향이 포함된 프로필 이미지를 저장함
        fileService.setUploadedImage(profileImage, Constant.FILE_TYPE_PROFILE, 31L);

        // 저장된 파일 접근 경로를 확인할 캡처 객체를 생성함
        ArgumentCaptor<FileDto> fileCaptor = ArgumentCaptor.forClass(FileDto.class);
        // 파일 메타정보 등록 요청을 캡처함
        verify(fileMapper).setFile(fileCaptor.capture());
        // 접근 경로를 임시 업로드 루트 기준의 실제 저장 경로로 변환함
        Path storedPath = uploadRootPath.resolve(fileCaptor.getValue().getFilePath().substring("/uploads/".length()));
        // 재인코딩된 JPEG 픽셀을 조회함
        BufferedImage storedImage = ImageIO.read(storedPath.toFile());

        // 시계 방향 회전으로 가로와 세로 크기가 교환되었는지 확인함
        assertEquals(20, storedImage.getWidth());
        // 시계 방향 회전으로 원본 가로 길이가 출력 세로 길이가 되었는지 확인함
        assertEquals(30, storedImage.getHeight());

        // 원본 왼쪽의 빨간 영역이 회전 후 상단으로 이동했는지 확인할 색상을 조회함
        Color topColor = new Color(storedImage.getRGB(10, 5));
        // 원본 오른쪽의 파란 영역이 회전 후 하단으로 이동했는지 확인할 색상을 조회함
        Color bottomColor = new Color(storedImage.getRGB(10, 25));
        // 회전 후 상단이 빨간 영역인지 확인함
        assertTrue(topColor.getRed() > topColor.getBlue());
        // 회전 후 하단이 파란 영역인지 확인함
        assertTrue(bottomColor.getBlue() > bottomColor.getRed());
    }

    /**
     * 교체 전 파일 메타정보 삭제가 성공해도 DB 커밋 전에는 물리 파일을 유지하고 커밋 후 삭제하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws IOException 테스트용 물리 파일 생성 중 오류가 발생한 경우
     */
    @Test
    void delFileAfterCommit() throws IOException {
        // 날짜별 프로필 저장 디렉터리를 생성함
        Path profileDirectory = Files.createDirectories(uploadRootPath.resolve("profile").resolve("260804"));
        // 교체 전 프로필 물리 파일을 생성함
        Path storedPath = Files.write(profileDirectory.resolve("old-profile.png"), new byte[] {1, 2, 3});

        // 교체 전 파일 메타정보를 생성함
        FileDto oldFile = new FileDto();
        // 교체 전 파일 번호를 설정함
        oldFile.setFileNumb(10L);
        // 실제 저장 파일명을 설정함
        oldFile.setStorName("old-profile.png");
        // 날짜 디렉터리가 포함된 접근 경로를 설정함
        oldFile.setFilePath("/uploads/profile/260804/old-profile.png");

        // 교체 전 파일 메타정보 조회 결과를 설정함
        when(fileMapper.getFileByNumb(10L)).thenReturn(oldFile);
        // 사용자 참조가 제거된 파일 메타정보 삭제 결과를 설정함
        when(fileMapper.delFileIfUnreferenced(10L)).thenReturn(1);

        // 커밋 후 파일 삭제 시점을 직접 검증할 트랜잭션 동기화 컨텍스트를 시작함
        TransactionSynchronizationManager.initSynchronization();

        // 테스트가 실패해도 다른 테스트에 트랜잭션 동기화 상태가 남지 않도록 정리함
        try {
            // 사용자 참조에서 교체된 파일 정리를 요청함
            fileService.delFile(10L);
            // DB 커밋 전에는 기존 물리 파일이 유지되는지 확인함
            assertTrue(Files.exists(storedPath));

            // 등록된 커밋 후 정리 작업을 순차 실행함
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                // 실제 DB 커밋 완료 상태를 모의 전달함
                synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            }

            // DB 커밋 후 기존 물리 파일이 삭제되는지 확인함
            assertFalse(Files.exists(storedPath));
        }

        // 트랜잭션 동기화 테스트 자원을 반드시 해제함
        finally {
            // 테스트 스레드에 등록된 트랜잭션 동기화 상태를 제거함
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /**
     * 비정상 파일 메타정보가 이미지 유형 루트 디렉터리 자체를 삭제하지 못하는지 검증함
     *
     * @author SeungHyeon.Kang
     * @throws IOException 테스트용 디렉터리 생성 중 오류가 발생한 경우
     */
    @Test
    void delFileKeepsInvalidRoot() throws IOException {
        // 삭제되어서는 안 되는 프로필 이미지 유형 루트 디렉터리를 생성함
        Path profileRoot = Files.createDirectories(uploadRootPath.resolve("profile"));

        // 이미지 유형 루트 자체를 파일 경로로 가진 비정상 메타정보를 생성함
        FileDto invalidFile = new FileDto();
        // 비정상 메타정보의 파일 번호를 설정함
        invalidFile.setFileNumb(11L);
        // 경로 끝과 일치하도록 위조된 저장 파일명을 설정함
        invalidFile.setStorName("profile");
        // 파일이 아닌 이미지 유형 루트 접근 경로를 설정함
        invalidFile.setFilePath("/uploads/profile");

        // 비정상 파일 메타정보 조회 결과를 설정함
        when(fileMapper.getFileByNumb(11L)).thenReturn(invalidFile);
        // 사용자 참조가 없는 파일 메타정보 삭제 결과를 설정함
        when(fileMapper.delFileIfUnreferenced(11L)).thenReturn(1);

        // 비정상 메타정보에 대한 파일 정리를 요청함
        fileService.delFile(11L);

        // 프로필 이미지 유형 루트 디렉터리가 유지되는지 확인함
        assertTrue(Files.isDirectory(profileRoot));
    }

    /** 배경사진 메타정보가 삭제되면 원본과 화면용 파생본이 함께 정리되는지 검증함 */
    @Test
    void delFileRemovesBgDisplay() throws IOException {
        String storedName = "123e4567-e89b-12d3-a456-426614174000.png";
        Path backgroundDirectory = Files.createDirectories(uploadRootPath.resolve("background").resolve("260807"));
        Path originalPath = Files.write(backgroundDirectory.resolve(storedName), new byte[] {1, 2, 3});
        Path displayDirectory = Files.createDirectories(backgroundDirectory.resolve("display"));
        Path displayPath = Files.write(displayDirectory.resolve(storedName), new byte[] {4, 5, 6});
        FileDto oldFile = new FileDto();
        oldFile.setFileNumb(12L);
        oldFile.setStorName(storedName);
        oldFile.setFilePath("/uploads/background/260807/" + storedName);
        when(fileMapper.getFileByNumb(12L)).thenReturn(oldFile);
        when(fileMapper.delFileIfUnreferenced(12L)).thenReturn(1);

        // 트랜잭션 밖의 즉시 정리 경로로 배경사진 삭제를 요청함
        fileService.delFile(12L);

        // 원본과 파생본이 같은 수명주기로 제거되는지 확인함
        assertFalse(Files.exists(originalPath));
        assertFalse(Files.exists(displayPath));
    }

    /**
     * 이미지 검증과 재인코딩을 통과할 최소 PNG 바이트를 생성함
     *
     * @author SeungHyeon.Kang
     * @return 실제 PNG 형식의 이미지 바이트
     * @throws IOException PNG 인코딩 중 오류가 발생한 경우
     */
    private byte[] createPngBytes() throws IOException {
        // 기존 최소 이미지 테스트는 2x2 크기를 사용함
        return createPngBytes(2, 2);
    }

    /**
     * 지정한 크기의 PNG 테스트 이미지를 생성함
     *
     * @author HanWon.Jang
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @return 실제 PNG 형식의 이미지 바이트
     * @throws IOException PNG 인코딩 중 오류가 발생한 경우
     */
    private byte[] createPngBytes(int width, int height) throws IOException {
        // 테스트 픽셀을 담을 RGB 이미지 객체를 생성함
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // PNG 인코딩 결과를 담을 출력 스트림을 생성함
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // 이미지 검증기가 읽을 수 있는 PNG 바이트를 생성함
        ImageIO.write(image, "PNG", output);
        // 생성한 PNG 바이트를 반환함
        return output.toByteArray();
    }

    /**
     * 좌우 색상이 다른 JPEG에 지정한 EXIF Orientation APP1 구간을 추가함
     *
     * @author SeungHyeon.Kang
     * @param orientation JPEG에 기록할 EXIF Orientation 값
     * @return EXIF 방향값이 포함된 JPEG 바이트
     * @throws IOException JPEG 인코딩 중 오류가 발생한 경우
     */
    private byte[] createExifJpegBytes(int orientation) throws IOException {
        // 회전 전후의 방향을 색상 위치로 판별할 원본 이미지를 생성함
        BufferedImage image = new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB);
        // 좌우 색상 영역을 원본 이미지에 그릴 그래픽 객체를 생성함
        Graphics2D graphics = image.createGraphics();

        // 테스트 이미지 생성 중에도 그래픽 자원이 해제되도록 색상 영역 작성을 격리함
        try {
            // 회전 후 상단에 위치해야 하는 원본 왼쪽 영역의 색상을 설정함
            graphics.setColor(Color.RED);
            // 원본 이미지의 왼쪽 절반을 빨간색으로 채움
            graphics.fillRect(0, 0, 15, 20);
            // 회전 후 하단에 위치해야 하는 원본 오른쪽 영역의 색상을 설정함
            graphics.setColor(Color.BLUE);
            // 원본 이미지의 오른쪽 절반을 파란색으로 채움
            graphics.fillRect(15, 0, 15, 20);
        }

        // 테스트 이미지 생성이 끝나면 그래픽 자원을 해제함
        finally {
            // 테스트 이미지에 사용한 그래픽 자원을 해제함
            graphics.dispose();
        }

        // EXIF 메타정보를 추가하기 전 JPEG 바이트를 담을 출력 스트림을 생성함
        ByteArrayOutputStream jpegOutput = new ByteArrayOutputStream();
        // 색상 위치를 검증할 원본 이미지를 JPEG 형식으로 인코딩함
        ImageIO.write(image, "JPEG", jpegOutput);
        // JPEG SOI 마커 뒤에 APP1 구간을 삽입할 원본 바이트를 조회함
        byte[] jpegBytes = jpegOutput.toByteArray();

        // Big Endian TIFF 형식의 Orientation 태그 한 개를 가진 EXIF APP1 구간을 생성함
        byte[] exifSegment = {
                (byte) 0xFF, (byte) 0xE1, 0x00, 0x22
              , 0x45, 0x78, 0x69, 0x66, 0x00, 0x00
              , 0x4D, 0x4D, 0x00, 0x2A, 0x00, 0x00, 0x00, 0x08
              , 0x00, 0x01
              , 0x01, 0x12, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01
              , 0x00, (byte) orientation, 0x00, 0x00
              , 0x00, 0x00, 0x00, 0x00
        };

        // SOI와 EXIF APP1 및 원본 JPEG 본문을 순서대로 조합할 출력 스트림을 생성함
        ByteArrayOutputStream exifOutput = new ByteArrayOutputStream();
        // JPEG 파일 시작 마커를 먼저 기록함
        exifOutput.write(jpegBytes, 0, 2);
        // 촬영 방향을 나타내는 EXIF APP1 구간을 기록함
        exifOutput.write(exifSegment);
        // 기존 JPEG의 나머지 메타정보와 압축 픽셀 데이터를 기록함
        exifOutput.write(jpegBytes, 2, jpegBytes.length - 2);
        // EXIF Orientation이 포함된 JPEG 바이트를 반환함
        return exifOutput.toByteArray();
    }
}
