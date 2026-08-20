package org.our.sadari.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
 * 2026-08-13        SeungHyeon.Kang    탈퇴한 Kakao 계정의 유효 제재 조회 추가
 * 2026-08-14        Hanwon.Jang,SeungHyeon.Kang    탈퇴 회원 공개 정보 해제 추가
 * 2026-08-20        SeungHyeon.Kang    탈퇴 회원의 모임 회차 참여 비식별 처리 추가
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
     * 계정 비활성화 또는 영구 삭제 대기 회원의 독서 통계를 비공개로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @param privateYsno 비공개 여부 코드
     * @return 변경된 회원 수
     */
    int uptReadingStatsPrivate(@Param("userNumb") Long userNumb, @Param("privateYsno") String privateYsno);

    /**
     * 계정 비활성화 또는 영구 삭제 대기 회원의 모임 회차 참여 연결을 비식별화한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 탈퇴 회원 번호
     * @param anonymousYsno 비식별 참여 여부 코드
     * @return 변경된 모임 회차 참여 수
     */
    int uptClubParticipantAnonymous(@Param("userNumb") Long userNumb, @Param("anonymousYsno") String anonymousYsno);

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

    /**
     * 같은 OAuth 식별값으로 탈퇴한 모든 과거 회원 번호에 유효한 이용 정지가 있는지 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param userIdhs OAuth 사용자 식별값의 SHA-256 해시
     * @return 유효한 이용 정지가 있으면 1, 없으면 0
     */
    int getActiveSuspensionCountByUserIdHash(String userIdhs);
}
