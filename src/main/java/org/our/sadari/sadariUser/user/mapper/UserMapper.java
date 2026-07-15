package org.our.sadari.sadariUser.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariUser.user.dto.UserDto;


/**
 * 회원 기본 정보 조회, 등록, 프로필 수정을 담당하는 MyBatis Mapper입니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface UserMapper {

    /**
     * 소셜 제공자 ID로 회원 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param idxx 소셜 제공자 사용자 ID
     * @return 회원 정보
     */
    UserDto getUserByIdxx(String idxx);

    /**
     * 회원 번호로 회원 정보를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 회원 정보
     */
    UserDto getUserByNumb(Long userNumb);

    /**
     * 신규 회원을 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param request 등록할 회원 정보
     * @return 등록 건수
     */
    int setUser(UserDto request);

    /**
     * 회원 프로필 정보를 수정합니다.
     *
     * @author Seunghyeon.Kang
     * @param request 수정할 회원 프로필 정보
     * @return 수정 건수
     */
    int uptUserProfile(UserDto request);
}
