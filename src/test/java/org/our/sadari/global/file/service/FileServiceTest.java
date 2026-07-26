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
 * 사용자 이미지 업로드가 파일명이나 Content-Type만 이미지인 위장 파일을 거부하는지 검증합니다.
 *
 * @author Seunghyeon.Kang
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileMapper fileMapper;

    private FileService fileService;

    /**
     * 각 테스트에서 운영 기본값과 같은 이미지 제한값을 FileService에 설정합니다.
     *
     * @author Seunghyeon.Kang
     */
    @BeforeEach
    void setUp() {
        fileService = new FileService(fileMapper);
        ReflectionTestUtils.setField(fileService, "maxImageBytes", 10_485_760L);
        ReflectionTestUtils.setField(fileService, "maxImagePixels", 20_000_000L);
        ReflectionTestUtils.setField(fileService, "maxImageDimension", 8_192);
    }

    /**
     * PNG 파일명과 MIME 타입으로 위장한 일반 텍스트가 저장되지 않는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setUploadedImageRejectsTextDisguisedAsPng() {
        MockMultipartFile disguisedFile = new MockMultipartFile(
                "profileImage"
              , "profile.png"
              , "image/png"
              , "not-an-image".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        disguisedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        verifyNoInteractions(fileMapper);
    }

    /**
     * JPEG 시그니처만 붙이고 본문은 손상된 파일이 디코딩 단계에서 거부되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setUploadedImageRejectsInvalidJpegBody() {
        byte[] invalidJpeg = {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01, 0x02
        };
        MockMultipartFile disguisedFile = new MockMultipartFile(
                "profileImage"
              , "profile.jpg"
              , "image/jpeg"
              , invalidJpeg
        );

        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        disguisedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        verifyNoInteractions(fileMapper);
    }

    /**
     * 설정된 최대 바이트를 넘긴 파일이 이미지 디코딩이나 파일 생성 전에 거부되는지 검증합니다.
     *
     * @author Seunghyeon.Kang
     */
    @Test
    void setUploadedImageRejectsOversizedFile() {
        ReflectionTestUtils.setField(fileService, "maxImageBytes", 4L);
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "profileImage"
              , "profile.png"
              , "image/png"
              , new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x00}
        );

        assertThrows(
                InvalidImageFileException.class
              , () -> fileService.setUploadedImage(
                        oversizedFile
                      , Constant.FILE_TYPE_PROFILE
                      , 1L
                )
        );
        verifyNoInteractions(fileMapper);
    }
}
