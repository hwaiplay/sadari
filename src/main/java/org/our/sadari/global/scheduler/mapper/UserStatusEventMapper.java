package org.our.sadari.global.scheduler.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.our.sadari.global.scheduler.dto.UserStatusEventDto;

import java.util.List;

/**
 * fileName       : UserStatusEventMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 회원 상태 변경 Outbox 전달과 정지 동기화 상태 SQL을 연결한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    이벤트 삭제를 처리 완료 상태 수정으로 변경
 * 2026-07-30        SeungHyeon.Kang    처리 완료 이벤트 삭제와 정지 동기화 상태 수정으로 변경
 */
@Mapper
public interface UserStatusEventMapper {

    /**
     * 등록 순서대로 처리할 회원 상태 변경 이벤트와 현재 회원 상태를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param maxSize 한 실행에서 조회할 최대 이벤트 수
     * @return 처리 대기 회원 상태 변경 이벤트 목록
     */
    List<UserStatusEventDto> getUserStatusEventList(@Param("maxSize") int maxSize);

    /**
     * 사용자 서버 반영이 필요한 회원 상태 변경 Outbox 이벤트를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param event 등록할 회원 상태 변경 이벤트
     * @return 등록된 이벤트 수
     */
    int setUserStatusEvent(UserStatusEventDto event);

    /**
     * 최신 전달 이벤트까지 처리한 정지 이력을 사용자 서버 반영 완료 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param spndNumb 반영 완료 정지 이력 번호
     * @param evntNumb 현재 처리 중인 이벤트 번호
     * @return 반영 완료 상태로 변경된 정지 이력 수
     */
    int uptSuspensionSyncCompleted(@Param("spndNumb") Long spndNumb
                                 , @Param("evntNumb") Long evntNumb);

    /**
     * Redis 상태 동기화를 마친 Outbox 이벤트를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param evntNumb 처리 완료 이벤트 번호
     * @return 삭제된 이벤트 수
     */
    int delUserStatusEvent(@Param("evntNumb") Long evntNumb);
}
