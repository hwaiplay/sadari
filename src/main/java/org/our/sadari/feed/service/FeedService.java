package org.our.sadari.feed.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : FeedService
 * author         : Codex
 * date           : 2026-08-25
 * description    : 팔로잉 피드 조회 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        Codex              최초 생성
 */
public interface FeedService {

    /** 로그인 사용자가 팔로우하는 활성 사용자의 피드를 조회한다. */
    ResultData getFeedList(Long userNumb, int page);
}
