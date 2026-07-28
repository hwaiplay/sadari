package org.our.sadari.global.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : FileDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-14
 * description    : 이미지 파일 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-14        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Data
@Schema(description = "업로드 파일 저장 정보 DTO", hidden = true)
public class FileDto {

    // 파일 번호
    private Long fileNumb;
    // 업로드 당시 원본 파일명
    private String origName;
    // 서버에 저장된 파일명
    private String storName;
    // 파일 접근 경로
    private String filePath;
    // 파일 크기
    private Long fileSize;
    // 파일 MIME 유형
    private String mimeType;
    // 파일 등록 사용자 번호
    private Long regiUser;
    // 파일 등록 일시
    private LocalDateTime regiDate;
}
