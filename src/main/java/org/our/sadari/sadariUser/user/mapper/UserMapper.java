package org.our.sadari.sadariUser.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariUser.user.dto.UserDto;

/**
 * packageName    : org.our.sadari.sadariUser.mapper
 * fileName       : UserMapper
 * author         : hanwon.Jang
 * date           : 2026-05-03
 * description    : 사용자 관련 mapper
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       hanwon.Jang       최초 생성
 */

@Mapper
public interface UserMapper {
    /**
     * 유저 조회
     * @param idxx
     * @return count 
     */
    UserDto getUserByIdxx(String idxx);

    UserDto getUserByNumb(Long userNumb);
    
    /**
     * 유저 회원가입
     * @param request
     * @return 결과 (0/1)
     */
    int setUser(UserDto request);
}
