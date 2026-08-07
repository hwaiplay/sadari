package org.our.sadari.notice.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.our.sadari.notice.service.NoticeService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : NoticeFileController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자에게만 공지사항 본문 이미지를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@RestController
public class NoticeFileController {

    // yyMMdd 업로드 날짜 형식
    private static final Pattern UPLOAD_DATE_PATTERN = Pattern.compile("[0-9]{6}");
    // 서버 생성 UUID 이미지 파일명 형식
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png)"
    );

    // 실행 환경에 연결된 공지 이미지 저장소
    private final FileStorage fileStorage;
    // 활성 사용자 확인 서비스
    private final NoticeService noticeService;

    /** 공지 이미지 조회에 저장소와 사용자 상태 확인 기능을 주입한다. */
    public NoticeFileController(FileStorage fileStorage, NoticeService noticeService) {
        this.fileStorage = fileStorage;
        this.noticeService = noticeService;
    }

    @GetMapping("/uploads/notice/{uploadDate}/{storedName}")
    public ResponseEntity<byte[]> getNoticeFile(
            @AuthenticationPrincipal Long userNumb
          , @PathVariable String uploadDate
          , @PathVariable String storedName) throws IOException {
        if (!noticeService.isActiveUser(userNumb)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!UPLOAD_DATE_PATTERN.matcher(uploadDate).matches()
                || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            return ResponseEntity.notFound().build();
        }
        Optional<StoredFile> storedFile = fileStorage.getFile("notice/" + uploadDate + "/" + storedName);
        if (storedFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = StringUtil.isEmpty(storedFile.get().contentType())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(storedFile.get().contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.get().bytes().length)
                .cacheControl(CacheControl.noStore())
                .body(storedFile.get().bytes());
    }
}
