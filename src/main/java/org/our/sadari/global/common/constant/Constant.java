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
 * 2026-07-29        SeungHyeon.Kang    자동 닉네임 정책 추가
 * 2026-07-30        SeungHyeon.Kang    계정·팝업·회원 상태 코드 정리
 * 2026-07-31        Hanwon.Jang        댓글 알림 코드 수정
 * 2026-08-05        SeungHyeon.Kang    독서 관심분야 코드 통합
 * 2026-08-12        SeungHyeon.Kang    알림 아이콘 코드 추가
 * 2026-08-13        SeungHyeon.Kang    팔로우 상태 코드 추가
 * 2026-08-14        SeungHyeon.Kang,Hanwon.Jang    독서 타이머·통계·모임 코드 정리
 * 2026-08-15        SeungHyeon.Kang    친구·독후감 정렬 코드 추가
 * 2026-08-20        SeungHyeon.Kang    고객문의·타이머 알림 코드 추가
 * 2026-08-21        SeungHyeon.Kang    독후감 설정·알림 상황 통합
 * 2026-08-22        SeungHyeon.Kang    신고 대상·자동 조치 코드 추가
 */
public final class Constant {

    // 독후감 내용 최대 바이트 설정값
    public static final int REPORT_CONTENT_MAX_BYTES = 4000;
    // 탈퇴 사유 상세 내용 최대 바이트 설정값
    public static final int WITHDRAWAL_REASON_MAX_BYTES = 500;
    // 사용자 닉네임 최대 길이 설정값
    public static final int USER_NICK_MAX_LENGTH = 25;
    // 자동 닉네임 연월과 네 자리 번호 및 구분자 길이
    public static final int NICK_GENERATED_SUFFIX_LENGTH = 9;
    // 닉네임 조합별 월간 최대 발급 번호
    public static final int NICK_SEQUENCE_MAX_NUMBER = 9999;

    // 코드 읽기 상태 설정값
    public static final String CODE_READ_STAT = "READ_STAT";
    // 코드 도서 색상 설정값
    public static final String CODE_BOOK_COLR = "BOOK_COLR";
    // 계층형 독서 관심분야 공통코드
    public static final String CODE_READING_CATEGORY = "CATE_CODE";
    // 코드 공통 여부 설정값
    public static final String CODE_COMM_YSNO = "COMM_YSNO";
    // 코드 비속어 단어 설정값
    public static final String CODE_BADX_WORD = "BADX_WORD";
    // 코드 예외 단어 설정값
    public static final String CODE_EXCP_WORD = "EXCP_WORD";
    // 코드 알림 상황 설정값
    public static final String CODE_ALIM_SITU = "ALIM_SITU";
    // 팝업 사용 화면 구분 공통코드
    public static final String CODE_POPU_SITU = "POPU_SITU";
    // 공지사항 카테고리 공통코드
    public static final String CODE_NOTICE_CATEGORY = "NOTI_CATE";
    // 서비스 정보 카테고리 공통코드
    public static final String CODE_SERVICE_INFO_CATEGORY = "SVIF_CATE";
    // 고객문의 카테고리 공통코드
    public static final String CODE_INQUIRY_CATEGORY = "INQR_CATG";
    // 고객문의 상태 공통코드
    public static final String CODE_INQUIRY_STATUS = "INQR_STAT";
    // 신고 대상 유형 공통코드
    public static final String CODE_COMPLAINT_TARGET = "CMPL_TAGT";
    // 신고 사유 공통코드
    public static final String CODE_COMPLAINT_REASON = "CMPL_RSON";
    // 스케줄러별 사용 여부를 관리하는 공통코드
    public static final String CODE_SCHD_CODE = "SCHD_CODE";
    // 독서 타이머 상태 공통코드
    public static final String CODE_TIMER_STAT = "TMRX_STAT";
    // 회원 상태 공통코드
    public static final String CODE_USER_STAT = "USER_STAT";
    // 팔로우 버튼 상태 공통코드
    public static final String CODE_FOLLOW_STAT = "FOLW_STAT";
    // 자동 닉네임 주어 공통코드
    public static final String CODE_NICK_SUBJ = "NICK_SUBJ";
    // 자동 닉네임 서술어 공통코드
    public static final String CODE_NICK_PRED = "NICK_PRED";
    // 자동 닉네임 동물 명사 공통코드
    public static final String CODE_NICK_ANML = "NICK_ANML";
    // 옵션 공개 여부 설정값
    public static final String OPT_PUBC_YSNO = "PUBC_YSNO";

    // 독후감 상태 읽기 설정값
    public static final String REPORT_STAT_READ = "READ";
    // 독후감 상태 DONE 설정값
    public static final String REPORT_STAT_DONE = "DONE";
    // 독후감 상태 STOP 설정값
    public static final String REPORT_STAT_STOP = "STOP";
    // 독후감 좋아요 알림 설정 유형
    public static final String REPORT_ALIM_LIKE = "like";
    // 독후감 댓글 알림 설정 유형
    public static final String REPORT_ALIM_REPLY = "reply";
    // 측정 중인 독서 타이머 상태
    public static final String TIMER_STAT_RUNNING = "RUNNING";
    // 일시정지된 독서 타이머 상태
    public static final String TIMER_STAT_PAUSED = "PAUSED";
    // 완료된 독서 타이머 상태
    public static final String TIMER_STAT_COMPLETED = "COMPLETED";
    // 좋아요 대상 독후감 설정값
    public static final String LIKE_TARGET_REPORT = "REPORT";
    // 좋아요 대상 댓글 설정값
    public static final String LIKE_TARGET_REPLY = "REPLY";
    // 사용자 신고 대상 설정값
    public static final String COMPLAINT_TARGET_USER = "CMPL_USER";
    // 독후감 신고 대상 설정값
    public static final String COMPLAINT_TARGET_REPORT = "CMPL_BOOK_REPORT";
    // 댓글 신고 대상 설정값
    public static final String COMPLAINT_TARGET_REPLY = "CMPL_REPLY";
    // 프로필 사진 신고 대상 설정값
    public static final String COMPLAINT_TARGET_PROFILE = "CMPL_PROF_IMAGE";
    // 한줄소개 신고 대상 설정값
    public static final String COMPLAINT_TARGET_INTRO = "CMPL_INTRO";
    // 기타 신고 사유 설정값
    public static final String COMPLAINT_REASON_OTHER = "CMPL_OTHER";
    // 신고 처리 접수 상태
    public static final String COMPLAINT_STATUS_RECEIVED = "CMPL_RECEIVED";
    // 신고 처리 검토 중 상태
    public static final String COMPLAINT_STATUS_REVIEWING = "CMPL_REVIEWING";
    // 신고 처리 조치 완료 상태
    public static final String COMPLAINT_STATUS_ACTIONED = "CMPL_ACTIONED";
    // 신고 처리 반려 상태
    public static final String COMPLAINT_STATUS_REJECTED = "CMPL_REJECTED";
    // 독후감 완전 삭제 자동 조치 유형
    public static final String COMPLAINT_ACTION_DELETE_REPORT = "CMPL_DEL_REPORT";
    // 댓글 논리 삭제 자동 조치 유형
    public static final String COMPLAINT_ACTION_DELETE_REPLY = "CMPL_DEL_REPLY";
    // 프로필 사진 기본 이미지 초기화 자동 조치 유형
    public static final String COMPLAINT_ACTION_RESET_PROFILE = "CMPL_RESET_PROF";
    // 한줄소개 Null 초기화 자동 조치 유형
    public static final String COMPLAINT_ACTION_CLEAR_INTRO = "CMPL_CLEAR_INTRO";
    // 신고 누적 자동 조치 적용 결과
    public static final String COMPLAINT_RESULT_APPLIED = "CMPL_APPLIED";
    // 상대를 팔로우하지 않는 기본 버튼 상태
    public static final String FOLLOW_STAT_FOLLOW = "FOLLOW";
    // 로그인 사용자가 상대를 팔로우하는 버튼 상태
    public static final String FOLLOW_STAT_FOLLOWING = "FOLLOWING";
    // 상대만 로그인 사용자를 팔로우하여 맞팔로우할 수 있는 버튼 상태
    public static final String FOLLOW_STAT_BOTH_FOLW = "BOTH_FOLW";
    // 로그인 사용자와 상대가 서로 팔로우하는 친구 버튼 상태
    public static final String FOLLOW_STAT_FRIEND = "FRIEND";
    // 알림 상황 좋아요 설정값
    public static final String ALIM_SITU_LIKE = "LIKE";
    // 팔로우 요청과 독서 모임이 공유하는 알림 상황 코드
    public static final String ALIM_SITU_FOLLOW_CLUB = "FOLLOW";
    // 독후감 상태나 도서 정보에 의해 발생하는 알림 상황 코드
    public static final String ALIM_SITU_REPORT = "REPORT";
    // 독후감 댓글에 의해 발생하는 알림 상황 코드
    public static final String ALIM_SITU_REPLY = "REPLY";
    // 상황별 아이콘이 등록되지 않았을 때 사용하는 기본 알림 상황 코드
    public static final String ALIM_SITU_DEFAULT = "DEFAULT";

    // 알림 템플릿 코드 좋아요 독후감 설정값
    public static final String ALIM_TEMP_CODE_LIKE_REPORT = "LIKE_REPORT";
    // 알림 템플릿 코드 팔로우 USER 설정값
    public static final String ALIM_TEMP_CODE_FOLLOW_USER = "FOLLOW_USER";
    // 목표 독서 종료일이 지난 진행 중 독후감에 사용하는 알림 템플릿 코드
    public static final String ALIM_TEMP_CODE_REPORT_DATE_OVER = "REPORT_DATE_OVER";
    // 설정한 독서 타이머 목표시간이 지난 경우 사용하는 알림 템플릿 코드
    public static final String ALIM_TEMP_CODE_BOOK_TIMER_OVER = "BOOK_TIMER_OVER";
    // 알림 템플릿 코드 댓글 설정값
    public static final String ALIM_TEMP_CODE_REPLY_REPORT = "REPLY_REPORT";
    // 알림 템플릿 코드 댓글 좋아요 설정값
    public static final String ALIM_TEMP_CODE_REPLY_LIKE = "REPLY_LIKE";
    // 독서 모임 회원 초대 알림 템플릿 코드
    public static final String ALIM_TEMP_CODE_INVITE_CLUB = "INVITE_CLUB";

    // 정상 이용 회원 상태
    public static final String USER_STAT_ACTIVE = "ACTIVE";
    // 계정 비활성화 회원 상태
    public static final String USER_STAT_WITHDRAWN = "WITHDRAWN";
    // 영구 삭제 대기 회원 상태
    public static final String USER_STAT_DELETE_PENDING = "DELETE_PENDING";
    // 관리자 이용 정지 회원 상태
    public static final String USER_STAT_SUSPENDED = "SUSPENDED";
    // 회원 상태가 변경되어 사용자 Redis 동기화가 필요한 Outbox 이벤트 유형
    public static final String EVENT_TYPE_USER_STATUS_CHANGED = "USER_STATUS_CHANGED";
    // 사용자 서버의 반영을 기다리는 회원 정지 동기화 상태
    public static final String SUSPENSION_SYNC_PENDING = "PENDING";
    // 사용자 서버의 반영이 완료된 회원 정지 동기화 상태
    public static final String SUSPENSION_SYNC_COMPLETED = "COMPLETED";
    // 기간 회원 정지 유형
    public static final String SUSPENSION_TYPE_PERIOD = "PERIOD";
    // 무기한 회원 정지 유형
    public static final String SUSPENSION_TYPE_INDEFINITE = "INDEFINITE";
    // 효력이 있는 회원 정지 상태
    public static final String SUSPENSION_STATUS_ACTIVE = "ACTIVE";
    // 기간이 만료된 회원 정지 상태
    public static final String SUSPENSION_STATUS_EXPIRED = "EXPIRED";
    // 계정 비활성화 유형
    public static final String WITHDRAWAL_TYPE_SOFT = "SOFT";
    // 영구 탈퇴 유형
    public static final String WITHDRAWAL_TYPE_HARD = "HARD";

    // 목표 독서기간 초과 알림 스케줄러를 식별하는 로그 코드
    public static final String SCHEDULER_CODE_REPORT_DATE_OVER = "REPORT_DATE_OVER";
    // 삭제 상태 알림 물리 삭제 스케줄러를 식별하는 로그 및 상세코드
    public static final String SCHEDULER_CODE_ALIM_DELETE = "ALIM_DELETE";
    // 영구 삭제 대기 회원 물리 삭제 스케줄러 상세코드
    public static final String SCHEDULER_CODE_USER_HARD_DELETE = "USER_HARD_DELETE";
    // 회원 상태 변경 Outbox를 사용자 Redis에 반영하는 스케줄러 상세코드
    public static final String SCHEDULER_CODE_USER_STATUS_SYNC = "USER_STATUS_SYNC";
    // 보존기간이 지난 독서 타이머 상세를 삭제하는 스케줄러 세부코드
    public static final String SCHEDULER_CODE_TIMER_DETAIL_DELETE = "TIMER_DETAIL_DELETE";
    // 독서 타이머 목표시간 알림 스케줄러를 식별하는 로그 및 상세코드
    public static final String SCHEDULER_CODE_BOOK_TIMER_OVER = "BOOK_TIMER_OVER";

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
    // 사용자 조회 이력의 공지사항 대상 유형
    public static final String VIEW_TYPE_NOTICE = "NOTICE";

    // 정렬 종료 날짜 내림차순 설정값
    public static final String SORT_END_DATE_DESC = "END_DATE_DESC";
    // 정렬 시작 날짜 내림차순 설정값
    public static final String SORT_START_DATE_DESC = "START_DATE_DESC";
    // 정렬 평점 내림차순 설정값
    public static final String SORT_GRADE_DESC = "GRADE_DESC";
    // 공개 독후감 친구와 팔로잉 우선 기본 정렬 설정값
    public static final String SORT_RELATION_DESC = "RELATION_DESC";
    // 공개 독후감 최신순 정렬 설정값
    public static final String SORT_LATEST_DESC = "LATEST_DESC";
    // 공개 독후감 좋아요 내림차순 정렬 설정값
    public static final String SORT_LIKE_DESC = "LIKE_DESC";

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
