package org.our.sadari.book.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.our.sadari.global.common.util.StringUtil;

/**
 * fileName       : BookCoverUrlUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-08-02
 * description    : 카카오 도서 썸네일에서 검증된 Daum 원본 표지 주소를 추출한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-02        SeungHyeon.Kang    최초 생성
 */
public final class BookCoverUrlUtil {

    // 카카오 도서 썸네일 호스트
    private static final String KAKAO_THUMBNAIL_HOST = "search1.kakaocdn.net";
    // 카카오 도서 썸네일 경로 접두사
    private static final String KAKAO_THUMBNAIL_PATH_PREFIX = "/thumb/";
    // Daum 도서 원본 이미지 호스트
    private static final String DAUM_ORIGINAL_HOST = "t1.daumcdn.net";
    // Daum 도서 원본 이미지 경로 접두사
    private static final String DAUM_ORIGINAL_PATH_PREFIX = "/lbook/image/";
    // 썸네일 쿼리에 포함된 원본 주소 파라미터명
    private static final String ORIGINAL_URL_PARAMETER = "fname";

    /**
     * 인스턴스 생성을 차단한다
     *
     * @author SeungHyeon.Kang
     */
    private BookCoverUrlUtil() {

        // 정적 유틸리티의 인스턴스 생성을 허용하지 않는다
        throw new IllegalStateException("Utility class");
    }

    /**
     * 카카오 도서 썸네일에서 검증된 Daum 원본 표지 HTTPS 주소를 추출한다
     *
     * @author SeungHyeon.Kang
     * @param thumbnailUrl 카카오 도서 검색 API의 썸네일 URL
     * @return 검증된 원본 표지 URL 또는 변환할 수 없을 때 기존 썸네일 URL
     */
    public static String getOriginalCoverUrl(String thumbnailUrl) {
        // 표지 URL이 없으면 기존 화면 계약에 맞는 빈 문자열을 반환한다
        if (StringUtil.isEmpty(thumbnailUrl) || thumbnailUrl.isBlank()) {
            // 빈 표지 URL을 반환한다
            return StringUtil.EMPTY;
        }

        String normalizedThumbnailUrl = thumbnailUrl.trim();

        // 잘못된 외부 URL은 원본 추출 실패로 격리하고 공식 썸네일을 유지한다
        try {
            // 카카오 썸네일의 프로토콜과 호스트 및 경로를 검증한다
            URI thumbnailUri = URI.create(normalizedThumbnailUrl);

            // 공식 카카오 도서 썸네일 형식이 아니면 입력 URL을 그대로 유지한다
            if (!isTrustedKakaoThumbnail(thumbnailUri)) {
                // 변환하지 않은 표지 URL을 반환한다
                return normalizedThumbnailUrl;
            }

            // 카카오 썸네일 쿼리에서 인코딩된 원본 주소를 조회한다
            String encodedOriginalUrl = getRawQueryParameter(thumbnailUri.getRawQuery(), ORIGINAL_URL_PARAMETER);

            // 원본 주소 파라미터가 없으면 공식 썸네일을 유지한다
            if (StringUtil.isEmpty(encodedOriginalUrl)) {
                // 변환하지 않은 카카오 썸네일 URL을 반환한다
                return normalizedThumbnailUrl;
            }

            // percent-encoding된 Daum 원본 주소를 URI 문자열로 복원한다
            String decodedOriginalUrl = URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8);
            // 복원한 원본 주소의 프로토콜과 호스트 및 경로를 검증한다
            URI originalUri = URI.create(decodedOriginalUrl);

            // 검증된 Daum 도서 원본 주소가 아니면 공식 썸네일을 유지한다
            if (!isTrustedDaumOriginal(originalUri)) {
                // 변환하지 않은 카카오 썸네일 URL을 반환한다
                return normalizedThumbnailUrl;
            }

            String originalQuery = StringUtil.isEmpty(originalUri.getRawQuery())
                    ? StringUtil.EMPTY : "?" + originalUri.getRawQuery();
            // HTTPS로 보정한 Daum 도서 원본 주소를 반환한다
            return "https://" + DAUM_ORIGINAL_HOST + originalUri.getRawPath() + originalQuery;
        }

        // URI 문법이나 인코딩이 잘못되면 공식 썸네일 URL로 복구한다
        catch (IllegalArgumentException e) {
            // 변환하지 않은 카카오 썸네일 URL을 반환한다
            return normalizedThumbnailUrl;
        }
    }

    /**
     * URI가 카카오 도서 썸네일의 신뢰 조건을 충족하는지 판정한다
     *
     * @author SeungHyeon.Kang
     * @param thumbnailUri 검증할 카카오 썸네일 URI
     * @return 공식 썸네일 형식이면 true
     */
    private static boolean isTrustedKakaoThumbnail(URI thumbnailUri) {
        String host = thumbnailUri.getHost();
        String path = thumbnailUri.getPath();
        int port = thumbnailUri.getPort();

        // 프로토콜과 호스트 및 경로가 공식 썸네일 형식인지 반환한다
        return "https".equalsIgnoreCase(thumbnailUri.getScheme()) && !StringUtil.isEmpty(host)
                && KAKAO_THUMBNAIL_HOST.equals(host.toLowerCase(Locale.ROOT)) && !StringUtil.isEmpty(path)
                && path.startsWith(KAKAO_THUMBNAIL_PATH_PREFIX) && StringUtil.isEmpty(thumbnailUri.getUserInfo())
                && (port == -1 || port == 443) && StringUtil.isEmpty(thumbnailUri.getFragment());
    }

    /**
     * URI가 Daum 도서 원본 이미지의 신뢰 조건을 충족하는지 판정한다
     *
     * @author SeungHyeon.Kang
     * @param originalUri 검증할 Daum 원본 URI
     * @return Daum 도서 원본 형식이면 true
     */
    private static boolean isTrustedDaumOriginal(URI originalUri) {
        String host = originalUri.getHost();
        String path = originalUri.getPath();
        int port = originalUri.getPort();
        boolean isSupportedScheme = "http".equalsIgnoreCase(originalUri.getScheme())
                || "https".equalsIgnoreCase(originalUri.getScheme());

        // 프로토콜과 호스트 및 경로가 Daum 도서 원본 형식인지 반환한다
        return isSupportedScheme && !StringUtil.isEmpty(host)
                && DAUM_ORIGINAL_HOST.equals(host.toLowerCase(Locale.ROOT)) && !StringUtil.isEmpty(path)
                && path.startsWith(DAUM_ORIGINAL_PATH_PREFIX) && StringUtil.isEmpty(originalUri.getUserInfo())
                && (port == -1 || port == 80 || port == 443) && StringUtil.isEmpty(originalUri.getFragment());
    }

    /**
     * 원시 쿼리 문자열에서 지정한 파라미터 값을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param rawQuery percent-encoding 상태의 쿼리 문자열
     * @param parameterName 조회할 파라미터명
     * @return 인코딩된 파라미터 값 또는 값이 없을 때 null
     */
    private static String getRawQueryParameter(String rawQuery, String parameterName) {
        // 쿼리가 없으면 원본 주소 파라미터도 존재하지 않는다
        if (StringUtil.isEmpty(rawQuery)) {
            // 조회할 파라미터가 없음을 반환한다
            return null;
        }

        // 여러 쿼리 파라미터를 순회하여 정확히 일치하는 이름을 찾는다
        for (String queryParameter : rawQuery.split("&")) {
            int separatorIndex = queryParameter.indexOf('=');

            // 이름과 값이 모두 있는 파라미터만 비교한다
            if (separatorIndex > 0 && parameterName.equals(queryParameter.substring(0, separatorIndex))) {
                // 원문 디코딩 전 파라미터 값을 반환한다
                return queryParameter.substring(separatorIndex + 1);
            }
        }

        // 지정한 쿼리 파라미터가 없음을 반환한다
        return null;
    }
}
