package org.our.sadari.sadariUser.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariUser.user.dto.LoginHistoryDto;

/**
 * 로그인 성공 이력을 저장하는 MyBatis Mapper입니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface LoginHistoryMapper {

    /**
     * 로그인 성공 이력을 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param loginHistoryDto 로그인 회원, IP, User-Agent, 제공자 코드 정보
     * @return 저장 건수
     */
    int setLoginHistory(LoginHistoryDto loginHistoryDto);
}
