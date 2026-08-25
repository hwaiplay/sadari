package org.our.sadari.feed.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.feed.dto.FeedDto;
import org.our.sadari.social.dto.SocialDto;

/**
 * fileName       : FeedMapper
 * author         : Codex
 * date           : 2026-08-25
 * description    : 팔로잉 피드와 사진 반응 데이터 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        Codex              최초 생성
 */
@Mapper
public interface FeedMapper {

    /** 팔로잉 피드 목록을 최신 활동순으로 조회한다. */
    List<FeedDto> getFeedList(FeedDto request);

    /** 현재 프로필 또는 배경 이미지 좋아요 대상과 소유자를 조회한다. */
    SocialDto.LikeDto getImageLikeTarget(SocialDto.LikeDto request);

    /** 교체되는 사진에 연결된 댓글 좋아요를 삭제한다. */
    int delImageReplyLikes(FeedDto request);

    /** 교체되는 사진에 연결된 답글을 삭제한다. */
    int delImageChildReplies(FeedDto request);

    /** 교체되는 사진에 연결된 부모 댓글을 삭제한다. */
    int delImageParentReplies(FeedDto request);

    /** 교체되는 사진에 연결된 좋아요를 삭제한다. */
    int delImageLikes(FeedDto request);
}
