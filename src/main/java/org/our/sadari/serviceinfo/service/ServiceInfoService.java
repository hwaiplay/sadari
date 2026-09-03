package org.our.sadari.serviceinfo.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : ServiceInfoService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 활성 사용자의 서비스 정보 카테고리와 현재 배포본 조회를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
public interface ServiceInfoService {

    /**
     * 사용자에게 노출할 서비스 정보 카테고리와 현재 배포본을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 접근하는 인증 사용자 번호
     * @return 활성 카테고리와 카테고리별 현재 배포본 조회 결과
     */
    ResultData getServiceInfoList(Long userNumb);
}
