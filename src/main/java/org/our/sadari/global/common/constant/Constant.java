package org.our.sadari.global.common.constant;

/**
 * Constant 클래스의 역할과 책임을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
public final class Constant {

    public static final int REPORT_CONTENT_MAX_BYTES = 4000;

    public static final String CODE_READ_STAT = "READ_STAT";
    public static final String CODE_BOOK_COLR = "BOOK_COLR";
    public static final String CODE_COMM_YSNO = "COMM_YSNO";
    public static final String CODE_BADX_WORD = "BADX_WORD";
    public static final String CODE_EXCP_WORD = "EXCP_WORD";
    public static final String CODE_ALIM_SITU = "ALIM_SITU";
    // 스케줄러별 사용 여부를 관리하는 공통코드
    public static final String CODE_SCHD_CODE = "SCHD_CODE";
    public static final String OPT_PUBC_YSNO = "PUBC_YSNO";

    public static final String REPORT_STAT_READ = "READ";
    public static final String REPORT_STAT_DONE = "DONE";
    public static final String REPORT_STAT_STOP = "STOP";
    public static final String LIKE_TARGET_REPORT = "REPORT";
    public static final String ALIM_SITU_LIKE = "LIKE";
    public static final String ALIM_SITU_FOLLOW = "FOLLOW";
    // 독후감 상태나 도서 정보에 의해 발생하는 알림 상황 코드
    public static final String ALIM_SITU_REPORT = "REPORT";
    public static final String ALIM_TEMP_CODE_LIKE_REPORT = "LIKE_REPORT";
    public static final String ALIM_TEMP_CODE_FOLLOW_USER = "FOLLOW_USER";
    // 목표 독서 종료일이 지난 진행 중 독후감에 사용하는 알림 템플릿 코드
    public static final String ALIM_TEMP_CODE_REPORT_DATE_OVER = "REPORT_DATE_OVER";

    // 목표 독서기간 초과 알림 스케줄러를 식별하는 로그 코드
    public static final String SCHEDULER_CODE_REPORT_DATE_OVER = "REPORT_DATE_OVER";
    // 읽음 처리된 알림을 알림센터에 계속 노출하는 시간
    public static final int ALIM_READ_VISIBLE_HOURS = 24;
    /**
     * 날짜만 저장된 목표 종료일을 기준으로 오늘을 포함해 최근 48시간 범위를 조회하기 위한 일수
     * 오늘, 어제, 이틀 전 종료 대상을 후보로 삼고 이미 알림이 저장된 대상은 Mapper에서 별도로 제외합니다.
     */
    public static final int REPORT_DATE_OVER_LOOKBACK_DAYS = 2;
    // 스케줄러 실행이 시작됐지만 아직 종료되지 않은 상태
    public static final String SCHEDULER_EXEC_RUNNING = "RUNNING";
    // 조회 대상이 없어 업무 처리 없이 정상 종료된 상태
    public static final String SCHEDULER_EXEC_NO_DATA = "NO_DATA";
    // 조회된 모든 대상이 정상 처리된 상태
    public static final String SCHEDULER_EXEC_SUCCESS = "SUCCESS";
    // 조회 대상 중 일부만 정상 처리된 상태
    public static final String SCHEDULER_EXEC_PARTIAL = "PARTIAL";
    // 실행 자체가 실패했거나 조회된 모든 대상의 처리가 실패한 상태
    public static final String SCHEDULER_EXEC_FAILURE = "FAILURE";
    // 서비스가 성공 코드 이외의 업무 응답을 반환한 실패 유형
    public static final String SCHEDULER_FAIL_REJECTED = "REJECTED";
    // 스케줄러 실행 중 Java 예외가 발생한 실패 유형
    public static final String SCHEDULER_FAIL_EXCEPTION = "EXCEPTION";

    // SQL에서 실패 순번의 최초 기준값을 계산할 때 사용하는 숫자
    public static final int NUMBER_ZERO = 0;
    // SQL에서 동일 실행 내 다음 실패 순번을 계산할 때 사용하는 증가값
    public static final int NUMBER_ONE = 1;

    public static final String COMM_YES = "Y";
    public static final String COMM_NO = "N";

    public static final String SORT_END_DATE_DESC = "END_DATE_DESC";
    public static final String SORT_START_DATE_DESC = "START_DATE_DESC";
    public static final String SORT_GRADE_DESC = "GRADE_DESC";

    public static final String FILE_TYPE_PROFILE = "PROFILE";
    public static final String FILE_TYPE_BACKGROUND = "BACKGROUND";

    public static final String GOAL_TYPE_WEEK = "WEEK";
    public static final String GOAL_TYPE_MONTH = "MONT";
    public static final String GOAL_TYPE_YEAR = "YEAR";

    private Constant() {
        // 아래 처리 단계의 업무 목적을 설명한다.
    }
}
