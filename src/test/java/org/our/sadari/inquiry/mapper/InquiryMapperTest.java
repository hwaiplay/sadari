package org.our.sadari.inquiry.mapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/**
 * fileName       : InquiryMapperTest
 * author         : HanWon.Jang
 * date           : 2026-08-26
 * description    : 고객문의 목록 Mapper의 미확인 답변 집계 SQL 구조를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-26        HanWon.Jang         최초 생성
 */
class InquiryMapperTest {

    /**
     * 문의별 미확인 답변 수가 행별 하위 조회 없이 조인 집계되는지 검증함
     *
     * @author HanWon.Jang
     * @throws IOException 운영 Mapper XML을 읽지 못한 경우
     */
    @Test
    void getInquiryJoinUnreadCount() throws IOException {

        // 문의 목록의 회원과 페이징 조건을 운영 Mapper에 전달함
        Map<String, Object> parameters = Map.of(
                "userNumb", 31L
              , "startRow", 0
              , "pageSize", 10
        );
        // 운영 문의 목록 구문으로 실제 실행 SQL을 생성함
        MappedStatement statement = loadConfiguration().getMappedStatement(
                "org.our.sadari.inquiry.mapper.InquiryMapper.getInquiryList"
        );
        BoundSql boundSql = statement.getBoundSql(parameters);
        // 공백 차이에 영향받지 않도록 SQL을 한 줄로 정규화함
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 사용자의 문의와 미확인 답변을 한 번 조인하는지 확인함
        assertTrue(sql.contains("LEFT JOIN CT_INQANS A ON A.INQR_NUMB = I.INQR_NUMB"));
        // 답변 기본키를 집계해 답변이 없는 문의도 0건으로 유지하는지 확인함
        assertTrue(sql.contains("COUNT(A.ANSW_NUMB) AS UNREAD_COUNT"));
        // 문의마다 별도 건수 조회를 실행하는 스칼라 하위 조회가 제거됐는지 확인함
        assertFalse(sql.contains("(SELECT COUNT"));
    }

    /**
     * 운영 고객문의 Mapper XML을 해석한 MyBatis 설정을 생성함
     *
     * @author HanWon.Jang
     * @return 고객문의 Mapper 구문이 등록된 MyBatis 설정
     * @throws IOException 운영 Mapper XML을 읽지 못한 경우
     */
    private Configuration loadConfiguration() throws IOException {

        // 고객문의 Mapper XML을 등록할 빈 MyBatis 설정을 생성함
        Configuration configuration = new Configuration();
        // 운영과 동일한 고객문의 Mapper XML을 자동으로 닫히는 입력 스트림으로 읽음
        try (InputStream mapperStream = Resources.getResourceAsStream(
                "org/our/sadari/inquiry/mapper/InquiryMapper.xml"
        )) {
            // 문의 목록 구문을 테스트 설정에 등록할 XML 파서를 생성함
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                    mapperStream, configuration, "InquiryMapper.xml", configuration.getSqlFragments()
            );
            // 운영 Mapper XML 전체를 파싱하여 실행 SQL을 생성할 수 있게 함
            mapperBuilder.parse();
        }

        // 고객문의 Mapper 구문이 등록된 설정을 반환함
        return configuration;
    }
}
