package org.our.sadari.global.file.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
 * 2026-08-06        SeungHyeon.Kang    파일 교체와 영구 탈퇴 삭제 쿼리 추가
 */
@Mapper
public interface FileMapper {

    /**
     * 업로드한 이미지 파일의 저장 메타정보를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param fileDto 등록할 파일 메타정보
     * @return 등록된 파일 메타정보 수
     */
    int setFile(FileDto fileDto);

    /**
     * 파일 번호로 이미지 파일 메타정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param fileNumb 조회할 파일 번호
     * @return 조회된 파일 메타정보
     */
    FileDto getFileByNumb(Long fileNumb);

    /**
     * 현재 활성 회원의 프로필 또는 배경으로 참조되는 공개 파일 수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param storName 서버가 생성한 저장 파일명
     * @param filePath 공개 업로드 경로
     * @return 공개 조회 가능한 파일 수
     */
    int getActivePublicFileCount(@Param("storName") String storName, @Param("filePath") String filePath);

    /**
     * 현재 활성 회원의 프로필 또는 배경으로 참조되는 파일 소유자 번호를 조회한다
     *
     * @author HanWon.Jang
     * @param storName 서버가 생성한 저장 파일명
     * @param filePath 공개 업로드 경로
     * @return 현재 공개 이미지 소유자 번호이며 없으면 null
     */
    Long getActivePublicFileOwner(@Param("storName") String storName, @Param("filePath") String filePath);

    /**
     * 파일 등록 사용자 번호로 영구 삭제할 파일 메타정보를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param regiUser 파일을 등록한 사용자 번호
     * @return 사용자가 등록한 파일 메타정보 목록
     */
    List<FileDto> getFileListByRegiUser(Long regiUser);

    /**
     * 사용자 프로필과 배경에서 참조하지 않는 파일 메타정보를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param fileNumb 삭제할 파일 번호
     * @return 삭제된 파일 메타정보 수
     */
    int delFileIfUnreferenced(Long fileNumb);
}
