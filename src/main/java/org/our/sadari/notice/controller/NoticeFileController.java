package org.our.sadari.notice.controller;

import java.io.IOException;
import java.time.Duration;
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
 * description    : 활성 사용자에게만 공지사항 본문 이미지를 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-28        OpenAI.Codex       웰컴 WebP 이미지 응답 지원
 */
@RestController
public class NoticeFileController {

    // 공지 이미지 저장 디렉터리
    private static final String NOTICE_DIRECTORY = "notice";
    // 웰컴페이지 이미지 저장 디렉터리
    private static final String WELCOME_DIRECTORY = "welcome";
    // UUID 경로가 바뀌지 않는 웰컴 이미지의 사용자별 브라우저 캐시 정책
    private static final CacheControl WELCOME_CACHE = CacheControl.maxAge(Duration.ofDays(365))
            .cachePrivate()
            .immutable();
    // yyMMdd 업로드 날짜 형식
    private static final Pattern UPLOAD_DATE_PATTERN = Pattern.compile("[0-9]{6}");
    // 서버 생성 UUID 이미지 파일명 형식
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png|webp)"
    );

    // 실행 환경에 연결된 공지 이미지 저장소
    private final FileStorage fileStorage;
    // 활성 사용자 확인 서비스
    private final NoticeService noticeService;

    /** 공지 이미지 조회에 저장소와 사용자 상태 확인 기능을 주입함 */
    public NoticeFileController(FileStorage fileStorage, NoticeService noticeService) {
        this.fileStorage = fileStorage;
        this.noticeService = noticeService;
    }

    @GetMapping("/uploads/notice/{uploadDate}/{storedName}")
    public ResponseEntity<byte[]> getNoticeFile(
            @AuthenticationPrincipal Long userNumb
          , @PathVariable String uploadDate
          , @PathVariable String storedName) throws IOException {

        // 공지 이미지는 기존 공개 정책대로 브라우저에 저장하지 않고 반환함
        return getStoredFile(userNumb, NOTICE_DIRECTORY, uploadDate, storedName, CacheControl.noStore());
    }

    /**
     * 활성 사용자에게 웰컴페이지 전용 이미지를 장기 캐시 정책과 함께 제공함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param uploadDate 업로드 일자
     * @param storedName 저장 파일명
     * @return 웰컴페이지 이미지 응답
     * @throws IOException 저장소 조회 실패 시 발생
     */
    @GetMapping("/uploads/welcome/{uploadDate}/{storedName}")
    public ResponseEntity<byte[]> getWelcomeFile(
            @AuthenticationPrincipal Long userNumb
          , @PathVariable String uploadDate
          , @PathVariable String storedName) throws IOException {

        // UUID가 변경될 때만 새 파일을 받도록 사용자별 장기 캐시 응답을 반환함
        return getStoredFile(userNumb, WELCOME_DIRECTORY, uploadDate, storedName, WELCOME_CACHE);
    }

    /** 활성 사용자와 서버 생성 경로를 검증한 뒤 저장소 이미지를 반환함 */
    private ResponseEntity<byte[]> getStoredFile(Long userNumb, String directory, String uploadDate
                                               , String storedName, CacheControl cacheControl) throws IOException {

        if (!noticeService.isActiveUser(userNumb)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (!UPLOAD_DATE_PATTERN.matcher(uploadDate).matches()
                || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            return ResponseEntity.notFound().build();
        }
        Optional<StoredFile> storedFile = fileStorage.getFile(directory + "/" + uploadDate + "/" + storedName);
        if (storedFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = StringUtil.isEmpty(storedFile.get().contentType())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(storedFile.get().contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.get().bytes().length)
                .cacheControl(cacheControl)
                .body(storedFile.get().bytes());
    }
}
