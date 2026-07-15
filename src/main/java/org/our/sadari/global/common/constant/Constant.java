package org.our.sadari.global.common.constant;

/**
 * 프로젝트 전역에서 사용하는 공통 상수를 관리합니다.
 *
 * @author Seunghyeon.Kang
 */
public final class Constant {

    public static final int REPORT_CONTENT_MAX_BYTES = 4000;

    public static final String CODE_READ_STAT = "READ_STAT";
    public static final String CODE_BOOK_COLR = "BOOK_COLR";
    public static final String CODE_COMM_YSNO = "COMM_YSNO";
    public static final String OPT_PUBC_YSNO = "PUBC_YSNO";

    public static final String REPORT_STAT_READ = "READ";
    public static final String REPORT_STAT_DONE = "DONE";

    public static final String COMM_YES = "Y";
    public static final String COMM_NO = "N";

    public static final String SORT_END_DATE_DESC = "END_DATE_DESC";
    public static final String SORT_START_DATE_DESC = "START_DATE_DESC";
    public static final String SORT_GRADE_DESC = "GRADE_DESC";

    public static final String FILE_TYPE_PROFILE = "PROFILE";
    public static final String FILE_TYPE_BACKGROUND = "BACKGROUND";

    public static final String GOAL_TYPE_MONTH = "MONT";
    public static final String GOAL_TYPE_YEAR = "YEAR";

    private Constant() {
        // 공통 상수 클래스는 상태를 가지지 않으므로 인스턴스 생성을 막습니다.
    }
}
