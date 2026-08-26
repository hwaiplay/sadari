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
    // 알림 클릭 시 이동할 대상 번호
    private final Long tagtNumb;
    // 대상 조회에서 함께 확인한 좋아요 등록자 닉네임
    private final String sendUserNick;
}
