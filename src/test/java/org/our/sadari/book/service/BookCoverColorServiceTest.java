package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.CRC32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.our.sadari.book.dto.BookCoverColorRequestDto;
import org.our.sadari.book.dto.BookCoverColorResponseDto;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;

/**
 * fileName       : BookCoverColorServiceTest
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 도서 표지 대표색과 책장 색상 공통코드의 자동 매칭을 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    카카오 도서 표지 호스트 검증 추가
 * 2026-11-26        SeungHyeon.Kang    대표 RGB 캐시 경계 검증
 */
@ExtendWith(MockitoExtension.class)
class BookCoverColorServiceTest {

    // 공통코드 조회 유틸리티
    @Mock
    private CodeUtil codeUtil;

    // 도서 표지 색상 분석 서비스
    private BookCoverColorService bookCoverColorService;

    /**
     * 각 테스트에서 도서 표지 색상 분석 서비스를 생성
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // 도서 표지 대표색 자동 매칭 테스트 대상을 생성
        bookCoverColorService = new BookCoverColorService(codeUtil);
    }

    /**
     * 표지 대표색과 같은 색상의 공통코드가 선택되는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getNearestBookColor() {

        // 비교할 파란색과 코랄색 책장 공통코드를 생성
        CodeDto blueColorCode = createColorCode("BLUE", "#6aa6d8");
        CodeDto coralColorCode = createColorCode("CORAL_RED", "#c96f64");
        // 코랄색으로 채운 테스트용 도서 표지 이미지를 생성
        BufferedImage coverImage = createSolidColorImage(0xffc96f64);

        // 표지 대표색과 가장 가까운 책장 색상을 계산
        CodeDto matchedColorCode = bookCoverColorService.findNearestBookColor(
                coverImage, List.of(blueColorCode, coralColorCode), blueColorCode
        );

        // 표지와 같은 코랄색 공통코드가 선택되었는지 확인
        assertEquals("CORAL_RED", matchedColorCode.getComdCode());
    }

    /**
     * 흰색만 있는 표지는 대표색을 만들지 않고 기본 색상을 사용하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBookColorWhiteFallback() {

        // 분석 실패 시 사용할 기본 책장 공통코드를 생성
        CodeDto fallbackColorCode = createColorCode("BLUE", "#6aa6d8");
        // 흰색으로 채운 테스트용 도서 표지 이미지를 생성
        BufferedImage coverImage = createSolidColorImage(0xffffffff);

        // 흰색 표지의 책장 색상을 계산
        CodeDto matchedColorCode = bookCoverColorService.findNearestBookColor(
                coverImage, List.of(fallbackColorCode), fallbackColorCode
        );

        // 분석 가능한 대표색이 없을 때 기본 색상이 선택되었는지 확인
        assertEquals("BLUE", matchedColorCode.getComdCode());
    }

    /**
     * 허용하지 않은 외부 호스트는 요청하지 않고 기본 색상을 사용하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getCoverColorHostFallback() {

        // 이미지 분석 실패 시 사용할 기본 책장 공통코드를 생성
        CodeDto fallbackColorCode = createColorCode("BLUE", "#6aa6d8");
        // BOOK_COLR 공통코드 조회 결과를 설정
        when(codeUtil.getCodeList(Constant.CODE_BOOK_COLR)).thenReturn(List.of(fallbackColorCode));
        // 허용 목록에 없는 외부 이미지 URL을 요청 DTO에 설정
        BookCoverColorRequestDto requestDto = new BookCoverColorRequestDto();
        requestDto.setBookCvim("https://example.com/cover.jpg");

        // 허용하지 않은 외부 이미지 URL의 책장 색상을 조회
        ResultData resultData = bookCoverColorService.getBookCoverColor(requestDto);
        BookCoverColorResponseDto responseDto = (BookCoverColorResponseDto) resultData.getData();

        // 외부 요청 없이 기본 색상의 성공 응답이 반환되었는지 확인
        assertEquals(200, resultData.getCode());
        assertEquals("BLUE", responseDto.getReptColr());
        assertEquals("#6aa6d8", responseDto.getReptColrName());
    }

    /**
     * 카카오 도서 검색의 HTTPS 표지 호스트만 외부 분석 대상으로 허용하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getTrustedKakaoCover() {
        // 카카오 공식 도서 검색 응답 형식의 표지 URI를 검증
        URI coverUri = bookCoverColorService.getTrustedCoverUri(
                "https://search1.kakaocdn.net/thumb/R120x174.q85/book-cover"
        );
        // 허용 목록에 등록된 카카오 표지 호스트인지 확인
        assertNotNull(coverUri);
        assertEquals("search1.kakaocdn.net", coverUri.getHost());
    }

    /**
     * 카카오 썸네일에서 추출한 Daum 도서 원본 표지 호스트를 허용하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getTrustedDaumCover() {
        // Daum 도서 원본 이미지 형식의 표지 URI를 검증
        URI coverUri = bookCoverColorService.getTrustedCoverUri(
                "https://t1.daumcdn.net/lbook/image/6253040?timestamp=20260115151223"
        );
        // 허용 목록에 등록된 Daum 원본 표지 호스트인지 확인
        assertNotNull(coverUri);
        assertEquals("t1.daumcdn.net", coverUri.getHost());
    }

    /**
     * 카카오 호스트명을 접미사로 위장한 외부 주소를 차단하는지 검증
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getCoverRejectsFakeKakao() {
        // 허용 호스트명을 접미사로 사용한 외부 주소를 검증
        URI coverUri = bookCoverColorService.getTrustedCoverUri(
                "https://search1.kakaocdn.net.example.com/book-cover"
        );
        // 정확히 일치하지 않는 외부 호스트가 차단되었는지 확인
        assertNull(coverUri);
    }

    /** 압축 바이트가 작아도 허용 크기를 넘는 이미지 헤더는 픽셀 디코딩 전에 차단 */
    @Test
    void decodeRejectsLargeHeader() throws IOException {
        // 전체 픽셀 데이터를 만들지 않고 가로 크기만 제한을 넘긴 유효한 PNG 헤더를 생성
        byte[] oversizedPngHeader = createPngHeader(4097, 1);

        // 이미지 헤더 크기 검증을 실행
        BufferedImage coverImage = bookCoverColorService.decodeCoverImage(oversizedPngHeader);

        // 과도한 픽셀 버퍼를 할당하지 않고 사용할 수 없는 이미지로 처리되는지 확인
        assertNull(coverImage);
    }

    /**
     * 테스트에 사용할 책장 색상 공통코드를 생성
     *
     * @author SeungHyeon.Kang
     * @param code 책장 색상 세부코드
     * @param hexColor 책장 색상 HEX 값
     * @return 테스트용 책장 색상 공통코드
     */
    private CodeDto createColorCode(String code, String hexColor) {

        // 테스트할 책장 색상 코드와 HEX 값을 DTO에 설정
        CodeDto colorCode = new CodeDto();
        colorCode.setComdCode(code);
        colorCode.setComdName(hexColor);

        // 생성한 책장 색상 공통코드를 반환
        return colorCode;
    }

    /**
     * 단일 색상으로 채운 테스트용 표지 이미지를 생성
     *
     * @author SeungHyeon.Kang
     * @param argb 표지 전체에 적용할 ARGB 색상
     * @return 단일 색상으로 채운 표지 이미지
     */
    private BufferedImage createSolidColorImage(int argb) {

        // 지정한 색상으로 채울 테스트용 표지 이미지를 생성
        BufferedImage coverImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);

        // 표지 이미지의 모든 픽셀을 지정한 색상으로 채움
        for (int y = 0; y < coverImage.getHeight(); y++) {
            // 같은 행의 픽셀을 가로 방향으로 순회
            for (int x = 0; x < coverImage.getWidth(); x++) {
                // 현재 픽셀에 테스트 색상을 설정
                coverImage.setRGB(x, y, argb);
            }
        }

        // 단일 색상으로 채운 테스트용 표지 이미지를 반환
        return coverImage;
    }

    /**
     * 이미지 크기 검증 테스트에 사용할 PNG 서명과 IHDR 청크를 생성
     *
     * @author SeungHyeon.Kang
     * @param width PNG 헤더 가로 크기
     * @param height PNG 헤더 세로 크기
     * @return PNG 헤더 바이트
     * @throws IOException 테스트 바이트 생성 실패
     */
    private byte[] createPngHeader(int width, int height) throws IOException {
        // IHDR 데이터에 PNG 기본 RGB 형식과 테스트 크기를 기록
        ByteArrayOutputStream headerDataBuffer = new ByteArrayOutputStream();
        try (DataOutputStream headerData = new DataOutputStream(headerDataBuffer)) {
            headerData.writeInt(width);
            headerData.writeInt(height);
            headerData.writeByte(8);
            headerData.writeByte(2);
            headerData.writeByte(0);
            headerData.writeByte(0);
            headerData.writeByte(0);
        }

        // PNG Reader가 헤더 크기를 읽을 수 있도록 서명, IHDR와 CRC를 직렬화
        byte[] chunkType = "IHDR".getBytes(StandardCharsets.US_ASCII);
        byte[] headerBytes = headerDataBuffer.toByteArray();
        CRC32 crc32 = new CRC32();
        crc32.update(chunkType);
        crc32.update(headerBytes);
        ByteArrayOutputStream pngBuffer = new ByteArrayOutputStream();
        try (DataOutputStream png = new DataOutputStream(pngBuffer)) {
            png.write(new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
            png.writeInt(headerBytes.length);
            png.write(chunkType);
            png.write(headerBytes);
            png.writeInt((int) crc32.getValue());
        }

        // 크기 헤더만 포함한 테스트 PNG 바이트를 반환
        return pngBuffer.toByteArray();
    }

    /**
     * 같은 표지는 한 번만 분석하고 현재 색상 팔레트는 매번 적용하는지 검증
     *
     * @author SeungHyeon.Kang
     * @throws Exception 이미지 분석 모의 설정 실패
     */
    @Test
    void reuseRgbWithNewPalette() throws Exception {
        // 동일 대표 RGB를 다시 매칭할 서로 다른 팔레트 구성
        CodeDto blue = createColorCode("BLUE", "#0000ff");
        // 기존 코드가 비활성화된 이후의 팔레트 구성
        CodeDto red = createColorCode("RED", "#ff0000");
        // 요청마다 다른 현재 팔레트 반환
        when(codeUtil.getCodeList(Constant.CODE_BOOK_COLR)).thenReturn(List.of(blue)).thenReturn(List.of(red));
        // 실제 서비스의 다운로드 경계만 대체
        BookCoverColorService service = org.mockito.Mockito.spy(bookCoverColorService);
        // 캐시 유효 시간 설정
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cacheSeconds", 900L);
        // 최대 캐시 항목 수 설정
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cacheMaxEntries", 2);
        // 대표색이 파란 유효 표지 구성
        org.mockito.Mockito.doReturn(createSolidColorImage(0xff0000ff)).when(service).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
        // 허용된 표지 요청 구성
        BookCoverColorRequestDto request = new BookCoverColorRequestDto();
        // 같은 외부 표지 주소 설정
        request.setBookCvim("https://search1.kakaocdn.net/thumb/book-cache");
        // 첫 분석과 두 번째 현재 팔레트 매칭 실행
        var first = (BookCoverColorResponseDto) service.getBookCoverColor(request).getData();
        // 원본 픽셀 재분석 없이 두 번째 요청 실행
        var second = (BookCoverColorResponseDto) service.getBookCoverColor(request).getData();
        // 첫 팔레트의 선택 결과 확인
        assertEquals("BLUE", first.getReptColr());
        // 오래된 코드 대신 현재 팔레트 선택 확인
        assertEquals("RED", second.getReptColr());
        // 대표 RGB를 재사용한 다운로드 한 번만 실행 확인
        org.mockito.Mockito.verify(service).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
    }

    /**
     * 동일 URL의 동시 분석을 하나로 합치고 캐시 크기를 제한하는지 검증
     *
     * @author SeungHyeon.Kang
     * @throws Exception 동시 분석 대기 실패
     */
    @Test
    void shareBoundedCoverCache() throws Exception {
        // 모든 요청에 같은 현재 팔레트 구성
        when(codeUtil.getCodeList(Constant.CODE_BOOK_COLR)).thenReturn(List.of(createColorCode("BLUE", "#0000ff")));
        // 외부 통신 횟수만 관찰할 실제 서비스 구성
        BookCoverColorService service = org.mockito.Mockito.spy(bookCoverColorService);
        // 양수 유효 시간 구성
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cacheSeconds", 900L);
        // 메모리 상한 경계 검증을 위한 한 항목 캐시 구성
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cacheMaxEntries", 1);
        // 유효한 표지 응답 구성
        org.mockito.Mockito.doReturn(createSolidColorImage(0xff0000ff)).when(service).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
        // 동시에 같은 URL을 요청할 실행기 구성
        var executor = java.util.concurrent.Executors.newFixedThreadPool(8);
        // 테스트 실패 시에도 실행기 정리 보장
        try {
            // 동시에 공유할 요청 구성
            BookCoverColorRequestDto request = new BookCoverColorRequestDto();
            // 허용된 고정 URL 설정
            request.setBookCvim("https://search1.kakaocdn.net/thumb/book-a");
            // 병렬 요청 결과 보관
            var futures = new java.util.ArrayList<java.util.concurrent.Future<ResultData>>();
            // 같은 표지의 동시 요청 구성
            for (int index = 0; index < 8; index++) {
                // 실제 캐시 잠금 경계를 통과하는 요청 실행
                futures.add(executor.submit(() -> service.getBookCoverColor(request)));
            }

            // 모든 병렬 결과의 완료와 성공 확인
            for (var future : futures) {
                // 무한 대기가 없는 정상 응답 확인
                assertEquals(200, future.get(5, java.util.concurrent.TimeUnit.SECONDS).getCode());
            }

            // 같은 URL의 다운로드 한 번만 실행 확인
            org.mockito.Mockito.verify(service).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
            // 다른 URL을 저장하여 캐시 상한 경계 이동
            request.setBookCvim("https://search1.kakaocdn.net/thumb/book-b");
            // 두 번째 URL의 대표색 저장
            service.getBookCoverColor(request);
            // 제거된 첫 번째 URL 재요청 구성
            request.setBookCvim("https://search1.kakaocdn.net/thumb/book-a");
            // 상한 때문에 제거된 표지 재분석
            service.getBookCoverColor(request);
            // 한 항목 캐시의 제거와 재분석 확인
            org.mockito.Mockito.verify(service, org.mockito.Mockito.times(3)).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
        }

        // 테스트 실행 스레드 누수 방지
        finally {
            // 남아 있는 실행 작업 종료
            executor.shutdownNow();
        }

    }

    /**
     * 실패 결과는 캐시하지 않고 만료한 성공 결과는 다시 분석하는지 검증
     *
     * @author SeungHyeon.Kang
     * @throws Exception 이미지 분석 및 만료 대기 실패
     */
    @Test
    void retryAndExpireCoverRgb() throws Exception {
        // 실패 시 기본 팔레트와 성공 시 매칭 팔레트 구성
        when(codeUtil.getCodeList(Constant.CODE_BOOK_COLR)).thenReturn(List.of(createColorCode("BLUE", "#0000ff")));
        // 다운로드 경계 대체
        BookCoverColorService service = org.mockito.Mockito.spy(bookCoverColorService);
        // 짧은 만료 시간으로 실제 단조 시계 경계 검증
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cacheSeconds", 1L);
        // 캐시 항목 수 구성
        org.springframework.test.util.ReflectionTestUtils.setField(service, "cacheMaxEntries", 2);
        // 최초 실패 이후 유효 표지를 반환하는 응답 구성
        org.mockito.Mockito.doReturn((Object) null, createSolidColorImage(0xff0000ff)).when(service).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
        // 허용된 표지 요청 구성
        BookCoverColorRequestDto request = new BookCoverColorRequestDto();
        // 같은 표지 주소 설정
        request.setBookCvim("https://search1.kakaocdn.net/thumb/book-expiry");
        // 실패 후 기본 색상 반환
        service.getBookCoverColor(request);
        // 실패를 캐시하지 않은 재시도 실행
        service.getBookCoverColor(request);
        // 성공한 RGB의 캐시 적중 실행
        service.getBookCoverColor(request);
        // 성공 전후 다운로드 두 번 확인
        org.mockito.Mockito.verify(service, org.mockito.Mockito.times(2)).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
        // 실제 설정 단위의 만료 경계 대기
        Thread.sleep(1100);
        // 만료 이후의 재분석 실행
        service.getBookCoverColor(request);
        // 만료된 결과의 다운로드 재실행 확인
        org.mockito.Mockito.verify(service, org.mockito.Mockito.times(3)).downloadCoverImage(org.mockito.ArgumentMatchers.any(URI.class));
    }
}
