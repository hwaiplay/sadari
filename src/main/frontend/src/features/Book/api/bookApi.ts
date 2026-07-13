import api from "@/app/api/axios";
import {
  AddBookResponse,
  ReportDtoType,
  uptReportType,
} from "../types/book.type";

/**
 * fileName       : bookApi
 * author         : hanwon.Jang
 * date           : 2026-04-03
 * description    : 독후감 관련 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-04-03       hanwon.Jang       최초 생성
 * 2026-04-25       hanwon.Jang       주석 수정 및 독후감 리스트 조회 API 추가
 * 2026-05-24       hanwon.Jang       독후감 수정, 삭제 API 추가
 */

/**
 * 독후감 등록 API
 * @param data
 * @returns 독후감 id
 */
/**
 * 독후감과 책 정보를 등록한다.
 * @Author Hanwon.Jang
 * @param data 등록할 독후감과 책 정보
 * @return 등록된 독후감 번호 응답
 */
export const setReportApi = async (
  data: ReportDtoType,
): Promise<AddBookResponse> => {
  const res = await api.post("/book/setReport", data);
  return res.data;
};

/**
 * 독후감 상세보기 API
 * @param bookNumb
 * @returns 독후감 상세내용
 */
/**
 * 독후감 상세 정보를 조회한다.
 * @Author Hanwon.Jang
 * @param bookNumb 조회할 독후감 번호
 * @return 독후감 상세 조회 응답
 */
export const getDetailApi = (bookNumb: number) => {
  return api.get(`/book/getBookdetail/${bookNumb}`);
};

/**
 * 도서 정보 상세보기 API
 * @param reportNumb
 * @returns 독후감에 연결된 책 정보
 */
/**
 * 독후감에 연결된 책 상세 정보를 조회한다.
 * @Author Hanwon.Jang
 * @param reportNumb 책 정보를 조회할 독후감 번호
 * @return 책 상세 정보 조회 응답
 */
export const getBookInfoApi = (reportNumb: number) => {
  return api.get(`/book/getBookInfo/${reportNumb}`);
};

/**
 * 기준 독후감과 같은 도서의 공개 독후감 목록을 조회한다.
 * @Author Hanwon.Jang
 * @param reportNumb 기준 독후감 번호
 * @return 다른 사용자의 공개 독후감 목록 조회 응답
 */
export const getPublicReportsByReportApi = (reportNumb: number) => {
  return api.get(`/book/publicReports/by-report/${reportNumb}`);
};

/**
 * ISBN이 같은 도서의 공개 독후감 목록을 조회한다.
 * @Author Hanwon.Jang
 * @param isbn 도서 ISBN
 * @return 다른 사용자의 공개 독후감 목록 조회 응답
 */
export const getPublicReportsByIsbnApi = (isbn: string) => {
  return api.get(`/book/publicReports/by-isbn?isbn=${encodeURIComponent(isbn)}`);
};

/**
 * ISBN 기준 전체 독후감 평균 별점 조회 API를 호출한다.
 * @Author Hanwon.Jang
 * @param isbn 평균 별점을 조회할 도서 ISBN
 * @return 전체 독후감 평균 별점 조회 응답
 */
export const getBookRatingAverageByIsbnApi = (isbn: string) => {
  return api.get(`/book/ratingAverage/by-isbn?isbn=${encodeURIComponent(isbn)}`);
};

/**
 * 공개 독후감 좋아요 상태를 토글한다.
 * @Author Hanwon.Jang
 * @param reportNumb 좋아요를 누르거나 취소할 공개 독후감 번호
 * @return 변경 후 좋아요 수와 현재 사용자 좋아요 여부 응답
 */
export const setPublicReportLikeApi = (reportNumb: number) => {
  return api.post(`/book/publicReports/${reportNumb}/like`);
};

/**
 * 독후감 리스트 조회 API
 * @param userNumb
 * @returns 독후감 리스트
 */
/**
 * 로그인 사용자의 독후감 목록을 조회한다.
 * @Author Hanwon.Jang
 * @return 독후감 목록 조회 응답
 */
export type BookListParams = {
  bookKeyword?: string;
  sortType?: string;
};

export const getListApi = (params: BookListParams = {}) => {
  return api.get(`/book/getBookList`, { params });
};

/**
 * 독후감 수정 API
 * @param reportNumb
 * @param data
 * @returns 독후감 번호
 */
/**
 * 지정한 독후감을 수정한다.
 * @Author Hanwon.Jang
 * @param reportNumb 수정할 독후감 번호
 * @param data 수정할 독후감 입력값
 * @return 수정된 독후감 번호 응답
 */
export const uptReportApi = async ({
  reportNumb,
  data,
}: uptReportType): Promise<AddBookResponse> => {
  const res = await api.put(`/book/uptReport/${reportNumb}`, data);
  return res.data;
};

/**
 * 독후감 삭제 API
 * @param reportNumb
 * @returns
 */
/**
 * 지정한 독후감을 삭제한다.
 * @Author Hanwon.Jang
 * @param reportNumb 삭제할 독후감 번호
 * @return 삭제 처리 응답
 */
export const delReportApi = async (reportNumb: number) => {
  const res = await api.delete(`/book/delReport/${reportNumb}`);
  return res.data;
};
