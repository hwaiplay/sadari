import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";

function BookDetail() {
  const { id } = useParams();
  const idNum = Number(id);

  const { data, isPending } = useBookDetail(idNum);

  if (!id || isNaN(idNum)) {
    return <div>잘못된 접근입니다</div>;
  }

  if (isPending) {
    return <Loading title={"독후감 불러오는 중"} />;
  }

  const bookData = data?.data;

  return bookData ? (
    <div>
      <div>
        <h1>{bookData.title}</h1>
      </div>
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
      <div>
        <h1>책 소개</h1>
        <div>
          <h1>저자</h1>
          <p>{bookData.author}</p>
        </div>
        <div>
          <h1>출판사</h1>
          <p>{bookData.publisher}</p>
        </div>
        <div>
          <h1>책 소개</h1>
          <p>{bookData.description}</p>
        </div>
        <div>
          <h1>isbn</h1>
          <p>{bookData.isbn}</p>
        </div>
      </div>
    </div>
  ) : (
    <h1>독후감 데이터가 존재하지 않습니다</h1>
  );
}

export default BookDetail;
