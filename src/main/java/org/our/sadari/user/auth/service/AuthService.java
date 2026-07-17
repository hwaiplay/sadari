package org.our.sadari.user.auth.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * AuthService 인터페이스에서 제공해야 하는 기능 계약을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public interface AuthService {

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    ResultData kakaoLogin(String code, String lognIpxx, String userAgnt);
}
