package org.our.sadari.notice.service;

import static org.our.sadari.global.common.constant.Constant.COMM_NO;
import static org.our.sadari.global.common.constant.Constant.COMM_YES;
import static org.our.sadari.global.common.constant.Constant.USER_STAT_ACTIVE;
import static org.our.sadari.global.common.constant.Constant.VIEW_TYPE_NOTICE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.notice.dto.NoticeDto;
import org.our.sadari.notice.dto.NoticePageDto;
import org.our.sadari.notice.mapper.NoticeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : NoticeServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 활성 사용자에게 현재 배포된 공지사항만 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    // 사용자 화면의 페이지당 공지 개수
    private static final int PAGE_SIZE = 20;
    // 공지사항 데이터 접근 객체
    private final NoticeMapper noticeMapper;

    @Override
    public ResultData getNoticeList(Long userNumb, int page) {
        if (!isActiveUser(userNumb)) {
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }
        int normalizedPage = Math.max(page, 1);
        List<NoticeDto> notices = noticeMapper.getNoticeList(
                userNumb, VIEW_TYPE_NOTICE, COMM_YES, COMM_NO,
                (normalizedPage - 1) * PAGE_SIZE, PAGE_SIZE + 1
        );
        boolean hasNext = notices.size() > PAGE_SIZE;
        List<NoticeDto> currentPage = hasNext ? notices.subList(0, PAGE_SIZE) : notices;
        return ResultData.success(new NoticePageDto(currentPage, normalizedPage, hasNext));
    }

    @Override
    @Transactional
    public ResultData getNoticeDtl(Long userNumb, Long notiNumb) {
        if (!isActiveUser(userNumb)) {
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }
        if (notiNumb == null || notiNumb < 1) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }
        NoticeDto notice = noticeMapper.getNoticeDtl(notiNumb, COMM_YES);
        if (notice == null) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }
        noticeMapper.setNoticeView(VIEW_TYPE_NOTICE, notiNumb, userNumb);
        notice.setReadYsno(COMM_YES);
        return ResultData.success(notice);
    }

    @Override
    public boolean isActiveUser(Long userNumb) {
        return userNumb != null && noticeMapper.getActiveUserCnt(userNumb, USER_STAT_ACTIVE) == 1;
    }
}
