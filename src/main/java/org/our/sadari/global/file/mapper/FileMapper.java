package org.our.sadari.global.file.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.global.file.dto.FileDto;

/**
 * FileMapper 인터페이스에서 제공해야 하는 기능 계약을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface FileMapper {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int setFile(FileDto fileDto);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    FileDto getFileByNumb(Long fileNumb);
}
