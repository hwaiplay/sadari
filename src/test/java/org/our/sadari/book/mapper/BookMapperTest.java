package org.our.sadari.book.mapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * fileName       : BookMapperTest
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 도서 목록 Mapper의 집계 조인 SQL 구조를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang         최초 생성
 */
class BookMapperTest {

    /**
     * 인기 도서 평점이 행별 하위 조회 없이 상위 도서 집합과 조인되어 집계되는지 검증한다.
     *
     * @author HanWon.Jang
     * @throws IOException 운영 Mapper XML을 읽지 못한 경우
     */
    @Test
    void getPopularBookJoinRating() throws IOException {

        // 인기 도서 조회 기간을 운영 Mapper에 전달한다
        Map<String, Object> parameters = Map.of(
                "periodStart", LocalDateTime.of(2026, 8, 1, 0, 0)
              , "nextPeriodStart", LocalDateTime.of(2026, 9, 1, 0, 0)
        );
        // 운영 인기 도서 구문으로 실제 실행 SQL을 생성한다
        MappedStatement statement = loadConfiguration().getMappedStatement(
                "org.our.sadari.book.mapper.BookMapper.getPopularBookList"
        );
        BoundSql boundSql = statement.getBoundSql(parameters);
        // 공백 차이에 영향받지 않도록 SQL을 한 줄로 정규화한다
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 상위 도서 집합에 평점 행을 한 번 조인하는지 확인한다
        assertTrue(sql.contains("LEFT JOIN TM_REPORT G ON G.BOOK_NUMB = P.BOOK_NUMB"));
        // 조인된 평점 행을 도서별 평균으로 집계하는지 확인한다
        assertTrue(sql.contains("ROUND(AVG(CAST(G.REPT_GRDE AS UNSIGNED)), 1)"));
        // 도서마다 별도 평균 조회를 실행하는 스칼라 하위 조회가 제거됐는지 확인한다
        assertFalse(sql.contains("( SELECT ROUND(AVG"));
    }

    /**
     * 운영 도서 Mapper XML을 해석한 MyBatis 설정을 생성한다.
     *
     * @author HanWon.Jang
     * @return 도서 Mapper 구문이 등록된 MyBatis 설정
     * @throws IOException 운영 Mapper XML을 읽지 못한 경우
     */
    private Configuration loadConfiguration() throws IOException {

        // 도서 Mapper XML을 등록할 빈 MyBatis 설정을 생성한다
        Configuration configuration = new Configuration();
        // 운영과 동일한 도서 Mapper XML을 자동으로 닫히는 입력 스트림으로 읽는다
        try (InputStream mapperStream = Resources.getResourceAsStream(
                "org/our/sadari/book/mapper/BookMapper.xml"
        )) {
            // 인기 도서 조회 구문을 테스트 설정에 등록할 XML 파서를 생성한다
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                    mapperStream, configuration, "BookMapper.xml", configuration.getSqlFragments()
            );
            // 운영 Mapper XML 전체를 파싱하여 실행 SQL을 생성할 수 있게 한다
            mapperBuilder.parse();
        }

        // 도서 Mapper 구문이 등록된 설정을 반환한다
        return configuration;
    }
}
