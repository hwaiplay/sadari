package org.our.sadari.alim.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.alim.dto.AlimDto;

/**
 * 알림 템플릿(TB_ALTEMP)과 사용자 알림함(TB_ALIMXX)을 조회/저장하는 MyBatis Mapper입니다.
 * 두 테이블에는 의도적으로 FK와 시퀀스를 두지 않았으므로, 템플릿 존재 여부와 사용자별 알림 번호 채번은 애플리케이션 쿼리에서 처리합니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface AlimMapper {

    /**
     * 알림 상황과 템플릿 코드로 사용 가능한 템플릿 1건을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 알림 상황, 템플릿 코드
     * @return 알림 템플릿
     */
    AlimDto.AlimTempDto getAlimTemp(AlimDto.AlimTempDto req);

    /**
     * 템플릿 내용을 치환한 뒤 사용자 알림함에 실제 발송 알림을 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 저장할 알림 정보
     * @return 반영 건수
     */
    int setAlim(AlimDto.AlimItemDto req);

    /**
     * 로그인 사용자의 삭제되지 않은 알림 목록을 최신순으로 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 사용자 알림 목록
     */
    List<AlimDto.AlimItemDto> getMyAlimList(Long userNumb);
}
