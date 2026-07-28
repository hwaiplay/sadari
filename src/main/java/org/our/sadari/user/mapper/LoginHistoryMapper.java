package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.user.dto.LoginHistoryDto;

/**
 * fileName       : LoginHistoryMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface LoginHistoryMapper {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    int setLoginHistory(LoginHistoryDto loginHistoryDto);
}
