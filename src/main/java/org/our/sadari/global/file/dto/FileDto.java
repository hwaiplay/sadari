package org.our.sadari.global.file.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * FileDto 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Data
public class FileDto {

    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Long fileNumb;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String origName;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String storName;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String filePath;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Long fileSize;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private String mimeType;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private Long regiUser;
    /**
     * 클래스 내부에서 사용하는 상태 또는 설정 값이다.
     */
    private LocalDateTime regiDate;
}
