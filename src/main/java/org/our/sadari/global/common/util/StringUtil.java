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

public class StringUtil {

    public static final String EMPTY = "";

    // 아래 처리 단계의 업무 목적을 설명한다.

    /**
     * 아래 코드의 처리 목적을 설명한다.
     */
    /**
     * byteString 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param source 처리에 필요한 입력값
     * @param output 처리에 필요한 입력값
     * @param slength 처리에 필요한 입력값
     * @return 처리 결과
     */



    public static String byteString(String source, String output, int slength) {

        String returnVal;
        try{
            returnVal= new String(source.getBytes(),0, slength);
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(returnVal.length()==0 ){
                returnVal= new String(source.getBytes(),0, slength+1);
            }
            returnVal += output;
        }catch(IndexOutOfBoundsException e){
            returnVal = source;
        }
        return returnVal;
    }



    public static String cutString(String source, String output, int slength) {
        String returnVal = null;
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (source != null) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (source.length() > slength) {
                returnVal = source.substring(0, slength) + output;
            } else
                returnVal = source;
        }
        return returnVal;
    }

    public static String cutString(String source, int slength) {
        String result = null;
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (source != null) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (source.length() > slength) {
                result = source.substring(0, slength);
            } else
                result = source;
        }
        return result;
    }

    public static String normalizePlainText(String value) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (isEmpty(value)) {
            return null;
        }

        return value.trim();
    }

    public static String normalizePlainText(String value, int maxLength) {
        String normalizedValue = normalizePlainText(value);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (isEmpty(normalizedValue)) {
            return null;
        }

        return cutString(normalizedValue, maxLength);
    }

    public static boolean isEmpty(Object obj) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (obj == null) {
            return true;
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (obj instanceof List) {
            return ((List<?>) obj).isEmpty();
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }

        return false;
    }

    public static boolean hasEmpty(Object... values) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (values == null) {
            return true;
        }

        for (Object value : values) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (isEmpty(value)) {
                return true;
            }
        }

        return false;
    }

    public static String remove(String str, char remove) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (isEmpty(str) || str.indexOf(remove) == -1) {
            return str;
        }
        char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    public static String removeCommaChar(String str) {
        return remove(str, ',');
    }

    public static String removeMinusChar(String str) {
        return remove(str, '-');
    }


    public static String replace(String source, String subject, String object) {
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        String srcStr  = source;

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(srcStr!=null){
            while (srcStr.indexOf(subject) >= 0) {
                preStr = srcStr.substring(0, srcStr.indexOf(subject));
                nextStr = srcStr.substring(srcStr.indexOf(subject) + subject.length(), srcStr.length());
                srcStr = nextStr;
                rtnStr.append(preStr).append(object);
            }
        }
        rtnStr.append(nextStr);
        return rtnStr.toString();
    }

    public static String replaceArray (String source, String[] subject, String[] object){
        String str = source;
        for(int i=0;i<subject.length;i++){
            str=replace(str, subject[i], object[i]);
        }
        return str;
    }

    public static String replaceOnce(String source, String subject, String object) {
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (source.indexOf(subject) >= 0) {
            preStr = source.substring(0, source.indexOf(subject));
            nextStr = source.substring(source.indexOf(subject) + subject.length(), source.length());
            rtnStr.append(preStr).append(object).append(nextStr);
            return rtnStr.toString();
        } else {
            return source;
        }
    }

    public static String replaceChar(String source, String subject, String object) {
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        String srcStr  = source;

        char chA;

        for (int i = 0; i < subject.length(); i++) {
            chA = subject.charAt(i);

            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (srcStr.indexOf(chA) >= 0) {
                preStr = srcStr.substring(0, srcStr.indexOf(chA));
                nextStr = srcStr.substring(srcStr.indexOf(chA) + 1, srcStr.length());
                srcStr = rtnStr.append(preStr).append(object).append(nextStr).toString();
            }
        }

        return srcStr;
    }

    public static int indexOf(String str, String searchStr) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.indexOf(searchStr);
    }


    public static String decode(String sourceStr, String compareStr, String returnStr, String defaultStr) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (sourceStr == null && compareStr == null) {
            return returnStr;
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (sourceStr == null && compareStr != null) {
            return defaultStr;
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (sourceStr.trim().equals(compareStr)) {
            return returnStr;
        }

        return defaultStr;
    }

    public static String decode(String sourceStr, String compareStr, String returnStr) {
        return decode(sourceStr, compareStr, returnStr, sourceStr);
    }

    public static String isNullToString(Object object) {
        String string = "";

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (object != null) {
            string = object.toString().trim();
        }

        return string;
    }

    public static String nullConvert(Object src) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (src != null && src instanceof java.math.BigDecimal) {
            return ((BigDecimal)src).toString();
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (src == null || src.equals("null")) {
            return "";
        } else {
            return ((String)src).trim();
        }
    }

    public static String nullConvert(String src) {

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (src == null || src.equals("null") || "".equals(src) || " ".equals(src)) {
            return "";
        } else {
            return src.trim();
        }
    }

    public static int zeroConvert(Object src) {

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (src == null || src.equals("null")) {
            return 0;
        } else {
            return Integer.parseInt(((String)src).trim());
        }
    }

    public static int zeroConvert(String src) {

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (src == null || src.equals("null") || "".equals(src) || " ".equals(src)) {
            return 0;
        } else {
            return Integer.parseInt(src.trim());
        }
    }


    public static int zeroConvertHashMap(Object src) {

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (src == null || src.equals("null")) {
            return 0;
        } else {
            return Integer.parseInt(src.toString());
        }
    }

    public static String removeWhitespace(String str) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (count == sz) {
            return str;
        }

        return new String(chs, 0, count);
    }

    public static String checkHtmlView(String strString) {
        String strNew = "";

        try {
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            int len = strString.length();

            for (int i = 0; i < len; i++) {
                chrBuff = (char)strString.charAt(i);

                switch (chrBuff) {
                    case '<':
                        strTxt.append("&lt;");
                        break;
                    case '>':
                        strTxt.append("&gt;");
                        break;
                    case '"':
                        strTxt.append("&quot;");
                        break;
                    case 10:
                        strTxt.append("<br>");
                        break;
                    case ' ':
                        strTxt.append("&nbsp;");
                        break;
                    // 아래 처리 단계의 업무 목적을 설명한다.
                    // 아래 처리 단계의 업무 목적을 설명한다.
                    // 아래 처리 단계의 업무 목적을 설명한다.
                    default:
                        strTxt.append(chrBuff);
                }
            }

            strNew = strTxt.toString();

        } catch (Exception ex) {
            return null;
        }

        return strNew;
    }

    public static String[] split(String source, String separator) throws NullPointerException {
        String[] returnVal = null;
        int cnt = 1;

        int index = source.indexOf(separator);
        int index0 = 0;
        while (index >= 0) {
            cnt++;
            index = source.indexOf(separator, index + 1);
        }
        returnVal = new String[cnt];
        cnt = 0;
        index = source.indexOf(separator);
        while (index >= 0) {
            returnVal[cnt] = source.substring(index0, index);
            index0 = index + 1;
            index = source.indexOf(separator, index + 1);
            cnt++;
        }
        returnVal[cnt] = source.substring(index0);

        return returnVal;
    }

    public static String lowerCase(String str) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (str == null) {
            return null;
        }

        return str.toLowerCase();
    }

    public static String upperCase(String str) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (str == null) {
            return null;
        }

        return str.toUpperCase();
    }


    public static boolean lowerNumCheck(String str) {

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (str == null) {
            return false;
        }

        String regex1 = "[a-z0-9]*";

        return str.matches(regex1);
    }

    public static String stripStart(String str, String stripChars) {
        int strLen;
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (stripChars == null) {
            while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }

        return str.substring(start);
    }


    public static String stripEnd(String str, String stripChars) {
        int end;
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (stripChars == null) {
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }

        return str.substring(0, end);
    }

    public static String strip(String str, String stripChars) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (isEmpty(str)) {
            return str;
        }

        String srcStr = str;
        srcStr = stripStart(srcStr, stripChars);

        return stripEnd(srcStr, stripChars);
    }

    public static String[] split(String source, String separator, int arraylength) throws NullPointerException {
        String[] returnVal = new String[arraylength];
        int cnt = 0;
        int index0 = 0;
        int index = source.indexOf(separator);
        while (index >= 0 && cnt < (arraylength - 1)) {
            returnVal[cnt] = source.substring(index0, index);
            index0 = index + 1;
            index = source.indexOf(separator, index + 1);
            cnt++;
        }
        returnVal[cnt] = source.substring(index0);
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (cnt < (arraylength - 1)) {
            for (int i = cnt + 1; i < arraylength; i++) {
                returnVal[i] = "";
            }
        }

        return returnVal;
    }

    public static String getRandomStr(char startChr, char endChr) {

        int randomInt;
        String randomStr = null;

        int startInt = Integer.valueOf(startChr);
        int endInt = Integer.valueOf(endChr);

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (startInt > endInt) {
            throw new IllegalArgumentException("Start String: " + startChr + " End String: " + endChr);
        }

        try {
            SecureRandom rnd = new SecureRandom();

            do {
                randomInt = rnd.nextInt(endInt + 1);
            } while (randomInt < startInt);

            randomStr = (char)randomInt + "";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return randomStr;
    }

    public static String getEncdDcd(String srcString, String srcCharsetNm, String cnvrCharsetNm) {

        String rtnStr = null;

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null)
            return null;

        try {
            rtnStr = new String(srcString.getBytes(srcCharsetNm), cnvrCharsetNm);
        } catch (UnsupportedEncodingException e) {
            rtnStr = null;
        }

        return rtnStr;
    }


    public static String getConvert8859(String srcString)
    {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null) {
            return "";
        }

        try {
            return new String(srcString.getBytes("KSC5601"),"8859_1");
        }
        catch (Exception e) {
            return "";
        }
    }

    public static String getConvertUTF8(String srcString)
    {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null) {
            return "";
        }

        try {
            return new String(srcString.getBytes("8859_1"),"KSC5601");
        }
        catch (Exception e)
        {
            return "";
        }
    }


    public static String getSpclStrCnvr(String srcString) {

        String rtnStr = null;

        try {
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            int len = srcString.length();

            for (int i = 0; i < len; i++) {
                chrBuff = (char)srcString.charAt(i);

                switch (chrBuff) {
                    case '<':
                        strTxt.append("&lt;");
                        break;
                    case '>':
                        strTxt.append("&gt;");
                        break;
                    default:
                        strTxt.append(chrBuff);
                }
            }

            rtnStr = strTxt.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rtnStr;
    }

    public static String getTimeStamp() {

        String rtnStr = null;

        String pattern = "yyyyMMddhhmmssSSS";

        try {
            SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, Locale.KOREA);
            Timestamp ts = new Timestamp(System.currentTimeMillis());

            rtnStr = sdfCurrent.format(ts.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rtnStr;
    }

    public static String getHtmlStrCnvrQuot(String srcString) {

        String tmpString = srcString;

        try
        {
            tmpString = tmpString.replaceAll("\"","&quot;");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return  tmpString;

    }

    public static String getHtmlStrCnvr(String srcString) {

        String tmpString = srcString;

        try
        {
            tmpString = tmpString.replaceAll("&lt;", "<");
            tmpString = tmpString.replaceAll("&gt;", ">");
            tmpString = tmpString.replaceAll("&amp;", "&");
            tmpString = tmpString.replaceAll("&nbsp;", " ");
            tmpString = tmpString.replaceAll("&apos;", "\'");
            tmpString = tmpString.replaceAll("&quot;", "\"");


            tmpString = tmpString.replaceAll("&middot;", "·");
            tmpString = tmpString.replaceAll("&#34;", "\"");
            tmpString = tmpString.replaceAll("&#39;", "'");
            tmpString = tmpString.replaceAll("&#35;", "#");
            tmpString = tmpString.replaceAll("&#37;", "%");
            tmpString = tmpString.replaceAll("&#92;", "\\");
            tmpString = tmpString.replaceAll("&#40;", "(");
            tmpString = tmpString.replaceAll("&#41;", ")");
            tmpString = tmpString.replaceAll("&#43;", "+");
            tmpString = tmpString.replaceAll("&#46;", ".");
            tmpString = tmpString.replaceAll("&#47;", "/");
            tmpString = tmpString.replaceAll("&#63;", "?");
            tmpString = tmpString.replaceAll("&#124;", "|");

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return  tmpString;

    }


    public static String specialTrim(String str) {

        StringBuffer    sb = new StringBuffer();

        for(int ii = 0; ii < str.length(); ii++) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(str.charAt(ii) <  ' ') { continue; }
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(' ' < str.charAt(ii) && str.charAt(ii) < '0') { continue; }
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if('9' < str.charAt(ii) && str.charAt(ii) < 'A') { continue; }
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if('Z' < str.charAt(ii) && str.charAt(ii) < 'a') { continue; }
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if('z' < str.charAt(ii) && str.charAt(ii) < '~') { continue; }
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(str.charAt(ii)=='\n' && str.charAt(ii)=='\r' && str.charAt(ii)=='\t') { continue; }
            sb.append(str.charAt(ii));
        }
        return (String)sb.toString();
    }

    public static String getPrmStrCnvr(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            srcString=StringUtil.replace(srcString,"'","");
            srcString=StringUtil.replace(srcString,"`","");
            srcString=StringUtil.replace(srcString,"\"","");
            srcString=StringUtil.replace(srcString,"%","");
            srcString=StringUtil.replace(srcString,"<","");
            srcString=StringUtil.replace(srcString,">","");
            srcString=StringUtil.replace(srcString,"(","");
            srcString=StringUtil.replace(srcString,")","");
            srcString=StringUtil.replace(srcString,"#","");
            srcString=StringUtil.replace(srcString,"&","");
            srcString=StringUtil.replace(srcString,";","");
            srcString=StringUtil.replace(srcString,"\\'", "''");
            srcString=StringUtil.replace(srcString,"\t'", "' '");
            srcString=StringUtil.replace(srcString," ", "");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String getPrmStrCnvr2(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            srcString=StringUtil.replace(srcString,"/","");
            srcString=StringUtil.replace(srcString,"\\","");
            srcString=StringUtil.replace(srcString,":","");
            srcString=StringUtil.replace(srcString,"*","");
            srcString=StringUtil.replace(srcString,"<","");
            srcString=StringUtil.replace(srcString,">","");
            srcString=StringUtil.replace(srcString,"?","");
            srcString=StringUtil.replace(srcString,"|","");
            srcString=StringUtil.replace(srcString,"&","");
            srcString=StringUtil.replace(srcString,"%","");
            srcString=StringUtil.replace(srcString,"@","");
            srcString=StringUtil.replace(srcString,"'","");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String getPrmStrCnvr3(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            srcString=StringUtil.replace(srcString,"'","");
            srcString=StringUtil.replace(srcString,"`","");
            srcString=StringUtil.replace(srcString,"\"","");
            srcString=StringUtil.replace(srcString,"%","");
            srcString=StringUtil.replace(srcString,"<","");
            srcString=StringUtil.replace(srcString,">","");
            srcString=StringUtil.replace(srcString,"(","");
            srcString=StringUtil.replace(srcString,")","");
            srcString=StringUtil.replace(srcString,"#","");
            srcString=StringUtil.replace(srcString,"&","");
            srcString=StringUtil.replace(srcString,";","");
            srcString=StringUtil.replace(srcString,"\\'", "''");
            srcString=StringUtil.replace(srcString,"\t'", "' '");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String cvtEndString(String str){

        int flag = 1;


        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.substring(str.length()- flag).equals("&")) {
            str = str.substring(0, str.length()- flag);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.substring(str.length()- flag).equals(":")) {
            str = str.substring(0, str.length()- flag);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.substring(str.length()- flag).equals(";")) {
            str = str.substring(0, str.length()- flag);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.substring(str.length()- flag).equals("/")) {
            str = str.substring(0, str.length()- flag);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.substring(str.length()- flag).equals(",")) {
            str = str.substring(0, str.length()- flag);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.substring(str.length()- flag).equals(".")) {
            str = str.substring(0, str.length()- flag);
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.length() > 1 && str.substring(str.length()- 2).equals("--")) {
            str = str.substring(0, str.length()- 2);
        }

        str = str.replace(" : ", " ");

        return str;

    }

    public static String getSearchStrCnvr(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null || srcString==""){
            return null;
        }

        try
        {
            srcString=StringUtil.replace(srcString,"'","");
            srcString=StringUtil.replace(srcString,"\"","");
            srcString=StringUtil.replace(srcString,"<","");
            srcString=StringUtil.replace(srcString,">","");
            srcString=StringUtil.replace(srcString,"(","");
            srcString=StringUtil.replace(srcString,")","");
            srcString=StringUtil.replace(srcString,"#","");
            srcString=StringUtil.replace(srcString,"&","");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String getSearchStrCnvr2(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null || srcString==""){
            return null;
        }

        try
        {
            srcString=StringUtil.replace(srcString,"'","");
            srcString=StringUtil.replace(srcString,"\"","");
            srcString=StringUtil.replace(srcString,"?","");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }


    public static String getContentsStrCnvr(String srcString) {  // 
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return null;
        }

        try
        {
            srcString=srcString.toUpperCase();
            srcString=StringUtil.replace(srcString,"<","&lt;");
            srcString=StringUtil.replace(srcString,">","&gt;");
            srcString=StringUtil.replace(srcString,"COOKIE","cook1e");
            srcString=StringUtil.replace(srcString,"SCRIPT","scr1pt");
            srcString=StringUtil.replace(srcString,"OBJECT","ob1ect");
            srcString=StringUtil.replace(srcString,"APPLET","app1et");
            srcString=StringUtil.replace(srcString,"EMBED","embedd");
            srcString=StringUtil.replace(srcString,"FRAME","frami");
            srcString=StringUtil.replace(srcString,"'","''");
            srcString=StringUtil.replace(srcString,"\"","\"\"");
            srcString=StringUtil.replace(srcString,"\\","\\\\");
            // 아래 처리 단계의 업무 목적을 설명한다.
            srcString=StringUtil.replace(srcString,"#","");
            srcString=StringUtil.replace(srcString,"--","");
            srcString=StringUtil.replace(srcString,"/","");
            srcString=StringUtil.replace(srcString,",","");
            srcString=StringUtil.replace(srcString,"?","");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String putContentsStrCnvr(String srcString) {

        String tmpString = srcString;

        try
        {
            // 아래 처리 단계의 업무 목적을 설명한다.
            // 아래 처리 단계의 업무 목적을 설명한다.
            tmpString = tmpString.replaceAll("<","&lt;");
            tmpString = tmpString.replaceAll(">","&gt;");
            tmpString = tmpString.replaceAll("&lt;br&gt;", "<br>");
// 아래 처리 단계의 업무 목적을 설명한다.


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return  tmpString;

    }




    public static String chktag(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return null;
        }

        try
        {
            srcString=srcString.toUpperCase();
            srcString=StringUtil.replace(srcString,"<","&lt;");
            srcString=StringUtil.replace(srcString,"</","&lt;/");
            srcString=StringUtil.replace(srcString,">","&gt;");
            srcString=StringUtil.replace(srcString,">/","&gt;/");
            srcString=StringUtil.replace(srcString,"'","''");
            srcString=StringUtil.replace(srcString,"\\","\\\\");
            srcString=StringUtil.replace(srcString,";","");
            srcString=StringUtil.replace(srcString,",","");
            srcString=StringUtil.replace(srcString,"/","");
            srcString=StringUtil.replace(srcString,"#","");
            srcString=StringUtil.replace(srcString,"--","");
            srcString=StringUtil.replace(srcString,"-","");
            srcString=StringUtil.replace(srcString,"NULL","");
            srcString=StringUtil.replace(srcString,"SCRIPT","scr1pt");
            srcString=StringUtil.replace(srcString,"FRAME","frami");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String chktel(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return null;
        }

        try
        {
            srcString=srcString.toUpperCase();
            srcString=StringUtil.replace(srcString,"<","&lt;");
            srcString=StringUtil.replace(srcString,"</","&lt;/");
            srcString=StringUtil.replace(srcString,">","&gt;");
            srcString=StringUtil.replace(srcString,">/","&gt;/");
            srcString=StringUtil.replace(srcString,"'","''");
            srcString=StringUtil.replace(srcString,"\"","\"\"");
            srcString=StringUtil.replace(srcString,"\\","\\\\");
            srcString=StringUtil.replace(srcString,";","");
            srcString=StringUtil.replace(srcString,",","");
            srcString=StringUtil.replace(srcString,"/","");
            srcString=StringUtil.replace(srcString,"#","");
            srcString=StringUtil.replace(srcString,"--","");
            srcString=StringUtil.replace(srcString,"NULL","");
            srcString=StringUtil.replace(srcString,"SCRIPT","scr1pt");
            srcString=StringUtil.replace(srcString,"FRAME","frami");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    public static String getFormattedNumber(String pInstr) {
        String rStr = pInstr;

        try {
            Object[] testArgs = { Long.valueOf(pInstr) };

            MessageFormat form = new MessageFormat("{0,number,###,###,##0}");
            rStr = form.format(testArgs);
        } catch (Exception e) {
        }

        return rStr;
    }


    public static String changeCode(String str) {
        String result = "";

        result = str.replaceAll("#!Enter!#", "\n");
        result = result.replaceAll("#QuestionMark#", "?");
        result = result.replaceAll("#SingQu#", "'");
        result = result.replaceAll("#DblSingQu#", "\"");

        return result;
    }


    public static String arrayToString(String[] str) {
        String result = "";

        for ( int i = 0; i < str.length; i++ ) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if ( "".equals(result) ) {
                result = "'" + str[i] + "'";
            } else {
                result = result + ", " + "'" + str[i] + "'";
            }
        }

        return result;
    }


    public static String arrayToInt(String[] str) {
        String result = "";

        for ( int i = 0; i < str.length; i++ ) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if ( "".equals(result) ) {
                result = str[i];
            } else {
                result = result + ", " + str[i];
            }
        }

        return result;
    }

    public static String isChecked(Object str1, Object str2) {
        String result = "";

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if ( str1 != null && str1 instanceof String[] ) {
            String[] val = (String[])str1;
            for ( int i = 0; i < val.length; i++ ) {
                // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
                if ( val[i].equals(String.valueOf(str2)) ) {
                    return "checked";
                }
            }
        } else if ( str1 != null && str1 instanceof String ) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if ( String.valueOf(str1).equals(String.valueOf(str2)) ) {
                return "checked";
            }
        }
        return result;
    }

    public static String isSelected(Object str1, Object str2) {
        String result = "";

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if ( str1 != null && str1 instanceof String[] ) {
            String[] val = (String[])str1;
            for ( int i = 0; i < val.length; i++ ) {
                // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
                if ( val[i].equals(String.valueOf(str2)) ) {
                    return "selected=\"selected\"";
                }
            }
        } else if ( str1 != null ) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if ( String.valueOf(str1).equals(String.valueOf(str2)) ) {
                return "selected=\"selected\"";
            }
        }
        return result;
    }

    public static String arrayToStringDelim(List<String> str1, String str2) {
        String result = "";
        for ( int i = 0; str1 != null && i < str1.size(); i++ ) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if ( result.equals("") ) {
                result = str1.get(i);
            } else {
                result += str2 + str1.get(i);
            }
        }
        return result;
    }

    public static String arrayToStringDelim(String[] str1, String str2) {
        String result = "";
        for ( int i = 0; str1 != null && i < str1.length; i++ ) {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if ( result.equals("") ) {
                result = str1[i];
            } else {
                result += str2 + str1[i];
            }
        }
        return result;
    }

    public static boolean checkArray (String source, String[] subject){
        boolean result = false;

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(subject==null){
            return result;
        }

        for(int i=0;i<subject.length;i++){
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(source.equals(subject[i])){
                result = true;
            }
        }

        return result;
    }


    public static String splitResult (String source, String str1, String str2){
        String str = null;

        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(source!=null) {

            try
            {

                String[] splitString = source.split(str1);
                String firstString = splitString[1];

                String[] resultString = firstString.split(str2);
                String nextString = resultString[0];

                str = nextString;

            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return str;
    }

    public static String convert_Length(Object obj, int length, String chr) {
        String result = String.valueOf(obj);
        for ( int i = result.length(); i < length; i++ ) {
            result = chr + result;
        }

        return result;
    }


    public static String getTagChage(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            srcString=StringUtil.replace(srcString,"\\", "");
            srcString=StringUtil.replace(srcString,"&lt","<");
            srcString=StringUtil.replace(srcString,"&gt",">");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return srcString;
    }

    /**
     * getURLEncode 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param srcString 처리에 필요한 입력값
     * @return 처리 결과
     */

    public static String getURLEncode(String srcString){
        return getURLEncode(srcString, "utf-8");
    }
    public static String getURLEncodeKr(String srcString){
        return getURLEncode(srcString, "euc-kr");
    }
    public static String getURLEncode(String srcString, String enc) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(enc != null && enc.length() > 0){
                srcString = URLEncoder.encode(srcString, enc);
            }else{
                srcString = URLEncoder.encode(srcString, "UTF-8");
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return srcString;
    }

    /**
     * getURLDecode 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param srcString 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String getURLDecode(String srcString) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            srcString = URLDecoder.decode(srcString, "UTF-8");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return  srcString;
    }

    public static String getURLDecode(String srcString,String enc) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (srcString == null){
            return "";
        }

        try
        {
            srcString = URLDecoder.decode(srcString,enc);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return  srcString;
    }

    /**
     * getURLDecodeObj 메서드의 요청을 검증하고 업무 처리 결과를 반환한다.
     *
     * @author Seunghyeon.Kang
     * @param object 처리에 필요한 입력값
     * @return 처리 결과
     */
    public static String getURLDecodeObj(Object object) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if (object == null){
            return "";
        }

        try
        {
            object = URLDecoder.decode((String) object, "UTF-8");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return (String) object;
    }

    public static String checkHtmlTag(String strString) {
        String strNew = "";

        try {
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            int len = strString.length();

            for (int i = 0; i < len; i++) {
                chrBuff = (char)strString.charAt(i);

                switch (chrBuff) {
                    case '<':
                        strTxt.append("&lt;");
                        break;
                    case '>':
                        strTxt.append("&gt;");
                        break;
                    case '"':
                        strTxt.append("&quot;");
                        break;
                    case '\'':
                        strTxt.append("&#39;");
                        break;
                    default:
                        strTxt.append(chrBuff);
                }
            }
            strNew = strTxt.toString();

        }catch(Exception e) {
            strNew = "";
        }
        return strNew;
    }

    public static String checkHtmlGetParam(String strString) {
        String rstr = "";
        try{
            rstr = URLEncoder.encode(StringUtil.checkHtmlView(strString),"UTF-8");
        }catch(Exception e){
        }
        return rstr;
    }

    public static String jumin_hide(String str) {
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str == null || str.length() <= 0){
            return "";
        }
        // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
        if(str.length() >= 6){
            return str.substring(0, 6) + "*******";
        }else{
            return str;
        }
    }

    public static String checkSqlParam(String strString) {
        String strNew = "";

        try {
            StringBuffer strTxt = new StringBuffer("");

            char chrBuff;
            int len = strString.length();

            for (int i = 0; i < len; i++) {
                chrBuff = (char)strString.charAt(i);

                switch (chrBuff) {
                    case '\'':
                        strTxt.append("''");
                        break;
                    case 0x00:
                        break;
                    case 0x0d:
                        break;
                    case 0x0a:
                        break;
                    default:
                        strTxt.append(chrBuff);
                }
            }
            strNew = strTxt.toString();

        }catch(Exception e) {
            strNew = "";
        }
        return strNew;
    }

    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);

        return sw.getBuffer().toString();
    }

    public static boolean toNumberCheck(String str) throws Exception {
        boolean flag = false;
        String reStr = "";
        try{
            str = str.replace(".", "A");
            reStr = str.replaceFirst("A", "");

            // 조건을 먼저 검증해 이후 처리 흐름에서 잘못된 데이터가 사용되지 않도록 분기한다.
            if(reStr.matches("[\\d]+")) flag = true;
            else flag = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
