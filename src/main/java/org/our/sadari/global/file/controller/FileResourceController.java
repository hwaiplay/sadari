package org.our.sadari.global.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : FileResourceController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 비공개 파일 저장소의 공개 이미지 객체를 기존 업로드 URL로 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@RestController
@Tag(name = "파일", description = "비공개 저장소의 공개 이미지 조회 API")
public class FileResourceController {

    // 공개 이미지 경로에서 허용하는 이미지 유형 디렉터리
    private static final Set<String> ALLOWED_DIRECTORIES = Set.of("profile", "background");
    // 업로드 날짜 경로가 yyMMdd 숫자로만 구성되는지 검증하는 패턴
    private static final Pattern UPLOAD_DATE_PATTERN = Pattern.compile("[0-9]{6}");
    // 서버가 생성한 UUID 이미지 파일명만 허용하는 패턴
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png)"
    );

    // 실행 환경에 따라 로컬 또는 S3로 연결되는 이미지 저장소
    private final FileStorage fileStorage;

    /**
     * 공개 이미지 조회에 사용할 파일 저장소를 구성한다.
     *
     * @author SeungHyeon.Kang
     * @param fileStorage 실행 환경에 맞는 이미지 저장소
     */
    public FileResourceController(FileStorage fileStorage) {

        // 검증된 공개 이미지 조회에 사용할 저장소를 보관한다
        this.fileStorage = fileStorage;
    }

    /**
     * 기존 업로드 URL 계약을 유지하면서 로컬 또는 S3 이미지를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param directory 프로필 또는 배경 이미지 디렉터리
     * @param uploadDate yyMMdd 형식의 업로드 날짜
     * @param storedName UUID 형식의 저장 파일명
     * @return 이미지 바이트 응답, 경로 또는 객체가 없으면 404 응답
     * @throws IOException 저장소 조회 실패 시 발생
     */
    @GetMapping("/uploads/{directory}/{uploadDate}/{storedName}")
    @Operation(summary = "업로드 이미지 조회", description = "검증된 공개 경로로 비공개 저장소의 프로필 또는 배경 이미지를 조회한다.")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "이미지 유형 디렉터리", example = "profile") @PathVariable String directory
          , @Parameter(description = "yyMMdd 형식의 업로드 날짜", example = "260807") @PathVariable String uploadDate
          , @Parameter(description = "UUID와 확장자로 구성된 저장 파일명", example = "123e4567-e89b-12d3-a456-426614174000.png") @PathVariable String storedName
    ) throws IOException {

        // 허용된 이미지 유형과 서버 생성 경로가 아니면 저장소 조회 전에 차단한다
        if (!ALLOWED_DIRECTORIES.contains(directory)
                || !UPLOAD_DATE_PATTERN.matcher(uploadDate).matches()
                || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            // 허용되지 않은 공개 경로에 404 응답을 반환한다
            return ResponseEntity.notFound().build();
        }

        // 검증된 세 경로 구간만 사용하여 저장소 객체 키를 구성한다
        String objectKey = directory + "/" + uploadDate + "/" + storedName;
        // 실행 환경에 연결된 저장소에서 공개 이미지 객체를 조회한다
        Optional<StoredFile> storedFile = fileStorage.getFile(objectKey);

        // 저장소에 객체가 없으면 파일 경로 노출 없이 404 응답을 반환한다
        if (storedFile.isEmpty()) {
            // 존재하지 않는 이미지 조회 결과를 반환한다
            return ResponseEntity.notFound().build();
        }

        // 저장된 MIME 유형이 없으면 안전한 바이너리 기본 유형을 사용한다
        MediaType mediaType = StringUtil.isEmpty(storedFile.get().contentType())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(storedFile.get().contentType());
        // UUID 파일은 내용이 변경되지 않으므로 장기 브라우저 캐시와 함께 이미지 바이트를 반환한다
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.get().bytes().length)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(storedFile.get().bytes());
    }
}
