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
 * 프로필 사진과 배경사진 파일을 저장하고 파일 메타정보를 관리하는 서비스입니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private static final String UPLOAD_ROOT_DIR = "uploads";
    private static final String UPLOAD_ACCESS_PREFIX = "/uploads/";
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private final FileMapper fileMapper;

    @Value("${app.upload.max-image-bytes:10485760}")
    private long maxImageBytes;

    @Value("${app.upload.max-image-pixels:20000000}")
    private long maxImagePixels;

    @Value("${app.upload.max-image-dimension:8192}")
    private int maxImageDimension;

    /**
     * 사용자가 업로드한 이미지 파일을 프로젝트 내부 저장소에 저장하고 파일 번호를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param imageFile 저장할 이미지 파일
     * @param imageType 프로필 사진인지 배경사진인지 구분하는 파일 타입
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호, 업로드 파일이 없으면 null
     * @throws IOException 파일 저장 중 오류가 발생한 경우
     */
    public Long setUploadedImage(MultipartFile imageFile, String imageType, Long regiUser) throws IOException {
        // 수정 화면에서 파일을 변경하지 않은 경우 기존 파일 번호를 유지해야 하므로 신규 파일 등록을 건너뜁니다.
        if (StringUtil.isEmpty(imageFile) || imageFile.isEmpty()) {
            return null;
        }

        validateImageType(imageType);

        String originalName = normalizeOriginalName(imageFile.getOriginalFilename());
        ValidatedImage validatedImage = validateAndNormalizeImage(imageFile.getBytes());
        String storedName = createStoredFileName(validatedImage.extension());

        Path uploadPath = getUploadPath(imageType);
        Files.createDirectories(uploadPath);
        Path storedPath = uploadPath.resolve(storedName);
        boolean fileMetadataSaved = false;

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

            FileDto fileDto = new FileDto();
            fileDto.setOrigName(originalName);
            fileDto.setStorName(storedName);
            fileDto.setFilePath(getAccessPrefix(imageType) + storedName);
            fileDto.setFileSize((long) validatedImage.bytes().length);
            fileDto.setMimeType(validatedImage.mimeType());
            fileDto.setRegiUser(regiUser);
            int insertCnt = fileMapper.setFile(fileDto);

            if (insertCnt != 1 || StringUtil.isEmpty(fileDto.getFileNumb())) {
                throw new IOException("File metadata could not be saved.");
            }

            /*
             * 뒤에서 사용자 프로필 UPDATE가 실패하면 DB 메타정보는 트랜잭션으로 롤백되지만 실제 파일은 자동 복구되지 않는다.
             * 현재 트랜잭션의 최종 상태를 확인해 롤백 시 물리 파일까지 함께 제거하도록 정리 작업을 예약한다.
             */
            registerRollbackCleanup(storedPath);
            fileMetadataSaved = true;

            return fileDto.getFileNumb();
        } finally {
            // DB 메타정보 저장 전에 실패한 파일을 남기면 접근되지 않는 파일이 누적되므로 즉시 정리한다.
            if (!fileMetadataSaved) {
                Files.deleteIfExists(storedPath);
            }
        }
    }

    /**
     * Kakao에서 전달받은 프로필 이미지를 내부 저장소에 복사하고 파일 번호를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param profileImageUrl Kakao 프로필 이미지 URL
     * @param userIdxx Kakao 사용자 식별값
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호, URL이 없으면 null
     */
    public Long setKakaoProfileImage(String profileImageUrl, String userIdxx, Long regiUser) {
        // Kakao 계정에 프로필 이미지가 없을 수 있으므로 회원 생성 흐름은 계속 진행합니다.
        if (StringUtil.isEmpty(profileImageUrl)) {
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(URI.create(profileImageUrl), byte[].class);
            byte[] imageBytes = response.getBody();

            // 응답 본문이 비어 있으면 원본 URL을 파일 경로로 등록해 프로필 표시 자체는 가능하게 합니다.
            if (StringUtil.isEmpty(imageBytes) || imageBytes.length == 0) {
                return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
            }

            ValidatedImage validatedImage = validateAndNormalizeImage(imageBytes);
            String storedName = createStoredFileName(validatedImage.extension());
            Path uploadPath = getUploadPath(Constant.FILE_TYPE_PROFILE);
            Files.createDirectories(uploadPath);
            Path storedPath = uploadPath.resolve(storedName);
            boolean fileMetadataSaved = false;

            try {
                Files.write(
                        storedPath
                      , validatedImage.bytes()
                      , StandardOpenOption.CREATE_NEW
                      , StandardOpenOption.WRITE
                );

                FileDto fileDto = new FileDto();
                fileDto.setOrigName("kakao-profile-" + userIdxx);
                fileDto.setStorName(storedName);
                fileDto.setFilePath(getAccessPrefix(Constant.FILE_TYPE_PROFILE) + storedName);
                fileDto.setFileSize((long) validatedImage.bytes().length);
                fileDto.setMimeType(validatedImage.mimeType());
                fileDto.setRegiUser(regiUser);
                int insertCnt = fileMapper.setFile(fileDto);

                if (insertCnt != 1 || StringUtil.isEmpty(fileDto.getFileNumb())) {
                    throw new IOException("Kakao profile file metadata could not be saved.");
                }

                registerRollbackCleanup(storedPath);
                fileMetadataSaved = true;
                return fileDto.getFileNumb();
            } finally {
                // 메타정보 저장 전 실패한 외부 프로필 파일도 서버에 남기지 않는다.
                if (!fileMetadataSaved) {
                    Files.deleteIfExists(storedPath);
                }
            }
        } catch (Exception e) {
            // 외부 이미지 다운로드 실패가 로그인 실패로 이어지지 않도록 원본 URL을 대체 경로로 저장합니다.
            log.warn("Kakao profile image download failed. userIdxx={}, message={}", userIdxx, e.getMessage());
            return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
        }
    }

    /**
     * 외부 이미지 URL 자체를 파일 경로로 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param imageUrl 외부 이미지 URL
     * @param ownerKey 파일 소유자를 식별할 수 있는 값
     * @param imageType 이미지 파일 타입
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호
     */
    private Long setExternalImage(String imageUrl, String ownerKey, String imageType, Long regiUser) {
        FileDto fileDto = new FileDto();
        fileDto.setOrigName(imageType.toLowerCase() + "-" + ownerKey);
        fileDto.setStorName(imageType.toLowerCase() + "-" + ownerKey);
        fileDto.setFilePath(imageUrl);
        fileDto.setMimeType("image/*");
        fileDto.setRegiUser(regiUser);
        fileMapper.setFile(fileDto);

        return fileDto.getFileNumb();
    }

    /**
     * 이미지 타입에 맞는 서버 저장 경로를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param imageType 이미지 파일 타입
     * @return 서버 파일 시스템 저장 경로
     */
    private Path getUploadPath(String imageType) {
        return Paths.get(UPLOAD_ROOT_DIR, getUploadDirectoryName(imageType)).toAbsolutePath().normalize();
    }

    /**
     * 이미지 타입에 맞는 브라우저 접근 URL prefix를 반환합니다.
     *
     * @author Seunghyeon.Kang
     * @param imageType 이미지 파일 타입
     * @return 브라우저 접근 URL prefix
     */
    private String getAccessPrefix(String imageType) {
        return UPLOAD_ACCESS_PREFIX + getUploadDirectoryName(imageType) + "/";
    }

    /**
     * 이미지 타입을 실제 저장 디렉터리명으로 변환합니다.
     *
     * @author Seunghyeon.Kang
     * @param imageType 이미지 파일 타입
     * @return 저장 디렉터리명
     */
    private String getUploadDirectoryName(String imageType) {
        if (Constant.FILE_TYPE_BACKGROUND.equals(imageType)) {
            return "background";
        }

        return "profile";
    }

    /**
     * 서버가 검증해 결정한 확장자로 충돌 없는 저장 파일명을 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param extension 검증된 이미지 확장자
     * @return UUID 기반 저장 파일명
     */
    private String createStoredFileName(String extension) {
        return UUID.randomUUID() + extension;
    }

    /**
     * 파일 타입 파라미터가 프로필 또는 배경 이미지인지 검증합니다.
     *
     * @author Seunghyeon.Kang
     * @param imageType 검증할 파일 타입
     */
    private void validateImageType(String imageType) {
        if (!Constant.FILE_TYPE_PROFILE.equals(imageType)
                && !Constant.FILE_TYPE_BACKGROUND.equals(imageType)) {
            throw new InvalidImageFileException("Unsupported image type.");
        }
    }

    /**
     * 원본 파일명에서 경로와 제어문자를 제거해 메타정보로 저장할 안전한 이름을 만듭니다.
     *
     * @author Seunghyeon.Kang
     * @param originalName 브라우저가 전달한 원본 파일명
     * @return 경로와 제어문자를 제거한 파일명
     */
    private String normalizeOriginalName(String originalName) {
        if (StringUtil.isEmpty(originalName)) {
            return "image";
        }

        String normalizedName = originalName.replace('\\', '/');
        normalizedName = normalizedName.substring(normalizedName.lastIndexOf('/') + 1);
        normalizedName = normalizedName.replaceAll("[\\p{Cntrl}]", "").trim();

        if (StringUtil.isEmpty(normalizedName)) {
            return "image";
        }

        return StringUtil.cutString(normalizedName, 255);
    }

    /**
     * 실제 파일 시그니처, 이미지 헤더, 해상도와 디코딩 결과를 검증한 뒤 픽셀만 새 이미지로 재인코딩합니다.
     *
     * @author Seunghyeon.Kang
     * @param originalBytes 업로드 또는 외부 다운로드로 받은 원본 바이트
     * @return 저장 가능한 정규화 이미지
     */
    private ValidatedImage validateAndNormalizeImage(byte[] originalBytes) {
        if (StringUtil.isEmpty(originalBytes)
                || originalBytes.length == 0
                || originalBytes.length > maxImageBytes) {
            throw new InvalidImageFileException("Image file size is invalid.");
        }

        ImageFormat imageFormat = detectImageFormat(originalBytes);

        // 파일명과 Content-Type은 조작할 수 있으므로 실제 선두 바이트가 JPG 또는 PNG일 때만 디코딩을 진행한다.
        if (imageFormat == null) {
            throw new InvalidImageFileException("Only JPEG and PNG image signatures are allowed.");
        }

        try (
                ByteArrayInputStream byteInput = new ByteArrayInputStream(originalBytes);
                ImageInputStream imageInput = ImageIO.createImageInputStream(byteInput)
        ) {
            if (imageInput == null) {
                throw new InvalidImageFileException("Image stream could not be created.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

            // 시그니처만 흉내 낸 파일은 ImageIO reader를 얻지 못하므로 실제 이미지로 인정하지 않는다.
            if (!readers.hasNext()) {
                throw new InvalidImageFileException("Image decoder was not found.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                // 선두 바이트와 실제 ImageIO 판독 형식이 다르면 다중 형식으로 위장한 파일로 보고 거부한다.
                if (!imageFormat.readerFormatName().equalsIgnoreCase(reader.getFormatName())) {
                    throw new InvalidImageFileException("Image signature and decoder format do not match.");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixelCount = Math.multiplyExact((long) width, (long) height);

                // 압축 해제 폭탄을 막기 위해 전체 픽셀 수를 확인한 뒤에만 실제 픽셀 디코딩을 수행한다.
                if (width <= 0
                        || height <= 0
                        || width > maxImageDimension
                        || height > maxImageDimension
                        || pixelCount > maxImagePixels) {
                    throw new InvalidImageFileException("Image dimensions are invalid.");
                }

                BufferedImage decodedImage = reader.read(0);

                if (decodedImage == null) {
                    throw new InvalidImageFileException("Image decoding failed.");
                }

                ByteArrayOutputStream normalizedOutput = new ByteArrayOutputStream();
                boolean encoded = ImageIO.write(decodedImage, imageFormat.imageIoName(), normalizedOutput);

                if (!encoded || normalizedOutput.size() == 0 || normalizedOutput.size() > maxImageBytes) {
                    throw new InvalidImageFileException("Normalized image size is invalid.");
                }

                return new ValidatedImage(
                        normalizedOutput.toByteArray()
                      , imageFormat.mimeType()
                      , imageFormat.extension()
                );
            } finally {
                reader.dispose();
            }
        } catch (InvalidImageFileException e) {
            throw e;
        } catch (IOException | ArithmeticException e) {
            throw new InvalidImageFileException("Image validation failed.", e);
        }
    }

    /**
     * 이미지 선두 바이트를 기준으로 허용 형식을 판별합니다.
     *
     * @author Seunghyeon.Kang
     * @param bytes 검사할 파일 바이트
     * @return 허용 이미지 형식, 일치하지 않으면 null
     */
    private ImageFormat detectImageFormat(byte[] bytes) {
        if (startsWith(bytes, JPEG_SIGNATURE)) {
            return ImageFormat.JPEG;
        }

        if (startsWith(bytes, PNG_SIGNATURE)) {
            return ImageFormat.PNG;
        }

        return null;
    }

    /**
     * 파일 바이트가 지정된 시그니처로 시작하는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param bytes 검사할 파일 바이트
     * @param signature 비교할 파일 시그니처
     * @return 시그니처 일치 여부
     */
    private boolean startsWith(byte[] bytes, byte[] signature) {
        if (bytes.length < signature.length) {
            return false;
        }

        for (int index = 0; index < signature.length; index++) {
            if (bytes[index] != signature[index]) {
                return false;
            }
        }

        return true;
    }

    /**
     * DB 트랜잭션이 롤백될 때 이미 생성한 실제 이미지 파일도 함께 제거하도록 정리 작업을 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param storedPath 트랜잭션 롤백 시 삭제할 실제 파일 경로
     */
    private void registerRollbackCleanup(Path storedPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_ROLLED_BACK) {
                    return;
                }

                try {
                    Files.deleteIfExists(storedPath);
                } catch (IOException e) {
                    log.error("Rolled-back image file cleanup failed. path={}", storedPath, e);
                }
            }
        });
    }

    /**
     * 검증을 마친 이미지 바이트와 서버 저장 형식을 함께 보관합니다.
     *
     * @param bytes 재인코딩된 이미지 바이트
     * @param mimeType 서버가 결정한 MIME 타입
     * @param extension 서버가 결정한 파일 확장자
     */
    private record ValidatedImage(byte[] bytes, String mimeType, String extension) {
    }

    /**
     * 서비스가 저장을 허용하는 이미지 형식 정보를 정의합니다.
     */
    private enum ImageFormat {
        JPEG("jpg", "JPEG", "image/jpeg", DEFAULT_IMAGE_EXTENSION),
        PNG("png", "PNG", "image/png", ".png");

        private final String imageIoName;
        private final String readerFormatName;
        private final String mimeType;
        private final String extension;

        ImageFormat(String imageIoName, String readerFormatName, String mimeType, String extension) {
            this.imageIoName = imageIoName;
            this.readerFormatName = readerFormatName;
            this.mimeType = mimeType;
            this.extension = extension;
        }

        private String imageIoName() {
            return imageIoName;
        }

        private String readerFormatName() {
            return readerFormatName;
        }

        private String mimeType() {
            return mimeType;
        }

        private String extension() {
            return extension;
        }
    }
}
