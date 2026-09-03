package org.our.sadari.popup.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : PopupContentService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠 조회 업무 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
public interface PopupContentService {

    /**
     * 사용 화면 구분과 팝업 코드에 해당하는 사용자 안내 콘텐츠를 조회함
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 공통코드
     * @param popuCode 팝업 식별 코드
     * @return 사용자 안내 팝업 콘텐츠 조회 결과
     */
    ResultData getPopupContentDtl(String popuSitu, String popuCode);
}
