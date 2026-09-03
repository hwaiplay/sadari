package org.our.sadari.notice.controller;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.our.sadari.notice.service.NoticeService;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : NoticeFileControllerTests
 * author         : HanWon.Jang
 * date           : 2026-08-28
 * description    : 웰컴페이지 이미지의 저장 루트와 브라우저 캐시 응답을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        HanWon.Jang        최초 생성
 */
class NoticeFileControllerTests {

    @Test
    void serveWelcomeWithCache() throws Exception {

        String storedName = "11111111-1111-1111-1111-111111111111.webp";
        FileStorage fileStorage = mock(FileStorage.class);
        NoticeService noticeService = mock(NoticeService.class);
        when(noticeService.isActiveUser(1L)).thenReturn(true);
        when(fileStorage.getFile("welcome/260828/" + storedName))
                .thenReturn(Optional.of(new StoredFile(new byte[] {1}, "image/webp")));
        NoticeFileController controller = new NoticeFileController(fileStorage, noticeService);

        ResponseEntity<byte[]> response = controller.getWelcomeFile(1L, "260828", storedName);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getCacheControl())
                .contains("private")
                .contains("max-age=31536000")
                .contains("immutable");
        verify(fileStorage).getFile("welcome/260828/" + storedName);
    }
}
