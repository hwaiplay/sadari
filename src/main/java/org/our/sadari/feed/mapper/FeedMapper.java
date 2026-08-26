package org.our.sadari.feed.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.feed.dto.FeedDto;
import org.our.sadari.social.dto.SocialDto;

/**
 * fileName       : FeedMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-25
 * description    : 팔로잉 피드와 사진 반응 데이터 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        SeungHyeon.Kang         최초 생성
 * 2026-08-26        SeungHyeon.Kang         주석 규칙 정비
 */
@Mapper
public interface FeedMapper {

    /**
     * 로그인 사용자가 팔로우하는 활성 사용자의 공개 활동 피드를 최신순으로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param request 로그인 사용자와 피드 페이지 조건
     * @return 다음 페이지 판정용 추가 한 건을 포함한 피드 목록
     */
    List<FeedDto> getFeedList(FeedDto request);

    /**
     * 현재 프로필 또는 배경 이미지와 일치하는 활성 사용자 소유 좋아요 대상을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param request 로그인 사용자와 사진 좋아요 대상 식별값
     * @return 사진 소유자와 알림 여부를 포함한 좋아요 대상 정보
     */
    SocialDto.LikeDto getImageLikeTarget(SocialDto.LikeDto request);

    /**
     * 교체되는 프로필 또는 배경 이미지의 댓글에 연결된 좋아요를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param request 교체되는 사진 유형과 파일 번호
     * @return 삭제된 댓글 좋아요 수
     */
    int delImageReplyLikes(FeedDto request);

    /**
     * 교체되는 프로필 또는 배경 이미지의 부모 댓글에 연결된 답글을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param request 교체되는 사진 유형과 파일 번호
     * @return 삭제된 답글 수
     */
    int delImageChildReplies(FeedDto request);

    /**
     * 교체되는 프로필 또는 배경 이미지에 직접 연결된 부모 댓글을 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param request 교체되는 사진 유형과 파일 번호
     * @return 삭제된 부모 댓글 수
     */
    int delImageParentReplies(FeedDto request);

    /**
     * 교체되는 프로필 또는 배경 이미지에 직접 연결된 좋아요를 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param request 교체되는 사진 유형과 파일 번호
     * @return 삭제된 사진 좋아요 수
     */
    int delImageLikes(FeedDto request);
}
