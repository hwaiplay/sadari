package org.our.sadari.global.common.constant;

/**
 * 애플리케이션 전역에서 공통으로 사용하는 상수를 관리한다.
 * @Author Seunghyeon.Kang
 */
public class Constant {

    private Constant() {
    }

    // 독후감 내용 최대 저장 바이트
    public static final int REPORT_CONTENT_MAX_BYTES = 4000;

    // 독서 상태 공통코드 그룹
    public static final String CODE_READ_STAT = "READ_STAT";
    // 책장 색상 공통코드 그룹
    public static final String CODE_BOOK_COLR = "BOOK_COLR";
    // Y/N 공통코드 그룹
    public static final String CODE_COMM_YSNO = "COMM_YSNO";
    // 공개여부 명칭 조회 옵션 코드
    public static final String OPT_PUBC_YSNO = "PUBC_YSNO";

    // 독서중 상태 코드
    public static final String REPORT_STAT_READ = "READ";
    // 공통 Y 값
    public static final String COMM_YES = "Y";
    // 공통 N 값
    public static final String COMM_NO = "N";

    // 독후감 목록 종료일 내림차순 정렬 코드
    public static final String SORT_END_DATE_DESC = "END_DATE_DESC";
    // 독후감 목록 시작일 내림차순 정렬 코드
    public static final String SORT_START_DATE_DESC = "START_DATE_DESC";
    // 독후감 목록 별점 내림차순 정렬 코드
    public static final String SORT_GRADE_DESC = "GRADE_DESC";

    // 프로필 사진 파일 구분값
    public static final String FILE_TYPE_PROFILE = "PROFILE";
    // 프로필 배경 사진 파일 구분값
    public static final String FILE_TYPE_BACKGROUND = "BACKGROUND";
}
