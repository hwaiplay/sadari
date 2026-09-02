import { getApiErrorMessage } from "@/app/api/resultData";
import { message } from "@/app/messages/message";
import Loading from "@/components/Loading/Loading";
import { getReadingGoalResultApi } from "@/features/ReadingClub/api/readingClubApi";
import ReadingGoalResultOverlay from "@/features/ReadingClub/components/ReadingGoalResultOverlay";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";

/**
 * fileName       : ReadingGoalResultPage
 * author         : Hanwon.Jang
 * date           : 2026-08-29
 * description    : 선택한 회차의 독서 목표 결과 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-29        Hanwon.Jang    최초 생성
 */

const ReadingGoalResultPage = () => {

  // 조회할 모임과 독서 회차 번호를 경로에서 확인
  const { clubNumb: clubNumbParam, rondNumb: rondNumbParam } = useParams();
  const clubNumb = Number(clubNumbParam);
  const rondNumb = Number(rondNumbParam);
  const isValidRoute = Number.isFinite(clubNumb) && clubNumb > 0
    && Number.isFinite(rondNumb) && rondNumb > 0;

  // 유효한 경로에서만 해당 회차의 독서 목표 결과를 조회함
  const resultQuery = useQuery({
    queryKey: ["readingClub", clubNumb, "readingGoalResult", rondNumb],
    queryFn: () => getReadingGoalResultApi(clubNumb, rondNumb),
    enabled: isValidRoute,
  });

  // 잘못된 경로에서는 서버를 호출하지 않고 접근 오류를 표시
  if (!isValidRoute) {
    return <p>{message("frontend.common.invalidAccess")}</p>;
  }

  // 로딩 화면
  if (resultQuery.isPending) {
    return <Loading />;
  }

  // 결과 조회 실패 또는 결과 없음 상태를 공통 오류 문구로 표시
  if (resultQuery.isError || !resultQuery.data) {
    return (
      <p role="alert">
        {resultQuery.isError
          ? getApiErrorMessage(resultQuery.error, message("frontend.common.tryAgain"))
          : message("frontend.common.invalidAccess")}
      </p>
    );
  }

  // 팝업과 동일한 독서 목표 결과 UI를 페이지 형태로 반환
  return <ReadingGoalResultOverlay result={resultQuery.data} variant="page" />;
};

export default ReadingGoalResultPage;
