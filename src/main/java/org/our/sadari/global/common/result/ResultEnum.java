package org.our.sadari.global.common.result;

import lombok.Getter;

/**
 * ResultEnum 열거형에서 사용하는 고정 값을 정의한다.
 *
 * @author Seunghyeon.Kang
 */
@Getter
public enum ResultEnum {

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_SAVE_SUCCESS(2001, "common.alert.0001"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_UPDATE_SUCCESS(2002, "common.alert.0002"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_DELETE_SUCCESS(2003, "common.alert.0003"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_NO_DATA(2004, "common.alert.0004"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_SAVE_REJECTED(2005, "common.alert.0005"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_UPDATE_REJECTED(2006, "common.alert.0006"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_DELETE_REJECTED(2007, "common.alert.0007"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_SEARCH_REJECTED(2008, "common.alert.0008"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_INVALID_REQUEST(2009, "common.alert.0009"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_REPORT_CONTENT_TOO_LONG(2010, "common.alert.0010"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_REPORT_DATE_RANGE_INVALID(2011, "common.alert.0011"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_REPORT_REQUIRED_MISSING(2012, "common.alert.0012"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_REPORT_BOOK_INVALID(2013, "common.alert.0013"),

    /**
     * 데이터베이스 연결을 가져오지 못해 요청을 처리할 수 없을 때 사용하는 enum 항목이다.
     */
    COMMON_DB_CONNECTION_FAILED(2014, "common.alert.0014"),

    COMMON_BAD_WORD_INCLUDED(2015, "common.alert.0015"),

    USER_NICK_DUPLICATED(2016, "user.alert.0001"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    COMMON_ACCESS_REJECTED(2020, "common.alert.0020"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    AUTH_FAIL(1001, "auth.common.fail"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    TOKEN_INVALID(1002, "auth.token.invalid"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    TOKEN_EXPIRED(1003, "auth.token.expired"),

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    FORBIDDEN(1004, "auth.common.forbidden");

    private final int code;
    private final String messageKey;

    /**
     * 처리 결과와 메시지 키를 연결하는 enum 항목이다.
     */
    ResultEnum(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
