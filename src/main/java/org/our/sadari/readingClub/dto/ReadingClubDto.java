package org.our.sadari.readingClub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * fileName       : ReadingClubDto
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 독서 모임 1차 기능의 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Schema(description = "독서 모임 API DTO 컨테이너", hidden = true)
public final class ReadingClubDto {

    /**
     * fileName       : ClubCreateReqDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 독서 모임 생성 입력값을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "독서 모임 생성 요청")
    public static class ClubCreateReqDto {

        @Schema(description = "생성된 모임 번호", hidden = true)
        private Long clubNumb;

        @NotBlank
        @Size(max = 100)
        @Schema(description = "모임명", example = "주말 소설 읽기")
        private String clubName;

        @NotBlank
        @Size(max = 2000)
        @Schema(description = "모임 소개")
        private String clubCntn;

        @NotBlank
        @Schema(description = "공개 범위", allowableValues = {"PUBLIC", "PRIVATE"})
        private String clubVisb;

        @NotBlank
        @Schema(description = "가입 방식", allowableValues = {"OPEN", "APPROVAL", "INVITE"})
        private String joinType;

        @NotNull
        @Min(2)
        @Max(100)
        @Schema(description = "모임 정원", example = "10")
        private Integer maxxMemb;

        @NotEmpty
        @Size(max = 3)
        @Schema(description = "관심분야 세부코드 1~3개")
        private List<@NotBlank String> categoryList;

        @Size(max = 5)
        @Schema(description = "승인형 가입 질문 1~5개")
        private List<@NotBlank @Size(max = 500) String> questionList;
    }

    /**
     * fileName       : ClubViewDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 독서 모임 목록과 상세 화면에 필요한 현재 상태를 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "독서 모임 조회 항목")
    public static class ClubViewDto {

        @Schema(description = "모임 번호")
        private Long clubNumb;

        @Schema(description = "현재 모임장 사용자 번호")
        private Long ownrNumb;

        @Schema(description = "모임장 닉네임")
        private String ownrNick;

        @Schema(description = "모임명")
        private String clubName;

        @Schema(description = "모임 소개")
        private String clubCntn;

        @Schema(description = "공개 범위")
        private String clubVisb;

        @Schema(description = "가입 방식")
        private String joinType;

        @Schema(description = "모임 운영 상태")
        private String clubStat;

        @Schema(description = "모임 정원")
        private Integer maxxMemb;

        @Schema(description = "활성 모임원 수")
        private Integer memberCnt;

        @Schema(description = "유효한 초대 예약석 수")
        private Integer invitedCnt;

        @Schema(description = "로그인 사용자의 모임원 상태")
        private String membStat;

        @Schema(description = "로그인 사용자의 모임원 역할")
        private String membRole;

        @Schema(description = "로그인 사용자의 가입 신청 상태")
        private String joinStat;

        @Schema(description = "로그인 사용자의 관심분야 정확 일치 수")
        private Integer matchCnt;

        @Schema(description = "카테고리 코드와 이름 목록")
        private List<CategoryDto> categoryList;

        @Schema(description = "승인 가입 질문 목록")
        private List<String> questionList;

        @Schema(description = "모임 생성 일시")
        private LocalDateTime regiDate;
    }

    /**
     * fileName       : CategoryDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 모임 카테고리 세부코드와 화면 표시명을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "모임 카테고리 항목")
    public static class CategoryDto {

        @Schema(description = "관심분야 세부코드")
        private String intrCode;

        @Schema(description = "관심분야 세부코드명")
        private String intrName;

        @Schema(description = "대분류명")
        private String intrCnam;

        @Schema(description = "모임 내 노출 순서")
        private Integer sortOrdr;

    }

    /**
     * fileName       : QuestionDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 모임당 한 행으로 저장한 승인 가입 질문을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "모임 가입 질문", hidden = true)
    public static class QuestionDto {

        // 질문이 적용되는 모임 번호
        private Long clubNumb;
        // 첫 번째 가입 질문
        private String quesFirs;
        // 두 번째 가입 질문
        private String quesSeco;
        // 세 번째 가입 질문
        private String quesThir;
        // 네 번째 가입 질문
        private String quesFour;
        // 다섯 번째 가입 질문
        private String quesFift;
    }

    /**
     * fileName       : MemberDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 동일 모임과 사용자의 현재 회원 또는 초대 관계를 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "모임 회원 관계", hidden = true)
    public static class MemberDto {

        // 회원 관계가 귀속되는 모임 번호
        private Long clubNumb;
        // 모임 회원 또는 초대 대상 사용자 번호
        private Long userNumb;
        // 모임장 또는 일반 회원 역할
        private String membRole;
        // 활성 회원 또는 초대 대기 상태
        private String membStat;
        // 초대 예약석 만료 일시
        private LocalDateTime exprDate;
        // 탈퇴 후 재가입 차단 여부
        private String blocYsno;
    }

    /**
     * fileName       : JoinReqDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 공개 승인형 모임의 질문별 장문 답변을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "모임 가입 요청")
    public static class JoinReqDto {

        @Size(max = 5)
        @Schema(description = "가입 질문 순서와 같은 장문 답변 목록")
        private List<@NotBlank @Size(max = 2000) String> answerList;
    }

    /**
     * fileName       : InviteReqDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 모임장이 맞팔로우 사용자에게 발송할 초대 대상을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "맞팔로우 초대 요청")
    public static class InviteReqDto {

        @NotEmpty
        @Schema(description = "초대할 맞팔로우 사용자 번호")
        private List<@NotNull Long> userNumbList;
    }

    /**
     * fileName       : ApplicationDecisionReqDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 모임장이 가입 신청에 내리는 승인 또는 거절 결정을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "가입 신청 처리 요청")
    public static class ApplicationDecisionReqDto {

        @NotBlank
        @Schema(description = "처리 상태", allowableValues = {"APPROVED", "REJECTED"})
        private String joinStat;
    }

    /**
     * fileName       : InviteCandidateDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 모임장의 맞팔 초대 후보 정보를 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "맞팔로우 초대 후보")
    public static class InviteCandidateDto {

        @Schema(description = "사용자 번호")
        private Long userNumb;

        @Schema(description = "닉네임")
        private String userNick;

        @Schema(description = "프로필 이미지 경로")
        private String porfPath;

        @Schema(description = "한 줄 소개")
        private String intrCntn;
    }

    /**
     * fileName       : InvitationDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 로그인 사용자에게 도착한 유효한 모임 초대를 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "수신 모임 초대")
    public static class InvitationDto {

        @Schema(description = "모임 번호")
        private Long clubNumb;

        @Schema(description = "모임명")
        private String clubName;

        @Schema(description = "초대 발송자 닉네임")
        private String senderNick;

        @Schema(description = "초대 발송 일시")
        private LocalDateTime invtDate;

        @Schema(description = "초대 만료 일시")
        private LocalDateTime exprDate;
    }

    /**
     * fileName       : ApplicationDto
     * author         : SeungHyeon.Kang
     * date           : 2026-08-05
     * description    : 모임장이 심사할 처리 중 가입 신청의 질문과 답변을 전달한다
     * ===========================================================
     * DATE              AUTHOR             NOTE
     * -----------------------------------------------------------
     * 2026-08-05        SeungHyeon.Kang    최초 생성
     */
    @Data
    @Schema(description = "가입 신청 심사 항목")
    public static class ApplicationDto {

        @Schema(description = "모임 번호")
        private Long clubNumb;

        @Schema(description = "모임별 신청 번호")
        private Long applNumb;

        @Schema(description = "신청 사용자 번호")
        private Long userNumb;

        @Schema(description = "신청자 닉네임")
        private String userNick;

        @Schema(description = "신청자 프로필 이미지 경로")
        private String porfPath;

        @Schema(description = "신청 당시 질문 목록")
        private List<String> questionList;

        @Schema(description = "처리 전 장문 답변 목록")
        private List<String> answerList;

        @Schema(description = "가입 신청 상태")
        private String joinStat;

        @Schema(description = "가입 신청 일시")
        private LocalDateTime applDate;

        // 첫 번째 가입 질문 사본
        private String quesFirs;
        // 두 번째 가입 질문 사본
        private String quesSeco;
        // 세 번째 가입 질문 사본
        private String quesThir;
        // 네 번째 가입 질문 사본
        private String quesFour;
        // 다섯 번째 가입 질문 사본
        private String quesFift;
        // 첫 번째 가입 답변
        private String ansrFirs;
        // 두 번째 가입 답변
        private String ansrSeco;
        // 세 번째 가입 답변
        private String ansrThir;
        // 네 번째 가입 답변
        private String ansrFour;
        // 다섯 번째 가입 답변
        private String ansrFift;
    }

    private ReadingClubDto() {
        // DTO 컨테이너 인스턴스 생성을 차단한다
    }
}
