package org.our.sadari.notice.service;

import static org.our.sadari.global.common.constant.Constant.COMM_NO;
import static org.our.sadari.global.common.constant.Constant.COMM_YES;
import static org.our.sadari.global.common.constant.Constant.USER_STAT_ACTIVE;
import static org.our.sadari.global.common.constant.Constant.VIEW_TYPE_NOTICE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.notice.dto.NoticeDto;
import org.our.sadari.notice.dto.NoticePageDto;
import org.our.sadari.notice.dto.UnreadNoticeDto;
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
 * 2026-08-14        SeungHyeon.Kang    사용자 공지사항 10개 단위 조회 반영
 * 2026-08-19        SeungHyeon.Kang    홈 미읽음 공지 제목 조회 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    // 사용자 화면의 페이지당 공지 개수
    private static final int PAGE_SIZE = 10;
    // 공지사항 데이터 접근 객체
    private final NoticeMapper noticeMapper;

    /**
     * 현재 배포 중인 공지사항을 사용자 읽음 여부와 함께 페이지 단위로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 배포 공지사항 목록과 다음 페이지 여부
     */
    @Override
    public ResultData getNoticeList(Long userNumb, int page) {
        // 비활성 계정은 공지 목록과 사용자별 읽음 이력에 접근하지 못하게 한다
        if (!isActiveUser(userNumb)) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 1보다 작은 페이지 요청은 첫 페이지 조회로 보정한다
        int normalizedPage = Math.max(page, 1);
        // 현재 페이지와 다음 페이지 존재 여부를 함께 판단할 수 있는 배포 공지를 조회한다
        List<NoticeDto> notices = noticeMapper.getNoticeList(
                userNumb, VIEW_TYPE_NOTICE, COMM_YES, COMM_NO,
                (normalizedPage - 1) * PAGE_SIZE, PAGE_SIZE + 1
        );

        // 배포 공지가 없으면 빈 현재 페이지를 정상 응답으로 제공한다
        if (StringUtil.isEmpty(notices)) {
            // 공지사항이 없는 현재 페이지 응답을 반환한다
            return ResultData.success(new NoticePageDto(List.of(), normalizedPage, false));
        }

        boolean hasNext = notices.size() > PAGE_SIZE;
        // 다음 페이지 판정용 추가 행은 현재 화면 목록에서 제외한다
        List<NoticeDto> currentPage = hasNext ? notices.subList(0, PAGE_SIZE) : notices;
        // 사용자 읽음 여부가 포함된 현재 배포 공지 페이지를 반환한다
        return ResultData.success(new NoticePageDto(currentPage, normalizedPage, hasNext));
    }

    /**
     * 홈 화면에 표시할 로그인 사용자의 미읽음 공지 제목 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 현재 배포 중인 미읽음 공지 제목 목록
     */
    @Override
    public ResultData getUnreadNoticeList(Long userNumb) {
        // 비활성 계정은 홈 미읽음 공지와 사용자별 읽음 이력에 접근하지 못하게 한다
        if (!isActiveUser(userNumb)) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 현재 배포 공지 중 로그인 사용자의 읽음 이력이 없는 제목만 조회한다
        List<UnreadNoticeDto> notices = noticeMapper.getUnreadNoticeList(userNumb, VIEW_TYPE_NOTICE, COMM_YES);

        // 미읽음 공지가 없으면 홈에서 슬라이드를 숨길 수 있도록 빈 목록을 제공한다
        if (StringUtil.isEmpty(notices)) {
            // 미읽음 공지가 없는 정상 응답을 반환한다
            return ResultData.success(List.of());
        }

        // 홈 제목 슬라이드가 순환할 미읽음 공지 목록을 반환한다
        return ResultData.success(notices);
    }

    /**
     * 현재 배포 공지 상세와 로그인 사용자의 기존 읽음 여부를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param notiNumb 조회할 공지사항 주키
     * @return 현재 배포 중인 공지사항 상세
     */
    @Override
    public ResultData getNoticeDtl(Long userNumb, Long notiNumb) {
        // 비활성 계정은 공지 상세와 사용자별 읽음 여부 조회를 차단한다
        if (!isActiveUser(userNumb)) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 유효한 양수 공지사항 주키만 상세 조회에 사용한다
        if (StringUtil.isEmpty(notiNumb) || notiNumb < 1) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 공지사항 주키에 해당하는 현재 배포 버전 상세를 조회한다
        NoticeDto notice = noticeMapper.getNoticeDtl(
                notiNumb, userNumb, VIEW_TYPE_NOTICE, COMM_YES, COMM_NO);

        // 현재 배포 버전이 없으면 데이터 없음으로 반환한다
        if (StringUtil.isEmpty(notice)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 저장 부작용 없이 기존 읽음 여부가 포함된 현재 배포 공지 상세를 반환한다
        return ResultData.success(notice);
    }

    /**
     * 현재 배포 공지에 로그인 사용자의 읽음 이력을 저장한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param notiNumb 읽은 공지사항 주키
     * @return 읽음 이력 저장 결과
     */
    @Override
    @Transactional
    public ResultData setNoticeView(Long userNumb, Long notiNumb) {
        // 비활성 계정은 신규 읽음 이력 생성을 차단한다
        if (!isActiveUser(userNumb)) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 유효한 양수 공지사항 주키만 읽음 이력에 사용한다
        if (StringUtil.isEmpty(notiNumb) || notiNumb < 1) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 현재 배포 중인 공지에 대해서만 읽음 이력을 허용한다
        NoticeDto notice = noticeMapper.getNoticeDtl(
                notiNumb, userNumb, VIEW_TYPE_NOTICE, COMM_YES, COMM_NO);
        // 현재 배포 버전이 없으면 읽음 이력을 만들지 않는다
        if (StringUtil.isEmpty(notice)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 로그인 사용자의 최초 읽음 이력을 멱등하게 저장한다
        noticeMapper.setNoticeView(VIEW_TYPE_NOTICE, notiNumb, userNumb);
        // 읽음 이력 저장 성공 응답을 반환한다
        return ResultData.success();
    }

    /**
     * 공지사항 접근자가 현재 활성 사용자인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 사용자 번호
     * @return 활성 사용자 여부
     */
    @Override
    public boolean isActiveUser(Long userNumb) {
        // 인증 사용자 번호가 없으면 계정 조회 없이 접근을 거부한다
        if (StringUtil.isEmpty(userNumb)) {
            // 인증되지 않은 사용자를 비활성 상태로 판정한다
            return false;
        }

        // 사용자 번호가 현재 활성 상태인지 데이터베이스에서 확인한다
        int activeUserCnt = noticeMapper.getActiveUserCnt(userNumb, USER_STAT_ACTIVE);
        // 정확히 한 명의 활성 사용자와 일치할 때만 공지 접근을 허용한다
        return activeUserCnt == 1;
    }
}
