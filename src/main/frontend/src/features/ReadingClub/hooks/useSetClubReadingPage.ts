/**
 * fileName       : useSetClubReadingPage
 * author         : Hanwon.Jang
 * date           : 2026-08-14
 * description    : 모임 독서 등록 화면의 도서, 목표 기간과 저장 동작을 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-14        Hanwon.Jang        최초 생성
 */

import { getApiErrorMessage } from "@/app/api/resultData";
import { sweetError, sweetSuccess, sweetWarning } from "@/app/lib/sweetAlert/sweetAlert";
import { message } from "@/app/messages/message";
import { normalizeBookAuthor, stripHtmlTags } from "@/app/utils/htmlUtil";
import type { BookSearchResultType } from "@/features/Book/types/book.type";
import { getBookCoverImageSource } from "@/features/Book/utils/bookCoverImage";
import { createClubReadingApi } from "@/features/ReadingClub/api/readingClubApi";
import type { CSSProperties, FormEvent } from "react";
import { useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

/**
 * 모임 독서 등록 화면의 상태와 이벤트 처리 함수를 제공한다.
 *
 * @author Hanwon.Jang
 * @return 모임 독서 등록 화면 상태와 이벤트 처리 함수
 */
export function useSetClubReadingPage() {

  const location = useLocation();
  const navigate = useNavigate();
  const { clubNumb: clubNumbParam } = useParams<{ clubNumb: string }>();
  const clubNumb = Number(clubNumbParam);
  const selectedBook = (
    location.state as { book?: BookSearchResultType } | null
  )?.book;
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isPending, setIsPending] = useState(false);
  const idempotencyKeyRef = useRef(
    globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`,
  );
  const hasValidClubNumb = Number.isSafeInteger(clubNumb) && clubNumb > 0;
  const pageStyle = selectedBook
    ? ({
        "--book-bg-image": `url("${getBookCoverImageSource(selectedBook.image)}")`,
        "--book-bg-fade-height": "680px",
      } as CSSProperties)
    : undefined;

  /**
   * 목표 독서 시작일과 종료일을 화면 상태에 반영한다.
   *
   * @author Hanwon.Jang
   * @param nextStartDate 선택한 시작일
   * @param nextEndDate 선택한 종료일
   * @return 반환값이 없다
   */
  function handleRangeChange(nextStartDate: string, nextEndDate: string): void {

    setStartDate(nextStartDate);
    setEndDate(nextEndDate);
  }

  /**
   * 모임 도서 검색 결과를 유지한 채 책을 다시 선택하도록 이동한다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  function handleBookChange(): void {

    if (!hasValidClubNumb) {
      navigate("/reading-clubs/mine", { replace: true });
      return;
    }
    navigate(`/reading-clubs/${clubNumb}/books/search`, {
      state: { keepSearchResult: true },
    });
  }

  /**
   * 입력 중인 모임 독서 등록을 취소하고 모임 상세로 돌아간다.
   *
   * @author Hanwon.Jang
   * @return 반환값이 없다
   */
  function handleCancel(): void {

    navigate(hasValidClubNumb ? `/reading-clubs/${clubNumb}` : "/reading-clubs/mine");
  }

  /**
   * 선택 도서와 목표 기간을 서버에 전달해 모임 독서를 등록한다.
   *
   * @author Hanwon.Jang
   * @param event 모임 독서 등록 폼 제출 이벤트
   * @return 반환값이 없다
   */
  async function handleFormSubmit(event: FormEvent<HTMLFormElement>): Promise<void> {

    event.preventDefault();
    if (!hasValidClubNumb || !selectedBook) {
      // "책 정보가 없어요."
      await sweetWarning(
        message("frontend.readingClub.reading.bookMissingTitle"),
        // "모임에서 읽을 책을 다시 선택해주세요."
        message("frontend.readingClub.reading.bookMissingDescription"),
      );
      handleBookChange();
      return;
    }
    if (!startDate || !endDate) {
      // "목표 독서 기간을 선택해주세요."
      await sweetWarning(message("frontend.readingClub.reading.periodRequired"));
      return;
    }

    setIsPending(true);
    try {
      // 선택 도서와 목표 기간을 모임 독서 등록 API 계약으로 변환한다
      await createClubReadingApi(clubNumb, {
        bookTitl: stripHtmlTags(selectedBook.title),
        bookAthr: normalizeBookAuthor(selectedBook.author),
        bookPubl: stripHtmlTags(selectedBook.publisher),
        bookIsbn: stripHtmlTags(selectedBook.isbn),
        bookCvim: selectedBook.image || "/img/common/no-image.png",
        bookDesc: stripHtmlTags(selectedBook.description)
          || /* "도서 소개가 없습니다." */ message("frontend.readingClub.reading.noBookDescription"),
        publDate: stripHtmlTags(selectedBook.pubdate),
        goalStdt: startDate,
        goalEndt: endDate,
        idemKeyx: idempotencyKeyRef.current,
      });
      // "모임 독서가 등록됐어요."
      await sweetSuccess(message("frontend.readingClub.reading.savedTitle"));
      navigate(`/reading-clubs/${clubNumb}`, { replace: true });
    } catch (error) {
      // 서버 공통 응답의 원인을 사용자에게 표시하고 동일 요청 키로 재시도할 수 있게 유지한다
      await sweetError(
        // "모임 독서를 등록하지 못했어요."
        message("frontend.readingClub.reading.saveFailedTitle"),
        // "잠시 후 다시 시도해주세요."
        getApiErrorMessage(error, message("frontend.common.tryAgain")),
      );
    } finally {
      setIsPending(false);
    }
  }

  return {
    selectedBook,
    startDate,
    endDate,
    isPending,
    pageStyle,
    handleRangeChange,
    handleBookChange,
    handleCancel,
    handleFormSubmit,
  };
}
