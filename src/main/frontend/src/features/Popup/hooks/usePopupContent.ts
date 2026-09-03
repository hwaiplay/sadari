import { useQuery } from "@tanstack/react-query";
import {
  getPopupContentApi,
  type PopupContentData,
  type PopupContentKey,
} from "@/features/Popup/api/popupContentApi";

const POPUP_CONTENT_STALE_MILLISECONDS = 1000 * 60 * 10;

/**
 * 사용자 안내 팝업 콘텐츠를 React Query 캐시에 저장하여 화면 간 재사용함
 *
 * @author HanWon.Jang
 * @param popupContentKey 팝업 콘텐츠 복합 식별값
 * @return 팝업 콘텐츠 조회 Query 객체
 */
export const usePopupContent = (popupContentKey: PopupContentKey) => {
  /**
   * 현재 화면이 요청한 복합 식별값으로 사용자 안내 팝업 콘텐츠를 조회함
   *
   * @author HanWon.Jang
   * @return 사용자 안내 팝업 콘텐츠
   * @throws 팝업 콘텐츠 API 응답이 실패할 때 발생
   */
  const getPopupContentQuery = async (): Promise<PopupContentData> => {
    // 현재 화면의 팝업 복합 식별값으로 사용자 안내 콘텐츠를 조회함
    const popupContent = await getPopupContentApi(popupContentKey);

    // 조회된 사용자 안내 팝업 콘텐츠를 React Query에 전달함
    return popupContent;
  };

  // 같은 팝업 콘텐츠 요청이 여러 화면에서 중복 실행되지 않도록 공통 캐시 키를 사용함
  return useQuery({
    queryKey: [
      "popupContent",
      popupContentKey.popuSitu,
      popupContentKey.popuCode,
    ],
    queryFn: getPopupContentQuery,
    staleTime: POPUP_CONTENT_STALE_MILLISECONDS,
    retry: false,
  });
};
