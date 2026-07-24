package org.our.sadari.alim.service;

import java.util.Map;
import org.our.sadari.global.common.result.ResultData;

/**
 * 알림 조회와 공통 발송 기능을 제공하는 Service 계약.
 * 다른 도메인 구현체는 sendAlim 메서드만 주입받아 호출하면 알림 템플릿 조회, 문구 치환, 사용자 알림함 INSERT를 공통 처리할 수 있다.
 *
 * @author Seunghyeon.Kang
 */
public interface AlimService {

    /**
     * 로그인 사용자의 알림 목록을 조회.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 알림 목록
     */
    ResultData getMyAlimList(Long userNumb);

    /**
     * 알림 수신자, 상황 코드, 템플릿 코드, 이동 대상 번호, 치환 Map을 받아 사용자 알림을 발송.
     * 실제 링크는 TB_ALTEMP.LINK_URLX를 기준으로 조합하므로 호출부에서는 도메인 대상 번호만 넘긴다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 알림을 받을 사용자 번호
     * @param alimSitu 알림 상황
     * @param tempCode 알림 템플릿 코드
     * @param tagtNumb 알림 클릭 시 이동할 대상 번호
     * @param replaceMap 템플릿 치환 값
     * @return 발송 결과
     */
    ResultData sendAlim(Long userNumb, String alimSitu, String tempCode, Long tagtNumb, Map<String, Object> replaceMap);
}
