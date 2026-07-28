package org.our.sadari.alim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * fileName       : AlimDto
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : 알림 요청과 응답 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    DTO 문서화 규칙 정비
 */
@Schema(description = "알림 API 요청과 응답 DTO 컨테이너", hidden = true)
public class AlimDto {
    /**
     * TB_ALTEMP에서 알림 템플릿을 조회할 때 사용하는 DTO이다.
     * 알림 상황과 템플릿 코드가 복합 PK이므로 두 값을 항상 함께 넘겨 템플릿을 특정한다.
     *
     * @author SeungHyeon.Kang
     */
    // 알림 상황과 템플릿 코드로 조회한 알림 템플릿
    @Data
    @Schema(description = "알림 템플릿 조회 DTO")
    public static class AlimTempDto {

        @Schema(description = "알림 상황", example = "LIKE")
        private String alimSitu;

        @Schema(description = "알림 템플릿 코드", example = "LIKE_REPORT")
        private String tempCode;

        @Schema(description = "템플릿명")
        private String tempTitl;

        @Schema(description = "알림 제목")
        private String alimTitl;

        @Schema(description = "알림 내용")
        private String tempCont;

        @Schema(description = "이동 URL")
        private String linkUrlx;

        @Schema(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"})
        private String useeYsno;
    }

    /**
     * TB_ALIMXX에 실제 사용자 알림을 저장하고 조회할 때 사용하는 DTO이다.
     * ALIM_NUMB는 사용자별 순번이므로 발송 시점에 Mapper에서 해당 사용자의 다음 번호를 계산한다.
     *
     * @author SeungHyeon.Kang
     */
    // 사용자에게 발송되어 알림센터에 표시되는 알림
    @Data
    @Schema(description = "사용자 알림 DTO")
    public static class AlimItemDto {

        @Schema(description = "수신 사용자 번호", example = "31")
        private Long userNumb;

        @Schema(description = "사용자별 알림 순번", example = "1")
        private Long alimNumb;

        @Schema(description = "알림 상황", example = "LIKE")
        private String alimSitu;

        @Schema(description = "알림 템플릿 코드", example = "LIKE_REPORT")
        private String tempCode;

        @Schema(description = "알림 제목")
        private String alimTitl;

        @Schema(description = "알림 내용")
        private String alimCont;

        @Schema(description = "이동 URL")
        private String linkUrlx;

        @Schema(description = "읽음 여부", example = "N", allowableValues = {"Y", "N"})
        private String readYsno;

        @Schema(description = "읽은 일시")
        private String readDate;

        @Schema(description = "발송 일시")
        private String sendDate;

        @Schema(description = "삭제 여부", example = "N", allowableValues = {"Y", "N"})
        private String deltYsno;

        @Schema(description = "알림 아이콘 옵션 코드", example = "1")
        private String alimIconCode;

        @Schema(description = "알림 아이콘 이름", example = "HEART")
        private String alimIconName;
    }

    /**
     * 알림 목록을 20개 단위로 끊어 조회하기 위한 요청 DTO이다.
     * 화면에서 스크롤로 다음 페이지를 요청할 때도 같은 DTO를 사용하며, 실제 조회 범위는 서비스에서 보정한다.
     *
     * @author SeungHyeon.Kang
     */
    // 로그인 사용자의 알림 목록 페이징 조회 조건
    @Data
    @Schema(description = "알림 목록 페이징 조회 DTO")
    public static class AlimListReqDto {

        @Schema(description = "사용자 번호", example = "31")
        private Long userNumb;

        @Schema(description = "페이지 번호", example = "1")
        private int page;

        @Schema(description = "페이지 크기", example = "20")
        private int pageSize;

        @Schema(description = "시작 행", example = "1")
        private int startRow;

        @Schema(description = "종료 행", example = "21")
        private int endRow;
    }

    /**
     * 알림 목록 응답 DTO이다.
     * 목록 조회 자체는 읽음 상태를 변경하지 않으며 삭제되지 않은 알림을 읽음 여부와 함께 전달한다.
     *
     * @author SeungHyeon.Kang
     */
    // 알림 목록과 다음 페이지 및 미읽음 건수
    @Data
    @Schema(description = "알림 목록 응답 DTO")
    public static class AlimListResDto {

        @Schema(description = "알림 목록")
        private List<AlimItemDto> list;

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(description = "다음 페이지 번호", example = "2")
        private int nextPage;

        @Schema(description = "미읽음 알림 수", example = "3")
        private int unreadCnt;
    }

    /**
     * 사용자가 클릭한 알림 한 건을 읽음 처리하기 위한 DTO이다.
     * USER_NUMB는 인증 정보로 서비스에서 설정하고 외부 요청은 사용자별 ALIM_NUMB만 전달한다.
     *
     * @author SeungHyeon.Kang
     */
    // 사용자가 클릭한 단일 알림의 읽음 처리 조건
    @Data
    @Schema(description = "알림 개별 읽음 처리 DTO")
    public static class AlimReadReqDto {

        @Schema(description = "사용자 번호", example = "31")
        private Long userNumb;

        @NotNull
        @Schema(description = "읽음 처리할 사용자별 알림 번호", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long alimNumb;
    }

    /**
     * 미읽음 알림 수만 내려줄 때 사용하는 응답 DTO이다.
     * 햄버거 메뉴에서는 목록 전체를 조회하지 않고 숫자만 표시해야 하므로 별도 DTO로 분리한다.
     *
     * @author SeungHyeon.Kang
     */
    // 로그인 사용자의 미읽음 알림 건수
    @Data
    @Schema(description = "미읽음 알림 수 DTO")
    public static class AlimUnreadCntDto {

        @Schema(description = "미읽음 알림 수", example = "3")
        private int unreadCnt;
    }

    /**
     * 외부 API 또는 다른 서비스 구현체에서 발송할 알림 정보를 전달한다
     *
     * @author SeungHyeon.Kang
     */
    // 알림 템플릿과 수신자 및 치환값을 포함한 발송 요청
    @Data
    @Schema(description = "알림 발송 요청 DTO")
    public static class AlimSendDto {

        @Schema(description = "알림 상황", example = "LIKE")
        private String alimSitu;

        @Schema(description = "알림 템플릿 코드", example = "LIKE_REPORT")
        private String tempCode;

        @Schema(description = "수신 사용자 번호", example = "31")
        private Long userNumb;

        @Schema(description = "이동 대상 번호", example = "1")
        private Long tagtNumb;

        @Schema(description = "치환 값 Map")
        private Map<String, Object> replaceMap;
    }
}
