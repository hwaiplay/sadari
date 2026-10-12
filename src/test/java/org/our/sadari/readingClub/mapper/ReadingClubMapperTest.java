package org.our.sadari.readingClub.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.our.sadari.readingClub.dto.ReadingClubDto;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * fileName       : ReadingClubMapperTest
 * author         : SeungHyeon.Kang
 * date           : 2026-10-12
 * description    : 모임 카테고리 SQL 바인딩과 응답 계약 검증
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-10-12        SeungHyeon.Kang        최초 생성
 */
class ReadingClubMapperTest {

    /**
     * 단일·복수 모임 번호가 SQL에 안전하게 바인딩되고 언어와 노출 순서가 유지되는지 검증
     *
     * @author SeungHyeon.Kang
     * @param english 영어 조회 여부
     * @throws IOException 운영 Mapper XML 읽기 실패
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void categorySqlBindsClubs(boolean english) throws IOException {

        // 테스트 전 요청 언어 보존
        Locale previousLocale = LocaleContextHolder.getLocale();
        // 요청 언어별 카테고리 이름 조회 바인딩 검증
        try {
            // 한국어와 영어 요청 언어 설정
            LocaleContextHolder.setLocale(english ? Locale.ENGLISH : Locale.KOREAN);
            // 운영 Mapper XML을 해석할 MyBatis 설정 생성
            Configuration configuration = new Configuration();
            // 자동 정리할 운영 Mapper XML 입력 스트림 생성
            try (InputStream stream = Resources.getResourceAsStream(
                    "org/our/sadari/readingClub/mapper/ReadingClubMapper.xml")) {
                // 운영 모임 Mapper 전체를 해석할 XML 파서 생성
                XMLMapperBuilder builder = new XMLMapperBuilder(stream, configuration, "ReadingClubMapper.xml", configuration.getSqlFragments());
                // 동적 SQL과 Java Mapper 계약 해석
                builder.parse();
            }

            // 상세 화면 단일 모임과 목록 복수 모임의 실제 SQL 바인딩 검증
            for (List<Long> clubNumbList : List.of(List.of(10L), List.of(10L, 20L))) {
                // 모임 목록 파라미터로 실제 실행 SQL 생성
                BoundSql boundSql = configuration.getMappedStatement(
                        "org.our.sadari.readingClub.mapper.ReadingClubMapper.getClubCategoryList")
                        .getBoundSql(Map.of("clubNumbList", clubNumbList));
                // SQL 공백을 정규화하여 조회 조건과 순서 검증
                String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
                // 일괄 조회 결과가 원래 모임에 연결될 식별 컬럼 확인
                assertTrue(sql.contains("CATEGORY.CLUB_NUMB"));
                // 바인딩 파라미터만 사용하는 모임 번호 IN 조건 확인
                assertTrue(sql.contains("WHERE CATEGORY.CLUB_NUMB IN"));
                // 모임별 원래 카테고리 노출 순서 확인
                assertTrue(sql.endsWith("ORDER BY CATEGORY.CLUB_NUMB , CATEGORY.SORT_ORDR"));
                // SQL에 실제로 바인딩될 모임 번호만 수집할 목록 생성
                List<Long> boundClubs = new ArrayList<>();
                // MyBatis가 생성한 foreach 파라미터의 실제 값 검증
                for (ParameterMapping mapping : boundSql.getParameterMappings()) {
                    // 모임 번호 바인딩만 확인하여 코드명 조회 파라미터와 구분
                    if (mapping.getProperty().startsWith("__frch_clubNumb_")) {
                        // 실제 PreparedStatement에 전달할 모임 번호 수집
                        boundClubs.add((Long) boundSql.getAdditionalParameter(mapping.getProperty()));
                    }

                }

                // 요청한 모임 번호의 순서와 바인딩 개수 확인
                assertEquals(clubNumbList, boundClubs);
                // 한국어·영어 카테고리 표시명에 적용할 언어 바인딩 확인
                assertEquals(english ? "en" : "ko", boundSql.getAdditionalParameter("languageCode"));
            }

        }

        // 테스트 종료 후 기존 요청 언어 복원
        finally {
            // 다른 테스트의 메시지 언어에 미치는 영향 차단
            LocaleContextHolder.setLocale(previousLocale);
        }
    }

    /**
     * 일괄 조회 내부 모임 번호가 기존 카테고리 응답에 추가되지 않는지 검증
     *
     * @author SeungHyeon.Kang
     * @throws IOException 카테고리 JSON 직렬화 실패
     */
    @Test
    void categoryHidesClubNumber() throws IOException {

        // 일괄 조회 내부 식별값을 포함한 카테고리 생성
        ReadingClubDto.CategoryDto category = new ReadingClubDto.CategoryDto();
        // 목록 결합에 사용하는 내부 모임 번호 설정
        category.setClubNumb(10L);
        // 기존 응답의 관심분야 코드 설정
        category.setIntrCode("NOVEL");
        // 응답 어노테이션을 검증할 JSON 직렬화 객체 생성
        ObjectMapper objectMapper = new ObjectMapper();
        // 실제 카테고리 응답 JSON 생성
        String json = objectMapper.writeValueAsString(category);
        // 내부 모임 번호가 응답 계약에 추가되지 않는지 확인
        assertFalse(objectMapper.readTree(json).has("clubNumb"));
        // 기존 카테고리 코드 응답 유지 확인
        assertEquals("NOVEL", objectMapper.readTree(json).get("intrCode").asText());
    }
}
