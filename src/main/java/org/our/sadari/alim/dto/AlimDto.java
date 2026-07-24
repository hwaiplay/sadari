package org.our.sadari.alim.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    }

    /**
     * 외부 API 또는 다른 서비스 구현체에서 알림을 발송할 때 사용하는 DTO이다.
     * 수신자와 대상 번호는 발송 제어값으로 명확히 받고, replaceMap에는 #{userName} 같은 템플릿 치환 값만 담는다.
     *
     * @author Seunghyeon.Kang
     */
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