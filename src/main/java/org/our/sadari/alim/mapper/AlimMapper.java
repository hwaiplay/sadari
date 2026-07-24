package org.our.sadari.alim.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
     * 같은 수신자, 상황, 템플릿, 제목, 내용, 링크를 가진 알림이 최근 1시간 안에 있는지 확인합니다.
     * 알림 폭주를 막기 위한 발송 직전 중복 방지 조건입니다.
     *
     * @author Seunghyeon.Kang
     * @param req 발송하려는 최종 알림 스냅샷
     * @return 최근 1시간 이내 동일 알림 수
     */
    int dupSameAlimInHour(AlimDto.AlimItemDto req);

    /**
     * 로그인 사용자의 삭제되지 않은 알림 목록을 최신순으로 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 사용자 알림 목록
     */
    List<AlimDto.AlimItemDto> getMyAlimList(AlimDto.AlimListReqDto req);

    /**
     * 화면에 실제로 노출된 알림만 읽음 처리합니다.
     * 모두 읽음과 달리 리스트에 포함된 ALIM_NUMB만 갱신해야 첫 페이지 이탈 시 20개만 읽음 처리됩니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호와 읽음 처리 대상 알림 목록
     * @return 반영 건수
     */
    int uptAlimReadByList(AlimDto.AlimReadReqDto req);

    /**
     * 로그인 사용자의 모든 미읽음 알림을 읽음 처리합니다.
     * 화면에 아직 로드하지 않은 알림까지 처리해야 하므로 목록 조건 없이 사용자 기준으로 갱신합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 반영 건수
     */
    int uptAllAlimRead(@Param("userNumb") Long userNumb);

    /**
     * 햄버거 메뉴 배지에 표시할 미읽음 알림 수를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 미읽음 알림 수
     */
    int getUnreadAlimCnt(@Param("userNumb") Long userNumb);
}
