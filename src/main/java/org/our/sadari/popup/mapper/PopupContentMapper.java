package org.our.sadari.popup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.popup.dto.PopupContentDto;

/**
 * fileName       : PopupContentMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠 데이터베이스 접근 메서드를 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface PopupContentMapper {

    /**
     * 사용 화면 구분과 팝업 코드에 해당하는 사용자 안내 콘텐츠를 조회함
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 공통코드
     * @param popuCode 팝업 식별 코드
     * @return 조회된 사용자 안내 팝업 콘텐츠
     */
    PopupContentDto getPopupContentDtl(@Param("popuSitu") String popuSitu, @Param("popuCode") String popuCode);
}
