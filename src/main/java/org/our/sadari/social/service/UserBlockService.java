package org.our.sadari.social.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : UserBlockService
 * author         : HanWon.Jang
 * date           : 2026-09-03
 * description    : 사용자 차단 등록과 해제 및 양방향 격리 판정 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-03        HanWon.Jang        최초 생성
 */
public interface UserBlockService {

    /**
     * 두 사용자 사이의 양방향 차단 여부를 조회한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 상대 사용자 번호
     * @return 어느 한 방향의 차단이라도 존재하는지 여부
     */
    boolean isBlocked(Long userNumb, Long targetUserNumb);

    /**
     * 다른 사용자를 차단하고 양방향 팔로우 관계를 삭제한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 차단 대상 사용자 번호
     * @return 차단 처리 결과
     */
    ResultData setBlock(Long userNumb, Long targetUserNumb);

    /**
     * 로그인 사용자가 만든 차단 방향을 해제한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 차단 해제 대상 사용자 번호
     * @return 차단 해제 결과
     */
    ResultData delBlock(Long userNumb, Long targetUserNumb);

    /**
     * 로그인 사용자가 차단한 사용자 목록을 페이지로 조회한다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param page 조회할 페이지 번호
     * @return 차단 사용자 페이지
     */
    ResultData getBlockList(Long userNumb, int page);

    /**
     * 팔로우 등록과 차단 등록이 경합하지 않도록 사용자 쌍을 잠근다
     *
     * @author HanWon.Jang
     * @param userNumb 로그인 사용자 번호
     * @param targetUserNumb 상대 사용자 번호
     */
    void lockUsers(Long userNumb, Long targetUserNumb);
}
