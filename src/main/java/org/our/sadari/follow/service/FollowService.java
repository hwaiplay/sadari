package org.our.sadari.follow.service;

import org.our.sadari.global.common.result.ResultData;

/**
 * 팔로우 관계 조회, 등록, 삭제 업무를 처리하는 Service 계약입니다.
 * Controller는 인증 사용자와 상대 사용자 번호만 전달하고, 관계 검증과 DB 반영은 이 계층에서 수행합니다.
 *
 * @author Seunghyeon.Kang
 */
public interface FollowService {

    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    ResultData getFollowStatus(Long userNumb, Long flowNumb);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 관계를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    ResultData setFollow(Long userNumb, Long flowNumb);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param userNumb 로그인 사용자 번호
     * @param flowNumb 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    ResultData delFollow(Long userNumb, Long flowNumb);
}
