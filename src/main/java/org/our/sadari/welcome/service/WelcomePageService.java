package org.our.sadari.welcome.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : WelcomePageService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-28
 * description    : 활성 사용자의 현재 배포 웰컴페이지 조회를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        SeungHyeon.Kang    최초 생성
 */
public interface WelcomePageService {

    /** 사용자에게 노출할 관리자 웰컴페이지 목록을 조회한다. */
    ResultData getWelcomePageList(Long userNumb);
}
