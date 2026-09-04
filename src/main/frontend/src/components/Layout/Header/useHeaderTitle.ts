import { useEffect } from "react";
import { useOutletContext } from "react-router-dom";

export type SetHeaderTitle = (title: string | null) => void;

/**
 * 현재 화면의 동적 제목을 공통 헤더에 연결함
 *
 * @author HanWon.Jang
 * @param title 공통 헤더에 표시할 화면 제목
 * @return 반환값이 없음
 */
export const useHeaderTitle = (title?: string | null): void => {

  const setHeaderTitle = useOutletContext<SetHeaderTitle>();
  const normalizedTitle = title?.trim() || null;

  /**
   * 현재 화면 제목을 등록하고 화면 해제 시 제거함
   *
   * @author HanWon.Jang
   * @return 화면 제목 등록 해제 함수
   */
  const syncHeaderTitle = (): (() => void) => {

    // 전달된 화면 제목을 공통 헤더 상태에 반영함
    setHeaderTitle(normalizedTitle);

    /**
     * 현재 화면이 닫히면 등록한 헤더 제목을 제거함
     *
     * @author HanWon.Jang
     * @return 반환값이 없음
     */
    const clearHeaderTitle = (): void => {

      // 다음 화면에 이전 동적 제목이 남지 않도록 현재 제목을 해제함
      setHeaderTitle(null);
    };

    // 화면 해제 시 사용할 제목 정리 함수를 반환함
    return clearHeaderTitle;
  };

  // 제목 값이 바뀌면 공통 헤더에 최신 문자열을 연결함
  useEffect(syncHeaderTitle, [normalizedTitle, setHeaderTitle]);
};
