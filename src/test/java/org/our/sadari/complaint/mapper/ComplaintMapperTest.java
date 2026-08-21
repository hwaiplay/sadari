package org.our.sadari.complaint.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.our.sadari.complaint.dto.ComplaintDto;

/**
 * fileName       : ComplaintMapperTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상 원문 조회와 스냅샷 저장 Mapper SQL을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
class ComplaintMapperTest {

    /** 신고 접수 SQL이 서버 조회 원문 스냅샷 컬럼을 저장하는지 확인한다. */
    @Test
    void setComplaintIncludesTargetSnapshot() throws IOException {
        // 운영과 동일한 Mapper XML을 해석할 MyBatis 설정을 생성한다
        Configuration configuration = loadConfiguration();
        // 신고 저장 구문의 파라미터를 생성한다
        ComplaintDto complaint = new ComplaintDto();
        complaint.setTagtType("CMPL_BOOK_REPORT");
        complaint.setTagtNumb(31L);
        complaint.setTagtUser(22L);
        complaint.setTagtCntn("서버 원본 독후감");
        complaint.setCmplRson("CMPL_ABUSE");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("complaint", complaint);
        parameters.put("userNumb", 7L);
        // 운영 신고 저장 구문으로 실제 실행 SQL을 생성한다
        MappedStatement statement = configuration.getMappedStatement(
                "org.our.sadari.complaint.mapper.ComplaintMapper.setComplaint"
        );
        BoundSql boundSql = statement.getBoundSql(parameters);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 신고 대상 소유자와 내용 스냅샷 및 접수 상태가 저장 SQL에 포함되는지 확인한다
        assertTrue(sql.contains("TAGT_USER"));
        assertTrue(sql.contains("TAGT_CNTN"));
        assertTrue(sql.contains("'CMPL_RECEIVED'"));
    }

    /** 독후감 원문 조회 SQL이 공개된 다른 활성 사용자의 대상만 허용하는지 확인한다. */
    @Test
    void getReportTargetDtlChecksReportableTarget() throws IOException {
        // 운영과 동일한 Mapper XML을 해석할 MyBatis 설정을 생성한다
        Configuration configuration = loadConfiguration();
        // 독후감 원문 조회 구문의 파라미터를 생성한다
        Map<String, Object> parameters = Map.of("tagtNumb", 31L, "userNumb", 7L);
        // 운영 독후감 원문 조회 구문으로 실제 실행 SQL을 생성한다
        MappedStatement statement = configuration.getMappedStatement(
                "org.our.sadari.complaint.mapper.ComplaintMapper.getReportTargetDtl"
        );
        BoundSql boundSql = statement.getBoundSql(parameters);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 공개 여부와 본인 소유 차단 및 활성 작성자 조건이 유지되는지 확인한다
        assertTrue(sql.contains("R.PUBC_YSNO = 'Y'"));
        assertTrue(sql.contains("R.USER_NUMB != ?"));
        assertTrue(sql.contains("U.USER_STAT = 'ACTIVE'"));
        assertTrue(sql.contains("R.USER_NUMB AS TAGT_USER"));
    }

    /** 중복 신고 조회 SQL이 사용자와 대상 유형 및 대상 번호를 모두 비교하는지 확인한다. */
    @Test
    void dupComplaintUsesTargetKey() throws IOException {
        // 운영과 동일한 Mapper XML을 해석할 MyBatis 설정을 생성한다
        Configuration configuration = loadConfiguration();
        // 중복 신고 조회 구문의 파라미터를 생성한다
        Map<String, Object> parameters = Map.of(
                "userNumb", 7L,
                "tagtType", "CMPL_BOOK_REPORT",
                "tagtNumb", 31L
        );
        // 운영 중복 신고 조회 구문으로 실제 실행 SQL을 생성한다
        MappedStatement statement = configuration.getMappedStatement(
                "org.our.sadari.complaint.mapper.ComplaintMapper.dupComplaint"
        );
        BoundSql boundSql = statement.getBoundSql(parameters);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 처리 상태 조건 없이 사용자와 대상 복합 식별값 전체를 비교하는지 확인한다
        assertTrue(sql.contains("USER_NUMB = ?"));
        assertTrue(sql.contains("TAGT_TYPE = ?"));
        assertTrue(sql.contains("TAGT_NUMB = ?"));
    }

    /** 운영 신고 Mapper XML을 해석한 MyBatis 설정을 생성한다. */
    private Configuration loadConfiguration() throws IOException {
        // 신고 Mapper XML을 등록할 빈 MyBatis 설정을 생성한다
        Configuration configuration = new Configuration();
        // 운영과 동일한 신고 Mapper XML을 읽는다
        try (InputStream mapperStream = Resources.getResourceAsStream(
                "org/our/sadari/complaint/mapper/ComplaintMapper.xml"
        )) {
            // 신고 원문 조회와 저장 구문을 테스트 설정에 등록한다
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                    mapperStream, configuration, "ComplaintMapper.xml", configuration.getSqlFragments()
            );
            mapperBuilder.parse();
        }
        // 운영 신고 Mapper 구문이 등록된 설정을 반환한다
        return configuration;
    }
}
