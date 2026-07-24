package org.our.sadari.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.social.dto.SocialDto;

/**
 * 팔로우와 좋아요 기능의 SQL 접근을 담당하는 MyBatis Mapper 계약이다.
 * 팔로우는 SocialDto.FollowDto, 좋아요는 SocialDto.LikeDto를 사용해 XML까지 같은 파라미터 구조를 유지한다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface SocialMapper {

    /**
     * 로그인 사용자와 상대 사용자 번호를 기준으로 화면에 표시할 팔로우 버튼명을 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼명
     */
    String getFollowStatusName(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 관계를 저장한다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 반영 건수
     */
    int setFollow(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제한다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 반영 건수
     */
    int delFollow(SocialDto.FollowDto req);

    /**
     * 사용자가 해당 대상에 이미 좋아요를 눌렀는지 확인한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 중복 좋아요 수
     */
    int dupLike(SocialDto.LikeDto req);

    /**
     * 좋아요를 등록한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 반영 건수
     */
    int setLike(SocialDto.LikeDto req);

    /**
     * 좋아요를 취소한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 반영 건수
     */
    int delLike(SocialDto.LikeDto req);

    /**
     * 특정 대상에 연결된 좋아요를 모두 삭제한다.
     *
     * @author Seunghyeon.Kang
     * @param req 대상 유형과 대상 번호
     * @return 반영 건수
     */
    int delLikeByTarget(SocialDto.LikeDto req);

    /**
     * 좋아요 토글 후 화면에 표시할 좋아요 상태와 개수를 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 좋아요 상세 정보
     */
    SocialDto.LikeDto getLikeDtl(SocialDto.LikeDto req);

    /**
     * 독후감 좋아요 알림 발송에 필요한 수신자와 발송자 닉네임을 조회한다.
     * 좋아요 자체는 공용 테이블에서 처리하지만, REPORT 좋아요 알림은 독후감 작성자에게 보내야 하므로 TM_REPORT와 TM_USERXM을 함께 조회한다.
     *
     * @author Seunghyeon.Kang
     * @param req 좋아요 요청 DTO
     * @return 알림 발송 대상 정보
     */
    SocialDto.LikeDto getReportLikeAlimInfo(SocialDto.LikeDto req);

    /**
     * 마이페이지 프로필 통계에 표시할 총 읽은 책, 팔로우, 팔로워, 받은 좋아요 수를 한 번에 조회한다.
     * 해당 집계는 social 영역에서 관리하는 팔로우/좋아요 데이터를 포함하므로 MyPageController가 직접 SQL을 알지 않도록 분리한다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호
     * @return 마이페이지 프로필 통계
     */
    SocialDto.ProfileStatsDto getProfileStats(SocialDto.ProfileStatsDto req);

    /**
     * 특정 사용자가 팔로우하는 사용자 목록을 조회한다.
     * 각 행에는 로그인 사용자 기준 팔로우 상태를 함께 내려 화면 오른쪽 버튼 상태를 별도 API 없이 표시한다.
     *
     * @author Seunghyeon.Kang
     * @param req 목록 주인 사용자 번호와 로그인 사용자 번호
     * @return 팔로잉 사용자 목록
     */
    java.util.List<SocialDto.FollowUserDto> getFollowingList(SocialDto.FollowListReqDto req);

    /**
     * 특정 사용자를 팔로우하는 사용자 목록을 조회한다.
     * 각 행에는 로그인 사용자 기준 팔로우 상태를 함께 내려 화면 오른쪽 버튼 상태를 별도 API 없이 표시한다.
     *
     * @author Seunghyeon.Kang
     * @param req 목록 주인 사용자 번호와 로그인 사용자 번호
     * @return 팔로워 사용자 목록
     */
    java.util.List<SocialDto.FollowUserDto> getFollowerList(SocialDto.FollowListReqDto req);
}