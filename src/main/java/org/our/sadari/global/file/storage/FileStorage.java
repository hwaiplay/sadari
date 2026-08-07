package org.our.sadari.global.file.storage;

import java.io.IOException;
import java.util.Optional;

/**
 * fileName       : FileStorage
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 이미지 파일 저장소가 제공해야 하는 공통 작업을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
public interface FileStorage {

    /**
     * 검증된 이미지 바이트를 지정한 객체 키에 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 저장할 객체 키
     * @param bytes 저장할 이미지 바이트
     * @param contentType 이미지 MIME 유형
     * @throws IOException 저장소 쓰기 실패 시 발생
     */
    void setFile(String objectKey, byte[] bytes, String contentType) throws IOException;

    /**
     * 객체 키에 해당하는 이미지와 MIME 유형을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 조회할 객체 키
     * @return 저장된 파일, 객체가 없으면 빈 값
     * @throws IOException 저장소 읽기 실패 시 발생
     */
    Optional<StoredFile> getFile(String objectKey) throws IOException;

    /**
     * 객체 키에 해당하는 파일을 멱등하게 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param objectKey 삭제할 객체 키
     * @throws IOException 저장소 삭제 실패 시 발생
     */
    void delFile(String objectKey) throws IOException;
}
