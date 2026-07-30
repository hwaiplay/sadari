import api from "@/app/api/axios";
import {
  assertResultDataSuccess,
  type ResultData,
} from "@/app/api/resultData";

export type PopupContentKey = {
  popuSitu: string;
  popuCode: string;
};

export type PopupContentData = {
  popuSitu: string;
  popuCode: string;
  contFirs: string;
  contSeco?: string;
  contThir?: string;
  contFour?: string;
};

export const POPUP_CONTENT_KEYS = {
  accountWithdrawalPolicy: {
    popuSitu: "ACCOUNT",
    popuCode: "WITHDRAWAL_POLICY",
  },
  profileGoalDown: {
    popuSitu: "PROFILE",
    popuCode: "GOAL_DOWN",
  },
} as const satisfies Record<string, PopupContentKey>;

/**
 * 사용 화면 구분과 팝업 코드에 해당하는 사용자 안내 콘텐츠를 조회한다
 *
 * @author HanWon.Jang
 * @param popupContentKey 팝업 콘텐츠 복합 식별값
 * @return 사용자 안내 팝업 콘텐츠
 * @throws 공통 응답이 실패하거나 콘텐츠 데이터가 없을 때 발생
 */
export const getPopupContentApi = async (
  popupContentKey: PopupContentKey,
): Promise<PopupContentData> => {
  // 팝업 복합 식별값을 바인딩하여 사용자 안내 콘텐츠를 조회한다
  const response = await api.get<ResultData<PopupContentData>>(
    "/popup-content",
    {
      params: popupContentKey,
    },
  );
  // 공통 응답 코드를 검증하여 실패 응답을 React Query 오류 상태로 전달한다
  const result = assertResultDataSuccess(response.data);

  // 콘텐츠가 없으면 화면이 현재 기본 문구를 유지하도록 조회 실패로 처리한다
  if (!result.data) {
    // 팝업 콘텐츠 누락을 조회 실패로 전달할 오류 객체를 생성한다
    throw new Error("팝업 콘텐츠가 없습니다.");
  }

  // 검증된 사용자 안내 팝업 콘텐츠를 반환한다
  return result.data;
};
