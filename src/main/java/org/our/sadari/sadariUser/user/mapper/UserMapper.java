package org.our.sadari.sadariUser.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.global.security.dto.TokenDto;
import org.our.sadari.sadariUser.user.dto.TokenHistoryDto;
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
    
    /**
     * 유저 회원가입
     * @param request
     * @return 결과 (0/1)
     */
    int setUser(UserDto request);

    /**
     * 기존 토큰 삭제
     * @param userNumb
     * @return 결과 (0/1)
     */
    int deleteToken(@Param("userNumb") Long userNumb);

    /**
     * 새 토큰 저장
     * @param tokenHistoryDto
     * @return 결과 (0/1)
     */
    int setToken(TokenHistoryDto tokenHistoryDto);
}