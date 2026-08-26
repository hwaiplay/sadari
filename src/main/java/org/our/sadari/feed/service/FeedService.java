package org.our.sadari.feed.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : FeedService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-25
 * description    : 팔로잉 피드 조회 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        SeungHyeon.Kang         최초 생성
 * 2026-08-26        SeungHyeon.Kang         주석 규칙 정비
 */
public interface FeedService {

    /**
     * 로그인 사용자가 팔로우하는 활성 사용자의 공개 활동 피드를 페이지 단위로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 피드 페이지 번호
     * @return 팔로잉 피드 페이지 조회 결과
     */
    ResultData getFeedList(Long userNumb, int page);
}
