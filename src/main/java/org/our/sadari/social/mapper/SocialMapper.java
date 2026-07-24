package org.our.sadari.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.social.dto.SocialDto;

/**
 * 팔로우와 좋아요 기능의 SQL 접근을 담당하는 MyBatis Mapper 계약입니다.
 * 팔로우는 SocialDto.FollowDto, 좋아요는 SocialDto.LikeDto를 사용해 XML까지 같은 파라미터 구조를 유지합니다.
 *
 * @author Seunghyeon.Kang
 */
@Mapper
public interface SocialMapper {

    /**
     * 로그인 사용자와 상대 사용자 번호를 기준으로 화면에 표시할 팔로우 버튼명을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼명
     */
    String getFollowStatusName(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 TB_FOLLOW에 관계를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 반영 건수
     */
    int setFollow(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 반영 건수
     */
    int delFollow(SocialDto.FollowDto req);

    /**
     * 사용자가 해당 대상에 이미 좋아요를 눌렀는지 확인합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 중복 좋아요 수
     */
    int dupLike(SocialDto.LikeDto req);

    /**
     * 좋아요를 등록합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 반영 건수
     */
    int setLike(SocialDto.LikeDto req);

    /**
     * 좋아요를 취소합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 반영 건수
     */
    int delLike(SocialDto.LikeDto req);

    /**
     * 특정 대상에 연결된 좋아요를 모두 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 대상 유형과 대상 번호
     * @return 반영 건수
     */
    int delLikeByTarget(SocialDto.LikeDto req);

    /**
     * 좋아요 토글 후 화면에 표시할 좋아요 상태와 개수를 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 좋아요 상세 정보
     */
    SocialDto.LikeDto getLikeDtl(SocialDto.LikeDto req);
}
