import type {ClubChatMessage, ClubChatReadCount} from "@/features/ReadingClub/api/readingClubApi";

/**
 * 기존 본문을 보존하며 새 메시지와 미읽음 수만 병합
 *
 * @author SeungHyeon.Kang
 * @param current 현재 표시 메시지
 * @param incoming 새 메시지
 * @param readCounts 기존 메시지의 미읽음 수
 * @return 변경이 없으면 원래 배열, 변경 시 시간순 메시지
 */
export const mergeChatMessages = (
  current: ClubChatMessage[],
  incoming: ClubChatMessage[],
  readCounts: ClubChatReadCount[],
): ClubChatMessage[] => {
  // 본문과 메타 정보를 다시 만들지 않도록 번호별 원본 참조 유지
  const merged = new Map<number, ClubChatMessage>();
  // 현재 화면에 표시한 메시지만 읽음 수 갱신 대상으로 보존
  for (const chat of current) {
    // 기존 메시지 참조를 번호에 연결
    merged.set(chat.chatNumb, chat);
  }

  let changed = false;
  // 화면에 없는 오래된 메시지는 숫자만으로 새 카드 생성 금지
  for (const count of readCounts) {
    // 표시 중인 원본 메시지 조회
    const previous = merged.get(count.chatNumb);
    // 실제 미읽음 수가 달라진 카드만 교체
    if (previous && previous.unreadCnt !== count.unreadCnt) {
      // 이전 본문과 작성자 및 시간을 유지한 숫자 변경
      merged.set(count.chatNumb, {...previous, unreadCnt: count.unreadCnt});
      changed = true;
    }

  }

  // 중복된 발신자 응답도 같은 메시지 번호에 병합
  for (const chat of incoming) {
    // 새 본문 또는 서버 확정 메시지 반영
    merged.set(chat.chatNumb, chat);
    changed = true;
  }

  // 무변경 폴링은 React 재렌더링 없이 기존 배열 반환
  if (!changed) {
    // 변경 없는 표시 상태 유지
    return current;
  }

  /**
   * 채팅 번호의 시간순 비교
   *
   * @author SeungHyeon.Kang
   * @param left 앞 메시지
   * @param right 뒤 메시지
   * @return 번호 차이
   */
  const compareChat = (left: ClubChatMessage, right: ClubChatMessage): number => {
    // 서버의 시간순 번호 기준 반환
    return left.chatNumb - right.chatNumb;
  };
  // 새 메시지가 추가된 경우에도 원래 시간순 표시 보존
  return Array.from(merged.values()).sort(compareChat);
};
