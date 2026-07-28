package org.our.sadari.global.common.constant;

/**
 * fileName       : Constant
 * author         : SeungHyeon.Kang
 * date           : 2026-07-07
 * description    : 공통 처리에 사용하는 상수와 코드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-07        SeungHyeon.Kang    최초 생성
 */
public final class Constant {

    // 독후감 내용 최대 바이트 설정값
    public static final int REPORT_CONTENT_MAX_BYTES = 4000;

    // 코드 읽기 상태 설정값
    public static final String CODE_READ_STAT = "READ_STAT";
    // 코드 도서 색상 설정값
    public static final String CODE_BOOK_COLR = "BOOK_COLR";
    // 코드 공통 여부 설정값
    public static final String CODE_COMM_YSNO = "COMM_YSNO";
    // 코드 비속어 단어 설정값
    public static final String CODE_BADX_WORD = "BADX_WORD";
    // 코드 예외 단어 설정값
    public static final String CODE_EXCP_WORD = "EXCP_WORD";
    // 코드 알림 상황 설정값
    public static final String CODE_ALIM_SITU = "ALIM_SITU";
    // 스케줄러별 사용 여부를 관리하는 공통코드
    public static final String CODE_SCHD_CODE = "SCHD_CODE";
    // 옵션 공개 여부 설정값
    public static final String OPT_PUBC_YSNO = "PUBC_YSNO";

    // 독후감 상태 읽기 설정값
    public static final String REPORT_STAT_READ = "READ";
    // 독후감 상태 DONE 설정값
    public static final String REPORT_STAT_DONE = "DONE";
    // 독후감 상태 STOP 설정값
    public static final String REPORT_STAT_STOP = "STOP";
    // 좋아요 대상 독후감 설정값
    public static final String LIKE_TARGET_REPORT = "REPORT";
    // 알림 상황 좋아요 설정값
    public static final String ALIM_SITU_LIKE = "LIKE";
    // 알림 상황 팔로우 설정값
    public static final String ALIM_SITU_FOLLOW = "FOLLOW";
    // 독후감 상태나 도서 정보에 의해 발생하는 알림 상황 코드
    public static final String ALIM_SITU_REPORT = "REPORT";
    // 알림 템플릿 코드 좋아요 독후감 설정값
    public static final String ALIM_TEMP_CODE_LIKE_REPORT = "LIKE_REPORT";
    // 알림 템플릿 코드 팔로우 USER 설정값
    public static final String ALIM_TEMP_CODE_FOLLOW_USER = "FOLLOW_USER";
    // 목표 독서 종료일이 지난 진행 중 독후감에 사용하는 알림 템플릿 코드
    public static final String ALIM_TEMP_CODE_REPORT_DATE_OVER = "REPORT_DATE_OVER";

    // 목표 독서기간 초과 알림 스케줄러를 식별하는 로그 코드
      public static final String SCHEDULER_CODE_REPORT_DATE_OVER = "REPORT_DATE_OVER";
      // 삭제 상태 알림 물리 삭제 스케줄러를 식별하는 로그 및 상세코드
      public static final String SCHEDULER_CODE_ALIM_DELETE = "ALIM_DELETE";
    /**
     * 날짜만 저장된 목표 종료일을 기준으로 오늘을 포함해 최근 48시간 범위를 조회하기 위한 일수
     * 오늘, 어제, 이틀 전 종료 대상을 후보로 삼고 이미 알림이 저장된 대상은 Mapper에서 별도로 제외한다.
     */
    // 독후감 날짜 초과 조회 범위 일수 설정값
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

    // 공통 사용 설정값
    public static final String COMM_YES = "Y";
    // 공통 미사용 설정값
    public static final String COMM_NO = "N";

    // 정렬 종료 날짜 내림차순 설정값
    public static final String SORT_END_DATE_DESC = "END_DATE_DESC";
    // 정렬 시작 날짜 내림차순 설정값
    public static final String SORT_START_DATE_DESC = "START_DATE_DESC";
    // 정렬 평점 내림차순 설정값
    public static final String SORT_GRADE_DESC = "GRADE_DESC";

    // 파일 유형 프로필 설정값
    public static final String FILE_TYPE_PROFILE = "PROFILE";
    // 파일 유형 배경 설정값
    public static final String FILE_TYPE_BACKGROUND = "BACKGROUND";

    // 목표 유형 주간 설정값
    public static final String GOAL_TYPE_WEEK = "WEEK";
    // 목표 유형 월간 설정값
    public static final String GOAL_TYPE_MONTH = "MONT";
    // 목표 유형 연간 설정값
    public static final String GOAL_TYPE_YEAR = "YEAR";

    private Constant() {
        // 아래 처리 단계의 업무 목적을 설명한다.
    }
}
