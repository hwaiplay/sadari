package org.our.sadari.social.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
            // 댓글 본문 제거와 발신자 없는 기존 알림의 원본 댓글 검증 조건 확인
            if (method.equals("delBlockReplies")) {
                assertTrue(sql.contains("R.REPL_CNTN = NULL"));
                assertTrue(sql.contains("P.USER_NUMB = ?"));
            }
            if (method.equals("delBlockReactionAlims")) {
                assertFalse(sql.contains("SEND_NUMB"));
                assertTrue(sql.contains("AND R.USER_NUMB = ?"));
                assertTrue(sql.contains("A.TEMP_CODE IN (?, ?, ?, ?)"));
            }
        }
    }
}
