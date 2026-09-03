import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import { getAlimTargetApi } from "@/features/Alim/api/alimApi";
import { useEffect, useRef } from "react";
import { useNavigate, useParams } from "react-router-dom";

/**
 * 푸시 알림의 사용자별 알림번호를 현재 접근 가능한 화면 주소로 해석해 이동함
 *
 * @author SeungHyeon.Kang
 * @return 알림 이동 주소를 조회하는 동안 표시할 공통 로딩 화면
 */
const NotificationTargetPage = () => {

  const navigate = useNavigate();
  const { alimNumb } = useParams<{ alimNumb: string }>();
  const isResolvingRef = useRef(false);

  // 개발 모드의 Effect 재실행에서도 같은 알림 이동 요청이 중복되지 않도록 한 번만 처리함
  useEffect(() => {
    // 이미 같은 화면 인스턴스에서 이동 주소를 조회했다면 중복 알림과 이동을 막음
    if (isResolvingRef.current) {
      // 최초 요청이 이동을 완료하도록 추가 처리를 종료함
      return;
    }

    // 현재 화면 인스턴스의 알림 이동 조회가 시작되었음을 기록함
    isResolvingRef.current = true;

    /**
     * 경로의 알림번호를 검증하고 서버가 계산한 내부 화면으로 이동함
     *
     * @author SeungHyeon.Kang
     * @return 알림 이동 완료 Promise
     * @throws 알림 소유권 또는 현재 콘텐츠 접근 검증에 실패할 때 발생
     */
    const moveAlimTarget = async (): Promise<void> => {
      const targetAlimNumb = Number(alimNumb);

      // 양수 정수 알림번호가 아니면 서버를 호출하지 않고 알림 목록으로 복귀함
      if (!Number.isSafeInteger(targetAlimNumb) || targetAlimNumb <= 0) {
        // 유효하지 않은 알림 경로를 브라우저 이력에 남기지 않고 알림 목록으로 이동함
        navigate("/alim", { replace: true });
        // 잘못된 알림번호의 이동 처리를 종료함
        return;
      }

      // 알림 소유권과 현재 공개 및 팔로우 상태가 반영된 이동 주소를 조회함
      const response = await getAlimTargetApi(targetAlimNumb);
      // 서버가 검증한 내부 이동 주소로 현재 해석 경로를 교체함
      navigate(response.data.linkUrlx, { replace: true });
    };

    // 비동기 이동 실패를 공통 오류 안내와 알림 목록 복귀 흐름으로 격리함
    void moveAlimTarget().catch(async (error: unknown) => {
      // "알림을 불러오지 못했어요."
      await sweetError(
        message("frontend.alim.list.failedTitle"),
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
      // 접근할 수 없는 알림 경로를 브라우저 이력에서 제거하고 알림 목록으로 복귀함
      navigate("/alim", { replace: true });
    });
  }, [alimNumb, navigate]);

  // 알림번호의 최종 이동 주소를 계산하는 동안 공통 로딩 화면을 반환함
  return <Loading />;
};

export default NotificationTargetPage;
