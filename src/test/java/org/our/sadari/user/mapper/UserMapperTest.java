package org.our.sadari.user.mapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.our.sadari.global.common.constant.Constant;
import org.our.sadari.user.dto.UserDto;

/**
 * fileName       : UserMapperTest
 * author         : HanWon.Jang
 * date           : 2026-08-25
 * description    : 사용자 프로필 변경 Mapper의 동적 SQL 조건을 검증함
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-25        HanWon.Jang        최초 생성
 */
class UserMapperTest {

    /**
     * 배경사진 변경 피드 대상이면 문자열 Y 조건으로 변경일시 SQL을 생성하는지 검증함
     *
     * @author HanWon.Jang
     * @throws IOException 운영 Mapper XML을 읽지 못할 때 발생
     */
    @Test
    void uptProfileAddsBgimDate() throws IOException {
        // 배경사진 변경 동적 SQL을 생성할 요청 DTO를 생성함
        UserDto request = new UserDto();
        // 수정 대상 사용자 번호를 설정함
        request.setUserNumb(31L);
        // 필수 프로필 수정값인 닉네임을 설정함
        request.setUserNick("독서가");
        // 새 배경사진 파일 번호를 설정함
        request.setBgimNumb(41L);
        // 사용자가 직접 변경한 사진을 피드 대상으로 기록하도록 설정함
        request.setImageFeedYsno(Constant.COMM_YES);

        // 운영 사용자 Mapper의 프로필 수정 구문을 조회함
        MappedStatement statement = loadConfiguration().getMappedStatement(
                "org.our.sadari.user.mapper.UserMapper.uptUserProfile"
        );
        // 문자열 Y 조건을 평가하여 실제 실행 SQL을 생성함
        BoundSql boundSql = statement.getBoundSql(request);
        // 공백 차이에 영향받지 않도록 SQL을 한 줄로 정규화함
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 배경사진 번호와 변경일시는 포함하고 미변경 프로필 사진 일시는 제외하는지 확인함
        assertTrue(sql.contains("BGIM_NUMB = ?"));
        // 직접 변경한 배경사진의 피드 기준 일시가 함께 기록되는지 확인함
        assertTrue(sql.contains("BGIM_DATE = CURRENT_TIMESTAMP(6)"));
        // 프로필 사진 번호가 없으면 프로필 사진 변경일시를 수정하지 않는지 확인함
        assertFalse(sql.contains("PROF_DATE = CURRENT_TIMESTAMP(6)"));
    }

    /**
     * 내부 프로필 저장에서는 사진 번호만 변경하고 피드 변경일시는 생성하지 않는지 검증함
     *
     * @author HanWon.Jang
     * @throws IOException 운영 Mapper XML을 읽지 못할 때 발생
     */
    @Test
    void uptProfileSkipsFeedDate() throws IOException {
        // 피드에 기록하지 않을 배경사진 수정 요청 DTO를 생성함
        UserDto request = new UserDto();
        // 수정 대상 사용자 번호를 설정함
        request.setUserNumb(31L);
        // 필수 프로필 수정값인 닉네임을 설정함
        request.setUserNick("독서가");
        // 내부 저장 대상 배경사진 파일 번호를 설정함
        request.setBgimNumb(41L);
        // 사진 변경일시를 피드 기준으로 사용하지 않도록 설정함
        request.setImageFeedYsno(Constant.COMM_NO);

        // 운영 사용자 Mapper의 프로필 수정 구문을 조회함
        MappedStatement statement = loadConfiguration().getMappedStatement(
                "org.our.sadari.user.mapper.UserMapper.uptUserProfile"
        );
        // 문자열 N 조건을 평가하여 실제 실행 SQL을 생성함
        BoundSql boundSql = statement.getBoundSql(request);
        // 공백 차이에 영향받지 않도록 SQL을 한 줄로 정규화함
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 배경사진 번호는 수정 대상 SQL에 포함되는지 확인함
        assertTrue(sql.contains("BGIM_NUMB = ?"));
        // 피드 기록 대상이 아니면 배경사진 변경일시를 수정하지 않는지 확인함
        assertFalse(sql.contains("BGIM_DATE = CURRENT_TIMESTAMP(6)"));
    }

    /**
     * 운영 사용자 Mapper XML을 해석한 MyBatis 설정을 생성함
     *
     * @author HanWon.Jang
     * @return 사용자 Mapper 구문이 등록된 MyBatis 설정
     * @throws IOException 운영 Mapper XML을 읽지 못할 때 발생
     */
    private Configuration loadConfiguration() throws IOException {
        // 사용자 Mapper XML을 등록할 빈 MyBatis 설정을 생성함
        Configuration configuration = new Configuration();
        // 운영과 동일한 사용자 Mapper XML을 자동으로 닫히는 입력 스트림으로 읽음
        try (InputStream mapperStream = Resources.getResourceAsStream(
                "org/our/sadari/user/mapper/UserMapper.xml"
        )) {
            // 사용자 프로필 수정 구문을 테스트 설정에 등록할 XML 파서를 생성함
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                    mapperStream, configuration, "UserMapper.xml", configuration.getSqlFragments()
            );
            // 운영 Mapper XML 전체를 파싱하여 동적 SQL을 사용할 수 있게 함
            mapperBuilder.parse();
        }

        // 사용자 Mapper 구문이 등록된 설정을 반환함
        return configuration;
    }
}
