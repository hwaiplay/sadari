package org.our.sadari.notice.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : NoticeService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자의 배포 공지사항 조회 기능을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-19        SeungHyeon.Kang    홈 미읽음 공지 제목 조회 추가
 */
public interface NoticeService {

    /**
     * 현재 배포 중인 공지사항을 사용자 읽음 여부와 함께 페이지 단위로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 배포 공지사항 목록과 다음 페이지 여부
     */
    ResultData getNoticeList(Long userNumb, int page);

    /**
     * 홈 화면에 표시할 로그인 사용자의 미읽음 공지 제목 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 현재 배포 중인 미읽음 공지 제목 목록
     */
    ResultData getUnreadNoticeList(Long userNumb);

    /**
     * 현재 배포 공지 상세를 조회하고 로그인 사용자의 읽음 이력을 저장한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param notiNumb 조회할 공지사항 주키
     * @return 현재 배포 중인 공지사항 상세
     */
    ResultData getNoticeDtl(Long userNumb, Long notiNumb);

    /**
     * 공지사항 접근자가 현재 활성 사용자인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 사용자 번호
     * @return 활성 사용자 여부
     */
    boolean isActiveUser(Long userNumb);
}
