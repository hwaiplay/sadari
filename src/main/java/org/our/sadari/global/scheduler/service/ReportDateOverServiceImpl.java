package org.our.sadari.global.scheduler.service;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.alim.service.AlimService;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.scheduler.mapper.ReportDateOverMapper;
import org.our.sadari.report.dto.ReportDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 스케줄러 대상 조회와 목표 독서기간 초과 알림 발송 업무를 처리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Service
@Slf4j
public class ReportDateOverServiceImpl implements ReportDateOverService {

    private final ReportDateOverMapper reportDateOverMapper;
    private final AlimService alimService;
    private final int maxSize;

    /**
     * 환경별 yml의 scheduler.max-size를 주입받아 한 번에 조회할 대상 수를 결정
     * 별도 설정 객체를 만들지 않고 단일 숫자 설정을 생성자에서 직접 주입해 테스트에서도 같은 제한값을 명시할 수 있음
     *
     * @author Seunghyeon.Kang
     * @param reportDateOverMapper 목표기간 초과 대상을 제한 조회하는 Mapper
     * @param alimService 알림 저장과 FCM 푸시 발송을 담당하는 서비스
     * @param maxSize 한 번의 실행에서 조회할 최대 대상 수
     */
    public ReportDateOverServiceImpl(ReportDateOverMapper reportDateOverMapper, AlimService alimService
                                , @Value("${scheduler.max-size}") int maxSize) {
        this.reportDateOverMapper = reportDateOverMapper;
        this.alimService = alimService;
        this.maxSize = maxSize;
    }

    /**
     * yml에 설정한 최대 건수만큼 대상을 조회하고 각 대상에게 목표기간 초과 알림을 발송
     * 한 대상의 발송 실패가 나머지 대상을 중단시키지 않도록 대상 단위로 예외를 격리
     *
     * @author Seunghyeon.Kang
     */
    @Override
    public void sendReportDateOverAlim() {

        List<ReportDto> targetList = reportDateOverMapper.getReportDateOverTargetList(maxSize);
        // 리스트가 없을 시
        if (targetList.isEmpty()) {
            log.debug("목표 독서기간 초과 알림 대상이 없습니다. 최대 조회 건수={}", maxSize);
            return;
        }

        int successCnt = 0;
        int failureCnt = 0;

        for (ReportDto target : targetList) {

            try {

                //Mapper가 ReportDto에 독후감 번호, 사용자 번호, 책 제목을 함께 담아 반환한다.
                //조회한 제목을 그대로 치환 Map에 사용하므로 알림 발송 과정에서 도서 정보를 다시 조회하지 않는다.
                ResultData result = alimService.sendAlim(
                        target.getUserNumb()
                      , Constant.ALIM_SITU_REPORT
                      , Constant.ALIM_TEMP_CODE_REPORT_DATE_OVER
                      , target.getReptNumb()
                      , Map.of("bookTitl", target.getBookTitl())
                );

                if (result.getCode() == 200) {
                    successCnt++;
                    continue;
                }

                failureCnt++;
                log.warn("목표 독서기간 초과 알림 발송이 거부되었습니다. 사용자 번호={}, 독후감 번호={}, 응답 코드={}"
                      , target.getUserNumb()
                      , target.getReptNumb()
                      , result.getCode()
                );
            } catch (RuntimeException e) {

                //저장되지 않은 대상은 Mapper의 NOT EXISTS 조건을 계속 만족한다.현재 배치의 나머지 대상은 계속 처리하고, 실패한 대상은 다음 5분 실행에서 다시 시도한다.
                failureCnt++;
                log.error(
                        "목표 독서기간 초과 알림 발송 중 오류가 발생했습니다. 사용자 번호={}, 독후감 번호={}"
                      , target.getUserNumb()
                      , target.getReptNumb()
                      , e
                );
            }
        }

        log.info("목표 독서기간 초과 스케줄러가 종료되었습니다. 조회 건수={}, 성공 건수={}, 실패 건수={}, 최대 조회 건수={}"
                , targetList.size()
                , successCnt
                , failureCnt
                , maxSize
        );
    }
}
