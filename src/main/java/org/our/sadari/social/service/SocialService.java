package org.our.sadari.social.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.social.dto.SocialDto;

/**
 * fileName       : SocialService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-22
 * description    : 팔로우와 좋아요 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-22        SeungHyeon.Kang    최초 생성
 * 2026-08-04        OpenAI.Codex       프로필 통계 공개 범위 조건 추가
 */
public interface SocialService {
    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    ResultData getFollowStatus(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 관계를 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    ResultData setFollow(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    ResultData delFollow(SocialDto.FollowDto req);

    /**
     * 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호와 화면이 조회한 대상 작성자 번호
     * @return 변경 후 좋아요 상세 정보
     */
    ResultData setLike(SocialDto.LikeDto req);

    /**
     * 프로필 상단에 표시할 social 통계 값을 조회한다.
     * 다른 사용자 프로필은 공개 독후감 조건을 전달하고, 본인 화면은 조건 없이 전체 독후감을 집계한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 프로필 통계 조회 결과
     */
    ResultData getProfileStats(Long userNumb);

    /**
     * 마이페이지 기존 호출부 호환을 위해 프로필 통계 조회를 위임한다.
     * 실제 구현 기준은 getProfileStats로 통일하여 다른 사람 프로필에서도 같은 집계를 재사용한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 프로필 통계 조회 결과
     */
    ResultData getMyPageProfileStats(Long userNumb);

    /**
     * 특정 사용자가 팔로우하는 사용자 목록을 조회한다.
     * 로그인 사용자 기준 팔로우 상태를 함께 내려 모달에서 버튼명을 바로 표시한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 팔로잉 목록 조회 결과
     */
    ResultData getFollowingList(Long loginUserNumb, Long userNumb);

    /**
     * 특정 사용자를 팔로우하는 사용자 목록을 조회한다.
     * 로그인 사용자 기준 팔로우 상태를 함께 내려 모달에서 버튼명을 바로 표시한다.
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @return 팔로워 목록 조회 결과
     */
    ResultData getFollowerList(Long loginUserNumb, Long userNumb);
}
