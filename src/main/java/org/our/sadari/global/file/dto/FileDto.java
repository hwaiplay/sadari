package org.our.sadari.global.file.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 파일 테이블에 저장되는 파일 메타정보 DTO입니다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class FileDto {

    /** 파일 테이블의 PK 번호입니다. */
    private Long fileNumb;
    /** 사용자가 업로드했거나 외부 서비스에서 전달받은 원본 파일명입니다. */
    private String origName;
    /** 서버 저장소에서 충돌 없이 관리하기 위해 생성한 저장 파일명입니다. */
    private String storName;
    /** 브라우저가 접근할 수 있는 파일 URL 경로입니다. */
    private String filePath;
    /** 파일 크기입니다. 외부 URL만 저장한 경우 비어 있을 수 있습니다. */
    private Long fileSize;
    /** 파일 MIME 타입입니다. */
    private String mimeType;
    /** 파일을 등록한 회원 번호입니다. */
    private Long regiUser;
    /** 파일 메타정보 등록 일시입니다. */
    private LocalDateTime regiDate;
}
