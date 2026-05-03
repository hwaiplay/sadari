import { useNavigate, useParams } from "react-router-dom";
import { useBookDetail } from "@/features/Book/Detail/hook/useBookDetail";
import Loading from "@/components/Loading/Loading";
import { Container } from "@/components/Layout/Container/Container";

function DetailPage() {
  // 책 번호 파라미터
  const { id } = useParams();
  // 파라미터 타입 변환 string -> number
  const idNum = Number(id);

  const navigate = useNavigate();

  // 수정 페이지로 이동
  const goUpdatePage = (reportNumb: number) => {
    navigate(`/book/set/${reportNumb}`);
  };

  // 상세보기 데이터 불러옴
  const { data, isPending } = useBookDetail(idNum);

  // 조회 결과가 없는 경우
  if (data?.code == 2004) {
    return <div>{data.message}</div>;
  }

  // 로딩중인 경우
  if (isPending) {
    return <Loading title={"독후감 불러오는 중"} />;
  }

  // 독후감 데이터
  const bookData = data?.data;

  return data?.code === 200 && bookData ? (
    // {bookData.map(book =>()}
    <Container>
      <h1
        onClick={() => goUpdatePage(idNum)}
        style={{ backgroundColor: "#e3d3d3" }}
      >
        독후감 수정하기
      </h1>
      <div>
        <h3>{bookData.bookTitl}</h3>
      </div>
      <div>
        <img src={bookData.bookCvim} alt={bookData.bookTitl} width="300px" />
      </div>
      <div>
        <h3>독서기간</h3>
        {bookData.reportStdt} ~ {bookData.reportEndt}
      </div>
      <div>
        <h3>평점</h3>
        {bookData.reportGrde}
      </div>
      <div>
        <h3>독후감</h3>
        {bookData.reportCntn}
      </div>
      <div>
        <h3>책 소개</h3>
        <div>
          <h3>저자</h3>
          <p>{bookData.bookAthr}</p>
        </div>
        <div>
          <h3>출판사</h3>
          <p>{bookData.bookPubl}</p>
        </div>
        <div>
          <h3>책 소개</h3>
          <p>{bookData.bookDesc}</p>
        </div>
        <div>
          <h3>isbn</h3>
          <p>{bookData.bookIsbn}</p>
        </div>
      </div>
    </Container>
  ) : (
    <h3>{data.message}</h3>
  );
}

export default DetailPage;
