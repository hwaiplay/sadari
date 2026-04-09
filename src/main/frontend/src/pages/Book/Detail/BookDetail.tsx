import { useParams } from "react-router-dom";
import { homeDummyData } from "../../../app/assets/dummy";
import { useEffect, useState } from "react";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";
import { BookFormType } from "@/features/Book/types/book.type";

function BookDetail() {
  const { id } = useParams();
  const mutation = useBookDetail(Number(id));
  const bookData = mutation.data.data;

  if (mutation.isPending) {
    return <Loading title={"로딩중"} />;
  }

  return (
    <div>
      <div>
        <img src={bookData.coverImage} alt={""} width="300px" />
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
  );
}

export default BookDetail;
