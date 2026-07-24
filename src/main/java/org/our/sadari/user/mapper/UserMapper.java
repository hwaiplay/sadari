package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.user.dto.UserDto;


/**
 * UserMapper 인터페이스에서 제공해야 하는 기능 계약을 정의한다.
 *
 * @author Seunghyeon.Kang
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

    int getUserNickDuplicateCnt(UserDto request);
}
