/**
 * fileName       : useReplyList
 * author         : HanWon.Jang
 * date           : 2026-07-28
 * description    : 독후감 댓글 목록의 서버 조회 상태를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        HanWon.Jang        최초 생성
 * 2026-08-03        HanWon.Jang        일반 함수 선언을 화살표 함수로 변경
 */
import { useInfiniteQuery } from "@tanstack/react-query";
import { getReplyListApi } from "@/features/reply/api/replyApi";
import type { ReplyTarget } from "@/features/reply/types/reply.types";

export const REPLY_LIST_QUERY_KEY = "replyList";

/**
 * 독후감 번호별 댓글 목록을 React Query 캐시에 저장하여 제공한다
 *
 * @author HanWon.Jang
 * @param reptNumb 댓글 목록을 조회할 독후감 번호
 * @return 댓글 목록의 조회 데이터와 요청 상태
 */
export const useReplyList = (target: ReplyTarget, focusReplNumb?: number) => {
  // 동일한 독후감의 댓글 목록 요청과 캐시를 재사용한다
  return useInfiniteQuery({
    queryKey: [REPLY_LIST_QUERY_KEY, target.tagtType, target.tagtNumb, focusReplNumb],
    /**
     * 현재 독후감에 등록된 댓글 목록을 서버에서 조회한다
     *
     * @author HanWon.Jang
     * @return 댓글 목록 공통 응답
     * @throws 댓글 목록 API 요청 또는 응답 검증 실패 시 발생
     */
    queryFn: async ({ pageParam }) => {
      // 검증된 독후감 번호로 댓글 목록 API를 호출한다
      return await getReplyListApi(target, pageParam, focusReplNumb);
    },
    initialPageParam: 1,
    /**
     * 마지막 댓글 응답에서 다음 부모 댓글 페이지 번호를 계산한다
     *
     * @author SeungHyeon.Kang
     * @param lastPage 마지막으로 조회한 댓글 페이지
     * @return 다음 페이지 번호 또는 조회 종료값
     */
    getNextPageParam: (lastPage) => {
      // 서버가 다음 부모 댓글 페이지 존재를 확인한 경우에만 다음 번호를 반환한다
      return lastPage.data.hasNext ? lastPage.data.page + 1 : undefined;
    },
    enabled: target.tagtNumb > 0,
  });
};
