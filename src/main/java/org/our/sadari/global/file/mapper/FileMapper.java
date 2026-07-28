package org.our.sadari.global.file.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.global.file.dto.FileDto;

/**
 * fileName       : FileMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-14
 * description    : 이미지 파일 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-14        SeungHyeon.Kang    최초 생성
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
