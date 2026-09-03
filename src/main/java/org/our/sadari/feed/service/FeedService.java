package org.our.sadari.feed.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : FeedService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-25
 * description    : 본인과 팔로잉 피드 조회 업무 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        SeungHyeon.Kang         최초 생성
 * 2026-08-26        SeungHyeon.Kang         주석 규칙 정비
 * 2026-08-27        SeungHyeon.Kang         본인 피드와 알림 대상 단건 조회 계약 추가
 */
public interface FeedService {

    /**
     * 로그인 사용자 본인과 팔로우하는 활성 사용자의 공개 활동 피드를 페이지 단위로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 피드 페이지 번호
     * @return 본인과 팔로잉 피드 페이지 조회 결과
     */
    ResultData getFeedList(Long userNumb, int page);

    /**
     * 알림 링크가 지정한 현재 공개 피드 대상 한 건을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param tagtType 조회할 피드 대상 유형
     * @param tagtNumb 조회할 피드 대상 번호
     * @return 현재 공개 상태인 알림 이동 대상 피드 항목 조회 결과
     */
    ResultData getFeedDtl(Long userNumb, String tagtType, Long tagtNumb);
}
