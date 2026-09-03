package org.our.sadari.notice.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.notice.dto.NoticeDto;
import org.our.sadari.notice.dto.UnreadNoticeDto;

/**
 * fileName       : NoticeMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 사용자 계정 상태와 배포 공지사항 데이터에 접근함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-19        SeungHyeon.Kang    홈 미읽음 공지 제목 조회 추가
 */
@Mapper
public interface NoticeMapper {

    /**
     * 사용자 번호와 상태가 일치하는 활성 계정 수를 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 사용자 번호
     * @param userStat 허용할 사용자 상태
     * @return 조건과 일치하는 사용자 수
     */
    int getActiveUserCnt(@Param("userNumb") Long userNumb, @Param("userStat") String userStat);

    /**
     * 현재 배포 중인 공지사항을 사용자 읽음 여부와 함께 페이지 단위로 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param viewType 공지사항 조회 이력 유형
     * @param yes 사용 및 읽음 여부의 예 값
     * @param no 읽음 여부의 아니요 값
     * @param startRow 조회 시작 행
     * @param pageSize 조회할 최대 행 수
     * @return 배포 공지사항 목록
     */
    List<NoticeDto> getNoticeList(@Param("userNumb") Long userNumb, @Param("viewType") String viewType
            , @Param("yes") String yes, @Param("no") String no, @Param("startRow") int startRow
            , @Param("pageSize") int pageSize);

    /**
     * 로그인 사용자의 읽음 이력이 없는 현재 배포 공지 제목을 조회함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param viewType 공지사항 조회 이력 유형
     * @param yes 배포 여부의 예 값
     * @return 미읽음 공지 번호와 제목 목록
     */
    List<UnreadNoticeDto> getUnreadNoticeList(@Param("userNumb") Long userNumb
                                            , @Param("viewType") String viewType
                                            , @Param("yes") String yes);

    /**
     * 공지사항 주키에 해당하는 현재 배포 버전 상세를 조회함
     *
     * @author SeungHyeon.Kang
     * @param notiNumb 조회할 공지사항 주키
     * @param userNumb 로그인 사용자 번호
     * @param viewType 공지사항 조회 이력 유형
     * @param yes 배포 및 읽음 여부의 예 값
     * @param no 읽음 여부의 아니요 값
     * @return 현재 배포 중인 공지사항 상세
     */
    NoticeDto getNoticeDtl(@Param("notiNumb") Long notiNumb, @Param("userNumb") Long userNumb
                         , @Param("viewType") String viewType, @Param("yes") String yes
                         , @Param("no") String no);

    /**
     * 로그인 사용자의 공지사항 최초 읽음 이력을 멱등하게 저장함
     *
     * @author SeungHyeon.Kang
     * @param viewType 공지사항 조회 이력 유형
     * @param tagtNumb 읽은 공지사항 주키
     * @param userNumb 로그인 사용자 번호
     * @return 저장된 읽음 이력 수
     */
    int setNoticeView(@Param("viewType") String viewType, @Param("tagtNumb") Long tagtNumb
            , @Param("userNumb") Long userNumb);
}
