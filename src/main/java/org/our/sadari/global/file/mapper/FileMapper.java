package org.our.sadari.global.file.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.global.file.dto.FileDto;

/**
 * 파일 메타정보 저장과 조회를 담당하는 MyBatis Mapper입니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface FileMapper {

    /**
     * 파일 메타정보를 저장하고 생성된 파일 번호를 DTO에 채웁니다.
     *
     * @author Seunghyeon.Kang
     * @param fileDto 저장할 파일 메타정보
     * @return 저장된 행 수
     */
    int setFile(FileDto fileDto);

    /**
     * 파일 번호로 파일 메타정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param fileNumb 조회할 파일 번호
     * @return 파일 메타정보
     */
    FileDto getFileByNumb(Long fileNumb);
}
