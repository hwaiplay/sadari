import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";

function BookDetail() {
  const { id } = useParams();
  const mutation = useBookDetail(Number(id));
  const bookData = mutation.data.data;

  if (mutation.isPending) {
    return <Loading title={"로딩중"} />;
  }

  return bookData ? (
    <div>
      <div>
        <img src={bookData.image} alt={bookData.title} width="300px" />
      </div>
      <div>
        <h1>독서기간</h1>
        {bookData.startDate} ~ {bookData.endDate}
      </div>
      <div>
        <h1>평점</h1>
        {bookData.grade}
      </div>
      <div>
        <h1>독후감</h1>
        {bookData.content}
      </div>
    </div>
  ) : (
    <h1>독후감 데이터가 존재하지 않습니다</h1>
  );
}

export default BookDetail;
