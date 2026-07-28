package org.our.sadari.user.auth.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : AuthService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-17
 * description    : 사용자 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-17        SeungHyeon.Kang    최초 생성
 */
public interface AuthService {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    ResultData kakaoLogin(String code, String lognIpxx, String userAgnt);
}
