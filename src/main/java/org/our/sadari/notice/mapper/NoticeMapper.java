package org.our.sadari.notice.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.notice.dto.NoticeDto;

/**
 * fileName       : NoticeMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 사용자 계정 상태와 배포 공지사항 데이터에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface NoticeMapper {

    int getActiveUserCnt(@Param("userNumb") Long userNumb, @Param("userStat") String userStat);

    List<NoticeDto> getNoticeList(@Param("userNumb") Long userNumb, @Param("viewType") String viewType
            , @Param("yes") String yes, @Param("no") String no, @Param("startRow") int startRow
            , @Param("pageSize") int pageSize);

    NoticeDto getNoticeDtl(@Param("notiNumb") Long notiNumb, @Param("yes") String yes);

    int setNoticeView(@Param("viewType") String viewType, @Param("tagtNumb") Long tagtNumb
            , @Param("userNumb") Long userNumb);
}
