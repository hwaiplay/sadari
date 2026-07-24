package org.our.sadari.social.service;

import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.social.dto.SocialDto;

/**
 * 팔로우 관계와 좋아요 등록, 취소 업무를 처리하는 Service 계약입니다.
 * Controller에서 인증 사용자 번호를 DTO에 채운 뒤 전달하고, Service는 검증과 트랜잭션 처리를 담당합니다.
 *
 * @author Seunghyeon.Kang
 */
public interface SocialService {

    /**
     * 로그인 사용자와 상대 사용자 사이의 팔로우 버튼명을 조회합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 팔로우 버튼 상태 조회 결과
     */
    ResultData getFollowStatus(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우하도록 관계를 저장합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 저장 후 팔로우 버튼 상태 조회 결과
     */
    ResultData setFollow(SocialDto.FollowDto req);

    /**
     * 로그인 사용자가 상대 사용자를 팔로우 중인 관계를 삭제합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 로그인 사용자 번호와 상대 사용자 번호
     * @return 삭제 후 팔로우 버튼 상태 조회 결과
     */
    ResultData delFollow(SocialDto.FollowDto req);

    /**
     * 대상 유형과 대상 번호 기준으로 좋아요를 등록하거나 취소합니다.
     *
     * @author Seunghyeon.Kang
     * @param req 사용자 번호, 대상 유형, 대상 번호
     * @return 변경 후 좋아요 상세 정보
     */
    ResultData setLike(SocialDto.LikeDto req);
}
