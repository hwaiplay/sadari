package org.our.sadari.popup.service;

import lombok.RequiredArgsConstructor;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.our.sadari.popup.dto.PopupContentDto;
import org.our.sadari.popup.mapper.PopupContentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : PopupContentServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠 조회 업무 로직을 구현한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupContentServiceImpl implements PopupContentService {

    // 사용자 안내 팝업 콘텐츠 데이터 접근 객체
    private final PopupContentMapper popupContentMapper;

    /**
     * 사용 화면 구분과 팝업 코드에 해당하는 사용자 안내 콘텐츠를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 공통코드
     * @param popuCode 팝업 식별 코드
     * @return 사용자 안내 팝업 콘텐츠 조회 결과
     */
    @Override
    public ResultData getPopupContentDtl(String popuSitu, String popuCode) {
        // 복합 식별값이 모두 있어야 단건 팝업 콘텐츠를 정확히 조회할 수 있다
        if (StringUtil.hasEmpty(popuSitu, popuCode)) {
            // "요청값이 올바르지 않아요."
            return ResultData.fail(ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 팝업 사용 화면 구분과 팝업 코드로 사용자 안내 콘텐츠를 조회한다
        PopupContentDto popupContentDto = popupContentMapper.getPopupContentDtl(popuSitu, popuCode);

        // 등록된 팝업 콘텐츠가 없으면 화면이 기본 문구로 대체할 수 있도록 조회 실패를 구분한다
        if (StringUtil.isEmpty(popupContentDto)) {
            // "조회된 데이터가 없습니다."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 사용자 안내 팝업 콘텐츠 한 건을 성공 응답으로 반환한다
        return ResultData.success(popupContentDto);
    }
}
