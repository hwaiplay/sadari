package org.our.sadari.serviceinfo.service;

import static org.our.sadari.global.common.constant.Constant.CODE_SERVICE_INFO_CATEGORY;
import static org.our.sadari.global.common.constant.Constant.COMM_YES;
import static org.our.sadari.global.common.constant.Constant.USER_STAT_ACTIVE;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.serviceinfo.mapper.ServiceInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ServiceInfoServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 활성 사용자에게 서비스 정보 카테고리와 현재 배포 버전만 제공함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 * 2026-09-04        SeungHyeon.Kang    개인정보처리방침 공개 조회 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceInfoServiceImpl implements ServiceInfoService {

    // 개인정보처리방침 서비스 정보 카테고리 코드
    private static final String PRIVACY_POLICY_CODE = "PRIVACY";

    // 서비스 정보 데이터 접근 객체
    private final ServiceInfoMapper serviceInfoMapper;

    /**
     * 활성 사용자에게 서비스 정보 카테고리와 현재 배포본을 제공함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 접근하는 인증 사용자 번호
     * @return 접근 검증 결과와 서비스 정보 카테고리 목록
     */
    @Override
    public ResultData getServiceInfoList(Long userNumb) {
        // 활성 회원만 설정 화면의 서비스 정보에 접근할 수 있음
        if (StringUtil.isEmpty(userNumb)
                || serviceInfoMapper.getActiveUserCnt(userNumb, USER_STAT_ACTIVE) != 1) {
            // "접근 권한이 없습니다."
            return ResultData.fail(ResultEnum.FORBIDDEN);
        }

        // 활성 카테고리와 각 카테고리의 현재 배포본을 정렬 순서대로 반환함
        return ResultData.success(serviceInfoMapper.getServiceInfoList(CODE_SERVICE_INFO_CATEGORY, COMM_YES));
    }

    /**
     * 인증 정보 없이 현재 배포 개인정보처리방침만 제공함
     *
     * @author SeungHyeon.Kang
     * @return 현재 배포된 개인정보처리방침 조회 결과
     */
    @Override
    public ResultData getPrivacyPolicy() {
        // 공개 가능한 개인정보처리방침 카테고리의 현재 배포본만 조회함
        return ResultData.success(serviceInfoMapper.getServiceInfoDtl(CODE_SERVICE_INFO_CATEGORY, PRIVACY_POLICY_CODE, COMM_YES));
    }
}
