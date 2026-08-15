/**
 * 도서 검색과 독후감 업무에 필요한 API 요청을 처리한다
 *
 * @author HanWon.Jang
 */
import api from "@/app/api/axios";
import {
  assertResultDataSuccess,
  type ResultData,
} from "@/app/api/resultData";
import {
  AddBookResponse,
  ReportDetailType,
  ReportDtoType,
  uptReportType,
} from "../types/book.type";

export type PublicReportSortType =
  | "RELATION_DESC"
  | "LATEST_DESC"
  | "GRADE_DESC"
  | "LIKE_DESC";

export type BookCoverColor = {
  reptColr: string;
  reptColrName: string;
};

export type ExistingReportByIsbn = {
  // 동일 ISBN으로 가장 최근에 작성한 독후감 번호
  reptNumb: number;
  // 기존 독후감과 연결된 도서 ISBN
  bookIsbn: string;
};

/**
 * 신뢰된 도서 검색 표지 대표색과 가장 가까운 책장 색상 코드를 조회한다
 *
 * @author HanWon.Jang
 * @param bookCvim 대표색을 분석할 도서 검색 표지 URL
 * @return 표지 대표색과 가장 가까운 BOOK_COLR 코드
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getBookCoverColorApi = async (
  bookCvim: string,
): Promise<BookCoverColor | undefined> => {

  const res = await api.post<ResultData<BookCoverColor>>(
    "/book/cover-color",
    { bookCvim },
  );
  // 표지 대표색 분석 성공 응답의 책장 색상 데이터를 반환한다
  return assertResultDataSuccess(res.data).data;
};

/**
 * set Report 정보를 설정하거나 등록한다
 *
 * @author HanWon.Jang
 * @param data data 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const setReportApi = async (
  data: ReportDtoType,
): Promise<AddBookResponse> => {

  const res = await api.post("/book/setReport", data);
  return assertResultDataSuccess(res.data);
};

/**
 * get Detail 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param bookNumb book Numb 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getDetailApi = async (bookNumb: number) => {

  const res = await api.get<ResultData<ReportDetailType>>(
    `/book/getBookdetail/${bookNumb}`,
  );
  return assertResultDataSuccess(res.data);
};

/**
 * get Public Reports By Isbn 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @param sortType 공개 독후감 정렬 코드
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getPublicReportsByIsbnApi = async (
  isbn: string,
  sortType: PublicReportSortType,
) => {

  // ISBN과 서버가 검증할 정렬 코드를 조회 파라미터로 전달한다.
  const res = await api.get("/book/publicReports/by-isbn", {
    params: { isbn, sortType },
  });
  return assertResultDataSuccess(res.data);
};

/**
 * get Book Rating Average By Isbn 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param isbn isbn 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getBookRatingAvgApi = async (isbn: string) => {

  const res = await api.get(
    `/book/ratingAverage/by-isbn?isbn=${encodeURIComponent(isbn)}`,
  );
  return assertResultDataSuccess(res.data);
};

/**
 * 로그인 사용자가 동일 ISBN으로 가장 최근에 작성한 독후감을 조회한다
 *
 * @author HanWon.Jang
 * @param isbn 기존 독후감을 조회할 도서 ISBN
 * @return 동일 ISBN의 최근 독후감 조회 응답
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getMyReportByIsbnApi = async (isbn: string) => {
  // ISBN을 안전하게 전달하여 로그인 사용자의 최근 독후감을 조회한다
  const res = await api.get<ResultData<ExistingReportByIsbn | undefined>>(
    `/book/reports/by-isbn?isbn=${encodeURIComponent(isbn)}`,
  );
  // 공통 응답 검증을 통과한 최근 독후감 조회 결과를 반환한다
  return assertResultDataSuccess(res.data);
};

export type LikeTargetParams = {
  tagtType: string;
  tagtNumb: number;
  // 알림 대상 DB 재조회를 없애기 위해 화면이 이미 조회한 독후감 작성자 번호를 함께 전송한다
  targetUserNumb: number;
};

/**
 * set Public Report Like 정보를 설정하거나 등록한다
 *
 * @author HanWon.Jang
 * @param data data 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const setPublicReportLikeApi = (data: LikeTargetParams) => {

  return api.post("/social/like", data).then((res) => {

    return assertResultDataSuccess(res.data);
  });
};

export type BookListParams = {
  bookKeyword?: string;
  sortType?: string;
};

/**
 * get List 정보를 조회한다
 *
 * @author HanWon.Jang
 * @param params params 입력값
 * @return 처리 결과
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const getListApi = async (params: BookListParams = {}) => {

  const res = await api.get(`/book/getBookList`, { params });
  return assertResultDataSuccess(res.data);
};

/**
 * upt Report 정보를 수정한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const uptReportApi = async ({
  reptNumb,
  data,
}: uptReportType): Promise<AddBookResponse> => {

  const res = await api.put(`/book/uptReport/${reptNumb}`, data);
  return assertResultDataSuccess(res.data);
};

export type UptReptStatusGradeParams = {
  reptNumb: number;
  data: {
    reptStat: string;
    reptGrde: string;
    pubcYsno: "Y" | "N";
    reptEndt?: string;
  };
};

/**
 * 마이페이지에서 독서 상태와 별점 및 공개 여부를 빠르게 수정한다
 *
 * @author HanWon.Jang
 * @param props props 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const uptReptStatusGradeApi = async ({
  reptNumb,
  data,
}: UptReptStatusGradeParams): Promise<AddBookResponse> => {

  const res = await api.put(`/book/uptReport/status-grade/${reptNumb}`, data);
  return assertResultDataSuccess(res.data);
};

/**
 * del Report 정보를 삭제한다
 *
 * @author HanWon.Jang
 * @param reptNumb rept Numb 입력값
 * @return 반환값이 없다
 * @throws API 요청 또는 비동기 처리 실패 시 발생
 */
export const delReportApi = async (reptNumb: number) => {

  const res = await api.delete(`/book/delReport/${reptNumb}`);
  return assertResultDataSuccess(res.data);
};
