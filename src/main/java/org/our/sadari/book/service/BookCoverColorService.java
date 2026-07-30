package org.our.sadari.book.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.our.sadari.book.dto.BookCoverColorRequestDto;
import org.our.sadari.book.dto.BookCoverColorResponseDto;
import org.our.sadari.global.common.code.dto.CodeDto;
import org.our.sadari.global.common.code.util.CodeUtil;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.global.common.result.ResultData;
import org.our.sadari.global.common.result.ResultEnum;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;

/**
 * fileName       : BookCoverColorService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 도서 표지 대표색과 가장 가까운 책장 색상 공통코드를 판정한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookCoverColorService {

    // 네이버 도서 표지 이미지 허용 호스트
    private static final String NAVER_BOOK_IMAGE_HOST = "shopping-phinf.pstatic.net";
    // 표지 이미지 최대 응답 크기
    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;
    // 표지 이미지 최대 가로 또는 세로 크기
    private static final int MAX_IMAGE_DIMENSION = 4096;
    // 대표색 분석 최대 샘플 픽셀 수
    private static final int MAX_SAMPLE_PIXEL_COUNT = 4096;
    // RGB 색상 버킷 단위
    private static final int COLOR_BUCKET_BIT_SHIFT = 5;
    // RGB 색상 버킷 개수
    private static final int COLOR_BUCKET_COUNT = 512;
    // 투명 픽셀 제외 기준
    private static final int MIN_ALPHA = 128;
    // 흰 여백 제외 밝기 기준
    private static final double WHITE_BRIGHTNESS_THRESHOLD = 0.94;
    // 흰 여백 제외 채도 기준
    private static final double WHITE_SATURATION_THRESHOLD = 0.12;
    // HEX 색상 형식
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9a-fA-F]{6}$");
    // 표지 이미지 연결 제한 시간
    private static final Duration IMAGE_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    // 표지 이미지 응답 제한 시간
    private static final Duration IMAGE_REQUEST_TIMEOUT = Duration.ofSeconds(5);

    // 공통코드 조회 유틸리티
    private final CodeUtil codeUtil;
    // 제한 시간과 리다이렉트 차단 정책을 적용한 표지 이미지 HTTP 클라이언트
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(IMAGE_CONNECT_TIMEOUT).followRedirects(HttpClient.Redirect.NEVER).build();

    /**
     * 네이버 도서 표지 대표색과 가장 가까운 활성 BOOK_COLR 코드를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param requestDto 대표색을 분석할 도서 표지 URL
     * @return 표지 대표색과 가장 가까운 책장 색상 코드
     */
    public ResultData getBookCoverColor(BookCoverColorRequestDto requestDto) {
        // 자동 선택과 실패 기본값에 사용할 활성 책장 색상 목록을 조회한다
        List<CodeDto> colorCodeList = codeUtil.getCodeList(Constant.CODE_BOOK_COLR);

        // 활성 책장 색상이 없으면 저장 가능한 색상을 반환할 수 없으므로 조회 실패로 처리한다
        if (StringUtil.isEmpty(colorCodeList) || colorCodeList.isEmpty()) {
            // "조회 결과가 없어요."
            return ResultData.fail(ResultEnum.COMMON_NO_DATA);
        }

        // 정렬 순서가 가장 빠른 활성 색상을 이미지 분석 실패 시 사용할 기본값으로 지정한다
        CodeDto fallbackColorCode = colorCodeList.get(0);
        // 임의 외부 서버 접근을 차단하기 위해 네이버 표지 전용 HTTPS URL만 허용한다
        URI coverUri = getTrustedCoverUri(requestDto.getBookCvim());

        // 허용되지 않은 URL은 외부 요청을 보내지 않고 공통코드 기본값으로 보정한다
        if (StringUtil.isEmpty(coverUri)) {
            // 분석할 수 없는 표지의 기본 책장 색상 응답을 반환한다
            return ResultData.success(createColorResponse(fallbackColorCode));
        }

        // 외부 이미지 응답과 디코딩 실패를 기본 색상으로 복구하기 위한 블록이다
        try {
            // 크기 제한 안에서 네이버 도서 표지 이미지를 내려받는다
            BufferedImage coverImage = downloadCoverImage(coverUri);

            // 이미지가 비어 있거나 디코딩되지 않으면 기본 책장 색상을 사용한다
            if (StringUtil.isEmpty(coverImage)) {
                // 분석할 수 없는 표지의 기본 책장 색상 응답을 반환한다
                return ResultData.success(createColorResponse(fallbackColorCode));
            }

            // 표지 대표색과 CIELAB 거리가 가장 가까운 활성 공통코드를 계산한다
            CodeDto matchedColorCode = findNearestBookColor(coverImage, colorCodeList, fallbackColorCode);

            // 자동 선택된 책장 색상 코드를 성공 응답으로 반환한다
            return ResultData.success(createColorResponse(matchedColorCode));
        }

        // 요청 스레드 중단은 복구 상태를 유지한 뒤 기본 색상으로 전환한다
        catch (InterruptedException e) {
            // 상위 실행 흐름이 중단 상태를 확인할 수 있도록 인터럽트 표시를 복원한다
            Thread.currentThread().interrupt();
            // 표지 분석 실패 원인과 대상 호스트를 서버 로그에 남긴다
            log.warn("도서 표지 대표색 분석 중 요청이 중단되었습니다. host={}", coverUri.getHost(), e);

            // 중단된 표지 분석의 기본 책장 색상 응답을 반환한다
            return ResultData.success(createColorResponse(fallbackColorCode));
        }

        // 네이버 이미지 통신 또는 이미지 디코딩 실패는 등록을 막지 않고 기본 색상으로 복구한다
        catch (IOException | RuntimeException e) {
            // 원본 URL 전체를 노출하지 않고 허용 호스트와 예외만 기록한다
            log.warn("도서 표지 대표색 분석에 실패했습니다. host={}", coverUri.getHost(), e);

            // 실패한 표지 분석의 기본 책장 색상 응답을 반환한다
            return ResultData.success(createColorResponse(fallbackColorCode));
        }
    }

    /**
     * 외부 요청에 사용할 수 있는 네이버 도서 표지 HTTPS 주소인지 검증한다
     *
     * @author SeungHyeon.Kang
     * @param bookCvim 검증할 도서 표지 URL
     * @return 허용된 네이버 도서 표지 URI 또는 검증 실패 시 null
     */
    private URI getTrustedCoverUri(String bookCvim) {
        // 빈 표지 URL은 URI 변환 전에 차단한다
        if (StringUtil.isEmpty(bookCvim) || bookCvim.isBlank()) {
            // 허용할 표지 URI가 없음을 반환한다
            return null;
        }

        // 잘못된 URI 문법을 검증 실패로 전환하기 위한 블록이다
        try {
            // 표지 URL의 프로토콜과 호스트 및 포트를 개별 검증할 URI로 변환한다
            URI coverUri = URI.create(bookCvim.trim());
            String host = coverUri.getHost();
            int port = coverUri.getPort();

            // HTTPS와 네이버 이미지 호스트 및 기본 HTTPS 포트만 허용해 SSRF 우회 경로를 차단한다
            if (!"https".equalsIgnoreCase(coverUri.getScheme()) || StringUtil.isEmpty(host)
                    || !NAVER_BOOK_IMAGE_HOST.equals(host.toLowerCase(Locale.ROOT))
                    || !StringUtil.isEmpty(coverUri.getUserInfo()) || (port != -1 && port != 443)) {
                // 허용되지 않은 표지 URI임을 반환한다
                return null;
            }

            // 검증이 끝난 네이버 도서 표지 URI를 반환한다
            return coverUri;
        }

        // URI 문법이 잘못된 요청은 외부 통신 없이 검증 실패로 처리한다
        catch (IllegalArgumentException e) {
            // 허용되지 않은 표지 URI임을 반환한다
            return null;
        }
    }

    /**
     * 네이버 도서 표지를 제한된 크기로 내려받아 이미지로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param coverUri 검증이 끝난 네이버 도서 표지 URI
     * @return 디코딩된 도서 표지 이미지 또는 유효하지 않은 응답의 null
     * @throws IOException 이미지 응답 읽기 또는 디코딩에 실패한 경우 발생
     * @throws InterruptedException 외부 이미지 요청 중 스레드가 중단된 경우 발생
     */
    private BufferedImage downloadCoverImage(URI coverUri) throws IOException, InterruptedException {
        // 네이버 이미지 서버에 전달할 제한 시간과 응답 형식 헤더를 구성한다
        HttpRequest request = HttpRequest.newBuilder(coverUri).timeout(IMAGE_REQUEST_TIMEOUT).header("Accept", "image/*").header("User-Agent", "Sadari-Book-Cover-Color/1.0").GET().build();
        // 응답 본문을 스트림으로 받아 설정한 최대 크기를 초과하지 않게 읽는다
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        byte[] imageBytes;
        // 상태나 헤더 검증에 실패한 응답까지 포함해 HTTP 본문 스트림을 항상 닫는다
        try (InputStream inputStream = response.body()) {
            // 정상 이미지 응답이 아니면 본문을 이미지로 해석하지 않는다
            if (response.statusCode() != 200) {
                // 사용할 수 없는 이미지 응답임을 반환한다
                return null;
            }

            // 응답 헤더의 이미지 형식과 선언된 크기를 검증한다
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);

            // 이미지가 아니거나 허용 크기를 초과한 응답은 메모리에 적재하지 않는다
            if (!contentType.toLowerCase(Locale.ROOT).startsWith("image/") || contentLength > MAX_IMAGE_BYTES) {
                // 사용할 수 없는 이미지 응답임을 반환한다
                return null;
            }

            // 메모리 과다 사용을 막기 위해 표지 이미지 응답을 최대 허용 크기까지만 읽는다
            imageBytes = inputStream.readNBytes(MAX_IMAGE_BYTES + 1);
        }

        // 실제 응답이 비어 있거나 최대 크기를 초과하면 이미지 디코딩을 수행하지 않는다
        if (imageBytes.length == 0 || imageBytes.length > MAX_IMAGE_BYTES) {
            // 사용할 수 없는 이미지 응답임을 반환한다
            return null;
        }

        // 제한 크기 안의 이미지 바이트를 대표색 분석이 가능한 BufferedImage로 변환한다
        BufferedImage coverImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        // 디코딩 실패 또는 과도한 이미지 크기는 픽셀 분석 전에 차단한다
        if (StringUtil.isEmpty(coverImage) || coverImage.getWidth() > MAX_IMAGE_DIMENSION
                || coverImage.getHeight() > MAX_IMAGE_DIMENSION) {
            // 사용할 수 없는 이미지 응답임을 반환한다
            return null;
        }

        // 크기 검증이 끝난 도서 표지 이미지를 반환한다
        return coverImage;
    }

    /**
     * 표지 대표색과 CIELAB 거리가 가장 가까운 활성 BOOK_COLR 코드를 찾는다
     *
     * @author SeungHyeon.Kang
     * @param coverImage 대표색을 계산할 도서 표지 이미지
     * @param colorCodeList 비교할 활성 BOOK_COLR 목록
     * @param fallbackColorCode 대표색 또는 HEX 분석 실패 시 사용할 기본 코드
     * @return 표지 대표색과 가장 가까운 활성 책장 색상 코드
     */
    CodeDto findNearestBookColor(BufferedImage coverImage, List<CodeDto> colorCodeList, CodeDto fallbackColorCode) {
        // 흰 여백과 투명 픽셀을 제외한 표지의 지배적인 색상을 계산한다
        int[] representativeColor = getRepresentativeColor(coverImage);

        // 유효한 대표색이 없으면 정렬 순서가 가장 빠른 공통코드를 사용한다
        if (StringUtil.isEmpty(representativeColor)) {
            // 이미지 분석 실패 시 사용할 기본 색상 코드를 반환한다
            return fallbackColorCode;
        }

        // 표지 대표색을 사람의 색상 인지 차이에 가까운 CIELAB 값으로 변환한다
        double[] representativeLab = convertRgbToLab(representativeColor[0], representativeColor[1], representativeColor[2]);
        CodeDto nearestColorCode = fallbackColorCode;
        double nearestDistance = Double.MAX_VALUE;

        // 활성 BOOK_COLR의 HEX 색상을 표지 대표색과 하나씩 비교한다
        for (CodeDto colorCode : colorCodeList) {
            // 코드 식별값이나 HEX 색상이 없는 행은 최근접 색상 후보에서 제외한다
            if (StringUtil.isEmpty(colorCode) || StringUtil.isEmpty(colorCode.getComdCode())
                    || StringUtil.isEmpty(colorCode.getComdName())) {
                continue;
            }

            // 공통코드명을 RGB 비교값으로 변환한다
            int[] paletteColor = parseHexColor(colorCode.getComdName());

            // HEX 형식이 아닌 코드명은 잘못된 색상 후보이므로 제외한다
            if (StringUtil.isEmpty(paletteColor)) {
                continue;
            }

            // 공통코드 색상을 CIELAB 값으로 변환한다
            double[] paletteLab = convertRgbToLab(paletteColor[0], paletteColor[1], paletteColor[2]);
            // 제곱근 없이 CIELAB 축별 거리 제곱을 합산해 후보 간 상대 거리를 비교한다
            double distance = getLabDistanceSquared(representativeLab, paletteLab);

            // 현재 후보가 더 가까우면 자동 선택할 책장 색상 코드를 교체한다
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestColorCode = colorCode;
            }
        }

        // 모든 활성 공통코드를 비교해 결정한 최근접 책장 색상을 반환한다
        return nearestColorCode;
    }

    /**
     * 표지 픽셀을 RGB 버킷으로 집계해 가장 넓게 분포한 대표색을 계산한다
     *
     * @author SeungHyeon.Kang
     * @param coverImage 대표색을 계산할 도서 표지 이미지
     * @return 대표색 RGB 배열 또는 유효한 픽셀이 없을 경우 null
     */
    private int[] getRepresentativeColor(BufferedImage coverImage) {
        int width = coverImage.getWidth();
        int height = coverImage.getHeight();
        int sampleStep = Math.max(1, (int) Math.ceil(Math.sqrt((double) width * height / MAX_SAMPLE_PIXEL_COUNT)));
        double[] bucketWeights = new double[COLOR_BUCKET_COUNT];
        double[] redTotals = new double[COLOR_BUCKET_COUNT];
        double[] greenTotals = new double[COLOR_BUCKET_COUNT];
        double[] blueTotals = new double[COLOR_BUCKET_COUNT];

        // 전체 표지를 일정 간격으로 샘플링해 특정 해상도에 분석 비용이 치우치지 않게 한다
        for (int y = 0; y < height; y += sampleStep) {
            // 같은 행의 샘플 픽셀을 가로 방향으로 순회한다
            for (int x = 0; x < width; x += sampleStep) {
                int argb = coverImage.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xff;

                // 투명 픽셀은 실제 표지색이 아니므로 대표색 집계에서 제외한다
                if (alpha < MIN_ALPHA) {
                    continue;
                }

                int red = (argb >>> 16) & 0xff;
                int green = (argb >>> 8) & 0xff;
                int blue = argb & 0xff;
                double maximum = Math.max(red, Math.max(green, blue)) / 255.0;
                double minimum = Math.min(red, Math.min(green, blue)) / 255.0;
                double saturation = maximum == 0.0 ? 0.0 : (maximum - minimum) / maximum;
                double brightness = (red + green + blue) / (3.0 * 255.0);

                // 표지 주변의 흰 여백은 실제 디자인 대표색보다 넓게 잡힐 수 있어 제외한다
                if (brightness >= WHITE_BRIGHTNESS_THRESHOLD && saturation <= WHITE_SATURATION_THRESHOLD) {
                    continue;
                }

                int bucketIndex = ((red >> COLOR_BUCKET_BIT_SHIFT) << 6)
                        | ((green >> COLOR_BUCKET_BIT_SHIFT) << 3)
                        | (blue >> COLOR_BUCKET_BIT_SHIFT);
                double weight = 1.0 + saturation * 2.0;
                bucketWeights[bucketIndex] += weight;
                redTotals[bucketIndex] += red * weight;
                greenTotals[bucketIndex] += green * weight;
                blueTotals[bucketIndex] += blue * weight;
            }
        }

        int dominantBucketIndex = -1;
        double dominantBucketWeight = 0.0;

        // 채도 가중치를 반영한 픽셀 분포가 가장 큰 색상 버킷을 찾는다
        for (int bucketIndex = 0; bucketIndex < COLOR_BUCKET_COUNT; bucketIndex++) {
            // 더 넓게 분포한 버킷이면 표지 대표색 후보를 교체한다
            if (bucketWeights[bucketIndex] > dominantBucketWeight) {
                dominantBucketIndex = bucketIndex;
                dominantBucketWeight = bucketWeights[bucketIndex];
            }
        }

        // 흰 여백과 투명 픽셀을 제외한 유효 픽셀이 없으면 대표색을 만들지 않는다
        if (dominantBucketIndex < 0 || dominantBucketWeight == 0.0) {
            // 유효한 대표색이 없음을 반환한다
            return null;
        }

        // 지배적인 버킷에 포함된 실제 RGB 값의 가중 평균을 대표색으로 반환한다
        return new int[] {
                (int) Math.round(redTotals[dominantBucketIndex] / dominantBucketWeight),
                (int) Math.round(greenTotals[dominantBucketIndex] / dominantBucketWeight),
                (int) Math.round(blueTotals[dominantBucketIndex] / dominantBucketWeight)
        };
    }

    /**
     * HEX 색상 문자열을 RGB 정수 배열로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param hexColor #RRGGBB 형식의 색상 문자열
     * @return RGB 정수 배열 또는 잘못된 HEX 형식의 null
     */
    private int[] parseHexColor(String hexColor) {
        // 공통코드명이 #RRGGBB 형식이 아니면 색상 비교에서 제외한다
        if (StringUtil.isEmpty(hexColor) || !HEX_COLOR_PATTERN.matcher(hexColor.trim()).matches()) {
            // 유효한 RGB 색상이 없음을 반환한다
            return null;
        }

        // 공통코드 관리 과정에서 들어갈 수 있는 앞뒤 공백을 제거한다
        String normalizedHexColor = hexColor.trim();
        // HEX 각 채널을 0부터 255 사이의 RGB 정수로 변환한다
        int red = Integer.parseInt(normalizedHexColor.substring(1, 3), 16);
        int green = Integer.parseInt(normalizedHexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(normalizedHexColor.substring(5, 7), 16);

        // 변환된 RGB 채널 배열을 반환한다
        return new int[] {red, green, blue};
    }

    /**
     * RGB 색상을 D65 기준 CIELAB 색상으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param red 빨간색 채널
     * @param green 초록색 채널
     * @param blue 파란색 채널
     * @return L, a, b 축으로 구성된 CIELAB 배열
     */
    private double[] convertRgbToLab(int red, int green, int blue) {
        double linearRed = convertSrgbChannel(red / 255.0);
        double linearGreen = convertSrgbChannel(green / 255.0);
        double linearBlue = convertSrgbChannel(blue / 255.0);
        double x = (linearRed * 0.4124564 + linearGreen * 0.3575761 + linearBlue * 0.1804375) / 0.95047;
        double y = linearRed * 0.2126729 + linearGreen * 0.7151522 + linearBlue * 0.0721750;
        double z = (linearRed * 0.0193339 + linearGreen * 0.1191920 + linearBlue * 0.9503041) / 1.08883;
        double convertedX = convertXyzChannel(x);
        double convertedY = convertXyzChannel(y);
        double convertedZ = convertXyzChannel(z);

        // 표준 D65 기준으로 계산한 CIELAB 축 값을 반환한다
        return new double[] {
                116.0 * convertedY - 16.0,
                500.0 * (convertedX - convertedY),
                200.0 * (convertedY - convertedZ)
        };
    }

    /**
     * sRGB 채널을 선형 RGB 채널로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param channel 0부터 1 사이의 sRGB 채널값
     * @return 선형 RGB 채널값
     */
    private double convertSrgbChannel(double channel) {
        // 낮은 sRGB 구간은 표준 선형 변환식을 사용한다
        if (channel <= 0.04045) {
            // 선형 변환된 낮은 sRGB 채널값을 반환한다
            return channel / 12.92;
        }

        // 감마가 적용된 sRGB 채널을 선형 채널값으로 반환한다
        return Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    /**
     * XYZ 채널을 CIELAB 계산에 사용할 비선형 채널로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param channel 기준 백색으로 정규화한 XYZ 채널값
     * @return CIELAB 계산용 채널값
     */
    private double convertXyzChannel(double channel) {
        // CIELAB 표준 임계값을 넘는 XYZ 채널은 세제곱근 변환을 사용한다
        if (channel > 0.008856) {
            // 세제곱근으로 변환한 XYZ 채널값을 반환한다
            return Math.cbrt(channel);
        }

        // 낮은 XYZ 채널은 표준 선형 보정값으로 반환한다
        return 7.787 * channel + 16.0 / 116.0;
    }

    /**
     * 두 CIELAB 색상의 유클리드 거리 제곱을 계산한다
     *
     * @author SeungHyeon.Kang
     * @param firstLab 첫 번째 CIELAB 색상
     * @param secondLab 두 번째 CIELAB 색상
     * @return CIELAB 축별 차이 제곱의 합
     */
    private double getLabDistanceSquared(double[] firstLab, double[] secondLab) {
        double lightnessDifference = firstLab[0] - secondLab[0];
        double greenRedDifference = firstLab[1] - secondLab[1];
        double blueYellowDifference = firstLab[2] - secondLab[2];

        // 제곱근 계산을 생략한 상대 색상 거리를 반환한다
        return lightnessDifference * lightnessDifference
                + greenRedDifference * greenRedDifference
                + blueYellowDifference * blueYellowDifference;
    }

    /**
     * 선택된 공통코드를 등록 화면에서 사용할 색상 응답으로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param colorCode 선택된 BOOK_COLR 세부코드
     * @return 책장 색상 코드와 HEX 색상을 담은 응답 DTO
     */
    private BookCoverColorResponseDto createColorResponse(CodeDto colorCode) {
        // 선택된 책장 색상 코드와 HEX 색상을 응답 DTO로 반환한다
        return new BookCoverColorResponseDto(colorCode.getComdCode(), colorCode.getComdName());
    }
}
