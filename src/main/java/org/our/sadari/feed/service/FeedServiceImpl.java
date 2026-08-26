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
 * author         : SeungHyeon.Kang
 * date           : 2026-08-25
 * description    : 팔로잉 피드 조회 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        SeungHyeon.Kang         최초 생성
 * 2026-08-26        SeungHyeon.Kang         주석 규칙 정비
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    // 피드 한 페이지에 화면으로 반환할 활동 수
    private static final int FEED_PAGE_SIZE = 10;

    // 피드 목록과 교류 집계 데이터 접근 객체
    private final FeedMapper feedMapper;

    /**
     * 로그인 사용자가 팔로우하는 활성 사용자의 공개 활동 피드를 페이지 단위로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 피드 페이지 번호
     * @return 팔로잉 피드 페이지 조회 결과
     */
    @Override
    public ResultData getFeedList(Long userNumb, int page) {
        // 인증 사용자 번호가 없으면 피드 공개 범위를 판정할 수 없어 조회를 중단한다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 잘못된 페이지 번호가 전달돼도 첫 페이지부터 조회하도록 보정한다
        int safePage = Math.max(page, 1);
        // 인증 사용자와 페이지 조건을 Mapper에 전달할 요청 객체를 생성한다
        FeedDto request = new FeedDto();
        // 로그인 사용자를 기준으로 팔로잉 관계와 좋아요 여부를 조회한다
        request.setLoginUserNumb(userNumb);
        // 요청 페이지 앞에 있는 피드 수만큼 조회 시작 위치를 이동한다
        request.setPageOffset((safePage - 1) * FEED_PAGE_SIZE);
        // 다음 페이지 존재 여부를 판정하기 위해 화면 표시 수보다 한 건 더 조회한다
        request.setPageLimit(FEED_PAGE_SIZE + 1);

        // 공개 범위와 페이지 조건이 적용된 팔로잉 피드를 최신 활동순으로 조회한다
        List<FeedDto> result = feedMapper.getFeedList(request);
        // 화면 표시 수를 초과한 한 건이 있으면 다음 페이지가 존재하는 것으로 판정한다
        boolean hasNext = result.size() > FEED_PAGE_SIZE;
        // 다음 페이지 판정용 추가 한 건은 화면 응답에서 제외한다
        List<FeedDto> visibleList = hasNext
                ? new ArrayList<>(result.subList(0, FEED_PAGE_SIZE))
                : result;

        // 화면 표시 목록과 현재 페이지 및 다음 페이지 여부를 공통 페이지 응답으로 반환한다
        return ResultData.success(new PageDto<>(visibleList, safePage, hasNext));
    }
}
