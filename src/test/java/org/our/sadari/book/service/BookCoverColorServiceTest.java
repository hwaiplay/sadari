package org.our.sadari.book.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.util.List;
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
 * description    : 도서 표지 대표색과 책장 색상 공통코드의 자동 매칭을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class BookCoverColorServiceTest {

    // 공통코드 조회 유틸리티
    @Mock
    private CodeUtil codeUtil;

    // 도서 표지 색상 분석 서비스
    private BookCoverColorService bookCoverColorService;

    /**
     * 각 테스트에서 도서 표지 색상 분석 서비스를 생성한다.
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {

        // 도서 표지 대표색 자동 매칭 테스트 대상을 생성한다
        bookCoverColorService = new BookCoverColorService(codeUtil);
    }

    /**
     * 표지 대표색과 같은 색상의 공통코드가 선택되는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void findNearestBookColorSelectsMatchingPaletteColor() {

        // 비교할 파란색과 코랄색 책장 공통코드를 생성한다
        CodeDto blueColorCode = createColorCode("BLUE", "#6aa6d8");
        CodeDto coralColorCode = createColorCode("CORAL_RED", "#c96f64");
        // 코랄색으로 채운 테스트용 도서 표지 이미지를 생성한다
        BufferedImage coverImage = createSolidColorImage(0xffc96f64);

        // 표지 대표색과 가장 가까운 책장 색상을 계산한다
        CodeDto matchedColorCode = bookCoverColorService.findNearestBookColor(
                coverImage, List.of(blueColorCode, coralColorCode), blueColorCode
        );

        // 표지와 같은 코랄색 공통코드가 선택되었는지 확인한다
        assertEquals("CORAL_RED", matchedColorCode.getComdCode());
    }

    /**
     * 흰색만 있는 표지는 대표색을 만들지 않고 기본 색상을 사용하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void findNearestBookColorUsesFallbackForWhiteImage() {

        // 분석 실패 시 사용할 기본 책장 공통코드를 생성한다
        CodeDto fallbackColorCode = createColorCode("BLUE", "#6aa6d8");
        // 흰색으로 채운 테스트용 도서 표지 이미지를 생성한다
        BufferedImage coverImage = createSolidColorImage(0xffffffff);

        // 흰색 표지의 책장 색상을 계산한다
        CodeDto matchedColorCode = bookCoverColorService.findNearestBookColor(
                coverImage, List.of(fallbackColorCode), fallbackColorCode
        );

        // 분석 가능한 대표색이 없을 때 기본 색상이 선택되었는지 확인한다
        assertEquals("BLUE", matchedColorCode.getComdCode());
    }

    /**
     * 허용하지 않은 외부 호스트는 요청하지 않고 기본 색상을 사용하는지 검증한다.
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getBookCoverColorUsesFallbackForUntrustedHost() {

        // 이미지 분석 실패 시 사용할 기본 책장 공통코드를 생성한다
        CodeDto fallbackColorCode = createColorCode("BLUE", "#6aa6d8");
        // BOOK_COLR 공통코드 조회 결과를 설정한다
        when(codeUtil.getCodeList(Constant.CODE_BOOK_COLR)).thenReturn(List.of(fallbackColorCode));
        // 허용 목록에 없는 외부 이미지 URL을 요청 DTO에 설정한다
        BookCoverColorRequestDto requestDto = new BookCoverColorRequestDto();
        requestDto.setBookCvim("https://example.com/cover.jpg");

        // 허용하지 않은 외부 이미지 URL의 책장 색상을 조회한다
        ResultData resultData = bookCoverColorService.getBookCoverColor(requestDto);
        BookCoverColorResponseDto responseDto = (BookCoverColorResponseDto) resultData.getData();

        // 외부 요청 없이 기본 색상의 성공 응답이 반환되었는지 확인한다
        assertEquals(200, resultData.getCode());
        assertEquals("BLUE", responseDto.getReptColr());
        assertEquals("#6aa6d8", responseDto.getReptColrName());
    }

    /**
     * 테스트에 사용할 책장 색상 공통코드를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param code 책장 색상 세부코드
     * @param hexColor 책장 색상 HEX 값
     * @return 테스트용 책장 색상 공통코드
     */
    private CodeDto createColorCode(String code, String hexColor) {

        // 테스트할 책장 색상 코드와 HEX 값을 DTO에 설정한다
        CodeDto colorCode = new CodeDto();
        colorCode.setComdCode(code);
        colorCode.setComdName(hexColor);

        // 생성한 책장 색상 공통코드를 반환한다
        return colorCode;
    }

    /**
     * 단일 색상으로 채운 테스트용 표지 이미지를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param argb 표지 전체에 적용할 ARGB 색상
     * @return 단일 색상으로 채운 표지 이미지
     */
    private BufferedImage createSolidColorImage(int argb) {

        // 지정한 색상으로 채울 테스트용 표지 이미지를 생성한다
        BufferedImage coverImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);

        // 표지 이미지의 모든 픽셀을 지정한 색상으로 채운다
        for (int y = 0; y < coverImage.getHeight(); y++) {
            // 같은 행의 픽셀을 가로 방향으로 순회한다
            for (int x = 0; x < coverImage.getWidth(); x++) {
                // 현재 픽셀에 테스트 색상을 설정한다
                coverImage.setRGB(x, y, argb);
            }
        }

        // 단일 색상으로 채운 테스트용 표지 이미지를 반환한다
        return coverImage;
    }
}
