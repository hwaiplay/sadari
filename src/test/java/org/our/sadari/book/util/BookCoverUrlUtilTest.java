package org.our.sadari.book.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * fileName       : BookCoverUrlUtilTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-02
 * description    : 카카오 도서 썸네일의 Daum 원본 표지 주소 변환을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-02        SeungHyeon.Kang    최초 생성
 */
class BookCoverUrlUtilTest {

    /**
     * 카카오 썸네일 fname 주소를 HTTPS Daum 원본 주소로 변환하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getKakaoOriginalCover() {
        String thumbnailUrl = "https://search1.kakaocdn.net/thumb/R120x174.q85/"
                + "?fname=http%3A%2F%2Ft1.daumcdn.net%2Flbook%2Fimage%2F6253040%3Ftimestamp%3D20260115151223";

        // 카카오 썸네일에서 Daum 원본 표지 주소를 추출한다
        String originalUrl = BookCoverUrlUtil.getOriginalCoverUrl(thumbnailUrl);

        // 원본 주소의 프로토콜과 경로 및 쿼리가 보존되었는지 확인한다
        assertEquals("https://t1.daumcdn.net/lbook/image/6253040?timestamp=20260115151223", originalUrl);
    }

    /**
     * 원본 파라미터가 위장된 외부 호스트이면 기존 썸네일을 유지하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getCoverRejectsFakeHost() {
        String thumbnailUrl = "https://search1.kakaocdn.net/thumb/R120x174.q85/"
                + "?fname=https%3A%2F%2Ft1.daumcdn.net.example.com%2Flbook%2Fimage%2F1";

        // 위장된 원본 주소가 포함된 카카오 썸네일을 변환한다
        String convertedUrl = BookCoverUrlUtil.getOriginalCoverUrl(thumbnailUrl);

        // 신뢰할 수 없는 원본 대신 공식 카카오 썸네일이 유지되는지 확인한다
        assertEquals(thumbnailUrl, convertedUrl);
    }

    /**
     * 일반 이미지 URL은 카카오 원본 변환 없이 유지하는지 검증한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getNonKakaoCover() {
        String imageUrl = "https://shopping-phinf.pstatic.net/main_123/123.jpg";

        // 카카오 썸네일이 아닌 기존 도서 표지 URL을 변환한다
        String convertedUrl = BookCoverUrlUtil.getOriginalCoverUrl(imageUrl);

        // 기존 공급자 이미지 URL이 변경되지 않았는지 확인한다
        assertEquals(imageUrl, convertedUrl);
    }
}
