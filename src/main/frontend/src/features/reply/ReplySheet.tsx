import type { PublicReportType } from "@/features/Book/types/book.type";
import ReplySheetView from "@/features/reply/ReplySheetView";
import { useReplySheetController } from "@/features/reply/hooks/useReplySheetController";
import { createPortal } from "react-dom";
import type { ReplyTargetType } from "@/features/reply/types/reply.types";

type ReplySheetProps = {
  report: Pick<PublicReportType, "reptNumb"> &
    Partial<Pick<PublicReportType, "userNick">>;
  tagtType?: ReplyTargetType;
  focusReplNumb?: number;
  onClose: () => void;
};

/**
 * 독후감 댓글 기능 상태와 바텀시트 화면 렌더링을 연결함
 *
 * @author HanWon.Jang
 * @param props 댓글 바텀시트에 표시할 독후감과 닫기 처리 정보
 * @return 댓글 목록과 등록 폼을 포함한 바텀시트 Portal
 */
const ReplySheet = ({ report, tagtType = "REPORT", focusReplNumb, onClose }: ReplySheetProps) => {

  const target = { tagtType, tagtNumb: report.reptNumb };

  // 댓글 조회와 등록 및 바텀시트 상호작용 상태를 화면 전용 데이터로 구성함
  const controller = useReplySheetController({ target, focusReplNumb, onClose });

  // 기능 상태와 이벤트를 전달하여 댓글 바텀시트 화면을 구성함
  const sheet = (
    <ReplySheetView
      report={report}
      onClose={onClose}
      controller={controller}
    />
  );

  // 브라우저 문서가 있으면 댓글 바텀시트를 최상위 body에 렌더링함
  return typeof document !== "undefined"
    ? createPortal(sheet, document.body)
    : null;
};

export default ReplySheet;
