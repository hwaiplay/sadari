package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.user.dto.LoginHistoryDto;

/**
 * LoginHistoryMapper 인터페이스에서 제공해야 하는 기능 계약을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface LoginHistoryMapper {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int setLoginHistory(LoginHistoryDto loginHistoryDto);
}
