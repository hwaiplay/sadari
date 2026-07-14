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
import org.our.sadari.global.common.util.StringUtil;
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
        // 업로드 파일이 없으면 기존 프로필 또는 배경 이미지를 유지해야 하므로 파일 등록을 건너뛴다.
        if (StringUtil.isEmpty(imageFile) || imageFile.isEmpty()) {
            return null;
        }

        // 원본 파일명은 사용자에게 받은 값이고, 저장 파일명은 충돌 방지를 위해 UUID 기반으로 새로 만든다.
        String originalName = imageFile.getOriginalFilename();
        String storedName = createStoredFileName(originalName);

        // 프로필 사진과 배경 사진은 서로 다른 디렉터리에 저장해 파일 구분과 정적 리소스 관리를 단순하게 유지한다.
        Path uploadPath = getUploadPath(imageType);
        Files.createDirectories(uploadPath);
        Files.copy(imageFile.getInputStream(), uploadPath.resolve(storedName));

        // 브라우저 접근 경로와 등록자 회원 번호를 함께 저장해 사용자 테이블은 파일 번호만 참조하게 한다.
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
        // 카카오에서 프로필 이미지 URL을 제공하지 않으면 사용자 생성 흐름만 계속 진행한다.
        if (StringUtil.isEmpty(profileImageUrl)) {
            return null;
        }

        try {
            // 외부 URL 이미지를 로컬 저장소로 복사해 이후 카카오 URL 만료나 접근 실패에 영향을 덜 받게 한다.
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(URI.create(profileImageUrl), byte[].class);
            byte[] imageBytes = response.getBody();

            // 응답 본문이 없으면 원본 URL을 파일 경로로 저장해 프로필 표시 자체는 가능하게 둔다.
            if (StringUtil.isEmpty(imageBytes) || imageBytes.length == 0) {
                return setExternalImage(profileImageUrl, userIdxx, Constant.FILE_TYPE_PROFILE, regiUser);
            }

            // Content-Type이 없을 때는 일반적인 카카오 프로필 이미지 포맷인 jpg로 저장한다.
            String contentType = StringUtil.isEmpty(response.getHeaders().getContentType())
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
            // 외부 이미지 다운로드 실패는 로그만 남기고 원본 URL 저장으로 대체해 로그인 자체를 막지 않는다.
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
        // 실제 파일이 없는 외부 URL 케이스이므로 크기는 비우고 MIME 타입은 이미지 와일드카드로 남긴다.
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
        // DB에는 서버 파일 시스템 경로가 아니라 브라우저가 접근할 수 있는 URL 경로를 저장한다.
        return UPLOAD_ACCESS_PREFIX + getUploadDirectoryName(imageType) + "/";
    }

    /**
     * 이미지 파일 구분값을 실제 저장 디렉터리명으로 변환한다.
     * @Author Seunghyeon.Kang
     * @param imageType 이미지 파일 구분값
     * @return 저장 디렉터리명
     */
    private String getUploadDirectoryName(String imageType) {
        // 배경 이미지만 background 디렉터리에 저장하고, 그 외 이미지 타입은 프로필 디렉터리를 기본값으로 사용한다.
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

        // 원본 파일명에 확장자가 있으면 유지하고, 없으면 UUID만 저장 파일명으로 사용한다.
        if (!StringUtil.isEmpty(originalName) && originalName.lastIndexOf('.') >= 0) {
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
