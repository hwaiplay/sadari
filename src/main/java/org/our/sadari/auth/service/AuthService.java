package org.our.sadari.auth.service;

import org.our.sadari.auth.vo.AuthResponseVO;

public interface AuthService {

    AuthResponseVO.LoginResponse kakaoLogin(String code);
}
