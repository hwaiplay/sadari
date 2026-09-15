package org.our.sadari.social.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.social.dto.UserBlockDto;

/**
 * fileName       : UserBlockMapperTest
 * author         : HanWon.Jang
 * date           : 2026-09-14
 * description    : 차단 반응 삭제 SQL의 개인 영역 경계를 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-14        HanWon.Jang        최초 생성
 */
class UserBlockMapperTest {

    /**
     * 실제 Mapper 파싱과 사진 대상 및 공동 모임 보존 조건 검증
     *
     * @author HanWon.Jang
     * @throws IOException Mapper 리소스 조회 실패
     */
    @Test
    void delReactionKeepsClubScope() throws IOException {
        // 운영 Mapper를 사용하여 누락된 include와 OGNL 상수 참조 확인
        Configuration configuration = new Configuration();
        String resource = "org/our/sadari/social/mapper/UserBlockMapper.xml";
        try (InputStream stream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        }
        // 운영 사용자 대신 독립된 테스트 식별값 사용
        UserBlockDto request = new UserBlockDto();
        request.setUserNumb(31L);
        request.setBlocNumb(41L);
        // 세 삭제 경로 모두 개인 사진을 포함하면서 비공개 공동 모임을 보존하는지 확인
        for (String method : List.of("delBlockLikes", "delBlockReplies", "delBlockReactionAlims")) {
            BoundSql bound = configuration.getMappedStatement(
                    "org.our.sadari.social.mapper.UserBlockMapper." + method).getBoundSql(request);
            String sql = bound.getSql().replaceAll("\\s+", " ");
            assertTrue(sql.contains("TB_CLPART PART"));
            assertTrue(sql.contains("REPORT.PUBC_YSNO = ?"));
            assertEquals(Constant.LIKE_TARGET_PROFILE_IMAGE, bound.getAdditionalParameter("targetProfile"));
            assertEquals(Constant.LIKE_TARGET_BACKGROUND_IMAGE, bound.getAdditionalParameter("targetBackground"));
            // 댓글 본문 제거와 발신자 기반 개인 알림 삭제 조건 확인
            if (method.equals("delBlockReplies")) {
                assertTrue(sql.contains("R.REPL_CNTN = NULL"));
                assertTrue(sql.contains("P.USER_NUMB = ?"));
            }
            if (method.equals("delBlockReactionAlims")) {
                assertEquals(2, sql.split("A.USER_NUMB = \\?", -1).length - 1);
                assertEquals(2, sql.split("A.SEND_NUMB = \\?", -1).length - 1);
                assertTrue(sql.contains("A.SEND_NUMB IS NULL AND R.USER_NUMB = ?"));
                assertTrue(sql.contains("A.TEMP_CODE IN (?, ?, ?, ?)"));
            }
        }
        // 차단과 반응 요청이 같은 사용자 번호순 잠금을 공유하는지 확인
        BoundSql lockSql = configuration.getMappedStatement(
                "org.our.sadari.social.mapper.UserBlockMapper.lockUsers")
                .getBoundSql(Map.of("firstUserNumb", 31L, "secondUserNumb", 41L));
        String normalizedLockSql = lockSql.getSql().replaceAll("\\s+", " ");
        assertTrue(normalizedLockSql.contains("ORDER BY USER_NUMB FOR UPDATE"));
        // 동일 차단 재요청이 중복 행 없이 멱등 처리되는지 확인
        BoundSql setBlockSql = configuration.getMappedStatement(
                "org.our.sadari.social.mapper.UserBlockMapper.setBlock").getBoundSql(request);
        assertTrue(setBlockSql.getSql().replaceAll("\\s+", " ").contains("ON DUPLICATE KEY UPDATE"));
    }
}
