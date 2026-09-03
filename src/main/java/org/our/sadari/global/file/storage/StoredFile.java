package org.our.sadari.global.file.storage;

/**
 * fileName       : StoredFile
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 저장소에서 조회한 이미지 바이트와 MIME 유형을 전달함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 *
 * @param bytes 이미지 바이트
 * @param contentType 이미지 MIME 유형
 */
public record StoredFile(byte[] bytes, String contentType) {
}
