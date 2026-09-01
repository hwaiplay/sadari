package org.our.sadari.alim.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.alim.dto.AlimDto;
import org.our.sadari.user.dto.UserSettingDto;

/**
 * fileName       : AlimMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 알림 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 * 2026-08-27        SeungHyeon.Kang    알림번호 기반 현재 접근 상태 조회 추가
 */
@Mapper
public interface AlimMapper {
    /**
     * 알림 상황과 템플릿 코드로 사용 가능한 템플릿 1건을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 알림 상황, 템플릿 코드
     * @return 알림 템플릿
     */
    AlimDto.AlimTempDto getAlimTemp(AlimDto.AlimTempDto req);

    /**
     * 템플릿 내용을 치환한 뒤 사용자 알림함에 실제 발송 알림을 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param req 저장할 알림 정보
     * @return 반영 건수
     */
    int setAlim(AlimDto.AlimItemDto req);

    /**
     * 같은 수신자, 상황, 템플릿, 제목, 내용과 이동 대상 정보를 가진 알림이 최근 1시간 안에 있는지 확인한다.
     * 알림 폭주를 막기 위한 발송 직전 중복 방지 조건이다.
     *
     * @author SeungHyeon.Kang
     * @param req 발송하려는 최종 알림 스냅샷
     * @return 최근 1시간 이내 동일 알림 수
     */
    int dupSameAlimInHour(AlimDto.AlimItemDto req);

    /**
     * 로그인 사용자의 삭제되지 않은 알림 목록을 최신순으로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호
     * @return 사용자 알림 목록
     */
    List<AlimDto.AlimItemDto> getMyAlimList(AlimDto.AlimListReqDto req);

    /**
     * 인증 사용자의 알림번호로 원본 콘텐츠와 현재 접근 관계를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자와 사용자별 알림 번호
     * @return 알림 원본 콘텐츠와 현재 접근 상태
     */
    AlimDto.AlimTargetDto getAlimTargetDtl(AlimDto.AlimTargetDto req);

    /**
     * 사용자가 클릭한 알림 한 건을 읽음 처리한다.
     * 사용자별 알림 번호이므로 인증 사용자 번호와 ALIM_NUMB를 함께 조건으로 사용한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 읽음 처리할 알림 번호
     * @return 반영 건수
     */
    int uptAlimRead(AlimDto.AlimReadReqDto req);

    /**
     * 로그인 사용자의 삭제되지 않은 모든 알림을 삭제 상태로 변경한다.
     * 화면에 아직 로드하지 않은 알림까지 처리해야 하므로 사용자 번호만으로 갱신한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 반영 건수
     */
    int delAllAlim(@Param("userNumb") Long userNumb);

    /**
     * 햄버거 메뉴 배지에 표시할 미읽음 알림 수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @return 미읽음 알림 수
     */
    int getUnreadAlimCnt(@Param("userNumb") Long userNumb);

    /**
     * 알림 수신 회원이 정상 이용 상태인지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 알림 수신 회원 번호
     * @return 정상 이용 회원 수
     */
    int getActiveAlimUserCnt(@Param("userNumb") Long userNumb);

    /** 알림 생성 여부를 판단할 수신자 설정을 조회한다. */
    UserSettingDto getUserAlimSetting(@Param("userNumb") Long userNumb);
}
