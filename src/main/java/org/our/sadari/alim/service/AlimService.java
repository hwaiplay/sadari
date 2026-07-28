package org.our.sadari.alim.service;

import java.util.Map;
import org.our.sadari.alim.dto.AlimDto;
import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : AlimService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 알림 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 */
public interface AlimService {
    /**
     * 로그인 사용자의 알림 목록을 조회.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 알림 목록
     */
    ResultData getMyAlimList(Long userNumb, int page);

    /**
     * 로그인 사용자의 미읽음 알림 수를 조회.
     * 햄버거 메뉴 배지에서는 목록 조회 없이 숫자만 필요하므로 별도 메서드로 분리한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 미읽음 알림 수
     */
    ResultData getUnreadAlimCnt(Long userNumb);

    /**
     * 알림센터 항목 또는 푸시 알림을 클릭한 사용자의 알림 한 건을 읽음 처리한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req 읽음 처리할 사용자별 알림 번호
     * @return 읽음 처리 후 남은 미읽음 알림 수
     */
    ResultData uptAlimRead(Long userNumb, AlimDto.AlimReadReqDto req);

    /**
     * 로그인 사용자의 삭제되지 않은 모든 알림을 삭제 상태로 변경한다.
     * 화면에 아직 로드하지 않은 알림까지 처리해야 하는 모두 지우기 버튼에서 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 모두 지우기 처리 결과
     */
    ResultData delAllAlim(Long userNumb);

    /**
     * 알림 수신자, 상황 코드, 템플릿 코드, 이동 대상 번호, 치환 Map을 받아 사용자 알림을 발송.
     * 실제 링크는 TB_ALTEMP.LINK_URLX를 기준으로 조합하므로 호출부에서는 도메인 대상 번호만 넘긴다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 알림을 받을 사용자 번호
     * @param alimSitu 알림 상황
     * @param tempCode 알림 템플릿 코드
     * @param tagtNumb 알림 클릭 시 이동할 대상 번호
     * @param replaceMap 템플릿 치환 값
     * @return 발송 결과
     */
    ResultData sendAlim(Long userNumb, String alimSitu, String tempCode, Long tagtNumb, Map<String, Object> replaceMap);
}
