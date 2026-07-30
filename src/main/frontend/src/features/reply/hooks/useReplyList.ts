/**
 * fileName       : useReplyList
 * author         : HanWon.Jang
 * date           : 2026-07-28
 * description    : 독후감 댓글 목록의 서버 조회 상태를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        HanWon.Jang        최초 생성
 */
import { useQuery } from "@tanstack/react-query";
import { getReplyListApi } from "@/features/reply/api/replyApi";

export const REPLY_LIST_QUERY_KEY = "replyList";

/**
 * 독후감 번호별 댓글 목록을 React Query 캐시에 저장하여 제공한다
 *
 * @author HanWon.Jang
 * @param reptNumb 댓글 목록을 조회할 독후감 번호
 * @return 댓글 목록의 조회 데이터와 요청 상태
 */
export function useReplyList(reptNumb: number) {
  // 동일한 독후감의 댓글 목록 요청과 캐시를 재사용한다
  return useQuery({
    queryKey: [REPLY_LIST_QUERY_KEY, reptNumb],
    /**
     * 현재 독후감에 등록된 댓글 목록을 서버에서 조회한다
     *
     * @author HanWon.Jang
     * @return 댓글 목록 공통 응답
     * @throws 댓글 목록 API 요청 또는 응답 검증 실패 시 발생
     */
    queryFn: async () => {
      // 검증된 독후감 번호로 댓글 목록 API를 호출한다
      return await getReplyListApi(reptNumb);
    },
    enabled: reptNumb > 0,
  });
}
