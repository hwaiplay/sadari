import { useParams } from "react-router-dom";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";

function BookDetail() {
  const { id } = useParams();
  const idNum = Number(id);

  const { data, isPending } = useBookDetail(idNum);

  if (!id || isNaN(idNum)) {
    return <div>잘못된 접근입니다</div>;
  }

  // 조회 결과가 없는 경우
  if (data?.code == 2004) {
    return <div>{data.message}</div>;
  }

  // 로딩중인 경우
  if (isPending) {
    return <Loading title={"독후감 불러오는 중"} />;
  }

  // 독후감 데이터
  const bookData = data?.data[0];

  return data?.code === 200 && bookData ? (
    // {bookData.map(book =>()}
    <div>
      <div>
        <h1>{bookData.bookTitl}</h1>
      </div>
      <div>
        <img src={bookData.bookCvim} alt={bookData.bookTitl} width="300px" />
      </div>
      <div>
        <h1>독서기간</h1>
        {bookData.bookStdt} ~ {bookData.bookEndt}
      </div>
      <div>
        <h1>평점</h1>
        {bookData.bookGrde}
      </div>
      <div>
        <h1>독후감</h1>
        {bookData.bookCntn}
      </div>
      <div>
        <h1>책 소개</h1>
        <div>
          <h1>저자</h1>
          <p>{bookData.bookAthr}</p>
        </div>
        <div>
          <h1>출판사</h1>
          <p>{bookData.bookPubl}</p>
        </div>
        <div>
          <h1>책 소개</h1>
          <p>{bookData.bookDesc}</p>
        </div>
        <div>
          <h1>isbn</h1>
          <p>{bookData.bookIsbn}</p>
        </div>
      </div>
    </div>
  ) : (
    <h1>{data.message}</h1>
  );
}

export default BookDetail;
