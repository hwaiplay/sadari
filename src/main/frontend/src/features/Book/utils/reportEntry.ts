import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetConfirm, sweetError } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { getMyReportByIsbnApi } from "@/features/Book/api/bookApi";
import type { BookSearchResultType } from "@/features/Book/types/book.type";
import type { NavigateFunction } from "react-router-dom";

/**
 * 선택한 도서의 기존 독후감을 확인하고 수정 또는 추가 작성 화면으로 이동한다
 *
 * @author HanWon.Jang
 * @param book 독후감을 작성할 외부 검색 도서
 * @param navigate 화면 이동 함수
 * @return 화면 이동과 사용자 선택이 끝난 Promise
 */
export async function moveToReportEntry(
  book: BookSearchResultType,
  navigate: NavigateFunction,
): Promise<void> {
  // 외부 검색 결과의 ISBN 앞뒤 공백을 제거해 동일 도서 조회 조건으로 사용한다
  const bookIsbn = book.isbn.trim();

  // ISBN이 없으면 등록 화면의 기존 도서 검증에서 안내할 수 있도록 바로 이동한다
  if (bookIsbn.length === 0) {
    // 선택한 도서를 새 독후감 등록 화면의 초기값으로 전달한다
    navigate("/report/set", { state: { selectedBook: book } });
    // 동일 ISBN 조회 없이 등록 화면 이동을 종료한다
    return;
  }

  // 동일 ISBN 독후감 조회 실패를 화면 이동과 분리해 사용자에게 안내한다
  try {
    // 로그인 사용자가 동일 ISBN으로 작성한 가장 최근 독후감을 조회한다
    const response = await getMyReportByIsbnApi(bookIsbn);
    const existingReport = response.data;

    // 동일 ISBN 독후감이 없으면 현재와 같은 신규 등록 화면으로 이동한다
    if (!existingReport?.reptNumb) {
      // 선택한 도서를 새 독후감 등록 화면의 초기값으로 전달한다
      navigate("/report/set", { state: { selectedBook: book } });
      // 기존 독후감 선택 안내 없이 등록 화면 이동을 종료한다
      return;
    }

    // "이미 작성한 독후감이 있어요."
    const promptTitle = message("frontend.book.reportEntry.existingTitle");
    // "이 도서로 작성한 독후감이 있습니다. 기존 독후감을 수정할까요, 독후감을 하나 더 작성할까요?"
    const promptText = message("frontend.book.reportEntry.existingText");
    // "기존 독후감 수정"
    const editButtonText = message("frontend.book.reportEntry.edit");
    // "하나 더 작성"
    const addButtonText = message("frontend.book.reportEntry.add");
    // 기존 독후감 수정과 추가 작성 중 하나를 명시적으로 선택하도록 안내한다
    const selection = await sweetConfirm({
      title: promptTitle,
      text: promptText,
      confirmButtonText: editButtonText,
      cancelButtonText: addButtonText,
    });

    // 선택창 바깥을 눌러 닫은 경우 현재 화면을 유지하고 등록 또는 수정으로 이동하지 않는다
    if (selection.isDismissed) {
      // 사용자가 선택을 취소했으므로 기존 도서 검색 화면을 유지한다
      return;
    }

    // 기존 독후감 수정을 선택하면 가장 최근 독후감의 편집 흐름으로 이동한다
    if (selection.isConfirmed) {
      // 상세 조회 직후 편집 명령이 표시되도록 이동 상태를 함께 전달한다
      navigate(`/report/detail/${existingReport.reptNumb}`, {
        state: { startEditing: true },
      });
      // 기존 독후감 수정 화면 이동을 종료한다
      return;
    }

    // 두 번째 선택인 추가 작성을 명시적으로 누른 경우에만 독후감 등록 화면으로 이동한다
    if (selection.isSecondaryAction) {
      // 동일 도서를 초기값으로 둔 독후감 등록 화면에 전달한다
      navigate("/report/set", { state: { selectedBook: book } });
    }
  } catch (error) {
    // "독후감 확인에 실패했어요."
    const errorTitle = message("frontend.book.reportEntry.errorTitle");
    // "기존 독후감을 확인하지 못했습니다. 잠시 후 다시 시도해주세요."
    const fallbackMessage = message("frontend.book.reportEntry.errorText");
    // 서버 사용자 메시지가 있으면 우선 사용하고 없으면 안전한 공통 문구를 사용한다
    const errorMessage = getApiErrorMessage(error, fallbackMessage);
    // "독후감 확인에 실패했어요."
    // "기존 독후감을 확인하지 못했습니다. 잠시 후 다시 시도해주세요."
    await sweetError(errorTitle, errorMessage);
  }
}
