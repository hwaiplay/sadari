---
apply: always
---

# SQL Rules

이 문서는 Sadari 프로젝트의 Oracle 데이터베이스, SQL 및 MyBatis XML 규칙을 정의합니다.
DDL, DML, Mapper XML을 생성하거나 수정할 때 아래 규칙을 예외 없이 적용합니다.
규칙은 주석 공통 정책, SQL 보안, 스키마 무결성, 쿼리 정확성, 성능과 정렬, 품질 검증, 형상 관리 순으로 중요도가 높은 항목부터 나열합니다.

## 빠른 탐색

1. [공통 주석 작성 규칙](#1-공통-주석-작성-규칙)
2. [SQL 보안 및 안전 규칙](#2-sql-보안-및-안전-규칙)
3. [테이블 명명 규칙](#3-테이블-명명-규칙)
4. [컬럼 명명 규칙](#4-컬럼-명명-규칙)
5. [SQL 정렬 규칙](#5-sql-정렬-규칙)
6. [JOIN 및 조건 규칙](#6-join-및-조건-규칙)
7. [MyBatis XML 규칙](#7-mybatis-xml-규칙)
8. [DML 정렬 규칙](#8-dml-정렬-규칙)
9. [MERGE 문 정렬 규칙](#9-merge-문-정렬-규칙)
10. [CASE WHEN THEN 문 정렬 규칙](#10-case-when-then-문-정렬-규칙)
11. [인라인 뷰 및 서브쿼리 정렬 규칙](#11-인라인-뷰-및-서브쿼리-정렬-규칙)
12. [스칼라 서브쿼리 금지](#12-스칼라-서브쿼리-금지)
13. [SQL 로직 주석](#13-sql-로직-주석)
14. [운영 배포 SQL 동기화](#14-운영-배포-sql-동기화)
15. [품질 규칙](#15-품질-규칙)
16. [Git 규칙](#16-git-규칙)

## 1. 공통 주석 작성 규칙

### 1.1 문체

- SQL 주석과 XML 주석은 `한다`, `이다`, `된다` 형태의 반말 서술체로 작성합니다.
- DDL의 테이블 및 컬럼 `COMMENT`는 `이다`, `다` 등의 종결어미와 마침표를 사용하지 않고 의미가 명확한 명사형으로 작성합니다.
- 주석에 `합니다`, `입니다`, `됩니다` 형태의 존댓말을 사용하지 않습니다.
- 주석에 이모티콘과 이모지를 사용하지 않습니다.
- 주석에 느낌표와 물음표를 사용하지 않습니다.

DDL `COMMENT` 예시:

```sql
COMMENT ON TABLE TM_FILEXM IS '업로드 파일 메타 정보 관리 마스터 테이블';

COMMENT ON COLUMN TM_FILEXM.FILE_NUMB IS '파일 번호';
COMMENT ON COLUMN TM_FILEXM.ORIG_NAME IS '업로드 당시 원본 파일명';
```

### 1.2 내용

- 사용자에게 노출되는 실제 문구를 인용한 주석은 접두사 없이 원문만 작성하며 문구 안의 문체와 문장부호는 예외로 둡니다.
- SQL 문장을 그대로 읽는 주석은 작성하지 않고 조건의 이유와 데이터 정책을 설명합니다.

## 2. SQL 보안 및 안전 규칙

### 2.1 조회 컬럼과 별칭

- `SELECT *`를 사용하지 않고 필요한 컬럼을 모두 명시합니다.
- 테이블과 인라인 뷰 별칭은 역할을 알아볼 수 있는 영문 대문자 한 단어로 작성합니다.
- 테이블과 인라인 뷰 별칭에는 언더바를 사용하지 않습니다.
- Oracle은 테이블 별칭 앞의 `AS`를 지원하지 않으므로 테이블과 인라인 뷰 별칭에는 `AS`를 작성하지 않습니다.
- SELECT 컬럼과 계산식의 별칭에는 `AS`를 반드시 작성하여 테이블 별칭과 명확히 구분합니다.
- 같은 쿼리 안에서 사용하는 테이블 별칭은 서로 중복되지 않아야 합니다.

### 2.2 파라미터 바인딩

- 사용자 입력값은 반드시 `#{param}`으로 바인딩합니다.
- Oracle 조회 건수 제한에 바인딩 변수를 사용할 때는 `FETCH FIRST #{maxSize} ROWS ONLY`를 사용하지 않습니다.
- 정렬된 결과의 조회 건수를 동적으로 제한할 때는 정렬 쿼리를 인라인 뷰로 감싸고 외부 WHERE 절에서 `ROWNUM <= #{maxSize}`를 적용합니다.
- `${param}`은 테이블명, 컬럼명, 정렬 방향처럼 바인딩할 수 없는 경우에만 사용합니다.
- `${param}`을 사용하기 전에 Java 계층에서 허용 목록을 검증합니다.

### 2.3 코드와 Null 처리

- SQL에 업무 코드, Y/N 값, 검색 문자열을 직접 하드코딩하지 않습니다.
- 동적 값과 문자열 결합은 MyBatis `<bind>`로 정의합니다.
- 동일한 `<bind>`가 여러 쿼리에서 반복되면 `<sql>`과 `<include>`로 공통화합니다.
- Null 가능 값은 조건과 정렬 의도에 따라 `NVL` 또는 `COALESCE`로 명확히 처리합니다.

### 2.4 본인용과 공개용 공용 조회

- 본인 화면과 다른 사용자 화면이 같은 조회 SQL을 재사용하면 공개 여부를 선택 파라미터로 전달합니다.
- 공개 여부 파라미터가 비어 있으면 본인 조회 범위를 유지하고, 값이 있으면 원본 데이터 조회 단계에서 `#{}` 바인딩 조건으로 제한합니다.
- 하나의 응답을 구성하는 집계 SQL과 목록 SQL에는 같은 공개 여부 조건을 적용하여 집계값과 상세 목록의 범위가 달라지지 않게 합니다.
- 독서 목표처럼 공개가 허용된 별도 데이터는 독후감 공개 여부 조건으로 제외하지 않고, 공개 독후감만 독서량과 목표 달성 집계에 반영합니다.

## 3. 테이블 명명 규칙

### 3.1 테이블 접두사

테이블 성격에 따라 다음 접두사를 사용합니다.

- 사용자에게 노출되는 운영 콘텐츠 테이블: `CT_`
- 마스터 테이블: `TM_`
- 파생 테이블: `TB_`
- 히스토리 테이블: `TH_`
- 로그 테이블: `TL_`

### 3.2 물리 삭제와 이력 외래키

- 부모 데이터를 물리 삭제한 뒤에도 당시 내부 식별번호를 보존해야 하는 이력 테이블에는 해당 부모 테이블 FK를 생성하지 않습니다.
- 부모 FK의 `ON DELETE SET NULL`로 이력 식별번호가 소실되지 않도록 보존 목적과 삭제 정책을 함께 확인합니다.
- 이력 테이블에 부모 FK를 두지 않는 경우 원본 업무 데이터 등록 시점에 식별번호를 필수로 저장하고 이후 변경하지 않습니다.
- 부모 데이터를 물리 삭제하는 Service는 같은 트랜잭션에서 외래키 참조 자식을 먼저 삭제하고 부모를 마지막에 삭제합니다.
- 자기 참조 외래키가 있는 테이블은 가장 하위 자식부터 삭제하며, 공용 대상 번호처럼 물리 외래키가 없는 연관 데이터도 업무 대상 유형과 번호로 명시적으로 정리합니다.

### 3.3 테이블 본문 길이

접두사 뒤에는 영문 대문자 6자리를 사용합니다. 의미 글자가 6자리보다 짧으면 오른쪽을 `X`로 채웁니다.

- 허용: `CT_POPUPX`, `TM_USERXM`, `TM_ADMINX`
- 금지: `TM_USERS`, `TM_USER_INFO`

## 4. 컬럼 명명 규칙

### 4.1 기본 형식

- 컬럼은 `[영문 대문자 4자]_[영문 대문자 4자]` 형식으로 작성합니다.
- 각 구간이 4자리보다 짧으면 오른쪽을 `X`로 채웁니다.
- 지정된 단어를 임의로 다시 축약하지 않습니다.

### 4.2 고정 단어

고정 단어:

- 사용자: `USER`
- 번호: `NUMB`
- 사용 여부: `USEE`
- 여부: `YSNO`
- 등록: `REGI`
- 수정: `UPDT`
- 관리자: `ADMN`
- 아이디: `IDXX`
- 정렬: `ORDR`

고정 예시:

- `USER_IDXX`
- `MENU_NMSX`
- `USEE_YSNO`

### 4.3 관리 컬럼

수정 가능한 관리자 테이블에는 다음 관리 컬럼을 포함합니다.

- `REGI_ADMN`: 등록자 아이디
- `REGI_DATE`: 등록일시
- `UPDT_ADMN`: 수정자 아이디
- `UPDT_DATE`: 수정일시
- `DPLY_ADMN`: 배포자 아이디
- `DPLY_DATE`: 배포일시

관리 컬럼의 물리 배치와 조회 배치는 반드시 `REGI_ADMN`, `REGI_DATE`, `UPDT_ADMN`, `UPDT_DATE`, `DPLY_ADMN`, `DPLY_DATE` 순서를 사용합니다.

수정할 수 없는 이력성 테이블에는 해당 관리 컬럼을 강제하지 않습니다.

## 5. SQL 정렬 규칙

### 5.1 주요 절과 컬럼

- 주요 절의 키워드 오른쪽 끝과 데이터 시작 위치를 세로로 맞춥니다.
- SELECT 절은 한 줄에 한 컬럼만 작성합니다.
- 컬럼 구분자는 선행 콤마로 작성합니다.
- `JOIN`, `ON`, `AND`, `WHERE`, `ORDER BY`에도 같은 우측 정렬 기준을 적용합니다.

### 5.2 SELECT 정렬 예시

```sql
       SELECT /* getMenuList */
              M.MENU_NUMB
            , M.SUBX_NUMB
            , M.MENU_NAME
         FROM TM_MENUXM M
    LEFT JOIN TB_CODEXD R
           ON R.COMM_CODE = #{authCode}
          AND R.COMD_CODE = M.READ_AUTH
        WHERE NVL(M.USEE_YSNO, #{yes}) = #{yes}
          AND (M.READ_AUTH IS NULL OR M.READ_AUTH = #{auth})
     ORDER BY NVL(M.SORT_ORDR, 9999)
```

## 6. JOIN 및 조건 규칙

### 6.1 JOIN 배치

- JOIN 조건은 해당 JOIN 바로 아래에 작성합니다.
- 조건식의 괄호 시작 위치를 다른 데이터 시작 위치와 맞춥니다.

### 6.2 목록과 동적 정렬

- 다건 조회에 전달된 리스트가 비어 있는지 Java 또는 MyBatis 조건문에서 먼저 검사합니다.
- 빈 리스트로 `IN ()`이 생성되지 않도록 방지합니다.
- 정렬 기준을 동적으로 받을 경우 허용된 컬럼과 방향만 사용합니다.

### 6.3 공통코드명 조회

- 업무 테이블의 코드값을 표시명으로 변환할 때는 `TB_CODEXD`를 코드명 조회 목적으로 JOIN하지 않고 `FN_GET_CODE_NAME(공통코드, 세부코드, 옵션코드)`을 사용합니다.
- 계층, 정렬 순서, 사용 여부, 옵션값 등 코드명 이외의 공통코드 속성이 필요하여 `TB_CODEXD`를 JOIN하더라도 SELECT 절의 코드명은 `COMD_NAME`을 직접 반환하지 않고 `FN_GET_CODE_NAME`으로 조회합니다.
- 옵션코드명이 필요하지 않으면 `FN_GET_CODE_NAME`의 세 번째 파라미터에 `NULL`을 전달합니다.
- 공통코드 관리와 공통코드 목록처럼 `TB_CODEXD` 자체가 조회 대상인 쿼리는 코드 상세 데이터를 제공해야 하므로 `COMD_NAME`을 직접 조회할 수 있습니다.

## 7. MyBatis XML 규칙

### 7.1 XML 안전성

- `<`, `>`, `<=`, `>=`를 SQL에서 사용할 때 반드시 `<![CDATA[ ... ]]>`로 감쌉니다.
- MySQL 정규식에서 괄호와 같은 메타 문자를 리터럴로 비교할 때는 SQL 문자열의 백슬래시 재해석을 피하도록 문자 클래스를 우선 사용하고, 백슬래시가 필요하면 SQL과 정규식의 이중 이스케이프 결과를 확인합니다.

### 7.2 쿼리 주석

- 모든 `<select>`, `<insert>`, `<update>`, `<delete>` 바로 위에 쿼리 목적을 설명하는 XML 주석을 작성합니다.
- SELECT, UPDATE, DELETE의 실제 SQL 시작 키워드 뒤에는 Mapper ID를 SQL 주석으로 작성합니다.
- INSERT와 MERGE의 Mapper ID 주석은 각각 8장과 9장의 독립 주석 배치 규칙을 따릅니다.
- `<selectKey>` 내부 SQL에는 Mapper ID 주석을 작성하지 않습니다.

### 7.3 MyBatis SELECT 예시

```xml
<!-- 사용자 식별자로 사용자 상세 정보를 조회한다. -->
<select id="getUserDtl" parameterType="org.our.sadari.user.dto.UserDto" resultType="org.our.sadari.user.dto.UserDto">
    SELECT /* getUserDtl */
           U.USER_IDXX
         , U.USER_NAME
      FROM TM_USERXM U
     WHERE U.USER_IDXX = #{userId}
</select>
```

## 8. DML 정렬 규칙

### 8.1 INSERT 문 정렬

- `INSERT INTO 테이블명 (` 형식으로 테이블명 뒤에 한 칸을 두고 여는 괄호를 작성합니다.
- INSERT 대상 컬럼은 한 줄에 하나씩 작성합니다.
- 첫 번째 컬럼은 여는 괄호 다음 데이터 시작 위치에 맞추고, 두 번째 컬럼부터 선행 콤마를 사용합니다.
- 모든 컬럼명과 선행 콤마의 위치를 각각 세로로 정렬합니다.
- 닫는 괄호와 `VALUES`는 `) VALUES (` 형식으로 같은 줄에 작성합니다.
- 첫 번째 값은 `VALUES (` 뒤에 작성하고, 두 번째 값부터 한 줄에 하나씩 선행 콤마를 사용합니다.
- 컬럼 목록과 VALUES 목록의 각 순서가 일대일로 대응하도록 작성합니다.
- 마지막 닫는 괄호는 컬럼 목록의 닫는 괄호와 같은 위치에 정렬합니다.
- 시퀀스를 사용하는 경우 `<selectKey>`를 INSERT 본문 바로 위에 배치합니다.
- SQL 식별 주석 `/* Mapper ID */`는 `INSERT INTO` 바로 위에 작성합니다.

```xml
<!-- 신규 회원 정보를 등록한다. -->
<insert id="setUser" parameterType="org.our.sadari.user.dto.UserDto">
    <selectKey keyProperty="userNumb" resultType="long" order="BEFORE">
        SELECT TM_USERXM_SEQ.NEXTVAL FROM DUAL
    </selectKey>
    /* setUser */
    INSERT INTO TM_USERXM (
                USER_NUMB
              , PROF_NUMB
              , BGIM_NUMB
              , USER_IDXX
              , USER_NICK
              , JOIN_DATE
    ) VALUES (  #{userNumb}
              , #{profNumb,jdbcType=NUMERIC}
              , #{bgimNumb,jdbcType=NUMERIC}
              , #{userIdxx}
              , #{userNick}
              , SYSDATE
    )
</insert>
```

### 8.2 UPDATE 문 정렬

- SQL 식별 주석은 `UPDATE /* Mapper ID */` 형식으로 UPDATE 키워드 바로 뒤에 작성합니다.
- 수정 대상 테이블은 `UPDATE /* Mapper ID */` 다음 줄에 작성하고 UPDATE 키워드 오른쪽 끝을 기준으로 정렬합니다.
- 수정 대상 테이블에 별칭을 사용하면 대문자 한 단어로 작성하고 언더바를 사용하지 않습니다.
- `SET`과 `WHERE`는 주요 절의 키워드 오른쪽 끝을 기준으로 정렬합니다.
- 첫 번째 수정 컬럼은 `SET` 뒤에 작성합니다.
- 두 번째 수정 컬럼부터 선행 콤마를 사용하고 수정 컬럼의 시작 위치를 세로로 맞춥니다.
- WHERE 절의 두 번째 조건부터 `AND`를 선행하고 조건식 시작 위치를 세로로 맞춥니다.
- 동적 수정 컬럼은 MyBatis `<set>` 내부에서도 첫 번째 컬럼과 선행 콤마 정렬을 유지합니다.
- 수정일시 컬럼이 존재하면 업무 정책상 제외할 이유가 없는 한 같은 UPDATE에서 갱신합니다.

```xml
<!-- 회원 프로필 정보를 수정한다. -->
<update id="uptUserProfile" parameterType="org.our.sadari.user.dto.UserDto">
    UPDATE /* uptUserProfile */
           TM_USERXM
       SET USER_NICK = #{userNick}
         , INTR_CNTN = #{intrCntn,jdbcType=VARCHAR}
     WHERE USER_NUMB = #{userNumb}
</update>
```

### 8.3 DELETE 문 정렬

- SQL 식별 주석은 `DELETE /* Mapper ID */` 형식으로 DELETE 키워드 바로 뒤에 작성합니다.
- `FROM`은 `DELETE` 다음 줄에 작성하고 DELETE 키워드 오른쪽 끝을 기준으로 정렬합니다.
- 삭제 대상 테이블에 별칭을 사용하면 대문자 한 단어로 작성하고 언더바를 사용하지 않습니다.
- `WHERE`는 FROM 절과 같은 주요 절 정렬 기준을 사용합니다.
- WHERE 절의 두 번째 조건부터 `AND`를 선행하고 조건식 시작 위치를 세로로 맞춥니다.
- 전체 삭제가 업무 요구사항으로 명확하지 않으면 WHERE 절 없는 DELETE를 작성하지 않습니다.
- 논리 삭제 테이블은 물리 DELETE 대신 삭제 여부와 수정일시를 갱신하는 UPDATE를 우선 사용합니다.

```xml
<!-- 로그인 사용자의 독후감을 삭제한다. -->
<delete id="delReport" parameterType="org.our.sadari.report.dto.ReportDto">
    DELETE /* delReport */
      FROM TM_REPORT
     WHERE REPT_NUMB = #{reptNumb}
       AND USER_NUMB = #{userNumb}
</delete>
```

## 9. MERGE 문 정렬 규칙

### 9.1 USING 및 ON 절

- SQL 식별 주석 `/* Mapper ID */`는 `MERGE INTO` 바로 위에 작성합니다.
- `MERGE INTO`와 `USING`은 같은 시작 위치에 정렬합니다.
- `USING (` 뒤에는 두 칸을 두고 `SELECT`를 작성합니다.
- USING 인라인 뷰의 두 번째 컬럼부터 선행 콤마를 사용하고 컬럼 시작 위치를 세로로 맞춥니다.
- USING 인라인 뷰의 `FROM`과 닫는 괄호 및 별칭은 내부 SELECT 기준으로 우측 정렬합니다.
- `ON (` 뒤의 첫 번째 조건은 여는 괄호와 구분되도록 네 칸을 두고 작성합니다.
- 두 번째 ON 조건부터 `AND`를 선행하여 조건식 시작 위치를 맞춥니다.
- ON 절의 닫는 괄호는 `ON`과 같은 시작 기준선에 맞춥니다.

### 9.2 MATCHED 및 NOT MATCHED 절

- `WHEN MATCHED THEN`과 `WHEN NOT MATCHED THEN`은 같은 시작 위치에 정렬합니다.
- MATCHED 영역의 `UPDATE`와 `SET`은 SQL 주요 키워드 우측 정렬 기준을 따릅니다.
- SET 절의 두 번째 컬럼부터 선행 콤마를 사용하고 대입 대상 컬럼 시작 위치를 맞춥니다.
- NOT MATCHED 영역의 `INSERT`와 `) VALUES (`는 같은 시작 위치에 정렬합니다.
- INSERT 컬럼과 VALUES 값은 한 줄에 하나씩 작성하고 각각 선행 콤마 위치를 맞춥니다.
- MERGE 내부 INSERT의 컬럼 순서와 VALUES 순서는 반드시 일대일로 대응해야 합니다.

### 9.3 MERGE 정렬 예시

```xml
<!-- 독서 목표를 신규 등록하거나 기존 목표를 갱신한다. -->
<insert id="setReadingGoal" parameterType="org.our.sadari.myPage.dto.ReadingGoalDto">
    /* setReadingGoal */
    MERGE INTO TM_GOALXM G
    USING (  SELECT #{userNumb} AS USER_NUMB
                  , #{goalDate} AS GOAL_DATE
                  , #{goalType} AS GOAL_TYPE
                  , #{goalCnt} AS GOAL_CNTT
             FROM DUAL
           ) S
       ON (    G.USER_NUMB = S.USER_NUMB
           AND G.GOAL_DATE = S.GOAL_DATE
           AND G.GOAL_TYPE = S.GOAL_TYPE
       )
     WHEN MATCHED THEN
  UPDATE
     SET G.GOAL_CNTT = S.GOAL_CNTT
       , G.UPDT_DATE = SYSDATE
     WHEN NOT MATCHED THEN
   INSERT (    USER_NUMB
             , GOAL_DATE
             , GOAL_TYPE
             , GOAL_CNTT
             , REGI_DATE
             , UPDT_DATE
   ) VALUES (  S.USER_NUMB
             , S.GOAL_DATE
             , S.GOAL_TYPE
             , S.GOAL_CNTT
             , SYSDATE
             , SYSDATE
   )
</insert>
```

## 10. CASE WHEN THEN 문 정렬 규칙

### 10.1 CASE 절 배치

- 컬럼 대입식에서 CASE를 사용할 때 `대상컬럼 = CASE WHEN 조건`을 같은 줄에 작성합니다.
- 첫 번째 `WHEN`은 `CASE`와 같은 줄에 작성합니다.
- `THEN`은 WHEN 조건의 다음 줄에 작성하고 조건 뒤의 결과 영역임을 알 수 있도록 CASE보다 깊게 들여씁니다.
- `ELSE`는 THEN보다 한 단계 왼쪽에 작성하여 기본 분기 시작 위치를 구분합니다.
- `END`는 ELSE보다 한 단계 왼쪽에 작성하고 CASE 표현식의 종료 위치를 나타냅니다.
- CASE 표현식이 SET 절의 두 번째 항목 이후에 있으면 대상 컬럼 앞의 선행 콤마 정렬을 유지합니다.
- 비교 연산자 `<`, `>`, `<=`, `>=`는 MyBatis XML에서 반드시 CDATA로 감쌉니다.
- CASE가 여러 개 중첩되면 내부 CASE의 `WHEN`, `THEN`, `ELSE`, `END`를 외부 CASE보다 한 단계 더 들여씁니다.

### 10.2 CASE 정렬 예시

```sql
       , G.UPDT_CNTT = CASE WHEN S.GOAL_CNTT <![CDATA[<]]> G.GOAL_CNTT
                                 THEN NVL(G.UPDT_CNTT, 0) + 1
                            ELSE NVL(G.UPDT_CNTT, 0)
                        END
```

## 11. 인라인 뷰 및 서브쿼리 정렬 규칙

### 11.1 인라인 뷰 배치

- 인라인 뷰는 `FROM (  SELECT` 또는 `JOIN (  SELECT`처럼 여는 괄호 뒤 두 칸을 두고 `SELECT`를 시작합니다.
- 인라인 뷰의 첫 번째 SELECT 컬럼은 `SELECT` 다음 데이터 시작 위치에 작성합니다.
- 두 번째 컬럼부터 선행 콤마를 사용하고 컬럼 시작 위치를 맞춥니다.
- 인라인 뷰 내부의 `FROM`, `WHERE`, `AND`, `GROUP BY`는 내부 SELECT 기준으로 우측 정렬합니다.
- 닫는 괄호는 인라인 뷰를 시작한 `FROM` 또는 `JOIN`의 데이터 시작 기준에 맞추고, 같은 줄에 대문자 한 단어 별칭을 작성합니다.
- 인라인 뷰 뒤의 `ON`과 `AND`는 외부 쿼리의 JOIN 기준선에 맞춥니다.
- 같은 깊이의 인라인 뷰는 동일한 들여쓰기 폭을 사용합니다.

### 11.2 집계 서브쿼리

- SELECT 절에서 행마다 실행되는 스칼라 서브쿼리는 금지하며, 집계가 필요하면 GROUP BY 인라인 뷰를 JOIN합니다.

### 11.3 인라인 뷰 정렬 예시

```sql
       SELECT /* getLikeDtl */
              R.TAGT_TYPE
            , R.TAGT_NUMB
            , NVL(LC.LIKE_CNT, 0) AS LIKE_CNT
         FROM (  SELECT #{tagtType} AS TAGT_TYPE
                      , #{tagtNumb} AS TAGT_NUMB
                   FROM DUAL
               ) R
    LEFT JOIN (  SELECT L.TAGT_TYPE
                      , L.TAGT_NUMB
                      , COUNT(*) AS LIKE_CNT
                   FROM TB_LIKEXX L
                  WHERE L.TAGT_TYPE = #{tagtType}
                    AND L.TAGT_NUMB = #{tagtNumb}
               GROUP BY L.TAGT_TYPE, L.TAGT_NUMB
              ) LC
           ON LC.TAGT_TYPE = R.TAGT_TYPE
          AND LC.TAGT_NUMB = R.TAGT_NUMB
```

## 12. 스칼라 서브쿼리 금지

### 12.1 금지 이유와 대안

- SELECT 절에서 행마다 실행되는 스칼라 서브쿼리를 사용하지 않습니다.
- 집계값이나 연관 단건 데이터는 먼저 집계한 인라인 뷰를 JOIN합니다.
- 대량 데이터에서 반복 Random I/O가 발생하지 않도록 GROUP BY 결과를 한 번만 계산합니다.

### 12.2 금지 예시

금지:

```sql
SELECT B.BOOK_NUMB
     , (SELECT AVG(P.REPT_GRDE)
          FROM TM_REPORT P
         WHERE P.BOOK_NUMB = B.BOOK_NUMB) AS BOOK_AVG_GRDE
  FROM TM_BKINFO B
```

### 12.3 허용 예시

허용:

```sql
       SELECT B.BOOK_NUMB
            , G.BOOK_AVG_GRDE
         FROM TM_BKINFO B
    LEFT JOIN (  SELECT P.BOOK_NUMB
                      , ROUND(AVG(TO_NUMBER(P.REPT_GRDE)), 1) AS BOOK_AVG_GRDE
                   FROM TM_REPORT P
               GROUP BY P.BOOK_NUMB
              ) G
           ON G.BOOK_NUMB = B.BOOK_NUMB
```

## 13. SQL 로직 주석

### 13.1 주석 대상

- 복잡한 조건 분기와 업무 정책이 반영되는 WHERE 조건에는 왜 필요한지 설명합니다.
- 날짜 경계, 상태 제외, 중복 방지, 동시성 처리 등 결과 해석에 영향을 주는 조건을 주석으로 남깁니다.

### 13.2 주석 품질

- 단순히 SQL 문장을 한글로 옮긴 주석은 작성하지 않습니다.
- DDL에는 테이블 및 컬럼 `COMMENT`를 정확한 한글 명사형으로 작성합니다.
- 테이블 및 컬럼 `COMMENT`에는 `이다`, `다` 등의 종결어미와 마침표를 사용하지 않습니다.

## 14. 운영 배포 SQL 동기화

### 14.1 운영 필수 초기 데이터

- 공통코드를 추가하거나 변경할 때는 `scripts/db/mysql/output/02-admin-insert.sql`만 수정합니다.
- `scripts/db/mysql/output/02-admin-insert.sql`에는 이관 대상 테이블의 현재 데이터를 재현하는 `INSERT` 문만 작성합니다. 이 파일의 `INSERT`는 일반 INSERT 정렬 규칙의 예외로 두며, SQL 식별 주석은 직전 줄에 분리하고 `INSERT INTO ... VALUES` 헤더는 한 줄에 작성합니다. 각 값 목록은 한 행씩 줄을 나누고 첫 행은 네 칸 들여쓰며, 두 번째 행부터 선행 콤마를 사용해 같은 위치에 정렬합니다. 하나의 `INSERT` 문에는 값 목록을 최대 10행까지 작성하고, 10행을 초과하면 다음 `INSERT` 문으로 나눕니다. 중복키 갱신 절이 없으면 마지막 값 목록 끝에 세미콜론을 작성하고, 데이터 무결성을 위해 `ON DUPLICATE KEY UPDATE`가 필요하면 값 목록 다음 줄부터 후속 절을 정렬하여 마지막 갱신 항목 끝에 세미콜론을 작성합니다.
- `scripts/db/mysql/output/02-admin-insert.sql`에 한 건이라도 `INSERT`가 존재하는 테이블에 데이터를 추가할 때는 같은 작업에서 해당 테이블의 현재 전체 행을 다시 조회하여 파일의 모든 `INSERT`를 최신화합니다. 기존 행을 수정하거나 삭제하여 현재 데이터가 달라질 때도 같은 전체 데이터 기준으로 최신화합니다.
- 공통코드 추가를 위한 별도 SQL 파일을 생성하거나 `scripts/db/mysql/01-create.sql` 및 다른 SQL 파일에 중복 DML을 작성하지 않습니다.
- 관리자 메뉴, 사용자 메뉴 등 신규 기능을 운영 환경에서 사용하는 데 필요한 다른 기준 데이터나 초기 데이터를 추가 또는 변경하면 같은 작업에서 실행 가능한 DML을 `scripts/db/mysql/output/02-admin-insert.sql`에 추가하거나 수정합니다.
- 개발 DB에만 데이터를 등록하고 `scripts/db/mysql/output/02-admin-insert.sql` 반영을 생략한 상태로 작업을 완료하지 않습니다.
- 테이블 구조 또는 기준 데이터 변경을 위해 `03-*.sql`, `05-*.sql`, `09-*.sql` 같은 기능별 DDL, DML 또는 마이그레이션 SQL 파일을 새로 만들지 않습니다.
- 실행 중인 데이터베이스에 변경을 반영할 때도 `01-create.sql`과 `02-admin-insert.sql`을 원본으로 먼저 갱신하고, 인덱스 적용용 `04-user-statistics-indexes.sql` 외에는 실제 적용 명령을 별도 SQL 파일로 보관하지 않습니다.

### 14.2 스키마 정의

- 테이블, 컬럼, PK, FK 및 제약조건을 추가하거나 변경할 때는 `scripts/db/mysql/01-create.sql`의 `CREATE TABLE` 정의를 수정합니다.
- 인덱스를 추가하거나 변경할 때는 신규 운영 환경용 `scripts/db/mysql/01-create.sql`의 `CREATE TABLE` 정의와 기존 운영 환경 적용용 `scripts/db/mysql/04-user-statistics-indexes.sql`을 같은 작업에서 함께 수정합니다.
- 인덱스 적용 `ALTER TABLE`은 `scripts/db/mysql/04-user-statistics-indexes.sql`에만 작성하고 `05-*.sql` 등 별도 인덱스 SQL 파일을 생성하지 않습니다.
- 인덱스 이외의 스키마 정리를 위한 `ALTER` 문이나 기능별 CREATE 및 마이그레이션 SQL 파일을 생성하지 않습니다.
- 필요한 PK, FK, 제약조건, 인덱스 및 테이블·컬럼 코멘트를 `scripts/db/mysql/01-create.sql`의 생성 DDL에 함께 반영하여 신규 운영 환경에서 해당 파일만으로 동일한 스키마를 생성할 수 있게 합니다.

### 14.3 구글 스프레드시트 테이블 명세 동기화

- 테이블 또는 컬럼을 추가하면 같은 작업에서 `scripts/db/mysql/01-create.sql`과 [Sadari 테이블 명세](https://docs.google.com/spreadsheets/d/1rTSXXRTjucGW5NjY40LIFiTH-92QqBtahJ3lvHvnqQI/edit?gid=1090069395#gid=1090069395)의 `테이블_최신` 탭을 함께 갱신합니다.
- 시트에는 실제 DDL을 기준으로 테이블명, 테이블 설명, 테이블 생성일, 컬럼명, 컬럼 설명, 데이터 타입과 크기, PK 여부, 기본값 및 NULL 허용 여부를 반영합니다.
- 시트에는 `CREATE SQL`, `COMMENT SQL`, `ALTER SQL` 등 실행문 전용 열을 두지 않습니다.
- 테이블 생성일은 실제 데이터베이스의 `INFORMATION_SCHEMA.TABLES.CREATE_TIME`을 우선 사용합니다. 실제 데이터베이스에 접속할 수 없거나 값의 원본 생성 시각을 보장할 수 없으면 `scripts/db/mysql/01-create.sql`에 해당 테이블이 최초 추가된 Git 날짜를 사용하고 날짜 셀 메모에 근거 커밋과 실제 메타데이터 미확인 사실을 기록합니다.
- 메타데이터, 테이블 정보, 컬럼 헤더, PK 행, 제약조건 및 인덱스 영역에 적용된 기존 색상과 구분 형식을 유지합니다.
- 컬럼 명세 행의 회색 채우기는 `CT_NOTICE` 형식을 기준으로 `PRIMARY KEY` 열의 값이 `Y`인 PK 컬럼 행에만 적용합니다.
- 컬럼 순번 열은 PK 여부와 관계없이 기존 연한 회색과 검정 글자를 유지하고, PK 컬럼 행의 나머지 명세 영역은 회색 배경과 흰색 글자, PK가 아닌 컬럼 행의 나머지 명세 영역은 흰색 배경과 검정 글자로 유지합니다.
- PK 여부에 따라 컬럼 명세 행의 배경색을 변경할 때는 글자색도 같은 작업에서 함께 변경하여 흰색 배경에 흰색 글자 또는 회색 배경에 검정 글자가 남지 않게 합니다.
- 각 테이블의 마지막 컬럼 아래에는 값 없이 검정색 채우기만 적용된 구분 행을 두지 않으며, 기존 검정색 빈 행이 있으면 테이블과 컬럼 데이터를 보존한 채 해당 행을 삭제합니다.
- 행의 기존 색상이나 인접 행의 서식을 근거로 PK 여부를 추정하지 않고 `PRIMARY KEY` 열의 실제 값과 DDL의 PK 정의를 대조하여 서식을 결정합니다.
- 시트에 비밀번호, 토큰, 개인키 및 운영 접속 정보와 같은 비밀값을 기록하지 않습니다.
- 작업 완료 전에 `01-create.sql`과 시트의 테이블·컬럼 수 및 마지막 테이블 반영 여부를 대조하고, 모든 컬럼 명세 행에서 `PRIMARY KEY=Y`인 행만 회색 배경과 흰색 글자인지, 나머지 행은 흰색 배경과 검정 글자인지, 기존 셀 값이 유지되는지, 마지막 컬럼 아래에 검정색 빈 행이 남지 않았는지 전수 검사합니다. 외부 접근 또는 인증 문제로 시트를 갱신하지 못하면 누락한 채 완료하지 않고 미반영 사유를 사용자에게 보고합니다.

### 14.4 저장 루틴 실행

- `DELIMITER`를 사용하는 저장 함수와 프로시저 SQL은 DBeaver의 단일 문장 실행이 아니라 Native delimiter를 인식하는 전체 스크립트 실행으로 검증합니다.
- 저장 루틴 일부만 실행할 때는 해당 루틴에 필요한 `DELIMITER` 변경과 복원 구문까지 같은 실행 범위에 포함합니다.

## 15. 품질 규칙

### 15.1 계약 일치

- Mapper ID와 Java Mapper 메서드명을 일치시킵니다.
- parameterType, resultType 및 DTO 필드 타입을 실제 SQL 결과와 일치시킵니다.

### 15.2 미사용 요소와 검증

- 사용하지 않는 `<sql>`, `<bind>`, resultMap을 남기지 않습니다.
- Mapper XML 파싱 오류와 IDE 경고를 모두 해결한 뒤 완료합니다.

### 15.3 문자 인코딩과 데이터 무결성 검증

- SQL 파일과 SQL 실행 도구의 문자 인코딩을 UTF-8로 고정하고 MySQL 연결에는 `utf8mb4` 문자 집합을 사용합니다.
- 한글이 포함된 DDL과 DML을 적용한 뒤에는 영향 행 수만 확인하지 않고 `information_schema`에서 테이블명과 컬럼명을 조회하여 원본 정의와 정확히 일치하는지 확인합니다.
- 공통코드, 메뉴명, 설명 및 COMMENT처럼 한글이나 업무 코드가 저장되는 값은 적용 후 다시 조회하여 원본 문자열과 문자 단위로 정확히 일치하는지 확인합니다.
- 조회 결과에 대체 문자 `�`, 물음표 치환 또는 UTF-8과 다른 문자 집합으로 재해석된 깨진 문자열이 없는지 확인하고, 의심되는 값은 `HEX` 결과까지 비교합니다.
- 문자 데이터가 깨진 상태에서는 작업을 완료로 보고하지 않으며 원본 UTF-8 문자열로 복구한 뒤 같은 검증을 다시 실행합니다.

## 16. Git 규칙

### 16.1 커밋 접두사

SQL 변경 커밋도 표준 접두사를 사용합니다.

- `feat:` 스키마 또는 쿼리 기능 추가
- `fix:` 잘못된 쿼리 및 데이터 수정
- `refactor:` 결과 변경 없는 쿼리 구조 개선
- `docs:` DB 문서와 코멘트 수정
- `style:` SQL 정렬만 변경
