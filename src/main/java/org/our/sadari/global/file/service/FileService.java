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

    private final FileMapper fileMapper;

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
     * 원본 파일 확장자를 유지하면서 충돌 없는 저장 파일명을 생성합니다.
     *
     * @author Seunghyeon.Kang
     * @param originalName 원본 파일명
     * @return UUID 기반 저장 파일명
     */
    private String createStoredFileName(String originalName) {
        String extension = "";

        if (!StringUtil.isEmpty(originalName) && originalName.lastIndexOf('.') >= 0) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }

        return UUID.randomUUID() + extension;
    }

    /**
     * MIME 타입을 이미지 파일 확장자로 변환합니다.
     *
     * @author Seunghyeon.Kang
     * @param contentType MIME 타입
     * @return 저장 파일 확장자
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
