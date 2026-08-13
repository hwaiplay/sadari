package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.our.sadari.user.dto.UserWithdrawalDto;

/**
 * fileName       : UserWithdrawalMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-29
 * description    : 회원 탈퇴 상태와 연관 데이터 변경 SQL을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-29        SeungHyeon.Kang    최초 생성
 * 2026-08-03        HanWon.Jang        탈퇴 회원의 댓글 좋아요 삭제 메서드 추가
 * 2026-08-14        SeungHyeon.Kang    탈퇴 회원의 모임원 프로필 숨김 메서드 추가
 */
@Mapper
public interface UserWithdrawalMapper {

    /**
     * 회원 탈퇴 처리 이력을 등록한다.
     *
     * @author SeungHyeon.Kang
     * @param request 등록할 탈퇴 이력
     * @return 등록된 이력 수
     */
    int setUserWithdrawal(UserWithdrawalDto request);

    /**
     * 회원의 독후감을 모두 비공개로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @return 변경된 독후감 수
     */
    int uptUserReportPrivate(Long userNumb);

    /**
     * 회원의 알림을 모두 삭제 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @return 변경된 알림 수
     */
    int uptUserAlimDeleted(Long userNumb);

    /**
     * 회원의 푸시 구독을 모두 비활성화한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @return 변경된 푸시 구독 수
     */
    int uptUserPushDisabled(Long userNumb);

    /**
     * 탈퇴 회원의 모임원 프로필을 모임 목록에서 숨긴다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @return 변경된 모임원 관계 수
     */
    int uptClubMemberProfileHidden(Long userNumb);

    /**
     * 탈퇴 회원이 댓글에 등록한 좋아요를 삭제한다.
     *
     * @author HanWon.Jang
     * @param userNumb 탈퇴 회원 번호
     * @return 삭제된 댓글 좋아요 수
     */
    int delUserReplyLike(Long userNumb);

    /**
     * 영구 삭제 대기 중인 최신 탈퇴 이력을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원 번호
     * @return 영구 삭제 대기 이력
     */
    UserWithdrawalDto getPendingWithdrawal(Long userNumb);

    /**
     * 영구 삭제 대기 이력을 취소 상태로 변경한다.
     *
     * @author SeungHyeon.Kang
     * @param request 취소할 탈퇴 이력
     * @return 변경된 이력 수
     */
    int uptWithdrawalRestored(UserWithdrawalDto request);
}
