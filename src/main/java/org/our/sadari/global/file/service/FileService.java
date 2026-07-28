package org.our.sadari.global.file.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.global.file.dto.FileDto;
import org.our.sadari.global.file.exception.InvalidImageFileException;
import org.our.sadari.global.file.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : FileService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-14
 * description    : 이미지 파일 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-14        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    // 업로드 루트 디렉터리 설정값
    private static final String UPLOAD_ROOT_DIR = "uploads";
    // 업로드 접근 접두사 설정값
    private static final String UPLOAD_ACCESS_PREFIX = "/uploads/";
    // 기본 이미지 EXTENSION 설정값
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    // PNG 시그니처 설정값
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    // File 데이터 접근 객체
    private final FileMapper fileMapper;

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
        // 검증된 이미지 형식에 대응하는 파일 확장자를 결정한다
        String storedName = createStoredFileName(validatedImage.extension());

        // getUploadPath 조회로 후속 처리에 필요한 데이터를 가져온다
        Path uploadPath = getUploadPath(imageType);
        // 파일 저장에 필요한 디렉터리를 생성한다
        Files.createDirectories(uploadPath);
        // 기준 경로와 하위 경로를 결합한다
        Path storedPath = uploadPath.resolve(storedName);
        boolean fileMetadataSaved = false;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            /*
             * 원본 바이트를 그대로 보관하면 이미지 뒤에 붙인 실행 파일이나 메타데이터도 함께 저장될 수 있다.
             * 디코딩한 픽셀을 새 JPG/PNG로 재인코딩한 결과만 신규 파일로 생성해 검증되지 않은 데이터를 제거한다.
             */
            Files.write(
                    storedPath
                  , validatedImage.bytes()
                  , StandardOpenOption.CREATE_NEW
                  , StandardOpenOption.WRITE
            );

            // 업로드 파일의 저장 정보를 담을 객체를 생성한다
            FileDto fileDto = new FileDto();
            // OrigName 업무 값을 fileDto DTO에 설정한다
            fileDto.setOrigName(originalName);
            // StorName 업무 값을 fileDto DTO에 설정한다
            fileDto.setStorName(storedName);
            // FilePath 업무 값을 fileDto DTO에 설정한다
            fileDto.setFilePath(getAccessPrefix(imageType) + storedName);
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
            registerRollbackCleanup(storedPath);
            fileMetadataSaved = true;
            // 사용자가 업로드한 이미지 파일을 프로젝트 내부 저장소에 저장하고 파일 번호를 반환한 결과를 반환한다
            return fileDto.getFileNumb();
        }

        // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
        finally {
            // DB 메타정보 저장 전에 실패한 파일을 남기면 접근되지 않는 파일이 누적되므로 즉시 정리한다.
            if (!fileMetadataSaved) {
                // 검증 중 생성된 임시 파일이 있으면 삭제한다
                Files.deleteIfExists(storedPath);
            }
        }
    }

    /**
     * Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환한다.
     *
     * @author SeungHyeon.Kang
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
            // 외부 HTTP API 요청을 수행할 클라이언트를 담을 객체를 생성한다
            RestTemplate restTemplate = new RestTemplate();
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
            // getUploadPath 조회로 후속 처리에 필요한 데이터를 가져온다
            Path uploadPath = getUploadPath(Constant.FILE_TYPE_PROFILE);
            // 파일 저장에 필요한 디렉터리를 생성한다
            Files.createDirectories(uploadPath);
            // 기준 경로와 하위 경로를 결합한다
            Path storedPath = uploadPath.resolve(storedName);
            boolean fileMetadataSaved = false;

            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try {
                // 검증된 업로드 파일을 저장 경로에 기록한다
                Files.write(
                        storedPath
                      , validatedImage.bytes()
                      , StandardOpenOption.CREATE_NEW
                      , StandardOpenOption.WRITE
                );

                // 업로드 파일의 저장 정보를 담을 객체를 생성한다
                FileDto fileDto = new FileDto();
                // OrigName 업무 값을 fileDto DTO에 설정한다
                fileDto.setOrigName("kakao-profile-" + userIdxx);
                // StorName 업무 값을 fileDto DTO에 설정한다
                fileDto.setStorName(storedName);
                // FilePath 업무 값을 fileDto DTO에 설정한다
                fileDto.setFilePath(getAccessPrefix(Constant.FILE_TYPE_PROFILE) + storedName);
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
                registerRollbackCleanup(storedPath);
                fileMetadataSaved = true;
                // Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환한 결과를 반환한다
                return fileDto.getFileNumb();
            }

            // 성공 여부와 관계없이 반드시 자원을 정리하기 위한 블록이다
            finally {
                // 메타정보 저장 전 실패한 외부 프로필 파일도 서버에 남기지 않는다.
                if (!fileMetadataSaved) {
                    // 검증 중 생성된 임시 파일이 있으면 삭제한다
                    Files.deleteIfExists(storedPath);
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
     * 이미지 타입에 맞는 서버 저장 경로를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 이미지 파일 타입
     * @return 서버 파일 시스템 저장 경로
     */
    private Path getUploadPath(String imageType) {
        // 이미지 타입에 맞는 서버 저장 경로를 반환한 결과를 반환한다
        return Paths.get(UPLOAD_ROOT_DIR, getUploadDirectoryName(imageType)).toAbsolutePath().normalize();
    }

    /**
     * 이미지 타입에 맞는 브라우저 접근 URL prefix를 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param imageType 이미지 파일 타입
     * @return 브라우저 접근 URL prefix
     */
    private String getAccessPrefix(String imageType) {
        // 이미지 타입에 맞는 브라우저 접근 URL prefix를 반환한 결과를 반환한다
        return UPLOAD_ACCESS_PREFIX + getUploadDirectoryName(imageType) + "/";
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

                // 정규화한 이미지 데이터를 누적할 출력 스트림을 담을 객체를 생성한다
                ByteArrayOutputStream normalizedOutput = new ByteArrayOutputStream();
                // 검증된 업로드 파일을 저장 경로에 기록한다
                boolean encoded = ImageIO.write(decodedImage, imageFormat.imageIoName(), normalizedOutput);

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
     * DB 트랜잭션이 롤백될 때 이미 생성한 실제 이미지 파일도 함께 제거하도록 정리 작업을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param storedPath 트랜잭션 롤백 시 삭제할 실제 파일 경로
     */
    private void registerRollbackCleanup(Path storedPath) {
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
                    Files.deleteIfExists(storedPath);
                }

                // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
                catch (IOException e) {
                    // 실패 원인과 처리 대상을 오류 로그로 남긴다
                    log.error("Rolled-back image file cleanup failed. path={}", storedPath, e);
                }
            }
        });
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
