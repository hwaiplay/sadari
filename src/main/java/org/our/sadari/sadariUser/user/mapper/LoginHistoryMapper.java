package org.our.sadari.sadariUser.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.sadariUser.user.dto.LoginHistoryDto;

/**
 * packageName    : org.our.sadari.sadariUser.user.mapper
 * fileName       : LoginHistoryMapper.java
 * author         : Seunghyeon.Kang
 * date           : 2026-07-13
 * description    : 로그인 성공 이력을 TB_LOGHIS 테이블에 저장하는 Mapper
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-13        Seunghyeon.Kang    TB_TOKHIS 토큰 이력 저장 구조를 TB_LOGHIS 로그인 이력 저장 구조로 전환
 */
@Mapper
public interface LoginHistoryMapper {

    /**
     * 로그인 성공 이력을 저장한다.
     * JWT와 Redis가 실제 인증 토큰 상태를 관리하므로 DB에는 토큰 원문을 저장하지 않는다.
     * 대신 사용자 번호, 로그인 일시, IP, User-Agent, 로그인 공급자 코드를 남겨 사후 접속 이력 확인에 사용한다.
     *
     * @Author Seunghyeon.Kang
     * @param loginHistoryDto 로그인 사용자 번호, 로그인 일시, IP, User-Agent, 공급자 코드를 담은 DTO
     * @return 저장된 로그인 이력 건수
     */
    int setLoginHistory(LoginHistoryDto loginHistoryDto);
}
