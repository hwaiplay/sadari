export const UNREAD_ALIM_CNT_CHANGED_EVENT = "sadari:unread-alim-count-changed";

type UnreadAlimCntChangedEvent = CustomEvent<number>;

/**
 * notify Unread Alim Cnt Changed 사용자 동작을 처리한다
 *
 * @author HanWon.Jang
 * @param unreadAlimCnt unread Alim Cnt 입력값
 * @return 반환값이 없다
 */
export function notifyUnreadAlimCntChanged(unreadAlimCnt: number) {

  window.dispatchEvent(
    new CustomEvent<number>(UNREAD_ALIM_CNT_CHANGED_EVENT, {
      detail: unreadAlimCnt,
    }),
  );
}

/**
 * is Unread Alim Cnt Changed Event 여부를 판정한다
 *
 * @author HanWon.Jang
 * @param event event 입력값
 * @return 판정 결과
 */
export function isUnreadAlimCntChangedEvent(
  event: Event,
): event is UnreadAlimCntChangedEvent {

  return event.type === UNREAD_ALIM_CNT_CHANGED_EVENT && "detail" in event;
}
