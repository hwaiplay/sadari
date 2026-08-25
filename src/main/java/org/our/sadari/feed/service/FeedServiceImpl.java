package org.our.sadari.feed.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.feed.dto.FeedDto;
import org.our.sadari.feed.mapper.FeedMapper;
import org.our.sadari.global.common.dto.PageDto;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : FeedServiceImpl
 * author         : Codex
 * date           : 2026-08-25
 * description    : 팔로잉 피드 조회 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        Codex              최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private static final int FEED_PAGE_SIZE = 10;

    private final FeedMapper feedMapper;

    /** 로그인 사용자가 팔로우하는 활성 사용자의 피드를 조회한다. */
    @Override
    public ResultData getFeedList(Long userNumb, int page) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        int safePage = Math.max(page, 1);
        FeedDto request = new FeedDto();
        request.setLoginUserNumb(userNumb);
        request.setPageOffset((safePage - 1) * FEED_PAGE_SIZE);
        request.setPageLimit(FEED_PAGE_SIZE + 1);

        List<FeedDto> result = feedMapper.getFeedList(request);
        boolean hasNext = result.size() > FEED_PAGE_SIZE;
        List<FeedDto> visibleList = hasNext
                ? new ArrayList<>(result.subList(0, FEED_PAGE_SIZE))
                : result;

        return ResultData.success(new PageDto<>(visibleList, safePage, hasNext));
    }
}
