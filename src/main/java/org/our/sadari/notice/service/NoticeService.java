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
 */
public interface NoticeService {

    ResultData getNoticeList(Long userNumb, int page);

    ResultData getNoticeDtl(Long userNumb, Long notiNumb);

    boolean isActiveUser(Long userNumb);
}
