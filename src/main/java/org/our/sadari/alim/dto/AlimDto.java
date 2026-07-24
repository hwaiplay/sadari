package org.our.sadari.alim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 알림 템플릿, 발송 요청, 사용자 알림 목록에서 공통으로 사용하는 DTO 묶음이다.
 * TB_ALTEMP는 발송 전 템플릿 원본이고 TB_ALIMXX는 사용자에게 실제로 발송된 알림 스냅샷이므로,
 * 두 테이블을 한 도메인 안에서 다루되 용도별 중첩 DTO로 파라미터 의미를 분리한다.
 *
 * @author Seunghyeon.Kang
 */
public class AlimDto {

    /**
     * TB_ALTEMP에서 알림 템플릿을 조회할 때 사용하는 DTO이다.
     * 알림 상황과 템플릿 코드가 복합 PK이므로 두 값을 항상 함께 넘겨 템플릿을 특정한다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "알림 템플릿 조회 DTO")
    public static class AlimTempDto {

        // 알림 상황이다. 예를 들어 좋아요 알림은 LIKE를 사용한다.
        @Schema(description = "알림 상황", example = "LIKE")
        private String alimSitu;

        // 알림 템플릿 코드이다. 예를 들어 독후감 좋아요 알림은 LIKE_REPORT를 사용한다.
        @Schema(description = "알림 템플릿 코드", example = "LIKE_REPORT")
        private String tempCode;

        // 관리자 화면에서 구분하기 위한 템플릿 이름이다.
        @Schema(description = "템플릿명")
        private String tempTitl;

        // 사용자 알림함에 표시할 제목 원본이다.
        @Schema(description = "알림 제목")
        private String alimTitl;

        // #{key} 형식의 치환 상용구를 포함할 수 있는 알림 내용 원본이다.
        @Schema(description = "알림 내용")
        private String tempCont;

        // 알림 클릭 시 이동할 기본 URL이다.
        @Schema(description = "이동 URL")
        private String linkUrlx;

        // 사용 여부이다. Y가 아닌 템플릿은 발송하지 않는다.
        @Schema(description = "사용 여부", example = "Y")
        private String useeYsno;
    }

    /**
     * TB_ALIMXX에 실제 사용자 알림을 저장하고 조회할 때 사용하는 DTO이다.
     * ALIM_NUMB는 사용자별 순번이므로 발송 시점에 Mapper에서 해당 사용자의 다음 번호를 계산한다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "사용자 알림 DTO")
    public static class AlimItemDto {

        // 알림을 받을 사용자 번호이다.
        @Schema(description = "수신 사용자 번호", example = "31")
        private Long userNumb;

        // 사용자별 알림 순번이다.
        @Schema(description = "사용자별 알림 순번", example = "1")
        private Long alimNumb;

        // 발송에 사용된 알림 상황이다.
        @Schema(description = "알림 상황", example = "LIKE")
        private String alimSitu;

        // 발송에 사용된 템플릿 코드이다.
        @Schema(description = "알림 템플릿 코드", example = "LIKE_REPORT")
        private String tempCode;

        // 발송 시점의 제목이다. 템플릿이 바뀌어도 기존 알림 문구는 유지된다.
        @Schema(description = "알림 제목")
        private String alimTitl;

        // 발송 시점에 치환이 완료된 내용이다.
        @Schema(description = "알림 내용")
        private String alimCont;

        // 알림 클릭 시 이동할 URL이다.
        @Schema(description = "이동 URL")
        private String linkUrlx;

        // 읽음 여부이다.
        @Schema(description = "읽음 여부", example = "N")
        private String readYsno;

        // 읽은 일시이다.
        @Schema(description = "읽은 일시")
        private String readDate;

        // 발송 일시이다.
        @Schema(description = "발송 일시")
        private String sendDate;

        // 삭제 여부이다.
        @Schema(description = "삭제 여부", example = "N")
        private String deltYsno;

        // 알림 상황 공통코드의 OPT1_CODE이다. 현재는 아이콘 표시 옵션으로 1을 사용한다.
        @Schema(description = "알림 아이콘 옵션 코드", example = "1")
        private String alimIconCode;

        // 알림 상황 공통코드의 OPT1_NAME이다. 화면에서는 HEART, FOLLOW 값에 따라 아이콘을 분기한다.
        @Schema(description = "알림 아이콘 이름", example = "HEART")
        private String alimIconName;
    }

    /**
     * 외부 API 또는 다른 서비스 구현체에서 알림을 발송할 때 사용하는 DTO이다.
     * 수신자와 대상 번호는 발송 제어값으로 명확히 받고, replaceMap에는 #{userName} 같은 템플릿 치환 값만 담는다.
     *
     * @author Seunghyeon.Kang
     */
    /**
     * 알림 목록을 20개 단위로 끊어 조회하기 위한 요청 DTO이다.
     * 화면에서 스크롤로 다음 페이지를 요청할 때도 같은 DTO를 사용하며, 실제 조회 범위는 서비스에서 보정한다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "알림 목록 페이징 조회 DTO")
    public static class AlimListReqDto {

        // 알림을 조회할 로그인 사용자 번호이다.
        @Schema(description = "사용자 번호", example = "31")
        private Long userNumb;

        // 화면에서 요청한 페이지 번호이다. 1보다 작으면 서비스에서 1로 보정한다.
        @Schema(description = "페이지 번호", example = "1")
        private int page;

        // 실제로 화면에 노출할 개수이다. 현재 정책은 20개 고정이다.
        @Schema(description = "페이지 크기", example = "20")
        private int pageSize;

        // Oracle ROW_NUMBER 기준 시작 행이다.
        @Schema(description = "시작 행", example = "1")
        private int startRow;

        // 다음 페이지 존재 여부를 판단하기 위해 pageSize + 1개까지 조회하는 종료 행이다.
        @Schema(description = "종료 행", example = "21")
        private int endRow;
    }

    /**
     * 알림 목록 응답 DTO이다.
     * 조회된 목록만 읽음 처리하기 때문에, 사용자가 첫 화면만 보고 나가면 첫 페이지 20개만 읽음 처리된다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "알림 목록 응답 DTO")
    public static class AlimListResDto {

        // 화면에 실제로 표시할 알림 목록이다.
        @Schema(description = "알림 목록")
        private List<AlimItemDto> list;

        // pageSize보다 1개 더 조회된 데이터가 있으면 다음 페이지가 존재한다.
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        private boolean hasNext;

        // 다음 스크롤 요청 때 사용할 페이지 번호이다.
        @Schema(description = "다음 페이지 번호", example = "2")
        private int nextPage;

        // 현재 사용자의 남은 미읽음 알림 수이다.
        @Schema(description = "미읽음 알림 수", example = "3")
        private int unreadCnt;
    }

    /**
     * 조회된 알림만 읽음 처리하기 위한 DTO이다.
     * 모두 읽음과 달리 화면에 실제로 도착한 알림 번호만 담아 부분 읽음 처리한다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "알림 부분 읽음 처리 DTO")
    public static class AlimReadReqDto {

        // 로그인 사용자 번호이다. 다른 사용자의 알림 번호가 섞여 들어와도 WHERE 절에서 한 번 더 제한한다.
        @Schema(description = "사용자 번호", example = "31")
        private Long userNumb;

        // 이번 조회 결과로 화면에 노출된 알림 목록이다.
        @Schema(description = "읽음 처리할 알림 목록")
        private List<AlimItemDto> alimList;
    }

    /**
     * 미읽음 알림 수만 내려줄 때 사용하는 응답 DTO이다.
     * 햄버거 메뉴에서는 목록 전체를 조회하지 않고 숫자만 표시해야 하므로 별도 DTO로 분리한다.
     *
     * @author Seunghyeon.Kang
     */
    @Data
    @Schema(description = "미읽음 알림 수 DTO")
    public static class AlimUnreadCntDto {

        // 읽지 않은 알림 수이다.
        @Schema(description = "미읽음 알림 수", example = "3")
        private int unreadCnt;
    }

    @Data
    @Schema(description = "알림 발송 요청 DTO")
    public static class AlimSendDto {

        // 발송할 알림 상황이다.
        @Schema(description = "알림 상황", example = "LIKE")
        private String alimSitu;

        // 발송할 템플릿 코드이다.
        @Schema(description = "알림 템플릿 코드", example = "LIKE_REPORT")
        private String tempCode;

        // 알림을 받을 사용자 번호이다.
        @Schema(description = "수신 사용자 번호", example = "31")
        private Long userNumb;

        // 알림 클릭 시 이동할 대상 번호이다. 실제 링크는 TB_ALTEMP.LINK_URLX와 이 값을 조합한다.
        @Schema(description = "이동 대상 번호", example = "1")
        private Long tagtNumb;

        // 템플릿 치환 값만 담는 Map이다.
        @Schema(description = "치환 값 Map")
        private Map<String, Object> replaceMap;
    }
}
