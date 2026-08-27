package org.our.sadari.alim.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * fileName       : LikeAlimEvent
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 좋아요 저장 완료 후 비동기로 처리할 알림 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang        최초 생성
 * 2026-08-27        SeungHyeon.Kang    원본 콘텐츠 유형과 댓글 번호 전달 추가
 */
@Getter
@RequiredArgsConstructor
public class LikeAlimEvent {

    // 좋아요를 등록한 사용자 번호
    private final Long sendUserNumb;
    // 좋아요 알림을 받을 사용자 번호
    private final Long targetUserNumb;
    // 좋아요 대상별 알림 템플릿 코드
    private final String tempCode;
    // 알림 클릭 시 권한과 이동 화면을 다시 계산할 원본 콘텐츠 유형
    private final String tagtType;
    // 알림 클릭 시 이동할 대상 번호
    private final Long tagtNumb;
    // 댓글 좋아요 알림에서 강조할 댓글 번호
    private final Long replyNumb;
    // 대상 조회에서 함께 확인한 좋아요 등록자 닉네임
    private final String sendUserNick;
}
