package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.user.dto.UserDto;


/**
 * fileName       : UserMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface UserMapper {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    UserDto getUserByIdxx(@Param("userIdxx") String userIdxx);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    UserDto getUserByNumb(Long userNumb);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int setUser(UserDto request);

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int uptUserProfile(UserDto request);

    // getUserNickDuplicateCnt 조회로 후속 처리에 필요한 데이터를 가져온다
    int getUserNickDuplicateCnt(UserDto request);
}
