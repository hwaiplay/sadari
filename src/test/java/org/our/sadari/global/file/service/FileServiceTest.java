package org.our.sadari.global.file.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.file.exception.InvalidImageFileException;
import org.our.sadari.global.file.mapper.FileMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * fileName       : FileServiceTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-26
 * description    : 이미지 파일 로직의 동작을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-26        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    // File 데이터 접근 객체
    @Mock
    private FileMapper fileMapper;

    // File 업무 처리 서비스
    private FileService fileService;

    /**
     * 각 테스트에서 운영 기본값과 같은 이미지 제한값을 FileService에 설정한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // 파일 검증 서비스 단위 테스트 대상을 담을 객체를 생성한다
        fileService = new FileService(fileMapper);
        // Field 업무 값을 ReflectionTestUtils DTO에 설정한다
        ReflectionTestUtils.setField(fileService, "maxImageBytes", 10_485_760L);
        // Field 업무 값을 ReflectionTestUtils DTO에 설정한다
        ReflectionTestUtils.setField(fileService, "maxImagePixels", 20_000_000L);
        // Field 업무 값을 ReflectionTestUtils DTO에 설정한다
        ReflectionTestUtils.setField(fileService, "maxImageDimension", 8_192);
    }

    /**
     * PNG 파일명과 MIME 타입으로 위장한 일반 텍스트가 저장되지 않는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setUploadedImageRejectsTextDisguisedAsPng() {
        // 허용하지 않는 이미지 입력을 재현할 테스트 파일을 담을 객체를 생성한다
        MockMultipartFile disguisedFile = new MockMultipartFile("profileImage", "profile.png", "image/png", "not-an-image".getBytes(StandardCharsets.UTF_8));

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인한다
        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        disguisedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인한다
        verifyNoInteractions(fileMapper);
    }

    /**
     * JPEG 시그니처만 붙이고 본문은 손상된 파일이 디코딩 단계에서 거부되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setUploadedImageRejectsInvalidJpegBody() {

        byte[] invalidJpeg = {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02
        };

        // 허용하지 않는 이미지 입력을 재현할 테스트 파일을 담을 객체를 생성한다
        MockMultipartFile disguisedFile = new MockMultipartFile("profileImage", "profile.jpg", "image/jpeg", invalidJpeg);

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인한다
        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        disguisedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인한다
        verifyNoInteractions(fileMapper);
    }

    /**
     * 설정된 최대 바이트를 넘긴 파일이 이미지 디코딩이나 파일 생성 전에 거부되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setUploadedImageRejectsOversizedFile() {
        // Field 업무 값을 ReflectionTestUtils DTO에 설정한다
        ReflectionTestUtils.setField(fileService, "maxImageBytes", 4L);
        // 허용하지 않는 이미지 입력을 재현할 테스트 파일을 담을 객체를 생성한다
        MockMultipartFile oversizedFile = new MockMultipartFile("profileImage", "profile.png", "image/png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x00});

        // 검증 대상 코드가 예상 예외를 발생시키는지 확인한다
        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        oversizedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        // 검증 실패 시 의존 객체가 호출되지 않았는지 확인한다
        verifyNoInteractions(fileMapper);
    }
}
