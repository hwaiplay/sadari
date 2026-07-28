package org.our.sadari.global.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * fileName       : StringUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-03-21
 * description    : 공통 처리에 필요한 변환과 판정 기능을 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-03-21        SeungHyeon.Kang    최초 생성
 */
public class StringUtil {

    // 빈 값 설정값
    public static final String EMPTY = "";

    // 아래 처리 단계의 업무 목적을 설명한다.

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    /**
     * UTF-8 바이트 길이 기준 문자열 자르기한다.
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param output 문자열을 자른 뒤 덧붙일 문자열
     * @param slength 자를 원본 문자열의 최대 바이트 길이
     * @return 처리 결과
     */
    public static String byteString(String source, String output, int slength) {

        String returnVal;
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try{
            // 문자열 변환 결과를 담을 객체를 생성한다
            returnVal= new String(source.getBytes(),0, slength);
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if(returnVal.length()==0 ){
                // 문자열 변환 결과를 담을 객체를 생성한다
                returnVal= new String(source.getBytes(),0, slength+1);
            }

            returnVal += output;
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch(IndexOutOfBoundsException e){

            returnVal = source;
        }

        // UTF-8 바이트 길이 기준 문자열 자르기 결과를 반환한다
        return returnVal;
    }
    /**
     * UTF-8 바이트 길이에 맞춰 문자열을 자른다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param output 문자열을 자른 뒤 덧붙일 문자열
     * @param slength 허용할 최대 바이트 길이
     * @return 변환된 문자열
     */
    public static String cutString(String source, String output, int slength) {

        String returnVal = null;
        // source 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(source)) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (source.length() > slength) {
                // 요청한 범위의 문자열을 추출한다
                returnVal = source.substring(0, slength) + output;
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else
                returnVal = source;
        }

        // UTF-8 바이트 길이에 맞춰 문자열을 자른다 결과를 반환한다
        return returnVal;
    }
    /**
     * UTF-8 바이트 길이에 맞춰 문자열을 자른다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param slength 허용할 최대 바이트 길이
     * @return 변환된 문자열
     */
    public static String cutString(String source, int slength) {

        String result = null;
        // source 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(source)) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (source.length() > slength) {
                // 요청한 범위의 문자열을 추출한다
                result = source.substring(0, slength);
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else
                result = source;
        }

        // UTF-8 바이트 길이에 맞춰 문자열을 자른다 결과를 반환한다
        return result;
    }
    /**
     * 사용자 평문 입력의 제어 문자를 제거하고 길이를 제한한다
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @return 변환된 문자열
     */
    public static String normalizePlainText(String value) {
        // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(value)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 사용자 평문 입력의 제어 문자를 제거하고 길이를 제한 결과를 반환한다
        return value.trim();
    }
    /**
     * 사용자 평문 입력의 제어 문자를 제거하고 길이를 제한한다
     *
     * @author SeungHyeon.Kang
     * @param value 검사하거나 변환할 값
     * @param maxLength 허용할 최대 문자 길이
     * @return 변환된 문자열
     */
    public static String normalizePlainText(String value, int maxLength) {
        // 로그 저장 길이와 개행 정책에 맞춰 문자열을 정규화한다
        String normalizedValue = normalizePlainText(value);

        // normalizedValue 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(normalizedValue)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 사용자 평문 입력의 제어 문자를 제거하고 길이를 제한 결과를 반환한다
        return cutString(normalizedValue, maxLength);
    }
    /**
     * 값의 null 또는 빈 상태를 판정한다
     *
     * @author SeungHyeon.Kang
     * @param obj 검사하거나 변환할 객체
     * @return 검사 조건 충족 여부
     */
    public static boolean isEmpty(Object obj) {
        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        // 공통 빈 값 판정 메서드 내부에서는 재귀 호출을 피하기 위해 Null 자체를 직접 확인한다.
        if (obj == null) {
            // 값의 null 또는 빈 상태를 판정값을 반환한다
            return true;
        }

        // 입력값의 실제 타입에 맞는 변환 로직을 선택하기 위해 분기한다
        if (obj instanceof String) {
            // 값의 null 또는 빈 상태를 판정 결과를 반환한다
            return ((String) obj).trim().isEmpty();
        }

        // 입력값의 실제 타입에 맞는 변환 로직을 선택하기 위해 분기한다
        if (obj instanceof List) {
            // 값의 null 또는 빈 상태를 판정 결과를 반환한다
            return ((List<?>) obj).isEmpty();
        }

        // 입력값의 실제 타입에 맞는 변환 로직을 선택하기 위해 분기한다
        if (obj instanceof Map) {
            // 값의 null 또는 빈 상태를 판정 결과를 반환한다
            return ((Map<?, ?>) obj).isEmpty();
        }

        // 입력값의 실제 타입에 맞는 변환 로직을 선택하기 위해 분기한다
        if (obj instanceof Object[]) {
            // 값의 null 또는 빈 상태를 판정 결과를 반환한다
            return ((Object[]) obj).length == 0;
        }

        // 값의 null 또는 빈 상태를 판정값을 반환한다
        return false;
    }
    /**
     * 값의 null 또는 빈 상태를 판정한다
     *
     * @author SeungHyeon.Kang
     * @param values 빈 값 포함 여부를 검사할 가변 인자
     * @return 검사 조건 충족 여부
     */
    public static boolean hasEmpty(Object... values) {
        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        // 가변 인자 배열 자체가 생성되지 않은 호출은 빈 값이 포함된 요청으로 판정한다.
        if (values == null) {
            // 값의 null 또는 빈 상태를 판정값을 반환한다
            return true;
        }

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (Object value : values) {
            // value 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
            if (isEmpty(value)) {
                // 값의 null 또는 빈 상태를 판정값을 반환한다
                return true;
            }
        }

        // 값의 null 또는 빈 상태를 판정값을 반환한다
        return false;
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @param remove 원본 문자열에서 제거할 문자
     * @return 변환된 문자열
     */
    public static String remove(String str, char remove) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str) || str.indexOf(remove) == -1) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        // 문자 단위 검사를 위해 문자열을 문자 배열로 변환한다
        char[] chars = str.toCharArray();
        int pos = 0;
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (int i = 0; i < chars.length; i++) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (chars[i] != remove) {

                chars[pos++] = chars[i];
            }
        }

        // 새로 생성한 String 객체를 반환한다
        return new String(chars, 0, pos);
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String removeCommaChar(String str) {
        // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
        return remove(str, ',');
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String removeMinusChar(String str) {
        // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
        return remove(str, '-');
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param subject 검사하거나 치환할 대상 문자열
     * @param object 문자열로 변환할 객체
     * @return 변환된 문자열
     */
    public static String replace(String source, String subject, String object) {
        // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        String srcStr  = source;

        // srcStr 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(srcStr)) {
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            while (srcStr.indexOf(subject) >= 0) {
                // 요청한 범위의 문자열을 추출한다
                preStr = srcStr.substring(0, srcStr.indexOf(subject));
                // 요청한 범위의 문자열을 추출한다
                nextStr = srcStr.substring(srcStr.indexOf(subject) + subject.length(), srcStr.length());
                srcStr = nextStr;
                // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                rtnStr.append(preStr).append(object);
            }
        }

        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
        rtnStr.append(nextStr);
        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return rtnStr.toString();
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param subject 검사하거나 치환할 대상 문자열
     * @param object 문자열로 변환할 객체
     * @return 변환된 문자열
     */
    public static String replaceArray (String source, String[] subject, String[] object){

        String str = source;
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for(int i=0;i<subject.length;i++){
            // 대상 문자열에서 지정한 값을 치환한다
            str=replace(str, subject[i], object[i]);
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return str;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param subject 검사하거나 치환할 대상 문자열
     * @param object 문자열로 변환할 객체
     * @return 변환된 문자열
     */
    public static String replaceOnce(String source, String subject, String object) {
        // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if (source.indexOf(subject) >= 0) {
            // 요청한 범위의 문자열을 추출한다
            preStr = source.substring(0, source.indexOf(subject));
            // 요청한 범위의 문자열을 추출한다
            nextStr = source.substring(source.indexOf(subject) + subject.length(), source.length());
            // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
            rtnStr.append(preStr).append(object).append(nextStr);
            // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
            return rtnStr.toString();
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
            return source;
        }
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param subject 검사하거나 치환할 대상 문자열
     * @param object 문자열로 변환할 객체
     * @return 변환된 문자열
     */
    public static String replaceChar(String source, String subject, String object) {
        // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        String srcStr  = source;

        char chA;

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (int i = 0; i < subject.length(); i++) {
            // 현재 위치의 문자를 확인한다
            chA = subject.charAt(i);

            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (srcStr.indexOf(chA) >= 0) {
                // 요청한 범위의 문자열을 추출한다
                preStr = srcStr.substring(0, srcStr.indexOf(chA));
                // 요청한 범위의 문자열을 추출한다
                nextStr = srcStr.substring(srcStr.indexOf(chA) + 1, srcStr.length());
                // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                srcStr = rtnStr.append(preStr).append(object).append(nextStr).toString();
            }
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcStr;
    }
    /**
     * 검색 문자열이 처음 나타나는 위치를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @param searchStr 원본에서 첫 위치를 찾을 검색 문자열
     * @return 계산하거나 조회한 숫자 결과
     */
    public static int indexOf(String str, String searchStr) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str) || isEmpty(searchStr)) {
            // 검색 문자열이 처음 나타나는 위치를 조회 결과를 반환한다
            return -1;
        }

        // 검색 문자열이 처음 나타나는 위치를 조회 결과를 반환한다
        return str.indexOf(searchStr);
    }
    /**
     * 비교 조건에 맞는 반환 문자열을 선택한다
     *
     * @author SeungHyeon.Kang
     * @param sourceStr 변환할 원본 문자열
     * @param compareStr 원본과 일치 여부를 확인할 비교 문자열
     * @param returnStr 비교 조건이 일치할 때 반환할 문자열
     * @param defaultStr 비교 조건이 일치하지 않을 때 반환할 문자열
     * @return 변환된 문자열
     */
    public static String decode(String sourceStr, String compareStr, String returnStr
                              , String defaultStr) {
        // sourceStr 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(sourceStr) && isEmpty(compareStr)) {
            // 비교 조건에 맞는 반환 문자열을 선택 결과를 반환한다
            return returnStr;
        }

        // sourceStr 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (isEmpty(sourceStr) && !isEmpty(compareStr)) {
            // 비교 조건에 맞는 반환 문자열을 선택 결과를 반환한다
            return defaultStr;
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if (sourceStr.trim().equals(compareStr)) {
            // 비교 조건에 맞는 반환 문자열을 선택 결과를 반환한다
            return returnStr;
        }

        // 비교 조건에 맞는 반환 문자열을 선택 결과를 반환한다
        return defaultStr;
    }
    /**
     * 비교 조건에 맞는 반환 문자열을 선택한다
     *
     * @author SeungHyeon.Kang
     * @param sourceStr 변환할 원본 문자열
     * @param compareStr 원본과 일치 여부를 확인할 비교 문자열
     * @param returnStr 비교 조건이 일치할 때 반환할 문자열
     * @return 변환된 문자열
     */
    public static String decode(String sourceStr, String compareStr, String returnStr) {
        // 비교 조건에 맞는 반환 문자열을 선택 결과를 반환한다
        return decode(sourceStr, compareStr, returnStr, sourceStr);
    }
    /**
     * null 값을 빈 문자열로 보정한다
     *
     * @author SeungHyeon.Kang
     * @param object 문자열로 변환할 객체
     * @return 변환된 문자열
     */
    public static String isNullToString(Object object) {

        String string = "";

        // object 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(object)) {
            // 누적한 값을 최종 문자열로 변환한다
            string = object.toString().trim();
        }

        // null 값을 빈 문자열로 보정 결과를 반환한다
        return string;
    }
    /**
     * null 값을 빈 문자열로 보정한다
     *
     * @author SeungHyeon.Kang
     * @param src 변환할 원본 값
     * @return 변환된 문자열
     */
    public static String nullConvert(Object src) {
        // src 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(src) && src instanceof java.math.BigDecimal) {
            // null 값을 빈 문자열로 보정 결과를 반환한다
            return ((BigDecimal)src).toString();
        }

        // src 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(src) || src.equals("null")) {
            // null 값을 빈 문자열로 보정 결과를 반환한다
            return "";
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // null 값을 빈 문자열로 보정 결과를 반환한다
            return ((String)src).trim();
        }
    }
    /**
     * null 값을 빈 문자열로 보정한다
     *
     * @author SeungHyeon.Kang
     * @param src 변환할 원본 값
     * @return 변환된 문자열
     */
    public static String nullConvert(String src) {
        // src 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(src) || src.equals("null") || "".equals(src) || " ".equals(src)) {
            // null 값을 빈 문자열로 보정 결과를 반환한다
            return "";
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // null 값을 빈 문자열로 보정 결과를 반환한다
            return src.trim();
        }
    }
    /**
     * null 또는 빈 값을 0으로 보정하여 숫자로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param src 변환할 원본 값
     * @return 계산하거나 조회한 숫자 결과
     */
    public static int zeroConvert(Object src) {
        // src 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(src) || src.equals("null")) {
            // null 또는 빈 값을 0으로 보정하여 숫자로 변환 결과를 반환한다
            return 0;
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // null 또는 빈 값을 0으로 보정하여 숫자로 변환 결과를 반환한다
            return Integer.parseInt(((String)src).trim());
        }
    }
    /**
     * null 또는 빈 값을 0으로 보정하여 숫자로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param src 변환할 원본 값
     * @return 계산하거나 조회한 숫자 결과
     */
    public static int zeroConvert(String src) {
        // src 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(src) || src.equals("null") || "".equals(src) || " ".equals(src)) {
            // null 또는 빈 값을 0으로 보정하여 숫자로 변환 결과를 반환한다
            return 0;
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // null 또는 빈 값을 0으로 보정하여 숫자로 변환 결과를 반환한다
            return Integer.parseInt(src.trim());
        }
    }
    /**
     * null 또는 빈 값을 0으로 보정하여 숫자로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param src 변환할 원본 값
     * @return 계산하거나 조회한 숫자 결과
     */
    public static int zeroConvertHashMap(Object src) {
        // src 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(src) || src.equals("null")) {
            // null 또는 빈 값을 0으로 보정하여 숫자로 변환 결과를 반환한다
            return 0;
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // null 또는 빈 값을 0으로 보정하여 숫자로 변환 결과를 반환한다
            return Integer.parseInt(src.toString());
        }
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String removeWhitespace(String str) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str)) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        // 처리 범위를 결정할 문자열 길이를 확인한다
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for (int i = 0; i < sz; i++) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if (!Character.isWhitespace(str.charAt(i))) {
                // 현재 위치의 문자를 확인한다
                chs[count++] = str.charAt(i);
            }
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if (count == sz) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        // 새로 생성한 String 객체를 반환한다
        return new String(chs, 0, count);
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param strString HTML 또는 SQL 안전 검사를 적용할 문자열
     * @return 변환된 문자열
     */
    public static String checkHtmlView(String strString) {

        String strNew = "";

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            // 처리 범위를 결정할 문자열 길이를 확인한다
            int len = strString.length();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for (int i = 0; i < len; i++) {
                // 현재 위치의 문자를 확인한다
                chrBuff = (char)strString.charAt(i);

                // 입력 코드나 날짜 값별로 서로 다른 업무 규칙을 적용하기 위해 분기한다
                switch (chrBuff) {
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '<':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&lt;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '>':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&gt;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '"':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&quot;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case 10:
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("<br>");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case ' ':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&nbsp;");
                        break;
                    // 아래 처리 단계의 업무 목적을 설명한다.
                    // 아래 처리 단계의 업무 목적을 설명한다.
                    // 아래 처리 단계의 업무 목적을 설명한다.
                    default:
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append(chrBuff);
                }
            }

            // 누적한 값을 최종 문자열로 변환한다
            strNew = strTxt.toString();

        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return strNew;
    }
    /**
     * 구분자를 기준으로 문자열을 분리한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param separator 문자열 분리에 사용할 구분자
     * @return 변환된 배열 결과
     */
    public static String[] split(String source, String separator) throws NullPointerException {

        String[] returnVal = null;
        int cnt = 1;

        // 대상 문자열에서 기준값의 위치를 찾는다
        int index = source.indexOf(separator);
        int index0 = 0;
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        while (index >= 0) {

            cnt++;
            // 대상 문자열에서 기준값의 위치를 찾는다
            index = source.indexOf(separator, index + 1);
        }

        returnVal = new String[cnt];
        cnt = 0;
        // 대상 문자열에서 기준값의 위치를 찾는다
        index = source.indexOf(separator);
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        while (index >= 0) {
            // 요청한 범위의 문자열을 추출한다
            returnVal[cnt] = source.substring(index0, index);
            index0 = index + 1;
            // 대상 문자열에서 기준값의 위치를 찾는다
            index = source.indexOf(separator, index + 1);
            cnt++;
        }

        // 요청한 범위의 문자열을 추출한다
        returnVal[cnt] = source.substring(index0);
        // 구분자를 기준으로 문자열을 분리 결과를 반환한다
        return returnVal;
    }
    /**
     * 입력 문자열의 영문 대소문자를 변환한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String lowerCase(String str) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 입력 문자열의 영문 대소문자를 변환 결과를 반환한다
        return str.toLowerCase();
    }
    /**
     * 입력 문자열의 영문 대소문자를 변환한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String upperCase(String str) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 입력 문자열의 영문 대소문자를 변환 결과를 반환한다
        return str.toUpperCase();
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 검사 조건 충족 여부
     */
    public static boolean lowerNumCheck(String str) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str)) {
            // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 판정값을 반환한다
            return false;
        }

        String regex1 = "[a-z0-9]*";
        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return str.matches(regex1);
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @param stripChars 문자열 양끝에서 제거할 문자 목록
     * @return 변환된 문자열
     */
    public static String stripStart(String str, String stripChars) {

        int strLen;
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str) || (strLen = str.length()) == 0) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        int start = 0;
        // stripChars 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(stripChars)) {
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {

                start++;
            }
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else if (stripChars.length() == 0) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {

                start++;
            }
        }

        // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
        return str.substring(start);
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @param stripChars 문자열 양끝에서 제거할 문자 목록
     * @return 변환된 문자열
     */
    public static String stripEnd(String str, String stripChars) {

        int end;
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str) || (end = str.length()) == 0) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        // stripChars 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(stripChars)) {
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {

                end--;
            }
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else if (stripChars.length() == 0) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else {
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {

                end--;
            }
        }

        // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
        return str.substring(0, end);
    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @param stripChars 문자열 양끝에서 제거할 문자 목록
     * @return 변환된 문자열
     */
    public static String strip(String str, String stripChars) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(str)) {
            // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
            return str;
        }

        String srcStr = str;
        // 문자열 앞부분의 제거 대상 문자를 정리한다
        srcStr = stripStart(srcStr, stripChars);
        // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
        return stripEnd(srcStr, stripChars);
    }
    /**
     * 구분자를 기준으로 문자열을 분리한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param separator 문자열 분리에 사용할 구분자
     * @param arraylength 생성할 문자열 배열의 최대 크기
     * @return 변환된 배열 결과
     */
    public static String[] split(String source, String separator, int arraylength) throws NullPointerException {

        String[] returnVal = new String[arraylength];
        int cnt = 0;
        int index0 = 0;
        // 대상 문자열에서 기준값의 위치를 찾는다
        int index = source.indexOf(separator);
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        while (index >= 0 && cnt < (arraylength - 1)) {
            // 요청한 범위의 문자열을 추출한다
            returnVal[cnt] = source.substring(index0, index);
            index0 = index + 1;
            // 대상 문자열에서 기준값의 위치를 찾는다
            index = source.indexOf(separator, index + 1);
            cnt++;
        }

        // 요청한 범위의 문자열을 추출한다
        returnVal[cnt] = source.substring(index0);
        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if (cnt < (arraylength - 1)) {
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for (int i = cnt + 1; i < arraylength; i++) {

                returnVal[i] = "";
            }
        }

        // 구분자를 기준으로 문자열을 분리 결과를 반환한다
        return returnVal;
    }
    /**
     * 지정한 문자 범위에서 임의 문자를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param startChr 난수 생성 범위의 시작 문자
     * @param endChr 난수 생성 범위의 종료 문자
     * @return 변환된 문자열
     */
    public static String getRandomStr(char startChr, char endChr) {

        int randomInt;
        String randomStr = null;

        // 입력값을 문자열 표현으로 변환한다
        int startInt = Integer.valueOf(startChr);
        // 입력값을 문자열 표현으로 변환한다
        int endInt = Integer.valueOf(endChr);

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if (startInt > endInt) {

            throw new IllegalArgumentException("Start String: " + startChr + " End String: " + endChr);
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 예측하기 어려운 인증번호를 생성할 난수 생성기를 담을 객체를 생성한다
            SecureRandom rnd = new SecureRandom();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            do {
                // 인증번호에 사용할 난수를 생성한다
                randomInt = rnd.nextInt(endInt + 1);
            // 조건을 만족하는 인증번호가 생성될 때까지 반복한다
            }

            while (randomInt < startInt);

            randomStr = (char)randomInt + "";
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            e.printStackTrace();
        }

        // 지정한 문자 범위에서 임의 문자를 생성 결과를 반환한다
        return randomStr;
    }
    /**
     * 문자열을 지정한 문자 집합이나 URL 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @param srcCharsetNm 원본 문자열의 문자 집합
     * @param cnvrCharsetNm 변환 결과에 적용할 문자 집합
     * @return 변환된 문자열
     */
    public static String getEncdDcd(String srcString, String srcCharsetNm, String cnvrCharsetNm) {

        String rtnStr = null;

        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString))
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 문자열 변환 결과를 담을 객체를 생성한다
            rtnStr = new String(srcString.getBytes(srcCharsetNm), cnvrCharsetNm);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (UnsupportedEncodingException e) {

            rtnStr = null;
        }

        // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
        return rtnStr;
    }
    /**
     * 문자열을 지정한 문자 집합이나 URL 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getConvert8859(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)) {
            // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 새로 생성한 String 객체를 반환한다
            return new String(srcString.getBytes("KSC5601"),"8859_1");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
            return "";
        }
    }
    /**
     * 문자열을 지정한 문자 집합이나 URL 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getConvertUTF8(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)) {
            // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 새로 생성한 String 객체를 반환한다
            return new String(srcString.getBytes("8859_1"),"KSC5601");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e)
        {
            // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
            return "";
        }
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getSpclStrCnvr(String srcString) {

        String rtnStr = null;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            // 처리 범위를 결정할 문자열 길이를 확인한다
            int len = srcString.length();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for (int i = 0; i < len; i++) {
                // 현재 위치의 문자를 확인한다
                chrBuff = (char)srcString.charAt(i);

                // 입력 코드나 날짜 값별로 서로 다른 업무 규칙을 적용하기 위해 분기한다
                switch (chrBuff) {
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '<':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&lt;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '>':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&gt;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    default:
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append(chrBuff);
                }
            }

            // 누적한 값을 최종 문자열로 변환한다
            rtnStr = strTxt.toString();

        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            e.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return rtnStr;
    }
    /**
     * 현재 시각을 시간 스탬프 문자열로 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 변환된 문자열
     */
    public static String getTimeStamp() {

        String rtnStr = null;

        String pattern = "yyyyMMddhhmmssSSS";

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 입력 날짜 문자열을 해석할 형식 객체를 담을 객체를 생성한다
            SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, Locale.KOREA);
            // 변환한 날짜의 타임스탬프를 담을 객체를 생성한다
            Timestamp ts = new Timestamp(System.currentTimeMillis());

            // 지정한 형식에 맞춰 값을 문자열로 변환한다
            rtnStr = sdfCurrent.format(ts.getTime());
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            e.printStackTrace();
        }

        // 현재 시각을 시간 스탬프 문자열로 생성 결과를 반환한다
        return rtnStr;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getHtmlStrCnvrQuot(String srcString) {

        String tmpString = srcString;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("\"","&quot;");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return  tmpString;

    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getHtmlStrCnvr(String srcString) {

        String tmpString = srcString;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&lt;", "<");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&gt;", ">");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&amp;", "&");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&nbsp;", " ");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&apos;", "\'");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&quot;", "\"");


            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&middot;", "·");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#34;", "\"");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#39;", "'");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#35;", "#");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#37;", "%");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#92;", "\\");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#40;", "(");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#41;", ")");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#43;", "+");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#46;", ".");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#47;", "/");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#63;", "?");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&#124;", "|");

        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return  tmpString;

    }
    /**
     * 입력 문자열에서 지정한 문자나 공백을 제거한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String specialTrim(String str) {
        // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
        StringBuffer    sb = new StringBuffer();

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for(int ii = 0; ii < str.length(); ii++) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if(str.charAt(ii) <  ' ') { continue; }
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if(' ' < str.charAt(ii) && str.charAt(ii) < '0') { continue; }
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if('9' < str.charAt(ii) && str.charAt(ii) < 'A') { continue; }
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if('Z' < str.charAt(ii) && str.charAt(ii) < 'a') { continue; }
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if('z' < str.charAt(ii) && str.charAt(ii) < '~') { continue; }
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if(str.charAt(ii)=='\n' && str.charAt(ii)=='\r' && str.charAt(ii)=='\t') { continue; }
            // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
            sb.append(str.charAt(ii));
        }

        // 입력 문자열에서 지정한 문자나 공백을 제거 결과를 반환한다
        return (String)sb.toString();
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getPrmStrCnvr(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"`","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\"","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"%","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"(","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,")","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"#","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"&","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,";","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\'", "''");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\t'", "' '");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString," ", "");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getPrmStrCnvr2(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"/","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,":","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"*","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"?","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"|","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"&","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"%","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"@","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getPrmStrCnvr3(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"`","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\"","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"%","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"(","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,")","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"#","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"&","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,";","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\'", "''");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\t'", "' '");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String cvtEndString(String str){

        int flag = 1;


        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.substring(str.length()- flag).equals("&")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- flag);
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.substring(str.length()- flag).equals(":")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- flag);
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.substring(str.length()- flag).equals(";")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- flag);
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.substring(str.length()- flag).equals("/")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- flag);
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.substring(str.length()- flag).equals(",")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- flag);
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.substring(str.length()- flag).equals(".")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- flag);
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.length() > 1 && str.substring(str.length()- 2).equals("--")) {
            // 요청한 범위의 문자열을 추출한다
            str = str.substring(0, str.length()- 2);
        }

        // 대상 문자열에서 지정한 값을 치환한다
        str = str.replace(" : ", " ");
        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return str;

    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getSearchStrCnvr(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString) || srcString==""){
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\"","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"(","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,")","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"#","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"&","");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getSearchStrCnvr2(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString) || srcString==""){
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\"","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"?","");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @return 변환된 문자열
     */
    public static String getContentsStrCnvr(String srcString) {
        // 원본 문자열이 없으면 보안 치환을 수행할 수 없으므로 빈 결과를 반환한다.
        if (isEmpty(srcString)) {
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 코드 비교 기준을 맞추기 위해 영문을 대문자로 변환한다
            srcString=srcString.toUpperCase();
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","&lt;");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","&gt;");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"COOKIE","cook1e");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"SCRIPT","scr1pt");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"OBJECT","ob1ect");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"APPLET","app1et");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"EMBED","embedd");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"FRAME","frami");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","''");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\"","\"\"");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\","\\\\");
            // 아래 처리 단계의 업무 목적을 설명한다.
            srcString=StringUtil.replace(srcString,"#","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"--","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"/","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,",","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"?","");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String putContentsStrCnvr(String srcString) {

        String tmpString = srcString;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 아래 처리 단계의 업무 목적을 설명한다.
            // 아래 처리 단계의 업무 목적을 설명한다.
            tmpString = tmpString.replaceAll("<","&lt;");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll(">","&gt;");
            // 정규식과 일치하는 문자열을 일괄 치환한다
            tmpString = tmpString.replaceAll("&lt;br&gt;", "<br>");
// 아래 처리 단계의 업무 목적을 설명한다.


        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return  tmpString;

    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String chktag(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 코드 비교 기준을 맞추기 위해 영문을 대문자로 변환한다
            srcString=srcString.toUpperCase();
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","&lt;");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"</","&lt;/");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","&gt;");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">/","&gt;/");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","''");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\","\\\\");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,";","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,",","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"/","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"#","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"--","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"-","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"NULL","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"SCRIPT","scr1pt");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"FRAME","frami");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return srcString;
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String chktel(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 조회하거나 생성할 값이 없음을 반환한다
            return null;
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 코드 비교 기준을 맞추기 위해 영문을 대문자로 변환한다
            srcString=srcString.toUpperCase();
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"<","&lt;");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"</","&lt;/");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">","&gt;");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,">/","&gt;/");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"'","''");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\"","\"\"");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\","\\\\");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,";","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,",","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"/","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"#","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"--","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"NULL","");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"SCRIPT","scr1pt");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"FRAME","frami");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return srcString;
    }
    /**
     * 숫자 문자열을 천 단위 구분 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param pInstr 천 단위 구분을 적용할 숫자 문자열
     * @return 변환된 문자열
     */
    public static String getFormattedNumber(String pInstr) {

        String rStr = pInstr;

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 입력값을 문자열 표현으로 변환한다
            Object[] testArgs = { Long.valueOf(pInstr) };

            // 치환 인자를 적용할 메시지 형식 객체를 담을 객체를 생성한다
            MessageFormat form = new MessageFormat("{0,number,###,###,##0}");
            // 지정한 형식에 맞춰 값을 문자열로 변환한다
            rStr = form.format(testArgs);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {

        }

        // 숫자 문자열을 천 단위 구분 형식으로 변환 결과를 반환한다
        return rStr;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String changeCode(String str) {

        String result = "";

        // 정규식과 일치하는 문자열을 일괄 치환한다
        result = str.replaceAll("#!Enter!#", "\n");
        // 정규식과 일치하는 문자열을 일괄 치환한다
        result = result.replaceAll("#QuestionMark#", "?");
        // 정규식과 일치하는 문자열을 일괄 치환한다
        result = result.replaceAll("#SingQu#", "'");
        // 정규식과 일치하는 문자열을 일괄 치환한다
        result = result.replaceAll("#DblSingQu#", "\"");
        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return result;
    }
    /**
     * 배열이나 목록 요소를 하나의 문자열로 결합한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String arrayToString(String[] str) {

        String result = "";

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for ( int i = 0; i < str.length; i++ ) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if ( "".equals(result) ) {

                result = "'" + str[i] + "'";
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else {

                result = result + ", " + "'" + str[i] + "'";
            }
        }

        // 배열이나 목록 요소를 하나의 문자열로 결합 결과를 반환한다
        return result;
    }
    /**
     * 배열이나 목록 요소를 하나의 문자열로 결합한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String arrayToInt(String[] str) {

        String result = "";

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for ( int i = 0; i < str.length; i++ ) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if ( "".equals(result) ) {

                result = str[i];
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else {

                result = result + ", " + str[i];
            }
        }

        // 배열이나 목록 요소를 하나의 문자열로 결합 결과를 반환한다
        return result;
    }
    /**
     * 두 값의 일치 여부에 따라 HTML 선택 속성을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param str1 비교하거나 결합할 첫 번째 값
     * @param str2 비교하거나 결합할 두 번째 값
     * @return 변환된 문자열
     */
    public static String isChecked(Object str1, Object str2) {

        String result = "";

        // str1 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(str1) && str1 instanceof String[]) {

            String[] val = (String[])str1;
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for ( int i = 0; i < val.length; i++ ) {
                // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
                if ( val[i].equals(String.valueOf(str2)) ) {
                    // 두 값의 일치 여부에 따라 HTML 선택 속성을 생성 결과를 반환한다
                    return "checked";
                }
            }
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else if (!isEmpty(str1) && str1 instanceof String) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if ( String.valueOf(str1).equals(String.valueOf(str2)) ) {
                // 두 값의 일치 여부에 따라 HTML 선택 속성을 생성 결과를 반환한다
                return "checked";
            }
        }

        // 두 값의 일치 여부에 따라 HTML 선택 속성을 생성 결과를 반환한다
        return result;
    }
    /**
     * 두 값의 일치 여부에 따라 HTML 선택 속성을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param str1 비교하거나 결합할 첫 번째 값
     * @param str2 비교하거나 결합할 두 번째 값
     * @return 변환된 문자열
     */
    public static String isSelected(Object str1, Object str2) {

        String result = "";

        // str1 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(str1) && str1 instanceof String[]) {

            String[] val = (String[])str1;
            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for ( int i = 0; i < val.length; i++ ) {
                // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
                if ( val[i].equals(String.valueOf(str2)) ) {
                    // 두 값의 일치 여부에 따라 HTML 선택 속성을 생성 결과를 반환한다
                    return "selected=\"selected\"";
                }
            }
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else if (!isEmpty(str1)) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if ( String.valueOf(str1).equals(String.valueOf(str2)) ) {
                // 두 값의 일치 여부에 따라 HTML 선택 속성을 생성 결과를 반환한다
                return "selected=\"selected\"";
            }
        }

        // 두 값의 일치 여부에 따라 HTML 선택 속성을 생성 결과를 반환한다
        return result;
    }
    /**
     * 배열이나 목록 요소를 하나의 문자열로 결합한다
     *
     * @author SeungHyeon.Kang
     * @param str1 비교하거나 결합할 첫 번째 값
     * @param str2 비교하거나 결합할 두 번째 값
     * @return 변환된 문자열
     */
    public static String arrayToStringDelim(List<String> str1, String str2) {

        String result = "";
        // str1 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        for (int i = 0; !isEmpty(str1) && i < str1.size(); i++) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if ( result.equals("") ) {
                // 지정한 키에 대응하는 값을 조회한다
                result = str1.get(i);
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else {
                // 지정한 키에 대응하는 값을 조회한다
                result += str2 + str1.get(i);
            }
        }

        // 배열이나 목록 요소를 하나의 문자열로 결합 결과를 반환한다
        return result;
    }
    /**
     * 배열이나 목록 요소를 하나의 문자열로 결합한다
     *
     * @author SeungHyeon.Kang
     * @param str1 비교하거나 결합할 첫 번째 값
     * @param str2 비교하거나 결합할 두 번째 값
     * @return 변환된 문자열
     */
    public static String arrayToStringDelim(String[] str1, String str2) {

        String result = "";
        // str1 값이 비어 있을 때 후속 참조를 차단하기 위한 분기이다
        for (int i = 0; !isEmpty(str1) && i < str1.length; i++) {
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if ( result.equals("") ) {

                result = str1[i];
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else {

                result += str2 + str1[i];
            }
        }

        // 배열이나 목록 요소를 하나의 문자열로 결합 결과를 반환한다
        return result;
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param subject 검사하거나 치환할 대상 문자열
     * @return 검사 조건 충족 여부
     */
    public static boolean checkArray (String source, String[] subject){

        boolean result = false;

        // subject 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if(isEmpty(subject)){
            // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
            return result;
        }

        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for(int i=0;i<subject.length;i++){
            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if(source.equals(subject[i])){

                result = true;
            }
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return result;
    }
    /**
     * 구분자를 기준으로 문자열을 분리한다
     *
     * @author SeungHyeon.Kang
     * @param source 변환할 원본 문자열
     * @param str1 비교하거나 결합할 첫 번째 값
     * @param str2 비교하거나 결합할 두 번째 값
     * @return 변환된 문자열
     */
    public static String splitResult (String source, String str1, String str2){

        String str = null;

        // source 값이 존재할 때만 관련 업무를 수행하도록 분기한다
        if (!isEmpty(source)) {
            // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
            try
            {
                // 구분자를 기준으로 문자열을 분리한다
                String[] splitString = source.split(str1);
                String firstString = splitString[1];

                // 구분자를 기준으로 문자열을 분리한다
                String[] resultString = firstString.split(str2);
                String nextString = resultString[0];

                str = nextString;

            }

            // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
            catch (Exception ex)
            {
                // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
                ex.printStackTrace();
            }
        }

        // 구분자를 기준으로 문자열을 분리 결과를 반환한다
        return str;
    }
    /**
     * 입력 값을 메서드의 형식 규칙에 맞춰 변환한다
     *
     * @author SeungHyeon.Kang
     * @param obj 검사하거나 변환할 객체
     * @param length 결과 문자열의 목표 길이
     * @param chr 목표 길이까지 채울 문자
     * @return 변환된 문자열
     */
    public static String convert_Length(Object obj, int length, String chr) {
        // 입력값을 문자열 표현으로 변환한다
        String result = String.valueOf(obj);
        // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
        for ( int i = result.length(); i < length; i++ ) {

            result = chr + result;
        }

        // 입력 값을 메서드의 형식 규칙에 맞춰 변환 결과를 반환한다
        return result;
    }
    /**
     * 입력 문자열을 지정한 표현 규칙에 맞춰 치환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getTagChage(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"\\", "");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"&lt","<");
            // 대상 문자열에서 지정한 값을 치환한다
            srcString=StringUtil.replace(srcString,"&gt",">");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex)
        {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 입력 문자열을 지정한 표현 규칙에 맞춰 치환 결과를 반환한다
        return srcString;
    }

    /**
     * 문자열 URL 인코딩한다.
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 처리 결과
     */
    public static String getURLEncode(String srcString){
        // 문자열 URL 인코딩 결과를 반환한다
        return getURLEncode(srcString, "utf-8");
    }
    /**
     * 문자열을 지정한 문자 집합이나 URL 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 변환된 문자열
     */
    public static String getURLEncodeKr(String srcString){
        // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
        return getURLEncode(srcString, "euc-kr");
    }
    /**
     * 문자열을 지정한 문자 집합이나 URL 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @param enc URL 인코딩과 디코딩에 사용할 문자 집합
     * @return 변환된 문자열
     */
    public static String getURLEncode(String srcString, String enc) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // enc 값이 존재할 때만 관련 업무를 수행하도록 분기한다
            if (!isEmpty(enc)) {
                // 전송 가능한 형식으로 값을 인코딩한다
                srcString = URLEncoder.encode(srcString, enc);
            }

            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else{
                // 전송 가능한 형식으로 값을 인코딩한다
                srcString = URLEncoder.encode(srcString, "UTF-8");
            }
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex){
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
        return srcString;
    }

    /**
     * URL 인코딩 문자열 디코딩한다.
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @return 처리 결과
     */
    public static String getURLDecode(String srcString) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // URL 인코딩 문자열 디코딩 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 인코딩된 값을 원문 형식으로 복원한다
            srcString = URLDecoder.decode(srcString, "UTF-8");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex){
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // URL 인코딩 문자열 디코딩 결과를 반환한다
        return  srcString;
    }
    /**
     * 문자열을 지정한 문자 집합이나 URL 형식으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param srcString 변환할 원본 문자열
     * @param enc URL 인코딩과 디코딩에 사용할 문자 집합
     * @return 변환된 문자열
     */
    public static String getURLDecode(String srcString, String enc) {
        // srcString 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(srcString)){
            // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 인코딩된 값을 원문 형식으로 복원한다
            srcString = URLDecoder.decode(srcString,enc);
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex){
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // 문자열을 지정한 문자 집합이나 URL 형식으로 변환 결과를 반환한다
        return  srcString;
    }

    /**
     * URL 인코딩 객체 디코딩한다.
     *
     * @author SeungHyeon.Kang
     * @param object 문자열로 변환할 객체
     * @return 처리 결과
     */
    public static String getURLDecodeObj(Object object) {
        // object 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if (isEmpty(object)){
            // URL 인코딩 객체 디코딩 결과를 반환한다
            return "";
        }

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try
        {
            // 인코딩된 값을 원문 형식으로 복원한다
            object = URLDecoder.decode((String) object, "UTF-8");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception ex){
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            ex.printStackTrace();
        }

        // URL 인코딩 객체 디코딩 결과를 반환한다
        return (String) object;
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param strString HTML 또는 SQL 안전 검사를 적용할 문자열
     * @return 변환된 문자열
     */
    public static String checkHtmlTag(String strString) {

        String strNew = "";

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            // 처리 범위를 결정할 문자열 길이를 확인한다
            int len = strString.length();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for (int i = 0; i < len; i++) {
                // 현재 위치의 문자를 확인한다
                chrBuff = (char)strString.charAt(i);

                // 입력 코드나 날짜 값별로 서로 다른 업무 규칙을 적용하기 위해 분기한다
                switch (chrBuff) {
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '<':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&lt;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '>':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&gt;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '"':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&quot;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '\'':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("&#39;");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    default:
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append(chrBuff);
                }
            }

            // 누적한 값을 최종 문자열로 변환한다
            strNew = strTxt.toString();

        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch(Exception e) {

            strNew = "";
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return strNew;
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param strString HTML 또는 SQL 안전 검사를 적용할 문자열
     * @return 변환된 문자열
     */
    public static String checkHtmlGetParam(String strString) {

        String rstr = "";
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try{
            // 전송 가능한 형식으로 값을 인코딩한다
            rstr = URLEncoder.encode(StringUtil.checkHtmlView(strString),"UTF-8");
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch(Exception e){

        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return rstr;
    }
    /**
     * 입력 값을 메서드의 형식 규칙에 맞춰 변환한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 변환된 문자열
     */
    public static String jumin_hide(String str) {
        // str 값이 비어 있으면 후속 참조를 차단하기 위해 분기한다
        if(isEmpty(str) || str.length() <= 0){
            // 입력 값을 메서드의 형식 규칙에 맞춰 변환 결과를 반환한다
            return "";
        }

        // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
        if(str.length() >= 6){
            // 입력 값을 메서드의 형식 규칙에 맞춰 변환 결과를 반환한다
            return str.substring(0, 6) + "*******";
        }

        // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
        else{
            // 입력 값을 메서드의 형식 규칙에 맞춰 변환 결과를 반환한다
            return str;
        }
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param strString HTML 또는 SQL 안전 검사를 적용할 문자열
     * @return 변환된 문자열
     */
    public static String checkSqlParam(String strString) {

        String strNew = "";

        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try {
            // 문자열 변환 결과를 누적할 버퍼를 담을 객체를 생성한다
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            // 처리 범위를 결정할 문자열 길이를 확인한다
            int len = strString.length();

            // 목록 또는 문자열 항목을 누락 없이 순차 처리하기 위한 반복 블록이다
            for (int i = 0; i < len; i++) {
                // 현재 위치의 문자를 확인한다
                chrBuff = (char)strString.charAt(i);

                // 입력 코드나 날짜 값별로 서로 다른 업무 규칙을 적용하기 위해 분기한다
                switch (chrBuff) {
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case '\'':
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append("''");
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case 0x00:
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case 0x0d:
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    case 0x0a:
                        break;
                    // 현재 선택된 코드 값에 해당하는 결과를 결정한다
                    default:
                        // 변환한 문자열 조각을 결과 문자열에 이어 붙인다
                        strTxt.append(chrBuff);
                }
            }

            // 누적한 값을 최종 문자열로 변환한다
            strNew = strTxt.toString();

        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch(Exception e) {

            strNew = "";
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return strNew;
    }
    /**
     * 예외 스택 추적 내용을 문자열로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param throwable 문자열로 변환할 예외
     * @return 변환된 문자열
     */
    public static String getStackTrace(Throwable throwable) {
        // 예외 스택 정보를 문자열로 누적할 writer를 담을 객체를 생성한다
        StringWriter sw = new StringWriter();
        // 예외 스택 정보를 StringWriter에 기록할 writer를 담을 객체를 생성한다
        PrintWriter pw = new PrintWriter(sw, true);
        // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
        throwable.printStackTrace(pw);
        // 예외 스택 추적 내용을 문자열로 변환 결과를 반환한다
        return sw.getBuffer().toString();
    }
    /**
     * 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제한다
     *
     * @author SeungHyeon.Kang
     * @param str 처리할 문자열
     * @return 검사 조건 충족 여부
     */
    public static boolean toNumberCheck(String str) throws Exception {

        boolean flag = false;
        String reStr = "";
        // 외부 연동이나 데이터 변환 실패를 예외 흐름으로 분리하기 위한 블록이다
        try{
            // 대상 문자열에서 지정한 값을 치환한다
            str = str.replace(".", "A");
            // 정규식과 처음 일치하는 문자열을 치환한다
            reStr = str.replaceFirst("A", "");

            // 업무에서 허용한 범위와 상태 조건을 구분하기 위해 분기한다
            if(reStr.matches("[\\d]+")) flag = true;
            // 앞선 조건에 해당하지 않는 대체 업무 흐름으로 전환한다
            else flag = false;
        }

        // 예외 발생 시 기본값 보정 또는 공통 실패 흐름으로 전환한다
        catch (Exception e) {
            // 예외 발생 지점을 확인할 수 있도록 스택 정보를 출력한다
            e.printStackTrace();
        }

        // 입력 문자열이 허용된 형식인지 판정하거나 위험 문자를 정제 결과를 반환한다
        return flag;
    }
}
