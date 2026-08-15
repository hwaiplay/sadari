import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import {
  getMyFollowPageApi,
  getSocialFollowPageApi,
  type FollowListType,
  type FollowUser,
} from "@/features/Social/api/socialApi";
import { useState } from "react";

type FollowListModalOptions = {
  targetUserNumb?: number;
  isMyProfile: boolean;
};

/**
 * 본인과 공개 프로필의 팔로우 목록 모달 조회 및 페이지 상태를 공통으로 관리한다
 *
 * @author SeungHyeon.Kang
 * @param options 목록 주인 사용자와 본인 화면 여부
 * @return 팔로우 목록 모달 상태와 열기 및 닫기 및 추가 조회 함수
 */
export function useFollowListModal(options: FollowListModalOptions) {
  const [followListType, setFollowListType] = useState<FollowListType | null>(null);
  const [followUsers, setFollowUsers] = useState<FollowUser[]>([]);
  const [followPage, setFollowPage] = useState(0);
  const [hasNextFollowUser, setHasNextFollowUser] = useState(false);
  const [isFollowListLoading, setIsFollowListLoading] = useState(false);
  const [isNextFollowLoading, setIsNextFollowLoading] = useState(false);

  /**
   * 화면 유형에 맞는 팔로우 사용자 페이지를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param type 팔로잉 또는 팔로워 목록 유형
   * @param page 조회할 페이지 번호
   * @return 팔로우 사용자 페이지 데이터
   * @throws 대상 사용자 번호가 없거나 API 요청이 실패하면 발생
   */
  const getFollowPage = async (type: FollowListType, page: number) => {
    // 마이페이지는 인증 사용자를 목록 주인으로 사용하는 전용 API를 호출한다
    if (options.isMyProfile) {
      // 본인 팔로우 목록 페이지를 반환한다
      return (await getMyFollowPageApi(type, page)).data;
    }

    // 공개 프로필 사용자 번호가 유효하지 않으면 서버 요청을 시작하지 않는다
    if (!options.targetUserNumb || options.targetUserNumb <= 0) {
      throw new Error("FOLLOW_TARGET_REQUIRED");
    }

    // 다른 사용자의 팔로우 목록 페이지를 반환한다
    return (await getSocialFollowPageApi(options.targetUserNumb, type, page)).data;
  };

  /**
   * 선택한 팔로우 목록 모달을 열고 첫 서버 페이지를 조회한다
   *
   * @author SeungHyeon.Kang
   * @param type 팔로잉 또는 팔로워 목록 유형
   * @return 첫 페이지 조회 완료 Promise
   */
  const openFollowList = async (type: FollowListType): Promise<void> => {
    // 새 목록을 열 때 이전 사용자와 페이지 상태를 초기화한다
    setFollowListType(type);
    setFollowUsers([]);
    setFollowPage(0);
    setHasNextFollowUser(false);
    setIsFollowListLoading(true);

    // 첫 페이지 조회 실패를 모달 종료와 안전한 사용자 안내로 처리한다
    try {
      // 선택한 목록의 첫 서버 페이지를 조회한다
      const pageData = await getFollowPage(type, 1);

      // 공통 응답에 페이지 데이터가 없으면 불완전한 성공 응답으로 처리한다
      if (!pageData) {
        throw new Error("FOLLOW_PAGE_EMPTY");
      }

      // 첫 페이지 사용자 목록을 모달에 설정한다
      setFollowUsers(pageData.list);
      // 조회 완료 페이지 번호를 설정한다
      setFollowPage(pageData.page);
      // 서버가 판정한 다음 페이지 여부를 설정한다
      setHasNextFollowUser(pageData.hasNext);
    }

    // 팔로우 목록 조회 실패를 공통 오류 문구로 안내한다
    catch (error) {
      // 실패한 모달과 목록 상태를 닫는다
      setFollowListType(null);
      setFollowUsers([]);
      // "조회에 실패했습니다."
      await sweetError(
        message("frontend.alert.loadFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    // 성공과 실패 모두 최초 조회 상태를 종료한다
    finally {
      setIsFollowListLoading(false);
    }
  };

  /**
   * 열린 팔로우 목록의 다음 서버 페이지를 이어서 조회한다
   *
   * @author SeungHyeon.Kang
   * @return 다음 페이지 조회 완료 Promise
   */
  const loadMoreFollow = async (): Promise<void> => {
    // 열린 목록이 없거나 다음 페이지 조회 중 또는 마지막 페이지이면 중복 요청하지 않는다
    if (!followListType || !hasNextFollowUser || isNextFollowLoading) {
      return;
    }

    // 목록 끝에 추가 조회 로딩 상태를 표시한다
    setIsNextFollowLoading(true);

    // 추가 페이지 실패 시 기존 목록을 유지하고 오류만 안내한다
    try {
      // 마지막 성공 페이지 다음 번호를 조회한다
      const pageData = await getFollowPage(followListType, followPage + 1);

      // 공통 응답에 페이지 데이터가 없으면 기존 목록을 유지하고 실패 처리한다
      if (!pageData) {
        throw new Error("FOLLOW_PAGE_EMPTY");
      }

      // 기존 사용자 목록 뒤에 다음 페이지를 연결한다
      setFollowUsers((currentUsers) => [...currentUsers, ...pageData.list]);
      // 마지막 성공 페이지 번호를 갱신한다
      setFollowPage(pageData.page);
      // 서버가 판정한 다음 페이지 여부를 갱신한다
      setHasNextFollowUser(pageData.hasNext);
    }

    // 다음 페이지 조회 실패를 현재 모달 안에서 재시도할 수 있게 안내한다
    catch (error) {
      // "조회에 실패했습니다."
      await sweetError(
        message("frontend.alert.loadFailedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    }

    // 성공과 실패 모두 추가 조회 상태를 종료한다
    finally {
      setIsNextFollowLoading(false);
    }
  };

  /**
   * 팔로우 목록 모달과 조회된 페이지 상태를 초기화한다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const closeFollowList = (): void => {
    // 모달 식별값과 사용자 목록을 함께 비운다
    setFollowListType(null);
    setFollowUsers([]);
    setFollowPage(0);
    setHasNextFollowUser(false);
    setIsNextFollowLoading(false);
  };

  // 두 프로필 화면이 공유할 팔로우 목록 상태와 조작 함수를 반환한다
  return {
    followListType,
    followUsers,
    isFollowListLoading,
    isNextFollowLoading,
    hasNextFollowUser,
    openFollowList,
    loadMoreFollow,
    closeFollowList,
    setFollowUsers,
  };
}
