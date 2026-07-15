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

    // private static final int PAD_LIMIT = 8192;

    /**
     * <p>An array of <code>String</code>s used for padding.</p>
     * <p>Used for efficient space padding. The length of each String expands as needed.</p>
     */
    /*
	private static final String[] PADDING = new String[Character.MAX_VALUE];

	static {
		// space padding is most common, start with 64 chars
		PADDING[32] = "                                                                ";
	}	
     */



    public static String byteString(String source, String output, int slength) {

        String returnVal;
        try{
            returnVal= new String(source.getBytes(),0, slength);
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
        if (source != null) {
            if (source.length() > slength) {
                returnVal = source.substring(0, slength) + output;
            } else
                returnVal = source;
        }
        return returnVal;
    }

    public static String cutString(String source, int slength) {
        String result = null;
        if (source != null) {
            if (source.length() > slength) {
                result = source.substring(0, slength);
            } else
                result = source;
        }
        return result;
    }

    public static String normalizePlainText(String value) {
        if (isEmpty(value)) {
            return null;
        }

        return value.trim();
    }

    public static String normalizePlainText(String value, int maxLength) {
        String normalizedValue = normalizePlainText(value);

        if (isEmpty(normalizedValue)) {
            return null;
        }

        return cutString(normalizedValue, maxLength);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }

        if (obj instanceof List) {
            return ((List<?>) obj).isEmpty();
        }

        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }

        return false;
    }

    public static boolean hasEmpty(Object... values) {
        if (values == null) {
            return true;
        }

        for (Object value : values) {
            if (isEmpty(value)) {
                return true;
            }
        }

        return false;
    }

    public static String remove(String str, char remove) {
        if (isEmpty(str) || str.indexOf(remove) == -1) {
            return str;
        }
        char[] chars = str.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
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

            if (srcStr.indexOf(chA) >= 0) {
                preStr = srcStr.substring(0, srcStr.indexOf(chA));
                nextStr = srcStr.substring(srcStr.indexOf(chA) + 1, srcStr.length());
                srcStr = rtnStr.append(preStr).append(object).append(nextStr).toString();
            }
        }

        return srcStr;
    }

    public static int indexOf(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return -1;
        }
        return str.indexOf(searchStr);
    }


    public static String decode(String sourceStr, String compareStr, String returnStr, String defaultStr) {
        if (sourceStr == null && compareStr == null) {
            return returnStr;
        }

        if (sourceStr == null && compareStr != null) {
            return defaultStr;
        }

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

        if (object != null) {
            string = object.toString().trim();
        }

        return string;
    }

    public static String nullConvert(Object src) {
        //if (src != null && src.getClass().getName().equals("java.math.BigDecimal")) {
        if (src != null && src instanceof java.math.BigDecimal) {
            return ((BigDecimal)src).toString();
        }

        if (src == null || src.equals("null")) {
            return "";
        } else {
            return ((String)src).trim();
        }
    }

    public static String nullConvert(String src) {

        if (src == null || src.equals("null") || "".equals(src) || " ".equals(src)) {
            return "";
        } else {
            return src.trim();
        }
    }

    public static int zeroConvert(Object src) {

        if (src == null || src.equals("null")) {
            return 0;
        } else {
            return Integer.parseInt(((String)src).trim());
        }
    }

    public static int zeroConvert(String src) {

        if (src == null || src.equals("null") || "".equals(src) || " ".equals(src)) {
            return 0;
        } else {
            return Integer.parseInt(src.trim());
        }
    }


    public static int zeroConvertHashMap(Object src) {

        if (src == null || src.equals("null")) {
            return 0;
        } else {
            return Integer.parseInt(src.toString());
        }
    }

    public static String removeWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
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
                    //case '&' :
                    //strTxt.append("&amp;");
                    //break;
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
        if (str == null) {
            return null;
        }

        return str.toLowerCase();
    }

    public static String upperCase(String str) {
        if (str == null) {
            return null;
        }

        return str.toUpperCase();
    }


    public static boolean lowerNumCheck(String str) {

        if (str == null) {
            return false;
        }

        String regex1 = "[a-z0-9]*";

        return str.matches(regex1);
    }

    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
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
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

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


            tmpString = tmpString.replaceAll("&middot;", "쨌");
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
            if(str.charAt(ii) <  ' ') { continue; }
            if(' ' < str.charAt(ii) && str.charAt(ii) < '0') { continue; }
            if('9' < str.charAt(ii) && str.charAt(ii) < 'A') { continue; }
            if('Z' < str.charAt(ii) && str.charAt(ii) < 'a') { continue; }
            if('z' < str.charAt(ii) && str.charAt(ii) < '~') { continue; }
            if(str.charAt(ii)=='\n' && str.charAt(ii)=='\r' && str.charAt(ii)=='\t') { continue; }
            sb.append(str.charAt(ii));
        }
        return (String)sb.toString();
    }

    public static String getPrmStrCnvr(String srcString) {
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


        if(str.substring(str.length()- flag).equals("&")) {
            str = str.substring(0, str.length()- flag);
        }
        if(str.substring(str.length()- flag).equals(":")) {
            str = str.substring(0, str.length()- flag);
        }
        if(str.substring(str.length()- flag).equals(";")) {
            str = str.substring(0, str.length()- flag);
        }
        if(str.substring(str.length()- flag).equals("/")) {
            str = str.substring(0, str.length()- flag);
        }
        if(str.substring(str.length()- flag).equals(",")) {
            str = str.substring(0, str.length()- flag);
        }
        if(str.substring(str.length()- flag).equals(".")) {
            str = str.substring(0, str.length()- flag);
        }
        if(str.length() > 1 && str.substring(str.length()- 2).equals("--")) {
            str = str.substring(0, str.length()- 2);
        }

        str = str.replace(" : ", " ");

        return str;

    }

    public static String getSearchStrCnvr(String srcString) {
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
            //srcString=StringUtil.replace(srcString,";","");
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
            //tmpString = tmpString.replaceAll("&lt", "<");
            //tmpString = tmpString.replaceAll("&gt", ">");
            tmpString = tmpString.replaceAll("<","&lt;");
            tmpString = tmpString.replaceAll(">","&gt;");
            tmpString = tmpString.replaceAll("&lt;br&gt;", "<br>");
//			Log.debug("putContentsStrCnvr");


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return  tmpString;

    }




    public static String chktag(String srcString) {
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

        if ( str1 != null && str1 instanceof String[] ) {
            String[] val = (String[])str1;
            for ( int i = 0; i < val.length; i++ ) {
                if ( val[i].equals(String.valueOf(str2)) ) {
                    return "checked";
                }
            }
        } else if ( str1 != null && str1 instanceof String ) {
            if ( String.valueOf(str1).equals(String.valueOf(str2)) ) {
                return "checked";
            }
        }
        return result;
    }

    public static String isSelected(Object str1, Object str2) {
        String result = "";

        if ( str1 != null && str1 instanceof String[] ) {
            String[] val = (String[])str1;
            for ( int i = 0; i < val.length; i++ ) {
                if ( val[i].equals(String.valueOf(str2)) ) {
                    return "selected=\"selected\"";
                }
            }
        } else if ( str1 != null ) {
            if ( String.valueOf(str1).equals(String.valueOf(str2)) ) {
                return "selected=\"selected\"";
            }
        }
        return result;
    }

    public static String arrayToStringDelim(List<String> str1, String str2) {
        String result = "";
        for ( int i = 0; str1 != null && i < str1.size(); i++ ) {
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

        if(subject==null){
            return result;
        }

        for(int i=0;i<subject.length;i++){
            if(source.equals(subject[i])){
                result = true;
            }
        }

        return result;
    }


    public static String splitResult (String source, String str1, String str2){
        String str = null;

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
     *  URL Encode
     *
     * @param srcString, enc
     * @return String
     * @exception Exception
     * @see
     */

    public static String getURLEncode(String srcString){
        return getURLEncode(srcString, "utf-8");
    }
    public static String getURLEncodeKr(String srcString){
        return getURLEncode(srcString, "euc-kr");
    }
    public static String getURLEncode(String srcString, String enc) {
        if (srcString == null){
            return "";
        }

        try
        {
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
     *  URL Decode
     *
     * @param srcString, enc
     * @return String
     * @exception Exception
     * @see
     */
    public static String getURLDecode(String srcString) {
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
     *  URL Decode
     *
     * @param object
     * @return String
     * @exception Exception
     * @see
     */
    public static String getURLDecodeObj(Object object) {
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
        if(str == null || str.length() <= 0){
            return "";
        }
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

            if(reStr.matches("[\\d]+")) flag = true;
            else flag = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }
}
