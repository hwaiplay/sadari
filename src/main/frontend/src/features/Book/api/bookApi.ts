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
 * 독후감 리스트 조회 API
 * @param userNumb
 * @returns 독후감 리스트
 */
/**
 * 로그인 사용자의 독후감 목록을 조회한다.
 * @Author Hanwon.Jang
 * @return 독후감 목록 조회 응답
 */
export const getListApi = () => {
  return api.get(`/book/getBookList`);
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
