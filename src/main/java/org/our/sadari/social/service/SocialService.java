package org.our.sadari.social.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.social.dto.SocialDto;

/**
 * fileName       : SocialService
 * author         : HanWon.Jang
 * date           : 2026-07-22
 * description    : 사용자 검색과 팔로우 및 좋아요 업무 계약을 정의함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-22        SeungHyeon.Kang    최초 생성
 * 2026-08-04        SeungHyeon.Kang       프로필 통계 공개 범위 조건 추가
 * 2026-08-26        SeungHyeon.Kang        활성 좋아요 사용자 목록 추가
 * 2026-08-28        HanWon.Jang        활성 사용자 검색 계약 추가
 * 2026-09-04        HanWon.Jang        팔로우 목록 닉네임 검색 계약 추가
 */
public interface SocialService {

    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회함
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    ResultData getFollowStatus(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 관계를 저장함
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    ResultData setFollow(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제함
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    ResultData delFollow(SocialDto.FollowDto req);

    /**
     * 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소함
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호와 화면이 조회한 대상 작성자 번호
     * @return 변경 후 좋아요 상세 정보
     */
    ResultData setLike(SocialDto.LikeDto req);

    /**
     * 프로필 상단에 표시할 social 통계 값을 조회함
     * 다른 사용자 프로필은 공개 독후감 조건을 전달하고, 본인 화면은 조건 없이 전체 독후감을 집계함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 통계를 조회하는 로그인 사용자 번호
     * @param userNumb 조회할 사용자 번호
     * @return 프로필 통계 조회 결과
     */
    ResultData getProfileStats(Long loginUserNumb, Long userNumb);

    /**
     * 마이페이지 기존 호출부 호환을 위해 프로필 통계 조회를 위임함
     * 실제 구현 기준은 getProfileStats로 통일하여 다른 사람 프로필에서도 같은 집계를 재사용함
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 사용자 번호
     * @return 프로필 통계 조회 결과
     */
    ResultData getMyPageProfileStats(Long userNumb);

    /**
     * 닉네임 검색어와 로그인 사용자 관계를 기준으로 활성 사용자 목록을 조회함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param keyword 닉네임 검색어
     * @param page 조회할 페이지 번호
     * @return 관계 우선순위가 적용된 활성 사용자 페이지
     */
    ResultData getUserSearchList(Long loginUserNumb, String keyword, int page);

    /**
     * 특정 사용자가 팔로우하는 사용자 목록을 조회함
     * 로그인 사용자 기준 팔로우 상태를 함께 내려 모달에서 버튼명을 바로 표시함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param keyword 닉네임 검색어
     * @param page 조회할 페이지 번호
     * @return 팔로잉 목록 조회 결과
     */
    ResultData getFollowingList(Long loginUserNumb, Long userNumb, String keyword, int page);

    /**
     * 특정 사용자를 팔로우하는 사용자 목록을 조회함
     * 로그인 사용자 기준 팔로우 상태를 함께 내려 모달에서 버튼명을 바로 표시함
     *
     * @author HanWon.Jang
     * @param loginUserNumb 로그인 사용자 번호
     * @param userNumb 목록 주인 사용자 번호
     * @param keyword 닉네임 검색어
     * @param page 조회할 페이지 번호
     * @return 팔로워 목록 조회 결과
     */
    ResultData getFollowerList(Long loginUserNumb, Long userNumb, String keyword, int page);

    /**
     * 특정 대상에 좋아요를 등록한 활성 사용자 목록을 조회함
     *
     * @author SeungHyeon.Kang
     * @param loginUserNumb 로그인 사용자 번호
     * @param tagtType 좋아요 대상 유형
     * @param tagtNumb 좋아요 대상 번호
     * @param page 조회할 페이지 번호
     * @return 활성 좋아요 사용자 목록
     */
    ResultData getLikeUserList(Long loginUserNumb, String tagtType, Long tagtNumb, int page);
}
