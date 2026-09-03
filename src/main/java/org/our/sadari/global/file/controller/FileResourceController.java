package org.our.sadari.global.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.mapper.FileMapper;
import org.our.sadari.global.file.service.FileService;
import org.our.sadari.global.file.storage.FileStorage;
import org.our.sadari.global.file.storage.StoredFile;
import org.our.sadari.social.service.UserBlockService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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
 * 2026-08-26        SeungHyeon.Kang         이미지 재검증 캐시 적용
 * 2026-08-26        SeungHyeon.Kang         배경사진 화면용 파생본 제공
 * 2026-09-03        HanWon.Jang              이미지 직접 경로의 사용자 차단 검증 추가
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
    // 활성 계정 검증 뒤 브라우저 저장본을 조건부 재사용하는 캐시 정책
    private static final CacheControl REVALIDATED_CACHE_CONTROL =
            CacheControl.noCache().cachePrivate().mustRevalidate();

    // 실행 환경에 따라 로컬 또는 S3로 연결되는 이미지 저장소
    private final FileStorage fileStorage;
    // 활성 회원의 현재 프로필 또는 배경 파일인지 확인할 데이터 접근 객체
    private final FileMapper fileMapper;
    // 배경사진 화면용 파생본을 생성하고 조회할 파일 업무 서비스
    private final FileService fileService;
    // 이미지 소유자와 요청자의 양방향 차단 관계 조회 서비스
    private final UserBlockService userBlockService;

    /**
     * 공개 이미지 조회에 사용할 파일 저장소를 구성한다.
     *
     * @author SeungHyeon.Kang
     * @param fileStorage 실행 환경에 맞는 이미지 저장소
     * @param fileMapper 공개 이미지 참조 상태를 조회할 데이터 접근 객체
     * @param fileService 화면용 배경사진 파생본을 처리할 파일 업무 서비스
     * @param userBlockService 이미지 직접 경로의 사용자 차단 검증 서비스
     */
    public FileResourceController(FileStorage fileStorage, FileMapper fileMapper, FileService fileService
                                , UserBlockService userBlockService) {

        // 검증된 공개 이미지 조회에 사용할 저장소를 보관한다
        this.fileStorage = fileStorage;
        // 활성 회원의 현재 이미지인지 검증할 데이터 접근 객체를 보관한다
        this.fileMapper = fileMapper;
        // 일반 화면 요청에서 화면용 배경사진을 생성하거나 재사용할 서비스를 보관한다
        this.fileService = fileService;
        // 직접 경로로 접근한 사용자와 이미지 소유자의 차단 관계를 검증할 서비스를 보관한다
        this.userBlockService = userBlockService;
    }

    /**
     * 기존 업로드 URL 계약을 유지하면서 로컬 또는 S3 이미지를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param directory 프로필 또는 배경 이미지 디렉터리
     * @param uploadDate yyMMdd 형식의 업로드 날짜
     * @param storedName UUID 형식의 저장 파일명
     * @param variant 원본 또는 일반 화면용 파생본을 구분하는 값
     * @param ifNoneMatch 브라우저가 보관한 이미지의 조건부 요청 ETag
     * @param userNumb 이미지 조회를 요청한 로그인 사용자 번호
     * @return 이미지 바이트 응답, 경로 또는 객체가 없으면 404 응답
     * @throws IOException 저장소 조회 실패 시 발생
     */
    @GetMapping("/uploads/{directory}/{uploadDate}/{storedName}")
    @Operation(summary = "업로드 이미지 조회", description = "검증된 공개 경로로 비공개 저장소의 프로필 또는 배경 이미지를 조회한다.")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "이미지 유형 디렉터리", example = "profile") @PathVariable String directory
          , @Parameter(description = "yyMMdd 형식의 업로드 날짜", example = "260807") @PathVariable String uploadDate
          , @Parameter(description = "UUID와 확장자로 구성된 저장 파일명", example = "123e4567-e89b-12d3-a456-426614174000.png") @PathVariable String storedName
          , @Parameter(description = "배경사진 화면용 파생본", example = "display") @RequestParam(required = false) String variant
          , @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch
          , @Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
    ) throws IOException {

        // 허용된 이미지 유형과 서버 생성 경로가 아니면 저장소 조회 전에 차단한다
        if (!ALLOWED_DIRECTORIES.contains(directory)
                || !UPLOAD_DATE_PATTERN.matcher(uploadDate).matches()
                || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            // 허용되지 않은 공개 경로에 404 응답을 반환한다
            return ResponseEntity.notFound().build();
        }

        // display 파생본은 배경사진 경로에만 허용하고 알 수 없는 변형 요청은 차단한다
        boolean isDisplayVariant = "display".equals(variant);

        if (!StringUtil.isEmpty(variant) && (!isDisplayVariant || !"background".equals(directory))) {
            // 지원하지 않는 파생본 요청을 파일 부재와 같은 응답으로 처리한다
            return ResponseEntity.notFound().build();
        }

        // 검증된 세 경로 구간만 사용하여 저장소 객체 키를 구성한다
        String objectKey = directory + "/" + uploadDate + "/" + storedName;
        // DB 메타정보에 저장된 기존 공개 URL 형식으로 참조 경로를 구성한다
        String filePath = "/uploads/" + objectKey;

        // 활성 회원의 현재 프로필 또는 배경으로 참조되는 이미지 소유자를 조회한다
        Long fileOwnerNumb = fileMapper.getActivePublicFileOwner(storedName, filePath);

        // 현재 공개 이미지가 아니거나 요청자와 소유자가 격리되었으면 저장소 조회 전에 차단한다
        if (StringUtil.isEmpty(fileOwnerNumb) || userBlockService.isBlocked(userNumb, fileOwnerNumb)) {
            // 탈퇴 또는 삭제 대기 회원의 이전 이미지와 미참조 파일을 공개하지 않는다
            return ResponseEntity.notFound().build();
        }

        // UUID 파일명은 같은 URL의 내용이 변경되지 않으므로 저장소 조회 전 조건부 요청 식별자로 사용한다
        String entityTagSource = isDisplayVariant ? fileService.getBgDisplayTag(storedName) : storedName;
        String entityTag = "\"" + entityTagSource + "\"";

        // 활성 공개 상태를 확인한 동일 이미지이면 S3 원본 전송 없이 브라우저 저장본을 재사용한다
        if (entityTag.equals(ifNoneMatch) || ("W/" + entityTag).equals(ifNoneMatch)) {
            // 활성 계정 검증이 끝난 이미지의 브라우저 저장본 사용을 허용한다
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(entityTag)
                    .cacheControl(REVALIDATED_CACHE_CONTROL)
                    .build();
        }

        // 실행 환경에 연결된 저장소에서 공개 이미지 객체를 조회한다
        Optional<StoredFile> storedFile = isDisplayVariant
                ? fileService.getBgDisplayFile(objectKey)
                : fileStorage.getFile(objectKey);

        // 저장소에 객체가 없으면 파일 경로 노출 없이 404 응답을 반환한다
        if (storedFile.isEmpty()) {
            // 존재하지 않는 이미지 조회 결과를 반환한다
            return ResponseEntity.notFound().build();
        }

        // 저장된 MIME 유형이 없으면 안전한 바이너리 기본 유형을 사용한다
        MediaType mediaType = StringUtil.isEmpty(storedFile.get().contentType())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(storedFile.get().contentType());
        // 브라우저가 이미지를 저장하되 다음 사용 전 활성 계정 상태를 서버에 재검증하도록 반환한다
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.get().bytes().length)
                .eTag(entityTag)
                .cacheControl(REVALIDATED_CACHE_CONTROL)
                .body(storedFile.get().bytes());
    }
}
