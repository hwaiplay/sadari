package org.our.sadari.global.file.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.file.dto.FileDto;
import org.our.sadari.global.file.mapper.FileMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 파일을 프로젝트 내부 저장소에 저장하고 파일 메타 정보를 관리한다.
 * 프로필 사진과 배경 사진은 이 서비스를 통해 파일 테이블에 등록한 뒤 생성된 파일 번호를 사용자 테이블에서 참조한다.
 * @Author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private static final String UPLOAD_ROOT_DIR = "uploads";
    private static final String UPLOAD_ACCESS_PREFIX = "/uploads/";
    private static final String DEFAULT_IMAGE_EXTENSION = ".jpg";

    private final FileMapper fileMapper;

    /**
     * 사용자가 업로드한 이미지 파일을 파일 구분값에 맞는 하위 디렉터리에 저장하고 파일 번호를 반환한다.
     * 등록자 회원 번호는 파일 등록 시점에 함께 저장해 별도 보정 update가 필요 없도록 한다.
     * @Author Seunghyeon.Kang
     * @param imageFile 저장할 이미지 파일
     * @param imageType 이미지 파일 구분값
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호
     * @throws IOException 파일 저장 중 오류가 발생한 경우
     */
    public Long setUploadedImage(MultipartFile imageFile, String imageType, Long regiUser) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        String originalName = imageFile.getOriginalFilename();
        String storedName = createStoredFileName(originalName);
        Path uploadPath = getUploadPath(imageType);
        Files.createDirectories(uploadPath);
        Files.copy(imageFile.getInputStream(), uploadPath.resolve(storedName));

        FileDto fileDto = new FileDto();
        fileDto.setOrigName(originalName);
        fileDto.setStorName(storedName);
        fileDto.setFilePath(getAccessPrefix(imageType) + storedName);
        fileDto.setFileSize(imageFile.getSize());
        fileDto.setMimeType(imageFile.getContentType());
        fileDto.setRegiUser(regiUser);
        fileMapper.setFile(fileDto);

        return fileDto.getFileNumb();
    }

    /**
     * 카카오에서 전달받은 프로필 이미지를 프로젝트 내부 저장소로 복사하고 파일 번호를 반환한다.
     * 카카오 이미지 다운로드가 실패하면 외부 URL 자체를 파일 경로로 저장한다.
     * @Author Seunghyeon.Kang
     * @param profileImageUrl 카카오 프로필 이미지 URL
     * @param userIdxx 카카오 제공 사용자 ID
     * @param regiUser 파일을 등록한 회원 번호
     * @return 파일 테이블에 생성된 파일 번호
     */
    public Long setKakaoProfileImage(String profileImageUrl, String userIdxx, Long regiUser) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(URI.create(profileImageUrl), byte[].class);
            byte[] imageBytes = response.getBody();

            if (imageBytes == null || imageBytes.length == 0) {
                return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
            }

            String contentType = response.getHeaders().getContentType() == null
                    ? "image/jpeg"
                    : response.getHeaders().getContentType().toString();
            String storedName = UUID.randomUUID() + resolveExtension(contentType);
            Path uploadPath = getUploadPath(Constant.FILE_TYPE_PROFILE);
            Files.createDirectories(uploadPath);
            Files.write(uploadPath.resolve(storedName), imageBytes);

            FileDto fileDto = new FileDto();
            fileDto.setOrigName("kakao-profile-" + userIdxx);
            fileDto.setStorName(storedName);
            fileDto.setFilePath(getAccessPrefix(Constant.FILE_TYPE_PROFILE) + storedName);
            fileDto.setFileSize((long) imageBytes.length);
            fileDto.setMimeType(contentType);
            fileDto.setRegiUser(regiUser);
            fileMapper.setFile(fileDto);

            return fileDto.getFileNumb();
        } catch (Exception e) {
            log.warn("Kakao profile image download failed. userIdxx={}, message={}", userIdxx, e.getMessage());
            return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
        }
    }

    /**
     * 외부 이미지 URL을 파일 테이블에 저장한다.
     * 실제 파일을 내려받지 못한 경우에도 사용자 프로필 표시가 끊기지 않도록 URL을 파일 경로로 남긴다.
     * @Author Seunghyeon.Kang
     * @param imageUrl 외부 이미지 URL
     * @param ownerKey 외부 이미지 소유자를 식별할 수 있는 값
     * @param imageType 이미지 파일 구분값
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
     * 이미지 파일 구분값에 맞는 업로드 저장 경로를 반환한다.
     * @Author Seunghyeon.Kang
     * @param imageType 이미지 파일 구분값
     * @return 업로드 저장 경로
     */
    private Path getUploadPath(String imageType) {
        return Paths.get(UPLOAD_ROOT_DIR, getUploadDirectoryName(imageType)).toAbsolutePath().normalize();
    }

    /**
     * 이미지 파일 구분값에 맞는 브라우저 접근 경로 prefix를 반환한다.
     * @Author Seunghyeon.Kang
     * @param imageType 이미지 파일 구분값
     * @return 브라우저 접근 경로 prefix
     */
    private String getAccessPrefix(String imageType) {
        return UPLOAD_ACCESS_PREFIX + getUploadDirectoryName(imageType) + "/";
    }

    /**
     * 이미지 파일 구분값을 실제 저장 디렉터리명으로 변환한다.
     * @Author Seunghyeon.Kang
     * @param imageType 이미지 파일 구분값
     * @return 저장 디렉터리명
     */
    private String getUploadDirectoryName(String imageType) {
        if (Constant.FILE_TYPE_BACKGROUND.equals(imageType)) {
            return "background";
        }

        return "profile";
    }

    /**
     * 원본 파일명을 기준으로 충돌 가능성이 낮은 저장 파일명을 생성한다.
     * @Author Seunghyeon.Kang
     * @param originalName 원본 파일명
     * @return 저장 파일명
     */
    private String createStoredFileName(String originalName) {
        String extension = "";

        if (originalName != null && originalName.lastIndexOf('.') >= 0) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }

        return UUID.randomUUID() + extension;
    }

    /**
     * MIME 타입을 이미지 파일 확장자로 변환한다.
     * @Author Seunghyeon.Kang
     * @param contentType MIME 타입
     * @return 파일 확장자
     */
    private String resolveExtension(String contentType) {
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return ".gif";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }

        return DEFAULT_IMAGE_EXTENSION;
    }
}
