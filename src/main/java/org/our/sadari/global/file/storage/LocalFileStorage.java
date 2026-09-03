package org.our.sadari.global.file.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * fileName       : LocalFileStorage
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 로컬 개발 환경에서 이미지 파일을 디스크에 저장함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
public class LocalFileStorage implements FileStorage {

    // 허용된 로컬 파일 저장 루트 경로
    private final Path rootPath;

    /**
     * 설정된 디렉터리를 절대 경로로 정규화하여 저장 루트로 사용함
     *
     * @author SeungHyeon.Kang
     * @param rootDirectory 로컬 파일 저장 루트 디렉터리
     */
    public LocalFileStorage(String rootDirectory) {

        // 모든 객체 키를 격리할 로컬 파일 저장 루트를 구성함
        this.rootPath = Paths.get(rootDirectory).toAbsolutePath().normalize();
    }

    /**
     * 검증된 이미지 바이트를 로컬 저장 루트 아래에 저장함
     *
     * @author SeungHyeon.Kang
     * @param objectKey 저장할 객체 키
     * @param bytes 저장할 이미지 바이트
     * @param contentType 이미지 MIME 유형
     * @throws IOException 로컬 파일 쓰기 실패 시 발생
     */
    @Override
    public void setFile(String objectKey, byte[] bytes, String contentType) throws IOException {

        // 객체 키가 저장 루트 밖을 가리키지 않도록 실제 경로를 검증함
        Path storedPath = getStoredPath(objectKey);
        // 날짜별 하위 디렉터리를 파일 쓰기 전에 생성함
        Files.createDirectories(storedPath.getParent());
        // 이미 존재하는 객체를 덮어쓰지 않고 새 파일만 생성함
        Files.write(storedPath, bytes, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    /**
     * 로컬 저장 루트에서 객체 키에 해당하는 이미지 파일을 조회함
     *
     * @author SeungHyeon.Kang
     * @param objectKey 조회할 객체 키
     * @return 저장된 파일, 객체가 없으면 빈 값
     * @throws IOException 로컬 파일 읽기 실패 시 발생
     */
    @Override
    public Optional<StoredFile> getFile(String objectKey) throws IOException {

        // 객체 키가 저장 루트 밖을 가리키지 않도록 실제 경로를 검증함
        Path storedPath = getStoredPath(objectKey);

        // 일반 파일이 없으면 공개 응답에서 404로 처리할 빈 값을 반환함
        if (!Files.isRegularFile(storedPath)) {
            // 존재하지 않는 파일 조회 결과를 반환함
            return Optional.empty();
        }

        // 확장자가 아닌 운영체제의 MIME 판정 결과를 우선 사용함
        String contentType = Files.probeContentType(storedPath);
        // 조회한 바이트와 MIME 유형을 공개 응답 객체로 반환함
        return Optional.of(new StoredFile(Files.readAllBytes(storedPath), contentType));
    }

    /**
     * 로컬 저장 루트에서 객체 키에 해당하는 파일을 멱등하게 삭제함
     *
     * @author SeungHyeon.Kang
     * @param objectKey 삭제할 객체 키
     * @throws IOException 로컬 파일 삭제 실패 시 발생
     */
    @Override
    public void delFile(String objectKey) throws IOException {

        // 검증된 로컬 경로에 존재하는 파일만 삭제함
        Files.deleteIfExists(getStoredPath(objectKey));
    }

    /**
     * 객체 키를 저장 루트 아래의 안전한 로컬 절대 경로로 변환함
     *
     * @author SeungHyeon.Kang
     * @param objectKey 변환할 객체 키
     * @return 저장 루트 아래의 정규화된 절대 경로
     * @throws IOException 객체 키가 저장 루트를 벗어나는 경우 발생
     */
    private Path getStoredPath(String objectKey) throws IOException {

        // S3 객체 키 구분자를 현재 운영체제의 상대 경로로 해석함
        Path storedPath = rootPath.resolve(objectKey).normalize();

        // 상위 경로 이동 문자가 저장 루트를 벗어나면 파일 시스템 접근을 차단함
        if (!storedPath.startsWith(rootPath) || storedPath.equals(rootPath)) {
            throw new IOException("File storage object key is invalid.");
        }

        // 저장 루트 아래로 검증된 로컬 절대 경로를 반환함
        return storedPath;
    }
}
