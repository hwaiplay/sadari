package org.our.sadari.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.social.dto.SocialDto;

/**
 * fileName       : SocialMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-22
 * description    : 팔로우와 좋아요 데이터베이스 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-22        SeungHyeon.Kang    최초 생성
 * 2026-08-04        OpenAI.Codex       프로필 통계 공개 범위 조건 문서화
 */
@Mapper
public interface SocialMapper {
    /**
     * 로그인 사용자와 상대 사용자 번호를 기준으로 화면에 표시할 팔로우 버튼명을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼명
     */
    String getFollowStatusName(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 관계를 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 반영 건수
     */
    int setFollow(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 반영 건수
     */
    int delFollow(SocialDto.FollowDto req);

    /**
     * 사용자가 해당 대상에 이미 좋아요를 눌렀는지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 중복 좋아요 수
     */
    int dupLike(SocialDto.LikeDto req);

    /**
     * 좋아요를 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 반영 건수
     */
    int setLike(SocialDto.LikeDto req);

    /**
     * 좋아요를 취소한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 반영 건수
     */
    int delLike(SocialDto.LikeDto req);

    /**
     * 특정 대상에 연결된 좋아요를 모두 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param req 대상 유형과 대상 번호
     * @return 반영 건수
     */
    int delLikeByTarget(SocialDto.LikeDto req);

    /**
     * 좋아요 토글 후 화면에 표시할 좋아요 상태와 개수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 좋아요 상세 정보
     */
    SocialDto.LikeDto getLikeDtl(SocialDto.LikeDto req);

    /**
     * 프로필 통계에 표시할 총 읽은 책, 팔로우, 팔로워, 받은 좋아요 수를 한 번에 조회한다.
     * 다른 사용자 프로필은 공개 독후감 조건을 적용하고 본인 마이페이지는 전체 독후감 집계를 유지한다.
     *
     * @author SeungHyeon.Kang
     * @param req 사용자 번호와 선택적 독후감 공개 여부
     * @return 프로필 통계
     */
    SocialDto.ProfileStatsDto getProfileStats(SocialDto.ProfileStatsDto req);

    /**
     * 특정 사용자가 팔로우하는 사용자 목록을 조회한다.
     * 각 행에는 로그인 사용자 기준 팔로우 상태를 함께 내려 화면 오른쪽 버튼 상태를 별도 API 없이 표시한다.
     *
     * @author SeungHyeon.Kang
     * @param req 목록 주인 사용자 번호와 로그인 사용자 번호
     * @return 팔로잉 사용자 목록
     */
    java.util.List<SocialDto.FollowUserDto> getFollowingList(SocialDto.FollowListReqDto req);

    /**
     * 특정 사용자를 팔로우하는 사용자 목록을 조회한다.
     * 각 행에는 로그인 사용자 기준 팔로우 상태를 함께 내려 화면 오른쪽 버튼 상태를 별도 API 없이 표시한다.
     *
     * @author SeungHyeon.Kang
     * @param req 목록 주인 사용자 번호와 로그인 사용자 번호
     * @return 팔로워 사용자 목록
     */
    java.util.List<SocialDto.FollowUserDto> getFollowerList(SocialDto.FollowListReqDto req);
}
