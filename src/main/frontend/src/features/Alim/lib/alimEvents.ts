export const UNREAD_ALIM_CNT_CHANGED_EVENT = "sadari:unread-alim-count-changed";

type UnreadAlimCntChangedEvent = CustomEvent<number>;

export function notifyUnreadAlimCntChanged(unreadAlimCnt: number) {
  window.dispatchEvent(
    new CustomEvent<number>(UNREAD_ALIM_CNT_CHANGED_EVENT, {
      detail: unreadAlimCnt,
    }),
  );
}

export function isUnreadAlimCntChangedEvent(
  event: Event,
): event is UnreadAlimCntChangedEvent {
  return event.type === UNREAD_ALIM_CNT_CHANGED_EVENT && "detail" in event;
}
