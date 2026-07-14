package org.our.sadari.global.file.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 파일 테이블에 저장되는 파일 기본 정보를 전달한다.
 * 프로필 사진, 배경 사진처럼 다른 업무 테이블에서 파일 번호를 참조하는 공통 파일 메타 데이터이다.
 * @Author Seunghyeon.Kang
 */
@Data
public class FileDto {

    // 파일 번호이며 다른 테이블에서 파일을 참조하는 기준값이다.
    private Long fileNumb;
    // 사용자가 업로드했거나 외부 서비스에서 전달받은 원본 파일명이다.
    private String origName;
    // 서버 저장소에 중복되지 않게 저장한 실제 파일명이다.
    private String storName;
    // 브라우저가 파일을 표시할 때 사용하는 접근 경로이다.
    private String filePath;
    // 파일 크기이며 외부 URL만 저장한 경우에는 비어 있을 수 있다.
    private Long fileSize;
    // 파일 MIME 타입이다.
    private String mimeType;
    // 파일을 등록한 회원 번호이다.
    private Long regiUser;
    // 파일 정보 등록 일시이다.
    private LocalDateTime regiDate;
}
