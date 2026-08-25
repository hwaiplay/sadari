package org.our.sadari.alim.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.alim.dto.AlimDto;
import org.our.sadari.alim.mapper.AlimMapper;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.push.service.PushService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * fileName       : AlimServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 알림 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 * 2026-07-29        HanWon.Jang        댓글 등록 알림 중복 차단 제외
 * 2026-08-12        SeungHyeon.Kang    알림 아이콘 처리 정리
 * 2026-08-14        SeungHyeon.Kang    사용자 알림 10개 단위 조회 반영
 * 2026-08-20        SeungHyeon.Kang    타이머 알림 중복 제외
 * 2026-08-25        HanWon.Jang        사진 댓글 알림 중복 제외
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlimServiceImpl implements AlimService {

    // 알림 페이지 크기 설정값
    private static final int ALIM_PAGE_SIZE = 10;
    // Alim 데이터 접근 객체
    private final AlimMapper alimMapper;
    // Push 업무 처리 서비스
    private final PushService pushService;

    /**
     * 로그인 사용자의 알림 목록을 조회한다.
     * 인증 정보가 없으면 다른 사용자의 알림을 조회할 수 없도록 AUTH_FAIL을 반환한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 알림 목록
     */
    @Override
    @Transactional(readOnly = true)
    public ResultData getMyAlimList(Long userNumb, int page) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 요청 크기가 최소 조회 건수보다 작지 않도록 보정한다
        int currentPage = Math.max(page, 1);
        // 알림 목록 조회 조건을 담을 객체를 생성한다
        AlimDto.AlimListReqDto req = new AlimDto.AlimListReqDto();
        // UserNumb 업무 값을 req DTO에 설정한다
        req.setUserNumb(userNumb);
        // Page 업무 값을 req DTO에 설정한다
        req.setPage(currentPage);
        // PageSize 업무 값을 req DTO에 설정한다
        req.setPageSize(ALIM_PAGE_SIZE);
        // StartRow 업무 값을 req DTO에 설정한다
        req.setStartRow(((currentPage - 1) * ALIM_PAGE_SIZE) + 1);
        // 다음 페이지가 있는지 판단해야 하므로 화면 표시 개수보다 1개 더 조회한다.
        req.setEndRow(currentPage * ALIM_PAGE_SIZE + 1);

        // MyAlimList 데이터를 DB에서 조회한다
        List<AlimDto.AlimItemDto> searchedList = alimMapper.getMyAlimList(req);
        // searchedList 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(searchedList)) {
            // 조회 결과가 없을 때 사용할 빈 목록을 생성한다
            searchedList = Collections.emptyList();
        }

        // 처리된 데이터 건수를 확인한다
        boolean hasNext = searchedList.size() > ALIM_PAGE_SIZE;
        // 요청한 페이지 크기만큼 알림 목록을 분리한 객체를 생성한다
        List<AlimDto.AlimItemDto> visibleList = new ArrayList<>(hasNext ? searchedList.subList(0, ALIM_PAGE_SIZE) : searchedList);

        // 알림 목록과 다음 페이지 여부를 담을 객체를 생성한다
        AlimDto.AlimListResDto res = new AlimDto.AlimListResDto();
        // List 업무 값을 res DTO에 설정한다
        res.setList(visibleList);
        // HasNext 업무 값을 res DTO에 설정한다
        res.setHasNext(hasNext);
        // NextPage 업무 값을 res DTO에 설정한다
        res.setNextPage(currentPage + 1);
        // UnreadCnt 업무 값을 res DTO에 설정한다
        res.setUnreadCnt(alimMapper.getUnreadAlimCnt(userNumb));
        // 로그인 사용자의 알림 목록을 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(res);
    }

    /**
     * 햄버거 메뉴 배지에서 사용할 미읽음 알림 수를 조회한다.
     * 목록 진입 전 숫자만 필요하므로 목록 조회와 읽음 처리를 함께 수행하지 않는다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 미읽음 알림 수
     */
    @Override
    @Transactional(readOnly = true)
    public ResultData getUnreadAlimCnt(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 읽지 않은 알림 건수를 담을 객체를 생성한다
        AlimDto.AlimUnreadCntDto res = new AlimDto.AlimUnreadCntDto();
        // UnreadCnt 업무 값을 res DTO에 설정한다
        res.setUnreadCnt(alimMapper.getUnreadAlimCnt(userNumb));
        // 햄버거 메뉴 배지에서 사용할 미읽음 알림 수를 조회 결과를 성공 응답으로 반환한다
        return ResultData.success(res);
    }

    /**
     * 알림센터 항목 또는 푸시 알림을 클릭한 경우 해당 사용자의 알림 한 건을 읽음 처리한다.
     * 이미 읽은 알림에 같은 요청이 다시 들어와도 성공으로 응답하는 멱등 방식으로 처리한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req 읽음 처리할 사용자별 알림 번호
     * @return 읽음 처리 후 남은 미읽음 알림 수
     */
    @Override
    @Transactional
    public ResultData uptAlimRead(Long userNumb, AlimDto.AlimReadReqDto req) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // req 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(req) || StringUtil.isEmpty(req.getAlimNumb())) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // USER_NUMB는 요청 본문을 신뢰하지 않고 인증 정보로 덮어써 다른 사용자의 알림 변경을 차단한다.
        req.setUserNumb(userNumb);
        // AlimRead 데이터를 DB에서 수정한다
        alimMapper.uptAlimRead(req);

        // 읽지 않은 알림 건수를 담을 객체를 생성한다
        AlimDto.AlimUnreadCntDto res = new AlimDto.AlimUnreadCntDto();
        // UnreadCnt 업무 값을 res DTO에 설정한다
        res.setUnreadCnt(alimMapper.getUnreadAlimCnt(userNumb));
        // 알림센터 항목 또는 푸시 알림을 클릭한 경우 해당 사용자의 알림 한 건을 읽음 처리 결과를 성공 응답으로 반환한다
        return ResultData.success(res);
    }

    /**
     * 사용자가 모두 지우기 버튼을 누르면 아직 화면에 로드하지 않은 알림까지 전부 삭제 상태로 변경한다.
     * READ_YSNO와 READ_DATE는 변경하지 않아 읽음 이력과 삭제 이력을 서로 독립적으로 유지한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 모두 지우기 처리 결과
     */
    @Override
    @Transactional
    public ResultData delAllAlim(Long userNumb) {
        // userNumb 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(userNumb)) {
            // "인증에 실패했어요.\n다시 로그인 해주세요."
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 모두 지우기는 읽음 여부를 변경하지 않고 삭제 여부만 갱신하여 알림센터 노출 대상에서 제외한다.
        alimMapper.delAllAlim(userNumb);

        // 읽지 않은 알림 건수를 담을 객체를 생성한다
        AlimDto.AlimUnreadCntDto res = new AlimDto.AlimUnreadCntDto();
        // UnreadCnt 업무 값을 res DTO에 설정한다
        res.setUnreadCnt(0);
        // 사용자가 모두 지우기 버튼을 누르면 아직 화면에 로드하지 않은 알림까지 전부 삭제 상태로 변경 결과를 성공 응답으로 반환한다
        return ResultData.success(res);
    }

    /**
     * 알림 수신자와 템플릿 식별값으로 TB_ALTEMP의 사용 가능한 템플릿을 찾고, #{key} 형식의 상용구를 Map 값으로 치환해 TB_ALIMXX에 저장한다.
     * 링크는 템플릿 테이블의 LINK_URLX를 기준으로 만들고, 호출부에서는 어떤 대상 화면으로 갈지 결정하는 대상 번호만 넘긴다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 알림을 받을 사용자 번호
     * @param alimSitu 알림 상황
     * @param tempCode 알림 템플릿 코드
     * @param tagtNumb 알림 클릭 시 이동할 대상 번호
     * @param replaceMap 템플릿 치환 값
     * @return 발송 결과
     */
    @Override
    @Transactional
    public ResultData sendAlim(Long userNumb, String alimSitu, String tempCode
                             , Long tagtNumb, Map<String, Object> replaceMap) {
        // 수신자, 상황 코드, 템플릿 코드가 없으면 템플릿 조회와 사용자별 알림 저장 기준이 사라지므로 잘못된 요청으로 중단한다.
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(alimSitu) || StringUtil.isEmpty(tempCode)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 탈퇴 또는 영구 삭제 대기 회원에게는 알림과 푸시를 새로 만들지 않는다
        if (alimMapper.getActiveAlimUserCnt(userNumb) == 0) {
            // 수신 제외 상태를 정상 생략 결과로 반환한다
            return ResultData.success();
        }

        // 치환 문구가 없는 알림도 발송할 수 있어 null Map은 빈 Map으로 보정한다.
        // 이렇게 하면 호출부가 치환값 없는 알림을 보낼 때 불필요하게 new HashMap<>()을 만들 필요가 없다.
        Map<String, Object> safeReplaceMap = StringUtil.isEmpty(replaceMap)
                ? Collections.emptyMap()
                : replaceMap;

        // TB_ALTEMP는 알림 상황과 템플릿 코드가 복합 PK이므로 두 값을 함께 조회 조건으로 사용한다.
        AlimDto.AlimTempDto tempReq = new AlimDto.AlimTempDto();
        // AlimSitu 업무 값을 tempReq DTO에 설정한다
        tempReq.setAlimSitu(alimSitu);
        // TempCode 업무 값을 tempReq DTO에 설정한다
        tempReq.setTempCode(tempCode);
        // AlimTemp 데이터를 DB에서 조회한다
        AlimDto.AlimTempDto temp = alimMapper.getAlimTemp(tempReq);

        // 사용 가능한 템플릿이 없으면 어떤 제목/내용/링크로 발송해야 하는지 알 수 없으므로 알림을 저장하지 않는다.
        if (StringUtil.isEmpty(temp)) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 알림 목록 항목을 담을 객체를 생성한다
        AlimDto.AlimItemDto alim = new AlimDto.AlimItemDto();
        // UserNumb 업무 값을 alim DTO에 설정한다
        alim.setUserNumb(userNumb);
        // AlimSitu 업무 값을 alim DTO에 설정한다
        alim.setAlimSitu(alimSitu);
        // TempCode 업무 값을 alim DTO에 설정한다
        alim.setTempCode(tempCode);
        // AlimTitl 업무 값을 alim DTO에 설정한다
        alim.setAlimTitl(replaceTemplate(temp.getAlimTitl(), safeReplaceMap));
        // AlimCont 업무 값을 alim DTO에 설정한다
        alim.setAlimCont(replaceTemplate(temp.getTempCont(), safeReplaceMap));
        // LinkUrlx 업무 값을 alim DTO에 설정한다
        alim.setLinkUrlx(createLinkUrl(temp.getLinkUrlx(), tagtNumb));
        // ReadYsno 업무 값을 alim DTO에 설정한다
        alim.setReadYsno(Constant.COMM_NO);
        // DeltYsno 업무 값을 alim DTO에 설정한다
        alim.setDeltYsno(Constant.COMM_NO);

        // 댓글은 등록 건마다 별도 이벤트이므로 같은 작성자와 독후감이어도 알림을 모두 저장한다
        boolean isReplyReportAlim = Constant.ALIM_TEMP_CODE_REPLY_REPORT.equals(tempCode)
                || Constant.ALIM_TEMP_CODE_REPLY_PROFILE_IMAGE.equals(tempCode)
                || Constant.ALIM_TEMP_CODE_REPLY_BACKGROUND_IMAGE.equals(tempCode);
        // 타이머 알림은 세션마다 별도 이벤트이므로 한 시간 안에도 각각 저장한다
        boolean isBookTimerOverAlim = Constant.ALIM_TEMP_CODE_BOOK_TIMER_OVER.equals(tempCode);

        // 좋아요와 팔로우처럼 반복 조작으로 발생할 수 있는 동일 알림만 1시간 동안 중복 차단한다
        if (!isReplyReportAlim && !isBookTimerOverAlim && alimMapper.dupSameAlimInHour(alim) > 0) {
            // 알림 수신자와 템플릿 식별값으로 TB_ALTEMP의 사용 가능한 템플릿을 찾고, #{key} 형식의 상용구를 Map 값으로 치환해 TB_ALIMXX에 저장 결과를 성공 응답으로 반환한다
            return ResultData.success(alim);
        }

        // Alim 업무 값을 alimMapper DTO에 설정한다
        alimMapper.setAlim(alim);
        // 브라우저가 푸시 직후 미읽음 수를 조회해도 저장된 알림을 볼 수 있도록 DB commit 이후에 발송한다.
        schedulePushAfterCommit(alim);
        // 알림 수신자와 템플릿 식별값으로 TB_ALTEMP의 사용 가능한 템플릿을 찾고, #{key} 형식의 상용구를 Map 값으로 치환해 TB_ALIMXX에 저장 결과를 성공 응답으로 반환한다
        return ResultData.success(alim);
    }

    /**
     * 알림 저장 트랜잭션이 완료된 뒤 FCM 푸시를 발송한다.
     * commit 전에 푸시를 보내면 열린 브라우저가 즉시 미읽음 수를 조회할 때 이전 값을 받을 수 있다.
     *
     * @param alim 저장된 알림 정보
     */
    private void schedulePushAfterCommit(AlimDto.AlimItemDto alim) {

        Runnable sendPush = () -> {
            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try {
                // sendPush 업무 로직을 pushService에 위임한다
                pushService.sendPush(
                        // getUserNumb 조회로 후속 처리에 필요한 데이터를 가져온다
                        alim.getUserNumb(),
                        // getAlimTitl 조회로 후속 처리에 필요한 데이터를 가져온다
                        alim.getAlimTitl(),
                        // getAlimCont 조회로 후속 처리에 필요한 데이터를 가져온다
                        alim.getAlimCont(),
                        // getLinkUrlx 조회로 후속 처리에 필요한 데이터를 가져온다
                        alim.getLinkUrlx(),
                        // getAlimNumb 조회로 후속 처리에 필요한 데이터를 가져온다
                        alim.getAlimNumb()
                );
            }

            // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
            catch (RuntimeException e) {
                // 푸시는 부가 기능이므로 commit이 끝난 알림 저장 결과에는 영향을 주지 않는다.
                log.warn("FCM push send failed after notification commit. userNumb={}", alim.getUserNumb(), e);
            }
        };

        // 요청값이 업무에서 허용한 범위와 상태를 만족하는지 구분한다
        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /**
                 * 현재 트랜잭션이 커밋된 후 예약된 후처리를 실행한다
                 *
                 * @author SeungHyeon.Kang
                 * @return 반환값이 없다
                 */
                @Override
                public void afterCommit() {
                    // 검증 대상 작업을 실행한다
                    sendPush.run();
                }
            });

            // 현재 트랜잭션이 커밋된 후 예약된 후처리를 실행 결과를 반환한다
            return;
        }

        // 검증 대상 작업을 실행한다
        sendPush.run();
    }

    /**
     * 템플릿 문구의 #{key} 상용구를 replaceMap의 값으로 치환한다.
     * replaceMap에는 수신자나 링크 같은 발송 제어값을 넣지 않고 화면에 표시될 문구 치환값만 넣는다.
     *
     * @author SeungHyeon.Kang
     * @param template 템플릿 원문
     * @param replaceMap 치환 값 Map
     * @return 치환 완료 문구
     */
    private String replaceTemplate(String template, Map<String, Object> replaceMap) {
        // template 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        if (StringUtil.isEmpty(template)) {
            // 템플릿 문구의 #{key} 상용구를 replaceMap의 값으로 치환 결과를 반환한다
            return "";
        }

        String replacedTemplate = template;

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (Map.Entry<String, Object> entry : replaceMap.entrySet()) {
            // entry.getKey( 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
            if (StringUtil.isEmpty(entry.getKey())) {

                continue;
            }

            // 대상 문자열에서 지정한 값을 치환한다
            replacedTemplate = replacedTemplate.replace(
                    // 현재 항목의 키를 조회한다
                    "#{" + entry.getKey() + "}",
                    // 필수 값이 비어 있는지 공통 기준으로 확인한다
                    StringUtil.isEmpty(entry.getValue()) ? "" : String.valueOf(entry.getValue())
            );
        }

        // 템플릿 문구의 #{key} 상용구를 replaceMap의 값으로 치환 결과를 반환한다
        return replacedTemplate;
    }

    /**
     * 템플릿에 저장된 기본 링크와 대상 번호를 조합해 실제 이동 URL을 만든다.
     * 예를 들어 TB_ALTEMP.LINK_URLX가 /report/detail/이고 tagtNumb가 10이면 /report/detail/10으로 저장된다.
     *
     * @author SeungHyeon.Kang
     * @param linkUrlx 템플릿에 저장된 기본 링크
     * @param tagtNumb 알림 이동 대상 번호
     * @return 실제 이동 URL
     */
    private String createLinkUrl(String linkUrlx, Long tagtNumb) {
        // 링크가 없는 템플릿은 클릭 이동을 제공하지 않는 알림으로 볼 수 있으므로 빈 문자열을 저장한다.
        if (StringUtil.isEmpty(linkUrlx)) {
            // 템플릿에 저장된 기본 링크와 대상 번호를 조합해 실제 이동 URL을 만든다 결과를 반환한다
            return "";
        }

        // 대상 번호가 없으면 기본 링크만 저장한다.
        // 추후 단순 공지처럼 특정 상세 번호가 없는 알림도 같은 공통 메서드를 재사용할 수 있게 하기 위한 분기이다.
        if (StringUtil.isEmpty(tagtNumb)) {
            // 템플릿에 저장된 기본 링크와 대상 번호를 조합해 실제 이동 URL을 만든다 결과를 반환한다
            return linkUrlx;
        }

        // 템플릿에 저장된 기본 링크와 대상 번호를 조합해 실제 이동 URL을 만든다 결과를 반환한다
        return linkUrlx + tagtNumb;
    }
}
