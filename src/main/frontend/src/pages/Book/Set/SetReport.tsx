/**
 * fileName       : SetBookDetail
 * author         : Hanwon.Jang
 * date           : 2026-05-03
 * description    : 독후감 수정하는 페이지
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-05-03       Hanwon.Jang       최초 생성
 */

import { Container } from "@/components/Layout/Container/Container";
import Loading from "@/components/Loading/Loading";
import FormField from "@/features/Book/Add/components/form/field/FormField";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import { useParams } from "react-router-dom";
import { ReadingStatusType } from "@/features/Book/types/book.type";
import { useState } from "react";
import { statusContainer } from "../Add/BookAdd.css";
import { useSetReport } from "@/features/Book/Set/useSetReport";

interface SetReportProps {}

const SetReport = (props: SetReportProps) => {
  const { id } = useParams();
  const idNum = Number(id);

  if (!id || isNaN(idNum)) {
    return <div>잘못된 접근입니다</div>;
  }

  const { data, isPending } = useBookDetail(idNum);

  // 독후감 데이터
  const bookData = data?.data[0];

  // 독서 상태
  const [status, setStatus] = useState<ReadingStatusType>(bookData?.bookStat);

  // 조회 결과가 없는 경우
  if (data?.code == 2004) {
    return <div>{data.message}</div>;
  }

  // 로딩중인 경우
  if (isPending) {
    return <Loading title={"독후감 불러오는 중"} />;
  }

  //
  const { mutate } = useSetReport();

  // 폼 action
  const setFormAction = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const formData = new FormData(e.currentTarget);
    // 독서상태
    const status = formData.get("status");
    // 독서 시작일
    const startDate = formData.get("startDate");
    // 독서 종료일
    const endDate = formData.get("endDate");
    // 별점
    const grade = formData.get("grade");
    // 독후감 내용
    const content = formData.get("content");

    const data = {
      bookTitl: bookData.title,
      bookAthr: bookData.author,
      bookPubl: bookData.publisher,
      bookIsbn: bookData.isbn,
      bookCvim: bookData.image,
      bookDesc: bookData.description,
      bookStat: status as ReadingStatusType,
      bookStdt: startDate as string,
      bookEndt: endDate as string,
      bookGrde: grade as string,
      bookCntn: content as string,
    };

    mutate({ reportNumb: idNum, data });
  };

  return data?.code === 200 && bookData ? (
    <Container>
      <form onSubmit={setFormAction}>
        <div style={{ width: "300px" }}>
          <img
            src={bookData.bookCvim}
            alt={bookData.bookTitl}
            style={{ width: "100%" }}
          />
        </div>
        <FormField title="독서 상태">
          <div className={statusContainer}>
            {[
              { label: "다 읽었어요", value: "done" },
              { label: "읽고 있어요", value: "reading" },
              { label: "중단했어요", value: "stopped" },
            ].map((item) => (
              <label htmlFor={`status-${item.value}`} key={item.value}>
                <div>{item.label}</div>
                <input
                  type="radio"
                  name="status"
                  id={`status-${item.value}`}
                  defaultValue={item.value}
                  checked={status === item.value}
                  onChange={() => setStatus(item.value as ReadingStatusType)}
                />
              </label>
            ))}
          </div>
        </FormField>
        <FormField title="독서 기간">
          <div>
            <label htmlFor="startDate">시작일</label>
            <input
              type="date"
              name="startDate"
              id="startDate"
              defaultValue={bookData.bookStdt}
            />
          </div>
          <div>
            <label htmlFor="endDate">종료일</label>
            <input
              type="date"
              name="endDate"
              id="endDate"
              defaultValue={bookData.bookEndt}
            />
          </div>
        </FormField>
        <FormField title="평점">
          <input type="radio" name="grade" id="grade1" value="1" />
          <input type="radio" name="grade" id="grade2" value="2" />
          <input type="radio" name="grade" id="grade3" value="3" />
          <input type="radio" name="grade" id="grade4" value="4" />
          <input type="radio" name="grade" id="grade5" value="5" />
        </FormField>
        <FormField title="기록">
          <textarea
            name="content"
            id="content"
            placeholder="독후감을 남겨보세요"
            defaultValue={bookData.bookCntn}
          />
        </FormField>
        <button type="submit">수정하기</button>
      </form>
    </Container>
  ) : (
    <h3>{data.message}</h3>
  );
};

export default SetReport;
