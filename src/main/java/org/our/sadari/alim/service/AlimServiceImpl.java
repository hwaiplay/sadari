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
 * 알림 템플릿 조회, 치환, 사용자 알림함 저장을 공통으로 처리하는 Service 구현체이다.
 * 알림을 발생시키는 도메인은 템플릿 PK와 치환 Map만 넘기고, 이 구현체가 TB_ALTEMP와 TB_ALIMXX 접근을 책임진다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlimServiceImpl implements AlimService {

    private static final int ALIM_PAGE_SIZE = 20;

    private final AlimMapper alimMapper;
    private final PushService pushService;

    /**
     * 로그인 사용자의 알림 목록을 조회한다.
     * 인증 정보가 없으면 다른 사용자의 알림을 조회할 수 없도록 AUTH_FAIL을 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 알림 목록
     */
    @Override
    @Transactional(readOnly = true)
    public ResultData getMyAlimList(Long userNumb, int page) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        int currentPage = Math.max(page, 1);
        AlimDto.AlimListReqDto req = new AlimDto.AlimListReqDto();
        req.setUserNumb(userNumb);
        req.setPage(currentPage);
        req.setPageSize(ALIM_PAGE_SIZE);
        req.setStartRow(((currentPage - 1) * ALIM_PAGE_SIZE) + 1);
        // 다음 페이지가 있는지 판단해야 하므로 화면 표시 개수보다 1개 더 조회한다.
        req.setEndRow(currentPage * ALIM_PAGE_SIZE + 1);

        List<AlimDto.AlimItemDto> searchedList = alimMapper.getMyAlimList(req);
        if (searchedList == null) {
            searchedList = Collections.emptyList();
        }

        boolean hasNext = searchedList.size() > ALIM_PAGE_SIZE;
        List<AlimDto.AlimItemDto> visibleList = new ArrayList<>(
                hasNext ? searchedList.subList(0, ALIM_PAGE_SIZE) : searchedList
        );

        AlimDto.AlimListResDto res = new AlimDto.AlimListResDto();
        res.setList(visibleList);
        res.setHasNext(hasNext);
        res.setNextPage(currentPage + 1);
        res.setUnreadCnt(alimMapper.getUnreadAlimCnt(userNumb));

        return ResultData.success(res);
    }

    /**
     * 햄버거 메뉴 배지에서 사용할 미읽음 알림 수를 조회한다.
     * 목록 진입 전 숫자만 필요하므로 목록 조회와 읽음 처리를 함께 수행하지 않는다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 미읽음 알림 수
     */
    @Override
    @Transactional(readOnly = true)
    public ResultData getUnreadAlimCnt(Long userNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        AlimDto.AlimUnreadCntDto res = new AlimDto.AlimUnreadCntDto();
        res.setUnreadCnt(alimMapper.getUnreadAlimCnt(userNumb));
        return ResultData.success(res);
    }

    /**
     * 알림센터 항목 또는 푸시 알림을 클릭한 경우 해당 사용자의 알림 한 건을 읽음 처리한다.
     * 이미 읽은 알림에 같은 요청이 다시 들어와도 성공으로 응답하는 멱등 방식으로 처리한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param req 읽음 처리할 사용자별 알림 번호
     * @return 읽음 처리 후 남은 미읽음 알림 수
     */
    @Override
    @Transactional
    public ResultData uptAlimRead(Long userNumb, AlimDto.AlimReadReqDto req) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        if (req == null || StringUtil.isEmpty(req.getAlimNumb())) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // USER_NUMB는 요청 본문을 신뢰하지 않고 인증 정보로 덮어써 다른 사용자의 알림 변경을 차단한다.
        req.setUserNumb(userNumb);
        alimMapper.uptAlimRead(req);

        AlimDto.AlimUnreadCntDto res = new AlimDto.AlimUnreadCntDto();
        res.setUnreadCnt(alimMapper.getUnreadAlimCnt(userNumb));
        return ResultData.success(res);
    }

    /**
     * 사용자가 모두 지우기 버튼을 누르면 아직 화면에 로드하지 않은 알림까지 전부 삭제 상태로 변경한다.
     * READ_YSNO와 READ_DATE는 변경하지 않아 읽음 이력과 삭제 이력을 서로 독립적으로 유지한다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 모두 지우기 처리 결과
     */
    @Override
    @Transactional
    public ResultData delAllAlim(Long userNumb) {
        if (StringUtil.isEmpty(userNumb)) {
            return ResultData.fail(ResultEnum.AUTH_FAIL);
        }

        // 모두 지우기는 읽음 여부를 변경하지 않고 삭제 여부만 갱신하여 알림센터 노출 대상에서 제외한다.
        alimMapper.delAllAlim(userNumb);

        AlimDto.AlimUnreadCntDto res = new AlimDto.AlimUnreadCntDto();
        res.setUnreadCnt(0);
        return ResultData.success(res);
    }

    /**
     * 알림 수신자와 템플릿 식별값으로 TB_ALTEMP의 사용 가능한 템플릿을 찾고, #{key} 형식의 상용구를 Map 값으로 치환해 TB_ALIMXX에 저장한다.
     * 링크는 템플릿 테이블의 LINK_URLX를 기준으로 만들고, 호출부에서는 어떤 대상 화면으로 갈지 결정하는 대상 번호만 넘긴다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 알림을 받을 사용자 번호
     * @param alimSitu 알림 상황
     * @param tempCode 알림 템플릿 코드
     * @param tagtNumb 알림 클릭 시 이동할 대상 번호
     * @param replaceMap 템플릿 치환 값
     * @return 발송 결과
     */
    @Override
    @Transactional
    public ResultData sendAlim(Long userNumb, String alimSitu, String tempCode, Long tagtNumb, Map<String, Object> replaceMap) {
        // 수신자, 상황 코드, 템플릿 코드가 없으면 템플릿 조회와 사용자별 알림 저장 기준이 사라지므로 잘못된 요청으로 중단한다.
        if (StringUtil.isEmpty(userNumb) || StringUtil.isEmpty(alimSitu) || StringUtil.isEmpty(tempCode)) {
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 치환 문구가 없는 알림도 발송할 수 있어 null Map은 빈 Map으로 보정한다.
        // 이렇게 하면 호출부가 치환값 없는 알림을 보낼 때 불필요하게 new HashMap<>()을 만들 필요가 없다.
        Map<String, Object> safeReplaceMap = StringUtil.isEmpty(replaceMap)
                ? Collections.emptyMap()
                : replaceMap;

        // TB_ALTEMP는 알림 상황과 템플릿 코드가 복합 PK이므로 두 값을 함께 조회 조건으로 사용한다.
        AlimDto.AlimTempDto tempReq = new AlimDto.AlimTempDto();
        tempReq.setAlimSitu(alimSitu);
        tempReq.setTempCode(tempCode);
        AlimDto.AlimTempDto temp = alimMapper.getAlimTemp(tempReq);

        // 사용 가능한 템플릿이 없으면 어떤 제목/내용/링크로 발송해야 하는지 알 수 없으므로 알림을 저장하지 않는다.
        if (StringUtil.isEmpty(temp)) {
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        AlimDto.AlimItemDto alim = new AlimDto.AlimItemDto();
        alim.setUserNumb(userNumb);
        alim.setAlimSitu(alimSitu);
        alim.setTempCode(tempCode);
        alim.setAlimTitl(replaceTemplate(temp.getAlimTitl(), safeReplaceMap));
        alim.setAlimCont(replaceTemplate(temp.getTempCont(), safeReplaceMap));
        alim.setLinkUrlx(createLinkUrl(temp.getLinkUrlx(), tagtNumb));
        alim.setReadYsno(Constant.COMM_NO);
        alim.setDeltYsno(Constant.COMM_NO);

        // 최종 제목, 내용, 링크까지 완전히 같은 알림이 1시간 이내에 있으면 새 알림을 만들지 않는다.
        // 좋아요나 팔로우 버튼을 반복 조작할 때 같은 알림이 짧은 시간에 쌓이는 것을 막기 위한 공통 발송 분기이다.
        if (alimMapper.dupSameAlimInHour(alim) > 0) {
            return ResultData.success(alim);
        }

        alimMapper.setAlim(alim);
        // 브라우저가 푸시 직후 미읽음 수를 조회해도 저장된 알림을 볼 수 있도록 DB commit 이후에 발송한다.
        schedulePushAfterCommit(alim);
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
            try {
                pushService.sendPush(
                        alim.getUserNumb(),
                        alim.getAlimTitl(),
                        alim.getAlimCont(),
                        alim.getLinkUrlx(),
                        alim.getAlimNumb()
                );
            } catch (RuntimeException e) {
                // 푸시는 부가 기능이므로 commit이 끝난 알림 저장 결과에는 영향을 주지 않는다.
                log.warn("FCM push send failed after notification commit. userNumb={}", alim.getUserNumb(), e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendPush.run();
                }
            });
            return;
        }

        sendPush.run();
    }

    /**
     * 템플릿 문구의 #{key} 상용구를 replaceMap의 값으로 치환한다.
     * replaceMap에는 수신자나 링크 같은 발송 제어값을 넣지 않고 화면에 표시될 문구 치환값만 넣는다.
     *
     * @author Seunghyeon.Kang
     * @param template 템플릿 원문
     * @param replaceMap 치환 값 Map
     * @return 치환 완료 문구
     */
    private String replaceTemplate(String template, Map<String, Object> replaceMap) {
        if (StringUtil.isEmpty(template)) {
            return "";
        }

        String replacedTemplate = template;

        for (Map.Entry<String, Object> entry : replaceMap.entrySet()) {
            if (StringUtil.isEmpty(entry.getKey())) {
                continue;
            }

            replacedTemplate = replacedTemplate.replace(
                    "#{" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : String.valueOf(entry.getValue())
            );
        }

        return replacedTemplate;
    }

    /**
     * 템플릿에 저장된 기본 링크와 대상 번호를 조합해 실제 이동 URL을 만든다.
     * 예를 들어 TB_ALTEMP.LINK_URLX가 /book/detail/이고 tagtNumb가 10이면 /book/detail/10으로 저장된다.
     *
     * @author Seunghyeon.Kang
     * @param linkUrlx 템플릿에 저장된 기본 링크
     * @param tagtNumb 알림 이동 대상 번호
     * @return 실제 이동 URL
     */
    private String createLinkUrl(String linkUrlx, Long tagtNumb) {
        // 링크가 없는 템플릿은 클릭 이동을 제공하지 않는 알림으로 볼 수 있으므로 빈 문자열을 저장한다.
        if (StringUtil.isEmpty(linkUrlx)) {
            return "";
        }

        // 대상 번호가 없으면 기본 링크만 저장한다.
        // 추후 단순 공지처럼 특정 상세 번호가 없는 알림도 같은 공통 메서드를 재사용할 수 있게 하기 위한 분기이다.
        if (StringUtil.isEmpty(tagtNumb)) {
            return linkUrlx;
        }

        return linkUrlx + tagtNumb;
    }
}
