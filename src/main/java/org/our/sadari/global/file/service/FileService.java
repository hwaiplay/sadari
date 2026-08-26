package org.our.sadari.global.file.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.dto.FileDto;
import org.our.sadari.global.file.dto.ProfileImageDraftDto;
import org.our.sadari.global.file.exception.InvalidImageFileException;
import org.our.sadari.global.file.mapper.FileMapper;
import org.our.sadari.global.file.storage.FileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : FileService
 * author         : HanWon.Jang
 * date           : 2026-07-14
 * description    : 이미지 파일 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-14        SeungHyeon.Kang    최초 생성
 * 2026-08-06        SeungHyeon.Kang    이미지 저장·정규화 처리 추가
 * 2026-08-07        SeungHyeon.Kang    영구 이미지 저장소를 로컬 또는 S3 구현으로 분리
 * 2026-08-26        HanWon.Jang         공용 HTTP 클라이언트 적용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    // 업로드 루트 디렉터리 설정값
    // 업로드 접근 접두사 설정값
    private static final String UPLOAD_ACCESS_PREFIX = "/uploads/";
    // 업로드 일자 디렉터리 형식
    private static final DateTimeFormatter UPLOAD_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    // 기본 이미지 EXTENSION 설정값
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    // PNG 시그니처 설정값
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    // JPEG APP1 메타정보 구간을 식별하는 마커값
    private static final int JPEG_APP1_MARKER = 0xE1;
    // JPEG 이미지 데이터 시작을 식별하는 마커값
    private static final int JPEG_START_OF_SCAN_MARKER = 0xDA;
    // JPEG 이미지 종료를 식별하는 마커값
    private static final int JPEG_END_OF_IMAGE_MARKER = 0xD9;
    // TIFF 메타정보에서 이미지 방향을 식별하는 태그값
    private static final int EXIF_ORIENTATION_TAG = 0x0112;
    // EXIF APP1 구간의 고정 헤더값
    private static final byte[] EXIF_HEADER = {'E', 'x', 'i', 'f', 0x00, 0x00};
    // 프로필 이미지 임시 저장 루트 디렉터리 설정값
    private static final String PROFILE_IMAGE_DRAFT_ROOT_DIR = "profile-image-drafts";
    // 임시 이미지가 유지되는 최대 시간
    private static final Duration PROFILE_IMAGE_DRAFT_TTL = Duration.ofMinutes(30);
    // 프로필 미리보기 한 변의 최대 길이
    private static final int PROFILE_PREVIEW_MAX_EDGE = 512;
    // 배경 미리보기 한 변의 최대 길이
    private static final int BACKGROUND_PREVIEW_MAX_EDGE = 1600;
    // 임시 원본 파일명 구분값
    private static final String DRAFT_ORIGINAL_MARKER = ".original";
    // 임시 미리보기 파일명 구분값
    private static final String DRAFT_PREVIEW_MARKER = ".preview";

    // File 데이터 접근 객체
    private final FileMapper fileMapper;

    // 실행 환경에 따라 로컬 또는 S3로 연결되는 영구 이미지 저장소
    private final FileStorage fileStorage;

    // 외부 프로필 이미지 조회에 공통 타임아웃을 적용하는 HTTP 클라이언트
    private final RestTemplate restTemplate;

    // 파일 저장과 안전한 삭제의 기준이 되는 업로드 루트 경로
    // 공개 업로드 경로와 분리된 프로필 이미지 임시 저장 루트 경로
    private Path profileImageDraftRootPath = Paths.get(PROFILE_IMAGE_DRAFT_ROOT_DIR).toAbsolutePath().normalize();

    // 업로드 가능한 이미지 최대 바이트 크기
    @Value("${app.upload.max-image-bytes:10485760}")
    private long maxImageBytes;

    // 업로드 가능한 이미지 최대 픽셀 수
    @Value("${app.upload.max-image-pixels:20000000}")
    private long maxImagePixels;

    // 업로드 가능한 이미지 한 변의 최대 길이
    @Value("${app.upload.max-image-dimension:8192}")
    private int maxImageDimension;

    /**
     * 사용자가 업로드한 이미지 파일을 프로젝트 내부 저장소에 저장하고 파일 번호를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param imageFile 저장할 이미지 파일
     * @param imageType 프로필 사진인지 배경사진인지 구분하는 파일 타입
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호, 업로드 파일이 없으면 null
     * @throws IOException 파일 저장 중 오류가 발생한 경우
     */
    public Long setUploadedImage(MultipartFile imageFile, String imageType, Long regiUser) throws IOException {
        // 수정 화면에서 파일을 변경하지 않은 경우 기존 파일 번호를 유지해야 하므로 신규 파일 등록을 건너뜁니다.
        if (StringUtil.isEmpty(imageFile) || imageFile.isEmpty()) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // validateImageType 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
        validateImageType(imageType);

        // getOriginalFilename 조회로 후속 처리에 필요한 데이터를 가져온다
        String originalName = normalizeOriginalName(imageFile.getOriginalFilename());
        // 암호화 또는 전송에 사용할 바이트 배열로 변환한다
        ValidatedImage validatedImage = validateAndNormalizeImage(imageFile.getBytes());
        // 검증이 끝난 이미지 픽셀을 영구 저장하고 파일 번호를 반환한다
        return setValidatedImage(validatedImage, originalName, imageType, regiUser);
    }

    /**
     * 검증된 이미지 픽셀을 날짜별 영구 경로에 저장하고 파일 메타정보를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param validatedImage 서버 검증과 방향 보정을 마친 이미지
     * @param originalName 메타정보에 기록할 원본 파일명
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호
     * @throws IOException 파일 저장 중 오류가 발생한 경우
     */
    private Long setValidatedImage(ValidatedImage validatedImage, String originalName, String imageType
                                  , Long regiUser) throws IOException {
        // 검증된 이미지 형식에 대응하는 파일 확장자를 결정한다
        String storedName = createStoredFileName(validatedImage.extension());
        // 업로드 날짜를 저장 경로와 접근 경로에 동일하게 적용한다
        String uploadDate = getUploadDate();

        // 이미지 유형과 업로드 날짜에 맞는 저장 경로를 조회한다
        String objectKey = getObjectKey(imageType, uploadDate, storedName);
        // 파일 저장에 필요한 디렉터리를 생성한다
        // 기준 경로와 하위 경로를 결합한다
        boolean fileMetadataSaved = false;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            /*
             * 원본 바이트를 그대로 보관하면 이미지 뒤에 붙인 실행 파일이나 메타데이터도 함께 저장될 수 있다.
             * 디코딩한 픽셀을 새 JPG/PNG로 재인코딩한 결과만 신규 파일로 생성해 검증되지 않은 데이터를 제거한다.
             */
            fileStorage.setFile(objectKey, validatedImage.bytes(), validatedImage.mimeType());

            // 업로드 파일의 저장 정보를 담을 객체를 생성한다
            FileDto fileDto = new FileDto();
            // OrigName 업무 값을 fileDto DTO에 설정한다
            fileDto.setOrigName(originalName);
            // StorName 업무 값을 fileDto DTO에 설정한다
            fileDto.setStorName(storedName);
            // 날짜 디렉터리가 포함된 브라우저 접근 경로를 설정한다
            fileDto.setFilePath(getAccessPrefix(imageType, uploadDate) + storedName);
            // FileSize 업무 값을 fileDto DTO에 설정한다
            fileDto.setFileSize((long) validatedImage.bytes().length);
            // MimeType 업무 값을 fileDto DTO에 설정한다
            fileDto.setMimeType(validatedImage.mimeType());
            // RegiUser 업무 값을 fileDto DTO에 설정한다
            fileDto.setRegiUser(regiUser);
            // File 업무 값을 fileMapper DTO에 설정한다
            int insertCnt = fileMapper.setFile(fileDto);

            // fileDto.getFileNumb( 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
            if (insertCnt != 1 || StringUtil.isEmpty(fileDto.getFileNumb())) {

                throw new IOException("File metadata could not be saved.");
            }

            /*
             * 뒤에서 사용자 프로필 UPDATE가 실패하면 DB 메타정보는 트랜잭션으로 롤백되지만 실제 파일은 자동 복구되지 않는다.
             * 현재 트랜잭션의 최종 상태를 확인해 롤백 시 물리 파일까지 함께 제거하도록 정리 작업을 예약한다.
             */
            registerRollbackCleanup(objectKey);
            fileMetadataSaved = true;
            // 사용자가 업로드한 이미지 파일을 프로젝트 내부 저장소에 저장하고 파일 번호를 반환한 결과를 반환한다
            return fileDto.getFileNumb();
        }

        // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
        finally {
            // DB 메타정보 저장 전에 실패한 파일을 남기면 접근되지 않는 파일이 누적되므로 즉시 정리한다.
            if (!fileMetadataSaved) {
                // 검증 중 생성된 임시 파일이 있으면 삭제한다
                fileStorage.delFile(objectKey);
            }
        }
    }

    /**
     * Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환한다.
     *
     * @author HanWon.Jang
     * @param profileImageUrl Kakao 프로필 이미지 URL
     * @param userIdxx Kakao 사용자 식별값
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호, URL이 없으면 null
     */
    public Long setKakaoProfileImage(String profileImageUrl, String userIdxx, Long regiUser) {
        // Kakao 계정에 프로필 이미지가 없을 수 있으므로 회원 생성 흐름은 계속 진행한다.
        if (StringUtil.isEmpty(profileImageUrl)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // getForEntity 조회로 후속 처리에 필요한 데이터를 가져온다
            ResponseEntity<byte[]> response = restTemplate.getForEntity(URI.create(profileImageUrl), byte[].class);
            // getBody 조회로 후속 처리에 필요한 데이터를 가져온다
            byte[] imageBytes = response.getBody();

            // 응답 본문이 비어 있으면 원본 URL을 파일 경로로 등록해 프로필 표시 자체는 가능하게 한다.
            if (StringUtil.isEmpty(imageBytes) || imageBytes.length == 0) {
                // Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환한 결과를 반환한다
                return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
            }

            // validateAndNormalizeImage 검증으로 잘못된 요청이 업무 로직에 진입하지 않도록 차단한다
            ValidatedImage validatedImage = validateAndNormalizeImage(imageBytes);
            // 검증된 이미지 형식에 대응하는 파일 확장자를 결정한다
            String storedName = createStoredFileName(validatedImage.extension());
            // 업로드 날짜를 저장 경로와 접근 경로에 동일하게 적용한다
            String uploadDate = getUploadDate();
            // 프로필 이미지 유형과 업로드 날짜에 맞는 저장 경로를 조회한다
            String objectKey = getObjectKey(Constant.FILE_TYPE_PROFILE, uploadDate, storedName);
            // 파일 저장에 필요한 디렉터리를 생성한다
            // 기준 경로와 하위 경로를 결합한다
            boolean fileMetadataSaved = false;

            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try {
                // 검증된 업로드 파일을 저장 경로에 기록한다
                fileStorage.setFile(objectKey, validatedImage.bytes(), validatedImage.mimeType());

                // 업로드 파일의 저장 정보를 담을 객체를 생성한다
                FileDto fileDto = new FileDto();
                // OrigName 업무 값을 fileDto DTO에 설정한다
                fileDto.setOrigName("kakao-profile-" + userIdxx);
                // StorName 업무 값을 fileDto DTO에 설정한다
                fileDto.setStorName(storedName);
                // 날짜 디렉터리가 포함된 브라우저 접근 경로를 설정한다
                fileDto.setFilePath(getAccessPrefix(Constant.FILE_TYPE_PROFILE, uploadDate) + storedName);
                // FileSize 업무 값을 fileDto DTO에 설정한다
                fileDto.setFileSize((long) validatedImage.bytes().length);
                // MimeType 업무 값을 fileDto DTO에 설정한다
                fileDto.setMimeType(validatedImage.mimeType());
                // RegiUser 업무 값을 fileDto DTO에 설정한다
                fileDto.setRegiUser(regiUser);
                // File 업무 값을 fileMapper DTO에 설정한다
                int insertCnt = fileMapper.setFile(fileDto);

                // fileDto.getFileNumb( 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
                if (insertCnt != 1 || StringUtil.isEmpty(fileDto.getFileNumb())) {

                    throw new IOException("Kakao profile file metadata could not be saved.");
                }

                // DB 저장이 롤백되면 업로드 파일도 제거되도록 정리 작업을 등록한다
                registerRollbackCleanup(objectKey);
                fileMetadataSaved = true;
                // Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환한 결과를 반환한다
                return fileDto.getFileNumb();
            }

            // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
            finally {
                // 메타정보 저장 전 실패한 외부 프로필 파일도 서버에 남기지 않는다.
                if (!fileMetadataSaved) {
                    // 검증 중 생성된 임시 파일이 있으면 삭제한다
                    fileStorage.delFile(objectKey);
                }
            }
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 외부 이미지 다운로드 실패가 로그인 실패로 이어지지 않도록 원본 URL을 대체 경로로 저장한다.
            log.warn("Kakao profile image download failed. userIdxx={}, message={}", userIdxx, e.getMessage());
            // Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환한 결과를 반환한다
            return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
        }
    }

    /**
     * 선택한 프로필 또는 배경 이미지를 비공개 임시 저장소에 보관하고 축소 미리보기를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param imageFile 사용자가 선택한 이미지
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @param userNumb 로그인 사용자 번호
     * @return 임시 저장 식별값과 서버 미리보기
     * @throws IOException 임시 파일 저장 중 오류가 발생한 경우
     */
    public ProfileImageDraftDto setProfileImageDraft(MultipartFile imageFile, String imageType
                                                    , Long userNumb) throws IOException {
        // 인증 정보와 파일이 없으면 사용자별 임시 저장을 시작하지 않는다
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(imageFile) || imageFile.isEmpty()) {
            throw new InvalidImageFileException("Profile image draft is empty.");
        }

        // 허용된 프로필과 배경 유형만 사용자 임시 경로로 변환한다
        validateImageType(imageType);
        // 서버에서 시그니처와 크기 및 EXIF 방향을 검증하고 픽셀을 정규화한다
        ValidatedImage validatedImage = validateAndNormalizeImage(imageFile.getBytes());
        // 화면 표시용 미리보기는 대상별 제한 크기로 별도 축소한다
        ValidatedImage previewImage = createPreviewImage(
                validatedImage,
                Constant.FILE_TYPE_PROFILE.equals(imageType)
                        ? PROFILE_PREVIEW_MAX_EDGE
                        : BACKGROUND_PREVIEW_MAX_EDGE
        );
        // 임시 파일명을 추측하기 어렵게 UUID 식별값을 생성한다
        String draftToken = UUID.randomUUID().toString();
        // 사용자와 이미지 유형별 전용 디렉터리를 계산한다
        Path draftDirectory = getProfileDraftDir(userNumb, imageType);
        // 새 임시 원본과 미리보기를 기록할 경로를 생성한다
        Path originalPath = draftDirectory.resolve(getDraftFileName(
                draftToken,
                DRAFT_ORIGINAL_MARKER,
                validatedImage.extension()
        ));
        Path previewPath = draftDirectory.resolve(getDraftFileName(
                draftToken,
                DRAFT_PREVIEW_MARKER,
                previewImage.extension()
        ));
        boolean draftSaved = false;

        try {
            // 사용자별 비공개 임시 디렉터리를 생성한다
            Files.createDirectories(draftDirectory);
            // 검증과 방향 보정을 마친 원본 픽셀을 최종 저장 전까지 보관한다
            Files.write(originalPath, validatedImage.bytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            // 모바일 화면에서 원본을 디코딩하지 않도록 축소 미리보기를 별도 기록한다
            Files.write(previewPath, previewImage.bytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            // 두 파일이 모두 준비된 뒤 이전 선택본을 제거해 실패 시 기존 미리보기를 유지한다
            delOtherProfileDrafts(draftDirectory, draftToken);
            draftSaved = true;
            // 같은 로그인 사용자가 앱을 다시 열어도 만료 전 선택본을 복원할 응답을 반환한다
            return createProfileDraftDto(imageType, draftToken, previewPath, previewImage.mimeType(), originalPath);
        }

        finally {
            // 임시 저장 도중 실패한 신규 파일은 다음 요청에 노출되지 않게 즉시 정리한다
            if (!draftSaved) {
                Files.deleteIfExists(originalPath);
                Files.deleteIfExists(previewPath);
            }
        }
    }

    /**
     * 로그인 사용자의 만료되지 않은 프로필과 배경 임시 이미지를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 복원 가능한 임시 이미지 목록
     */
    public List<ProfileImageDraftDto> getProfileImageDraftList(Long userNumb) {
        // 인증 사용자 번호가 없으면 다른 사용자의 임시 경로를 조회하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            return List.of();
        }

        // 프로필과 배경 임시 선택본을 각각 조회해 만료되지 않은 값만 반환한다
        return Stream.of(Constant.FILE_TYPE_PROFILE, Constant.FILE_TYPE_BACKGROUND)
                .map(imageType -> getProfileImageDraft(userNumb, imageType))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    /**
     * 임시 이미지 식별값을 로그인 사용자와 유형에 맞춰 영구 파일로 승격한다.
     *
     * @author SeungHyeon.Kang
     * @param draftToken 임시 이미지 식별값
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @param userNumb 로그인 사용자 번호
     * @return 신규 영구 파일 번호, 식별값이 없으면 null
     * @throws IOException 파일 저장 중 오류가 발생한 경우
     */
    public Long setUploadedImageDraft(String draftToken, String imageType, Long userNumb) throws IOException {
        // 이미지가 변경되지 않은 저장 요청은 기존 파일 번호를 유지한다
        if (StringUtil.isEmpty(draftToken)) {
            return null;
        }

        // 사용자와 이미지 유형 및 UUID 식별값을 모두 검증한다
        validateImageType(imageType);
        validateDraftToken(draftToken);
        Path draftDirectory = getProfileDraftDir(userNumb, imageType);
        Path originalPath = findDraftPath(draftDirectory, draftToken, DRAFT_ORIGINAL_MARKER);

        // 사용자 전용 경로에 없거나 보존 시간이 지난 임시 이미지는 저장에 사용하지 않는다
        if (StringUtil.isEmpty(originalPath) || isExpiredDraft(originalPath)) {
            delProfileImageDraft(userNumb, imageType);
            throw new InvalidImageFileException("Profile image draft expired.");
        }

        // 비공개 임시 원본의 실제 시그니처로 최종 저장 형식을 다시 확인한다
        byte[] imageBytes = Files.readAllBytes(originalPath);
        ImageFormat imageFormat = detectImageFormat(imageBytes);
        if (StringUtil.isEmpty(imageFormat)) {
            throw new InvalidImageFileException("Profile image draft format is invalid.");
        }

        ValidatedImage validatedImage = new ValidatedImage(
                imageBytes,
                imageFormat.mimeType(),
                imageFormat.extension()
        );
        // 날짜별 영구 저장과 DB 메타정보 등록을 완료한다
        Long fileNumb = setValidatedImage(validatedImage, "profile-image" + validatedImage.extension(), imageType, userNumb);
        // 사용자 프로필 UPDATE까지 커밋된 경우에만 재시도를 위한 임시 원본을 제거한다
        setDraftCleanupOnCommit(draftDirectory, draftToken);
        // 승격된 영구 파일 번호를 반환한다
        return fileNumb;
    }

    /**
     * 로그인 사용자의 특정 유형 임시 이미지를 모두 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageType 프로필 또는 배경 이미지 구분값
     */
    public void delProfileImageDraft(Long userNumb, String imageType) {
        // 안전한 사용자 및 이미지 유형 경로만 삭제 대상으로 허용한다
        if (StringUtil.isEmpty(userNumb)) {
            return;
        }

        validateImageType(imageType);
        delFilesInDirectory(getProfileDraftDir(userNumb, imageType));
    }

    /**
     * 로그아웃과 계정 상태 변경 시 로그인 사용자의 모든 임시 이미지를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 삭제할 사용자 번호
     */
    public void delAllProfileImageDrafts(Long userNumb) {
        // 사용자 번호가 없으면 임시 저장 루트에 접근하지 않는다
        if (StringUtil.isEmpty(userNumb)) {
            return;
        }

        // 프로필과 배경 임시 파일을 각각 안전한 하위 경로에서 제거한다
        delFilesInDirectory(getProfileDraftDir(userNumb, Constant.FILE_TYPE_PROFILE));
        delFilesInDirectory(getProfileDraftDir(userNumb, Constant.FILE_TYPE_BACKGROUND));
    }

    /**
     * 계정 상태 변경 트랜잭션이 커밋된 뒤 사용자의 모든 임시 이미지를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 삭제할 사용자 번호
     */
    public void delProfileDraftsOnCommit(Long userNumb) {
        // 트랜잭션 밖에서는 이미 확정된 상태에 맞춰 즉시 임시 이미지를 삭제한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delAllProfileImageDrafts(userNumb);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 계정 상태 변경이 커밋된 경우에만 사용자 임시 이미지를 삭제한다.
             *
             * @author SeungHyeon.Kang
             * @param status 트랜잭션 종료 상태
             */
            @Override
            public void afterCompletion(int status) {
                // 롤백된 계정 상태에서는 사용자가 편집을 계속할 수 있도록 임시 선택본을 유지한다
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    return;
                }

                // 확정된 비활성화 또는 영구 탈퇴 상태의 사용자 임시 이미지를 모두 삭제한다
                delAllProfileImageDrafts(userNumb);
            }
        });
    }

    /**
     * 보존 시간이 지난 모든 사용자 임시 이미지를 정리한다.
     *
     * @author SeungHyeon.Kang
     */
    public void delExpiredProfileDrafts() {
        // 임시 저장 루트가 없으면 정리 작업을 종료한다
        if (!Files.isDirectory(profileImageDraftRootPath)) {
            return;
        }

        try (Stream<Path> pathStream = Files.walk(profileImageDraftRootPath)) {
            // 파일 수정 시각이 보존 시간을 지난 임시 파일만 삭제한다
            pathStream.filter(Files::isRegularFile)
                    .filter(this::isExpiredDraft)
                    .forEach(this::delDraftFileSafely);
        }

        catch (IOException e) {
            // 정기 정리 실패는 다음 실행에서 재시도할 수 있도록 운영 로그에 남긴다
            log.error("Expired profile image draft cleanup failed.", e);
        }
    }

    /**
     * 파일 등록 사용자 번호로 영구 탈퇴 시 삭제할 파일 메타정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param regiUser 파일을 등록한 사용자 번호
     * @return 사용자가 등록한 파일 메타정보 목록
     */
    public List<FileDto> getFileListByRegiUser(Long regiUser) {
        // 사용자 번호가 없으면 다른 사용자의 파일을 잘못 조회하지 않도록 빈 목록을 반환한다
        if (StringUtil.isEmpty(regiUser)) {
            // 영구 삭제할 파일이 없는 상태를 반환한다
            return List.of();
        }

        // 영구 탈퇴 트랜잭션이 삭제하기 전에 사용자가 등록한 파일 정보를 조회한다
        List<FileDto> fileList = fileMapper.getFileListByRegiUser(regiUser);

        // Mapper가 Null을 반환해도 영구 탈퇴 반복 처리가 중단되지 않도록 빈 목록으로 보정한다
        if (StringUtil.isEmpty(fileList)) {
            // 영구 삭제할 파일이 없는 상태를 반환한다
            return List.of();
        }

        // 영구 탈퇴 대상 사용자가 등록한 파일 메타정보 목록을 반환한다
        return fileList;
    }

    /**
     * 사용자 프로필과 배경에서 더 이상 참조하지 않는 파일 메타정보를 삭제하고 커밋 후 물리 파일을 정리한다.
     *
     * @author SeungHyeon.Kang
     * @param fileNumb 교체 전 파일 번호
     */
    public void delFile(Long fileNumb) {
        // 교체 전 파일 번호가 없으면 기존 파일 정리 대상이 아니므로 종료한다
        if (StringUtil.isEmpty(fileNumb)) {
            // 기존 파일 정리를 종료한다
            return;
        }

        // 물리 파일 삭제 경로를 커밋 이후에도 사용할 수 있도록 메타정보를 먼저 조회한다
        FileDto fileDto = fileMapper.getFileByNumb(fileNumb);

        // 이미 정리된 파일 번호이면 중복 삭제를 성공 상태로 처리한다
        if (StringUtil.isEmpty(fileDto)) {
            // 중복 파일 정리를 종료한다
            return;
        }

        // 다른 프로필이나 배경에서 참조하지 않는 파일 메타정보만 삭제한다
        int deleteCnt = fileMapper.delFileIfUnreferenced(fileNumb);

        // 다른 이미지 컬럼이 같은 파일을 참조하면 해당 참조가 교체될 때까지 물리 파일을 유지한다
        if (deleteCnt != 1) {
            // 아직 참조 중인 파일 정리를 종료한다
            return;
        }

        // DB 참조와 메타정보 삭제가 커밋된 뒤에만 교체 전 물리 파일을 삭제한다
        registerCommitCleanup(List.of(fileDto));
    }

    /**
     * 영구 탈퇴 트랜잭션이 커밋된 뒤 사용자가 등록한 물리 파일을 모두 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param fileList 영구 탈퇴 전에 조회한 파일 메타정보 목록
     */
    public void delFilesAfterCommit(List<FileDto> fileList) {
        // 영구 탈퇴 대상 파일이 없으면 커밋 후 정리 작업을 등록하지 않는다
        if (StringUtil.isEmpty(fileList)) {
            // 영구 탈퇴 물리 파일 정리를 종료한다
            return;
        }

        // 회원과 파일 메타정보 삭제가 커밋된 뒤 물리 파일을 제거하도록 정리 작업을 등록한다
        registerCommitCleanup(List.copyOf(fileList));
    }

    /**
     * 외부 이미지 URL 자체를 파일 경로로 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param imageUrl 외부 이미지 URL
     * @param ownerKey 파일 소유자를 식별할 수 있는 값
     * @param imageType 이미지 파일 타입
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호
     */
    private Long setExternalImage(String imageUrl, String ownerKey, String imageType
                                , Long regiUser) {
        // 업로드 파일의 저장 정보를 담을 객체를 생성한다
        FileDto fileDto = new FileDto();
        // OrigName 업무 값을 fileDto DTO에 설정한다
        fileDto.setOrigName(imageType.toLowerCase() + "-" + ownerKey);
        // StorName 업무 값을 fileDto DTO에 설정한다
        fileDto.setStorName(imageType.toLowerCase() + "-" + ownerKey);
        // FilePath 업무 값을 fileDto DTO에 설정한다
        fileDto.setFilePath(imageUrl);
        // MimeType 업무 값을 fileDto DTO에 설정한다
        fileDto.setMimeType("image/*");
        // RegiUser 업무 값을 fileDto DTO에 설정한다
        fileDto.setRegiUser(regiUser);
        // File 업무 값을 fileMapper DTO에 설정한다
        fileMapper.setFile(fileDto);
        // 외부 이미지 URL 자체를 파일 경로로 저장한 결과를 반환한다
        return fileDto.getFileNumb();
    }

    /**
     * 이미지 타입과 업로드 날짜 및 저장 파일명으로 저장소 객체 키를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 이미지 파일 타입
     * @param uploadDate yyMMdd 형식의 업로드 날짜
     * @param storedName 서버가 생성한 저장 파일명
     * @return 저장소에서 사용할 상대 객체 키
     */
    private String getObjectKey(String imageType, String uploadDate, String storedName) {
        // 이미지 유형 아래에 업로드 날짜와 파일명을 포함한 객체 키를 반환한다
        return getUploadDirectoryName(imageType) + "/" + uploadDate + "/" + storedName;
    }

    /**
     * 이미지 타입에 맞는 브라우저 접근 URL prefix를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 이미지 파일 타입
     * @param uploadDate yyMMdd 형식의 업로드 날짜
     * @return 브라우저 접근 URL prefix
     */
    private String getAccessPrefix(String imageType, String uploadDate) {
        // 이미지 유형과 업로드 날짜가 포함된 브라우저 접근 경로 접두사를 반환한다
        return UPLOAD_ACCESS_PREFIX + getUploadDirectoryName(imageType) + "/" + uploadDate + "/";
    }

    /**
     * 현재 서버 날짜를 이미지 저장 디렉터리 형식으로 반환한다.
     *
     * @author SeungHyeon.Kang
     * @return yyMMdd 형식의 업로드 날짜
     */
    private String getUploadDate() {
        // 동일한 날짜 기준으로 저장 경로와 접근 경로를 구성할 업로드 날짜를 반환한다
        return LocalDate.now().format(UPLOAD_DATE_FORMATTER);
    }

    /**
     * 이미지 타입을 실제 저장 디렉터리명으로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 이미지 파일 타입
     * @return 저장 디렉터리명
     */
    private String getUploadDirectoryName(String imageType) {
        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (Constant.FILE_TYPE_BACKGROUND.equals(imageType)) {
            // 이미지 타입을 실제 저장 디렉터리명으로 변환한 결과를 반환한다
            return "background";
        }

        // 이미지 타입을 실제 저장 디렉터리명으로 변환한 결과를 반환한다
        return "profile";
    }

    /**
     * 서버가 검증해 결정한 확장자로 충돌 없는 저장 파일명을 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param extension 검증된 이미지 확장자
     * @return UUID 기반 저장 파일명
     */
    private String createStoredFileName(String extension) {
        // 서버가 검증해 결정한 확장자로 충돌 없는 저장 파일명을 생성한 결과를 반환한다
        return UUID.randomUUID() + extension;
    }

    /**
     * 사용자와 이미지 유형에 제한된 비공개 임시 저장 경로를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @return 임시 이미지 전용 디렉터리
     */
    private Path getProfileDraftDir(Long userNumb, String imageType) {
        // 숫자 사용자 번호와 고정 유형 디렉터리만 조합해 경로 주입을 차단한다
        Path draftDirectory = profileImageDraftRootPath
                .resolve(String.valueOf(userNumb))
                .resolve(getUploadDirectoryName(imageType))
                .normalize();

        // 계산된 경로가 임시 저장 루트를 벗어나면 파일 시스템 접근을 차단한다
        if (!draftDirectory.startsWith(profileImageDraftRootPath)) {
            throw new InvalidImageFileException("Profile image draft path is invalid.");
        }

        // 사용자별 이미지 유형 임시 디렉터리를 반환한다
        return draftDirectory;
    }

    /**
     * 임시 이미지 식별값과 용도 및 확장자를 안전한 파일명으로 결합한다.
     *
     * @author SeungHyeon.Kang
     * @param draftToken UUID 임시 이미지 식별값
     * @param marker 원본 또는 미리보기 구분값
     * @param extension 서버 검증 이미지 확장자
     * @return 임시 저장 파일명
     */
    private String getDraftFileName(String draftToken, String marker, String extension) {
        // 서버가 생성하고 검증한 값만 조합한 임시 파일명을 반환한다
        return draftToken + marker + extension;
    }

    /**
     * 클라이언트가 전달한 임시 이미지 식별값이 UUID 형식인지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param draftToken 검증할 임시 이미지 식별값
     */
    private void validateDraftToken(String draftToken) {
        // 경로 문자나 임의 파일명을 사용할 수 없도록 UUID 파싱 결과만 허용한다
        try {
            UUID.fromString(draftToken);
        }

        catch (IllegalArgumentException e) {
            throw new InvalidImageFileException("Profile image draft token is invalid.", e);
        }
    }

    /**
     * 특정 임시 이미지 식별값에 대응하는 원본 또는 미리보기 파일을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param draftDirectory 사용자별 임시 디렉터리
     * @param draftToken 임시 이미지 식별값
     * @param marker 원본 또는 미리보기 구분값
     * @return 일치하는 임시 파일 경로, 없으면 null
     */
    private Path findDraftPath(Path draftDirectory, String draftToken, String marker) {
        // 사용자별 임시 디렉터리가 없으면 복원할 파일이 없다
        if (!Files.isDirectory(draftDirectory)) {
            return null;
        }

        try (Stream<Path> pathStream = Files.list(draftDirectory)) {
            // UUID와 용도 구분값이 일치하는 서버 생성 파일만 조회한다
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(draftToken + marker))
                    .findFirst()
                    .orElse(null);
        }

        catch (IOException e) {
            // 임시 저장소 조회 실패는 일반 파일 저장 실패 흐름으로 전환한다
            throw new InvalidImageFileException("Profile image draft could not be read.", e);
        }
    }

    /**
     * 사용자와 이미지 유형에 남은 최신 임시 선택본을 복원한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @return 복원할 임시 이미지, 없으면 null
     */
    private ProfileImageDraftDto getProfileImageDraft(Long userNumb, String imageType) {
        Path draftDirectory = getProfileDraftDir(userNumb, imageType);

        // 임시 디렉터리가 없으면 복원할 선택본이 없다
        if (!Files.isDirectory(draftDirectory)) {
            return null;
        }

        try (Stream<Path> pathStream = Files.list(draftDirectory)) {
            Path originalPath = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains(DRAFT_ORIGINAL_MARKER))
                    .max(Comparator.comparingLong(this::getLastModifiedMillis))
                    .orElse(null);

            // 원본이 없거나 만료되었으면 해당 유형의 남은 임시 파일을 정리한다
            if (StringUtil.isEmpty(originalPath) || isExpiredDraft(originalPath)) {
                delFilesInDirectory(draftDirectory);
                return null;
            }

            String fileName = originalPath.getFileName().toString();
            String draftToken = fileName.substring(0, fileName.indexOf(DRAFT_ORIGINAL_MARKER));
            Path previewPath = findDraftPath(draftDirectory, draftToken, DRAFT_PREVIEW_MARKER);

            // 원본과 한 쌍인 미리보기가 없으면 불완전한 임시 선택본을 제거한다
            if (StringUtil.isEmpty(previewPath)) {
                delFilesInDirectory(draftDirectory);
                return null;
            }

            // 미리보기 시그니처로 Data URL MIME 형식을 결정한다
            byte[] previewBytes = Files.readAllBytes(previewPath);
            ImageFormat previewFormat = detectImageFormat(previewBytes);
            if (StringUtil.isEmpty(previewFormat)) {
                delFilesInDirectory(draftDirectory);
                return null;
            }

            // 같은 로그인 사용자에게만 서버 미리보기와 만료 시각을 반환한다
            return createProfileDraftDto(
                    imageType,
                    draftToken,
                    previewPath,
                    previewFormat.mimeType(),
                    originalPath
            );
        }

        catch (IOException e) {
            // 복원 실패는 프로필 본문 조회를 방해하지 않고 운영 로그에 남긴다
            log.warn("Profile image draft restore failed. userNumb={}, imageType={}", userNumb, imageType, e);
            return null;
        }
    }

    /**
     * 임시 이미지 응답 객체를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 프로필 또는 배경 이미지 구분값
     * @param draftToken 임시 이미지 식별값
     * @param previewPath 축소 미리보기 경로
     * @param previewMimeType 미리보기 MIME 형식
     * @param originalPath 만료 시각 기준이 되는 임시 원본 경로
     * @return 임시 이미지 응답
     * @throws IOException 미리보기 파일을 읽을 수 없는 경우
     */
    private ProfileImageDraftDto createProfileDraftDto(String imageType, String draftToken
                                                            , Path previewPath, String previewMimeType
                                                            , Path originalPath) throws IOException {
        ProfileImageDraftDto draftDto = new ProfileImageDraftDto();
        // ImageType 업무 값을 draftDto DTO에 설정한다
        draftDto.setImageType(imageType);
        // DraftToken 업무 값을 draftDto DTO에 설정한다
        draftDto.setDraftToken(draftToken);
        // 작은 서버 미리보기만 인증 응답 본문에 포함하고 실제 임시 경로는 노출하지 않는다
        draftDto.setPreviewDataUrl(
                "data:" + previewMimeType + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(previewPath))
        );
        // 원본 생성 시각으로부터 고정된 30분 만료 시각을 설정한다
        draftDto.setExpiresAt(Instant.ofEpochMilli(Files.getLastModifiedTime(originalPath).toMillis())
                .plus(PROFILE_IMAGE_DRAFT_TTL));
        // 로그인 사용자의 임시 이미지 정보를 반환한다
        return draftDto;
    }

    /**
     * 방향 보정이 끝난 이미지를 화면 표시용 제한 크기로 축소한다.
     *
     * @author SeungHyeon.Kang
     * @param sourceImage 검증과 방향 보정을 마친 이미지
     * @param maxEdge 미리보기 한 변의 최대 길이
     * @return 축소된 서버 미리보기
     */
    private ValidatedImage createPreviewImage(ValidatedImage sourceImage, int maxEdge) {
        try {
            BufferedImage decodedImage = ImageIO.read(new ByteArrayInputStream(sourceImage.bytes()));
            if (StringUtil.isEmpty(decodedImage)) {
                throw new InvalidImageFileException("Profile image preview decoding failed.");
            }

            double scale = Math.min(1.0, (double) maxEdge / Math.max(decodedImage.getWidth(), decodedImage.getHeight()));
            int targetWidth = Math.max(1, (int) Math.round(decodedImage.getWidth() * scale));
            int targetHeight = Math.max(1, (int) Math.round(decodedImage.getHeight() * scale));
            int targetType = decodedImage.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage previewImage = new BufferedImage(targetWidth, targetHeight, targetType);
            Graphics2D graphics = previewImage.createGraphics();

            try {
                // 고해상도 원본을 축소할 때 계단 현상을 줄이는 보간 설정을 적용한다
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(decodedImage, 0, 0, targetWidth, targetHeight, null);
            }

            finally {
                graphics.dispose();
            }

            ImageFormat imageFormat = ".png".equals(sourceImage.extension()) ? ImageFormat.PNG : ImageFormat.JPEG;
            ByteArrayOutputStream previewOutput = new ByteArrayOutputStream();
            boolean encoded = ImageIO.write(previewImage, imageFormat.imageIoName(), previewOutput);

            if (!encoded || previewOutput.size() == 0) {
                throw new InvalidImageFileException("Profile image preview encoding failed.");
            }

            return new ValidatedImage(previewOutput.toByteArray(), imageFormat.mimeType(), imageFormat.extension());
        }

        catch (IOException e) {
            throw new InvalidImageFileException("Profile image preview could not be created.", e);
        }
    }

    /**
     * 새 임시 선택본을 제외한 같은 유형의 이전 파일을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param draftDirectory 사용자별 임시 디렉터리
     * @param retainedToken 유지할 신규 임시 이미지 식별값
     * @throws IOException 임시 디렉터리를 조회할 수 없는 경우
     */
    private void delOtherProfileDrafts(Path draftDirectory, String retainedToken) throws IOException {
        try (Stream<Path> pathStream = Files.list(draftDirectory)) {
            pathStream.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().startsWith(retainedToken + "."))
                    .forEach(this::delDraftFileSafely);
        }
    }

    /**
     * 임시 이미지 디렉터리 안의 파일만 안전하게 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param draftDirectory 삭제할 임시 파일이 있는 디렉터리
     */
    private void delFilesInDirectory(Path draftDirectory) {
        // 계산된 경로가 임시 저장 루트 밖이면 삭제를 실행하지 않는다
        if (!draftDirectory.startsWith(profileImageDraftRootPath) || !Files.isDirectory(draftDirectory)) {
            return;
        }

        try (Stream<Path> pathStream = Files.list(draftDirectory)) {
            pathStream.filter(Files::isRegularFile).forEach(this::delDraftFileSafely);
        }

        catch (IOException e) {
            log.error("Profile image draft cleanup failed. path={}", draftDirectory, e);
        }
    }

    /**
     * 임시 파일 하나를 삭제하고 실패를 운영 로그에 남긴다.
     *
     * @author SeungHyeon.Kang
     * @param draftPath 삭제할 임시 파일 경로
     */
    private void delDraftFileSafely(Path draftPath) {
        // 임시 저장 루트의 일반 파일만 삭제 대상으로 허용한다
        if (!draftPath.normalize().startsWith(profileImageDraftRootPath) || !Files.isRegularFile(draftPath)) {
            return;
        }

        try {
            Files.deleteIfExists(draftPath);
        }

        catch (IOException e) {
            log.error("Profile image draft file cleanup failed. path={}", draftPath, e);
        }
    }

    /**
     * 임시 파일이 30분 보존 시간을 지났는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param draftPath 확인할 임시 파일
     * @return 만료 여부
     */
    private boolean isExpiredDraft(Path draftPath) {
        return Instant.ofEpochMilli(getLastModifiedMillis(draftPath))
                .plus(PROFILE_IMAGE_DRAFT_TTL)
                .isBefore(Instant.now());
    }

    /**
     * 파일 수정 시각을 정렬과 만료 계산에 사용할 밀리초 값으로 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param path 확인할 파일 경로
     * @return 파일 수정 시각, 조회 실패 시 최소값
     */
    private long getLastModifiedMillis(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        }

        catch (IOException e) {
            return Long.MIN_VALUE;
        }
    }

    /**
     * 파일 타입 파라미터가 프로필 또는 배경 이미지인지 검증한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 검증할 파일 타입
     */
    private void validateImageType(String imageType) {
        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (!Constant.FILE_TYPE_PROFILE.equals(imageType) && !Constant.FILE_TYPE_BACKGROUND.equals(imageType)) {

            throw new InvalidImageFileException("Unsupported image type.");
        }
    }

    /**
     * 원본 파일명에서 경로와 제어문자를 제거해 메타정보로 저장할 안전한 이름을 만듭니다.
     *
     * @author SeungHyeon.Kang
     * @param originalName 브라우저가 전달한 원본 파일명
     * @return 경로와 제어문자를 제거한 파일명
     */
    private String normalizeOriginalName(String originalName) {
        // originalName 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(originalName)) {
            // 원본 파일명에서 경로와 제어문자를 제거해 메타정보로 저장할 안전한 이름을 만듭니다 결과를 반환한다
            return "image";
        }

        // 대상 문자열에서 지정한 값을 치환한다
        String normalizedName = originalName.replace('\\', '/');
        // 요청한 범위의 문자열을 추출한다
        normalizedName = normalizedName.substring(normalizedName.lastIndexOf('/') + 1);
        // 정규식과 일치하는 문자열을 일괄 치환한다
        normalizedName = normalizedName.replaceAll("[\\p{Cntrl}]", "").trim();

        // normalizedName 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(normalizedName)) {
            // 원본 파일명에서 경로와 제어문자를 제거해 메타정보로 저장할 안전한 이름을 만듭니다 결과를 반환한다
            return "image";
        }

        // 원본 파일명에서 경로와 제어문자를 제거해 메타정보로 저장할 안전한 이름을 만듭니다 결과를 반환한다
        return StringUtil.cutString(normalizedName, 255);
    }

    /**
     * 실제 파일 시그니처, 이미지 헤더, 해상도와 디코딩 결과를 검증한 뒤 픽셀만 새 이미지로 재인코딩한다.
     *
     * @author SeungHyeon.Kang
     * @param originalBytes 업로드 또는 외부 다운로드로 받은 원본 바이트
     * @return 저장 가능한 정규화 이미지
     */
    private ValidatedImage validateAndNormalizeImage(byte[] originalBytes) {
        // originalBytes 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(originalBytes) || originalBytes.length == 0
                || originalBytes.length > maxImageBytes) {

            throw new InvalidImageFileException("Image file size is invalid.");
        }

        // 파일 헤더를 기준으로 실제 이미지 형식을 판별한다
        ImageFormat imageFormat = detectImageFormat(originalBytes);

        // 파일명과 Content-Type은 조작할 수 있으므로 실제 선두 바이트가 JPG 또는 PNG일 때만 디코딩을 진행한다.
        if (StringUtil.isEmpty(imageFormat)) {

            throw new InvalidImageFileException("Only JPEG and PNG image signatures are allowed.");
        }

        // JPEG에 기록된 촬영 방향을 재인코딩 전에 픽셀에 적용할 값으로 조회한다
        int exifOrientation = getExifOrientation(originalBytes, imageFormat);

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try (
                // 이미지 형식 검증에 사용할 입력 스트림을 담을 객체를 생성한다
                ByteArrayInputStream byteInput = new ByteArrayInputStream(originalBytes);
                // createImageInputStream 호출로 후속 처리에 필요한 객체를 생성한다
                ImageInputStream imageInput = ImageIO.createImageInputStream(byteInput)
        ) {
            // imageInput 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
            if (StringUtil.isEmpty(imageInput)) {

                throw new InvalidImageFileException("Image stream could not be created.");
            }

            // getImageReaders 조회로 후속 처리에 필요한 데이터를 가져온다
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

            // 시그니처만 흉내 낸 파일은 ImageIO reader를 얻지 못하므로 실제 이미지로 인정하지 않는다.
            if (!readers.hasNext()) {

                throw new InvalidImageFileException("Image decoder was not found.");
            }

            // 검증에 사용할 첫 번째 이미지 리더를 선택한다
            ImageReader reader = readers.next();

            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try {
                // Input 업무 값을 reader DTO에 설정한다
                reader.setInput(imageInput, true, true);

                // 선두 바이트와 실제 ImageIO 판독 형식이 다르면 다중 형식으로 위장한 파일로 보고 거부한다.
                if (!imageFormat.readerFormatName().equalsIgnoreCase(reader.getFormatName())) {

                    throw new InvalidImageFileException("Image signature and decoder format do not match.");
                }

                // getWidth 조회로 후속 처리에 필요한 데이터를 가져온다
                int width = reader.getWidth(0);
                // getHeight 조회로 후속 처리에 필요한 데이터를 가져온다
                int height = reader.getHeight(0);
                // 이미지 픽셀 수를 오버플로 없이 계산한다
                long pixelCount = Math.multiplyExact((long) width, (long) height);

                // 압축 해제 폭탄을 막기 위해 전체 픽셀 수를 확인한 뒤에만 실제 픽셀 디코딩을 수행한다.
                if (width <= 0 || height <= 0
                        || width > maxImageDimension || height > maxImageDimension
                        || pixelCount > maxImagePixels) {

                    throw new InvalidImageFileException("Image dimensions are invalid.");
                }

                // 검증이 끝난 이미지를 메모리에 읽는다
                BufferedImage decodedImage = reader.read(0);

                // decodedImage 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
                if (StringUtil.isEmpty(decodedImage)) {

                    throw new InvalidImageFileException("Image decoding failed.");
                }

                // EXIF 방향값을 실제 픽셀 방향에 반영해 메타정보가 제거된 뒤에도 표시 방향을 유지한다
                BufferedImage orientedImage = uptImageOrientation(decodedImage, exifOrientation);

                // 정규화한 이미지 데이터를 누적할 출력 스트림을 담을 객체를 생성한다
                ByteArrayOutputStream normalizedOutput = new ByteArrayOutputStream();
                // 검증된 업로드 파일을 저장 경로에 기록한다
                boolean encoded = ImageIO.write(orientedImage, imageFormat.imageIoName(), normalizedOutput);

                // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
                if (!encoded || normalizedOutput.size() == 0 || normalizedOutput.size() > maxImageBytes) {

                    throw new InvalidImageFileException("Normalized image size is invalid.");
                }

                // 새로 생성한 ValidatedImage 객체를 반환한다
                // 정규화한 이미지와 파일 형식 정보를 담은 객체를 반환한다
                return new ValidatedImage(normalizedOutput.toByteArray(), imageFormat.mimeType(), imageFormat.extension());
            }

            // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
            finally {
                // 이미지 변환에 사용한 그래픽 자원을 해제한다
                reader.dispose();
            }
        }
        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (InvalidImageFileException e) {

            throw e;
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (IOException | ArithmeticException e) {

            throw new InvalidImageFileException("Image validation failed.", e);
        }
    }

    /**
     * 이미지 선두 바이트를 기준으로 허용 형식을 판별한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 검사할 파일 바이트
     * @return 허용 이미지 형식, 일치하지 않으면 null
     */
    private ImageFormat detectImageFormat(byte[] bytes) {
        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (startsWith(bytes, JPEG_SIGNATURE)) {
            // 이미지 선두 바이트를 기준으로 허용 형식을 판별한 결과를 반환한다
            return ImageFormat.JPEG;
        }

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (startsWith(bytes, PNG_SIGNATURE)) {
            // 이미지 선두 바이트를 기준으로 허용 형식을 판별한 결과를 반환한다
            return ImageFormat.PNG;
        }

        // 조회하거나 생성할 값이 없음을 반환한다
        return null;
    }

    /**
     * 파일 바이트가 지정된 시그니처로 시작하는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 검사할 파일 바이트
     * @param signature 비교할 파일 시그니처
     * @return 시그니처 일치 여부
     */
    private boolean startsWith(byte[] bytes, byte[] signature) {
        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (bytes.length < signature.length) {
            // 파일 바이트가 지정된 시그니처로 시작하는지 확인한다 판정값을 반환한다
            return false;
        }

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (int index = 0; index < signature.length; index++) {
            // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
            if (bytes[index] != signature[index]) {
                // 파일 바이트가 지정된 시그니처로 시작하는지 확인한다 판정값을 반환한다
                return false;
            }
        }

        // 파일 바이트가 지정된 시그니처로 시작하는지 확인한다 판정값을 반환한다
        return true;
    }

    /**
     * JPEG APP1 메타정보에서 촬영 당시 이미지 방향값을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 검사할 원본 이미지 바이트
     * @param imageFormat 판별된 이미지 형식
     * @return EXIF Orientation 값, 값이 없거나 손상되었으면 1
     */
    private int getExifOrientation(byte[] bytes, ImageFormat imageFormat) {
        // PNG에는 JPEG APP1 메타정보가 없으므로 방향 보정을 적용하지 않는다
        if (imageFormat != ImageFormat.JPEG) {
            // 원본 픽셀 방향을 유지하는 기본 방향값을 반환한다
            return 1;
        }

        int markerOffset = JPEG_SIGNATURE.length - 1;

        // 압축 이미지 데이터가 시작되기 전의 JPEG 메타정보 구간만 순차 검사한다
        while (markerOffset + 4 <= bytes.length) {
            // JPEG 마커 시작값이 아니면 손상된 메타정보로 보고 기본 방향을 사용한다
            if ((bytes[markerOffset] & 0xFF) != 0xFF) {
                // 손상된 메타정보 대신 원본 픽셀 방향을 유지한다
                return 1;
            }

            int marker = bytes[markerOffset + 1] & 0xFF;

            // 압축 이미지 또는 파일 종료 지점 이후의 바이트는 EXIF 메타정보로 해석하지 않는다
            if (marker == JPEG_START_OF_SCAN_MARKER || marker == JPEG_END_OF_IMAGE_MARKER) {
                // EXIF 방향값이 없는 이미지의 기본 방향을 반환한다
                return 1;
            }

            // JPEG 구간 길이를 네트워크 바이트 순서로 조회한다
            int segmentLength = getUnsignedShort(bytes, markerOffset + 2, false);

            // 구간 길이가 헤더보다 짧거나 파일 범위를 벗어나면 손상된 메타정보로 처리한다
            if (segmentLength < 2 || markerOffset + 2L + segmentLength > bytes.length) {
                // 손상된 메타정보 대신 원본 픽셀 방향을 유지한다
                return 1;
            }

            int segmentDataOffset = markerOffset + 4;
            int segmentDataLength = segmentLength - 2;

            // APP1 구간이 EXIF 헤더로 시작할 때만 TIFF 방향 태그를 조회한다
            if (marker == JPEG_APP1_MARKER && isExifHeader(bytes, segmentDataOffset, segmentDataLength)) {
                // 검증된 EXIF TIFF 구간에서 이미지 방향값을 반환한다
                return getTiffOrientation(bytes, segmentDataOffset + EXIF_HEADER.length
                        , segmentDataLength - EXIF_HEADER.length);
            }

            // 현재 JPEG 구간 전체 길이만큼 다음 마커 위치로 이동한다
            markerOffset += segmentLength + 2;
        }

        // EXIF Orientation 태그가 없는 이미지의 기본 방향값을 반환한다
        return 1;
    }

    /**
     * JPEG APP1 데이터가 EXIF 고정 헤더로 시작하는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 검사할 원본 이미지 바이트
     * @param offset APP1 데이터 시작 위치
     * @param length APP1 데이터 길이
     * @return EXIF 헤더 일치 여부
     */
    private boolean isExifHeader(byte[] bytes, int offset, int length) {
        // EXIF 고정 헤더보다 짧은 APP1 구간은 다른 메타정보로 처리한다
        if (length < EXIF_HEADER.length || offset + EXIF_HEADER.length > bytes.length) {
            // EXIF 메타정보가 아님을 반환한다
            return false;
        }

        // APP1 선두 바이트를 EXIF 고정 헤더와 순차 비교한다
        for (int index = 0; index < EXIF_HEADER.length; index++) {
            // 한 바이트라도 다르면 EXIF 메타정보로 해석하지 않는다
            if (bytes[offset + index] != EXIF_HEADER[index]) {
                // EXIF 메타정보가 아님을 반환한다
                return false;
            }
        }

        // APP1 데이터가 EXIF 고정 헤더와 일치함을 반환한다
        return true;
    }

    /**
     * EXIF TIFF 구간의 첫 번째 이미지 디렉터리에서 방향 태그를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 검사할 원본 이미지 바이트
     * @param tiffOffset TIFF 헤더 시작 위치
     * @param tiffLength TIFF 데이터 길이
     * @return EXIF Orientation 값, 값이 없거나 손상되었으면 1
     */
    private int getTiffOrientation(byte[] bytes, int tiffOffset, int tiffLength) {
        // TIFF 헤더와 첫 번째 이미지 디렉터리 위치를 읽을 수 없는 구간은 기본 방향으로 처리한다
        if (tiffLength < 8 || tiffOffset < 0 || tiffOffset + (long) tiffLength > bytes.length) {
            // 손상된 TIFF 메타정보 대신 원본 픽셀 방향을 유지한다
            return 1;
        }

        boolean littleEndian;

        // Intel 바이트 순서로 기록된 TIFF 메타정보를 구분한다
        if (bytes[tiffOffset] == 0x49 && bytes[tiffOffset + 1] == 0x49) {
            littleEndian = true;
        }

        // Motorola 바이트 순서로 기록된 TIFF 메타정보를 구분한다
        else if (bytes[tiffOffset] == 0x4D && bytes[tiffOffset + 1] == 0x4D) {
            littleEndian = false;
        }

        // 지원하지 않는 바이트 순서는 손상된 메타정보로 처리한다
        else {
            // 해석할 수 없는 TIFF 메타정보의 기본 방향값을 반환한다
            return 1;
        }

        // TIFF 고정 식별값이 아니면 이미지 디렉터리를 읽지 않는다
        if (getUnsignedShort(bytes, tiffOffset + 2, littleEndian) != 42) {
            // 유효하지 않은 TIFF 메타정보의 기본 방향값을 반환한다
            return 1;
        }

        // TIFF 기준 상대 위치를 파일 내 절대 위치로 변환한다
        long directoryOffset = tiffOffset + getUnsignedInt(bytes, tiffOffset + 4, littleEndian);
        long tiffEndOffset = tiffOffset + (long) tiffLength;

        // 첫 번째 이미지 디렉터리의 항목 수를 읽을 수 없는 위치는 기본 방향으로 처리한다
        if (directoryOffset < tiffOffset || directoryOffset + 2 > tiffEndOffset) {
            // 손상된 이미지 디렉터리 위치의 기본 방향값을 반환한다
            return 1;
        }

        // 검증된 이미지 디렉터리의 태그 항목 수를 조회한다
        int entryCount = getUnsignedShort(bytes, (int) directoryOffset, littleEndian);

        // 첫 번째 이미지 디렉터리의 태그를 선언된 항목 수만큼 순차 검사한다
        for (int index = 0; index < entryCount; index++) {
            long entryOffset = directoryOffset + 2L + index * 12L;

            // 선언된 태그 항목이 TIFF 구간을 벗어나면 손상된 메타정보로 처리한다
            if (entryOffset + 12 > tiffEndOffset) {
                // 손상된 태그 목록 대신 원본 픽셀 방향을 유지한다
                return 1;
            }

            // 현재 TIFF 태그 번호를 조회한다
            int tag = getUnsignedShort(bytes, (int) entryOffset, littleEndian);

            // 이미지 방향 이외의 태그는 후속 항목에서 계속 탐색한다
            if (tag != EXIF_ORIENTATION_TAG) {
                // 다음 TIFF 태그 항목을 확인한다
                continue;
            }

            // Orientation은 SHORT 한 개로 저장되어야 하므로 타입과 개수를 함께 검증한다
            if (getUnsignedShort(bytes, (int) entryOffset + 2, littleEndian) != 3
                    || getUnsignedInt(bytes, (int) entryOffset + 4, littleEndian) != 1) {
                // 유효하지 않은 방향 태그 대신 원본 픽셀 방향을 유지한다
                return 1;
            }

            // SHORT 한 개는 태그 값 영역의 선두 두 바이트에 직접 저장된다
            int orientation = getUnsignedShort(bytes, (int) entryOffset + 8, littleEndian);

            // EXIF 표준이 정의한 여덟 방향값만 픽셀 변환에 사용한다
            if (orientation >= 1 && orientation <= 8) {
                // 검증된 EXIF 이미지 방향값을 반환한다
                return orientation;
            }

            // 허용 범위 밖의 방향값은 원본 픽셀 방향으로 보정한다
            return 1;
        }

        // 이미지 디렉터리에 Orientation 태그가 없으면 원본 픽셀 방향을 유지한다
        return 1;
    }

    /**
     * 바이트 배열의 두 바이트를 부호 없는 정수로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 변환할 바이트 배열
     * @param offset 값의 시작 위치
     * @param littleEndian 리틀 엔디언 여부
     * @return 0부터 65535까지의 부호 없는 정수값
     */
    private int getUnsignedShort(byte[] bytes, int offset, boolean littleEndian) {
        // TIFF 바이트 순서에 따라 하위 바이트부터 조합한다
        if (littleEndian) {
            // 리틀 엔디언으로 조합한 부호 없는 정수값을 반환한다
            return (bytes[offset] & 0xFF) | ((bytes[offset + 1] & 0xFF) << 8);
        }

        // 네트워크 또는 Motorola 바이트 순서로 조합한 부호 없는 정수값을 반환한다
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    /**
     * 바이트 배열의 네 바이트를 부호 없는 정수로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param bytes 변환할 바이트 배열
     * @param offset 값의 시작 위치
     * @param littleEndian 리틀 엔디언 여부
     * @return 0부터 4294967295까지의 부호 없는 정수값
     */
    private long getUnsignedInt(byte[] bytes, int offset, boolean littleEndian) {
        // TIFF 바이트 순서에 따라 하위 바이트부터 조합한다
        if (littleEndian) {
            // 리틀 엔디언으로 조합한 부호 없는 정수값을 반환한다
            return (bytes[offset] & 0xFFL)
                    | ((bytes[offset + 1] & 0xFFL) << 8)
                    | ((bytes[offset + 2] & 0xFFL) << 16)
                    | ((bytes[offset + 3] & 0xFFL) << 24);
        }

        // 네트워크 또는 Motorola 바이트 순서로 조합한 부호 없는 정수값을 반환한다
        return ((bytes[offset] & 0xFFL) << 24)
                | ((bytes[offset + 1] & 0xFFL) << 16)
                | ((bytes[offset + 2] & 0xFFL) << 8)
                | (bytes[offset + 3] & 0xFFL);
    }

    /**
     * EXIF 방향값을 실제 이미지 픽셀에 적용한다.
     *
     * @author SeungHyeon.Kang
     * @param sourceImage 원본 방향의 디코딩 이미지
     * @param orientation EXIF Orientation 값
     * @return 촬영 당시 표시 방향으로 변환한 이미지
     */
    private BufferedImage uptImageOrientation(BufferedImage sourceImage, int orientation) {
        // 방향 보정이 필요하지 않으면 추가 이미지 할당 없이 원본을 사용한다
        if (orientation == 1) {
            // 원본 픽셀 방향의 이미지를 반환한다
            return sourceImage;
        }

        // 픽셀 좌표 변환과 출력 크기 계산에 사용할 원본 너비를 조회한다
        int sourceWidth = sourceImage.getWidth();
        // 픽셀 좌표 변환과 출력 크기 계산에 사용할 원본 높이를 조회한다
        int sourceHeight = sourceImage.getHeight();
        AffineTransform transform;

        // EXIF 방향값에 대응하는 미러링과 회전 좌표 변환을 선택한다
        switch (orientation) {
            // 좌우가 반전된 픽셀을 수평으로 복원한다
            case 2:
                transform = new AffineTransform(-1, 0, 0, 1, sourceWidth, 0);
                break;
            // 상하좌우가 반전된 픽셀을 180도 회전한다
            case 3:
                transform = new AffineTransform(-1, 0, 0, -1, sourceWidth, sourceHeight);
                break;
            // 상하가 반전된 픽셀을 수직으로 복원한다
            case 4:
                transform = new AffineTransform(1, 0, 0, -1, 0, sourceHeight);
                break;
            // 좌우 반전과 반시계 방향 회전이 함께 기록된 픽셀을 복원한다
            case 5:
                transform = new AffineTransform(0, 1, 1, 0, 0, 0);
                break;
            // 시계 방향 90도 표시 방향을 실제 픽셀에 적용한다
            case 6:
                transform = new AffineTransform(0, 1, -1, 0, sourceHeight, 0);
                break;
            // 좌우 반전과 시계 방향 회전이 함께 기록된 픽셀을 복원한다
            case 7:
                transform = new AffineTransform(0, -1, -1, 0, sourceHeight, sourceWidth);
                break;
            // 반시계 방향 90도 표시 방향을 실제 픽셀에 적용한다
            case 8:
                transform = new AffineTransform(0, -1, 1, 0, 0, sourceWidth);
                break;
            // 허용되지 않은 방향값은 호출자가 검증했더라도 원본 방향으로 안전하게 처리한다
            default:
                // 변환할 수 없는 방향값에서는 원본 이미지를 반환한다
                return sourceImage;
        }

        int targetWidth = sourceWidth;
        int targetHeight = sourceHeight;

        // 90도 계열 회전은 출력 이미지의 가로와 세로 크기를 서로 교환한다
        if (orientation >= 5 && orientation <= 8) {
            targetWidth = sourceHeight;
            targetHeight = sourceWidth;
        }

        // 사용자 정의 이미지 형식은 알파 채널 여부에 맞는 표준 픽셀 형식으로 보정한다
        int targetType = sourceImage.getType();

        // 사용자 정의 색상 모델은 BufferedImage 생성자에서 직접 재사용할 수 없어 표준 형식으로 변환한다
        if (targetType == BufferedImage.TYPE_CUSTOM) {
            // 알파 채널이 있는 사용자 정의 이미지는 투명도를 유지하는 표준 형식으로 변환한다
            if (sourceImage.getColorModel().hasAlpha()) {
                targetType = BufferedImage.TYPE_INT_ARGB;
            }

            // 알파 채널이 없는 사용자 정의 이미지는 불투명 표준 형식으로 변환한다
            else {
                targetType = BufferedImage.TYPE_INT_RGB;
            }
        }

        // 방향이 반영된 픽셀을 담을 출력 이미지를 생성한다
        BufferedImage orientedImage = new BufferedImage(targetWidth, targetHeight, targetType);
        // 원본 픽셀을 좌표 변환하여 출력 이미지에 그릴 그래픽 객체를 생성한다
        Graphics2D graphics = orientedImage.createGraphics();

        // 그래픽 자원이 예외 상황에서도 해제되도록 이미지 변환을 격리한다
        try {
            // EXIF 방향에 대응하는 좌표 변환으로 원본 픽셀을 출력 이미지에 기록한다
            graphics.drawImage(sourceImage, transform, null);
        }

        // 이미지 변환이 끝나면 네이티브 그래픽 자원을 해제한다
        finally {
            // 이미지 변환에 사용한 그래픽 자원을 해제한다
            graphics.dispose();
        }

        // EXIF 표시 방향이 실제 픽셀에 반영된 이미지를 반환한다
        return orientedImage;
    }

    /**
     * DB 트랜잭션이 커밋된 뒤 교체되거나 영구 탈퇴로 제거된 물리 파일을 삭제하도록 정리 작업을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param fileList 커밋 후 삭제할 파일 메타정보 목록
     */
    private void registerCommitCleanup(List<FileDto> fileList) {
        // 트랜잭션 밖에서 호출되면 이미 완료된 DB 처리에 맞춰 물리 파일을 즉시 정리한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 트랜잭션이 없는 호출의 물리 파일을 즉시 삭제한다
            delPhysicalFileList(fileList);
            // 즉시 물리 파일 정리를 마친다
            return;
        }

        // DB 최종 상태에 따라 물리 파일 삭제 여부를 결정할 동기화 작업을 등록한다
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * DB 커밋이 완료된 경우에만 기존 물리 파일을 삭제한다.
             *
             * @author SeungHyeon.Kang
             * @param status 트랜잭션 종료 상태
             * @return 반환값이 없다
             */
            @Override
            public void afterCompletion(int status) {
                // 롤백된 DB가 기존 파일을 계속 참조할 수 있으므로 커밋 외 상태에서는 물리 파일을 유지한다
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    // 기존 물리 파일을 유지하고 커밋 후 정리를 종료한다
                    return;
                }

                // DB에서 참조와 메타정보가 제거된 물리 파일을 삭제한다
                delPhysicalFileList(fileList);
            }
        });
    }

    /**
     * 내부 업로드 경로로 확인된 저장소 객체를 목록 단위로 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param fileList 삭제할 파일 메타정보 목록
     */
    private void delPhysicalFileList(List<FileDto> fileList) {
        // 하나의 파일 삭제 실패가 나머지 영구 탈퇴 파일 정리를 중단하지 않도록 개별 처리한다
        for (FileDto fileDto : fileList) {
            // 외부 URL과 허용된 업로드 경로 밖의 값은 영구 객체 삭제 대상에서 제외한다
            String objectKey = getStoredObjectKey(fileDto);

            // 내부 업로드 객체 키가 아니면 메타정보 삭제만 유지한다
            if (StringUtil.isEmpty(objectKey)) {
                // 다음 파일 메타정보를 확인한다
                continue;
            }

            // 저장소 오류를 파일별로 격리해 나머지 삭제 대상을 계속 처리한다
            try {
                // DB에서 참조가 제거된 영구 이미지 객체를 삭제한다
                fileStorage.delFile(objectKey);
            }

            // 커밋 이후 물리 삭제 실패는 롤백할 수 없으므로 운영 로그에 재정리 대상을 남긴다
            catch (IOException e) {
                // 파일 번호와 안전하게 검증된 저장 경로를 오류 로그로 남긴다
                log.error("Committed image file cleanup failed. fileNumb={}, objectKey={}", fileDto.getFileNumb(), objectKey, e);
            }
        }
    }

    /**
     * 파일 접근 경로를 안전한 영구 저장소 객체 키로 변환한다.
     *
     * @author SeungHyeon.Kang
     * @param fileDto 경로와 서버 저장 파일명이 포함된 파일 메타정보
     * @return 삭제 가능한 저장소 객체 키, 외부 URL 또는 허용 범위 밖이면 null
     */
    private String getStoredObjectKey(FileDto fileDto) {
        // 파일 메타정보나 필수 경로가 없으면 내부 저장소 객체로 판단하지 않는다
        if (StringUtil.isEmpty(fileDto) || StringUtil.hasEmpty(fileDto.getFilePath(), fileDto.getStorName())) {
            // 삭제할 수 있는 로컬 저장 경로가 없음을 반환한다
            return null;
        }

        // 외부 이미지 URL과 이전 연동 경로는 내부 업로드 객체가 아니므로 물리 삭제에서 제외한다
        if (!fileDto.getFilePath().startsWith(UPLOAD_ACCESS_PREFIX)) {
            // 외부 경로는 로컬 저장 경로가 아님을 반환한다
            return null;
        }

        // 브라우저 접근 접두사를 제외한 프로필 또는 배경 하위 경로를 추출한다
        String relativePath = fileDto.getFilePath().substring(UPLOAD_ACCESS_PREFIX.length());
        // 업로드 루트를 기준으로 정규화해 상위 디렉터리 이동 문자를 제거한다
        Path storedPath = Paths.get(relativePath).normalize();
        // 프로필 이미지가 저장될 수 있는 루트 경로를 계산한다
        Path profileRoot = Paths.get(getUploadDirectoryName(Constant.FILE_TYPE_PROFILE)).normalize();
        // 배경 이미지가 저장될 수 있는 루트 경로를 계산한다
        Path backgroundRoot = Paths.get(getUploadDirectoryName(Constant.FILE_TYPE_BACKGROUND)).normalize();

        // 이미지 유형 루트 자체이거나 허용된 이미지 디렉터리 밖이면 삭제 요청을 차단한다
        if (storedPath.isAbsolute() || storedPath.getNameCount() != 3
                || (!storedPath.startsWith(profileRoot) && !storedPath.startsWith(backgroundRoot))
                || !fileDto.getStorName().equals(storedPath.getFileName().toString())) {
            // 안전한 업로드 파일 경로가 아님을 반환한다
            return null;
        }

        // 운영체제 경로 구분자를 S3 객체 키 구분자로 통일해 검증된 상대 키를 반환한다
        return storedPath.toString().replace('\\', '/');
    }

    /**
     * DB 트랜잭션이 롤백될 때 이미 생성한 실제 이미지 파일도 함께 제거하도록 정리 작업을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 트랜잭션 롤백 시 삭제할 저장소 객체 키
     */
    private void registerRollbackCleanup(String objectKey) {
        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // DB 트랜잭션이 롤백될 때 이미 생성한 실제 이미지 파일도 함께 제거하도록 정리 작업을 등록한 결과를 반환한다
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 트랜잭션 종료 상태에 맞춰 임시 파일을 정리한다
             *
             * @author SeungHyeon.Kang
             * @param status 트랜잭션 종료 상태
             * @return 반환값이 없다
             */
            @Override
            public void afterCompletion(int status) {
                // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
                if (status != TransactionSynchronization.STATUS_ROLLED_BACK) {
                    // 트랜잭션 종료 상태에 맞춰 임시 파일을 정리 결과를 반환한다
                    return;
                }

                // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
                try {
                    // 검증 중 생성된 임시 파일이 있으면 삭제한다
                    fileStorage.delFile(objectKey);
                }

                // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
                catch (IOException e) {
                    // 실패 원인과 처리 대상을 오류 로그로 남긴다
                    log.error("Rolled-back image file cleanup failed. objectKey={}", objectKey, e);
                }
            }
        });
    }

    /**
     * 프로필 저장 트랜잭션이 커밋된 경우에만 승격을 마친 임시 이미지 쌍을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param draftDirectory 사용자별 임시 디렉터리
     * @param draftToken 삭제할 임시 이미지 식별값
     */
    private void setDraftCleanupOnCommit(Path draftDirectory, String draftToken) {
        // 트랜잭션 밖에서 영구 저장이 완료되었다면 임시 파일을 즉시 삭제한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delDraftTokenFiles(draftDirectory, draftToken);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * DB 커밋 성공 시에만 임시 원본과 미리보기를 정리한다.
             *
             * @author SeungHyeon.Kang
             * @param status 트랜잭션 종료 상태
             */
            @Override
            public void afterCompletion(int status) {
                // 롤백 시에는 사용자가 다시 저장할 수 있도록 만료 전 임시 이미지를 유지한다
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    return;
                }

                // 최종 프로필에 반영된 임시 원본과 미리보기를 삭제한다
                delDraftTokenFiles(draftDirectory, draftToken);
            }
        });
    }

    /**
     * 특정 임시 이미지 식별값에 대응하는 원본과 미리보기 파일을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param draftDirectory 사용자별 임시 디렉터리
     * @param draftToken 삭제할 임시 이미지 식별값
     */
    private void delDraftTokenFiles(Path draftDirectory, String draftToken) {
        // 안전한 사용자별 디렉터리 안에서만 식별값 파일을 조회한다
        if (!draftDirectory.startsWith(profileImageDraftRootPath) || !Files.isDirectory(draftDirectory)) {
            return;
        }

        try (Stream<Path> pathStream = Files.list(draftDirectory)) {
            pathStream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(draftToken + "."))
                    .forEach(FileService.this::delDraftFileSafely);
        }

        catch (IOException e) {
            log.error("Committed profile image draft cleanup failed. path={}", draftDirectory, e);
        }
    }

    /**
     * 검증을 마친 이미지 바이트와 서버 저장 형식을 함께 보관한다.
     *
     * @param bytes 재인코딩된 이미지 바이트
     * @param mimeType 서버가 결정한 MIME 타입
     * @param extension 서버가 결정한 파일 확장자
     */
    private record ValidatedImage(byte[] bytes, String mimeType, String extension) {

    }

    /**
     * 서비스가 저장을 허용하는 이미지 형식 정보를 정의한다.
     */
    private enum ImageFormat {
        // JPEG 이미지 형식 정보를 생성한다
        JPEG("jpg", "JPEG", "image/jpeg", DEFAULT_IMAGE_EXTENSION),
        // PNG 이미지 형식 정보를 생성한다
        PNG("png", "PNG", "image/png", ".png");

        // ImageIO에서 사용하는 이미지 형식명
        private final String imageIoName;
        // 이미지 리더가 판별한 원본 형식명
        private final String readerFormatName;
        // 파일 MIME 유형
        private final String mimeType;
        // 저장 파일 확장자
        private final String extension;

        ImageFormat(String imageIoName, String readerFormatName, String mimeType, String extension) {

            this.imageIoName = imageIoName;
            this.readerFormatName = readerFormatName;
            this.mimeType = mimeType;
            this.extension = extension;
        }

        private String imageIoName() {
            // io name 처리 결과를 반환한다
            return imageIoName;
        }

        private String readerFormatName() {
            // format name 처리 결과를 반환한다
            return readerFormatName;
        }

        private String mimeType() {
            // type 처리 결과를 반환한다
            return mimeType;
        }

        private String extension() {
            // extension 처리 결과를 반환한다
            return extension;
        }
    }
}
