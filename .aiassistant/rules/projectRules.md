---
apply: always
---

# Project Naming and Coding Conventions

이 문서는 프로젝트의 데이터베이스, SQL, 백엔드(Java), 프론트엔드(React), API 설계 및 소스 형상 관리를 아우르는 프로젝트 개발 지침입니다. AI와 개발자가 개발 및 유지보수 시 반드시 준수해야 하는 엄격한 규칙과 금지 사항을 정의합니다. 코드를 생성하거나 수정할 때 아래의 규칙을 예외 없이 적용하십시오. 허용되지 않는 규칙이나 금지 사항이 발견될 경우 컴파일 및 정적 분석 단계에서 반려됩니다.

---

## 1. Database Table Naming Rule

테이블 명칭은 성격에 따라 접두사를 구분하며, 접두사 이후의 글자 수를 엄격하게 제한합니다. 이 규칙을 벗어난 테이블 생성 및 참조는 절대 허용하지 않습니다.

* **마스터 테이블 (Master Data):** `TM_` 로 시작
* **파생/트랜잭션 테이블 (Derived Data):** `TB_` 로 시작
* **길이 및 패딩 규칙:**
    * 접두사(`TM_`, `TB_`) 뒤에는 반드시 **영문 대문자 6자리**가 와야 합니다.
    * 의미를 담기에 글자 수가 6자리보다 짧거나 애매한 경우, 남는 뒷자리는 무조건 **대문자 X**로 채워서 6자리를 맞춥니다. (공백이나 다른 패딩 문자 사용 금지)

### Examples:
* `TM_USERXM` (TM_ + USERXM 6자) `[O]`
* `TM_ADMINX` (TM_ + ADMINX 6자) `[O]`
* `TM_USERS` (6자 미달) `[X - 허용 불가]`
* `TM_USER_INFO` (구분자 포함 및 길이 초과) `[X - 허용 불가]`

---

## 2. Database Column Naming Rule

컬럼 명칭은 언더바(`_`)를 기준으로 앞뒤 글자 수를 고정합니다. 임의의 길이로 컬럼명을 생성하는 행위는 절대 금지합니다.

* **형식 규칙:** `[영문 대문자 4자]_[영문 대문자 4자]` (예: `AAAA_BBBB`)
* **길이 및 패딩 규칙:**
    * 언더바 앞과 뒤는 각각 반드시 **영문 대문자 4자리**여야 합니다.
    * 단어의 길이가 4자리보다 짧으면, 남는 뒷자리는 무조건 **대문자 X**로 채워서 4자리를 맞춥니다.
* **관리자 테이블 필수 컬럼:** (수정 불가능한 이력성 테이블이 아니라면 아래 4개 컬럼은 필수 탑재해야 하며, 명칭 변경을 금지합니다.)
    * `REGI_ADMN`: 등록자 아이디
    * `REGI_DATE`: 등록일시
    * `UPDT_ADMN`: 수정자 아이디
    * `UPDT_DATE`: 수정일시
* **고정 사용 컬럼 단어** (지정된 단어 외 임의 축약 금지):
    * 사용자: `USER`
    * 사용 여부: `USEE` (예: 사용여부 컬럼은 반드시 `USEE_YSNO`로 통일)
    * 여부 표시(Y/N): `YSNO`
    * 등록: `REGI`
    * 관리자: `ADMN`
    * 아이디: `IDXX`

### Examples:
* `USER_IDXX` (USER 4자 + _ + IDXX 4자) `[O]`
* `MENU_NMSX` (MENU 4자 + _ + NMSX 4자) `[O]`
* `USEE_YSNO` (USEE 4자 + _ + YSNO 4자) `[O]`
* `USER_ID` (뒤쪽 4자리 미달) `[X - 허용 불가]`
* `REG_DATE` (앞쪽 4자리 미달) `[X - 허용 불가]`

---

## 3. SQL Query Formatting & Safety Guide

### [금지 사항]
* `SELECT *` (와일드카드) 사용을 절대 금지합니다. 무조건 필요한 컬럼을 명시하십시오.
* 명시되지 않은 테이블 별칭(Alias) 사용을 금지합니다. (반드시 `AS` 없이 **한 글자 대문자 별칭** 사용)

### 1. 핵심 규칙: 키워드 우측 정렬
* SQL의 주요 절(Clause)을 나타내는 키워드들의 우측 끝을 가상의 세로선에 맞추어 정렬합니다.
* `SELECT`, `FROM`, `LEFT JOIN`, `WHERE`, `ORDER BY` 등 길이가 다른 키워드들의 오른쪽 끝이 한 줄로 일치해야 합니다.
* 키워드 뒤에 오는 컬럼명, 테이블명, 조건식 등은 모두 동일한 시작 위치(좌측 정렬)를 가집니다.

### 2. SELECT 절 및 컬럼 포맷팅
* **1줄 1컬럼 규칙:** `SELECT` 절의 각 컬럼은 반드시 새로운 줄에 작성합니다. 한 줄에 여러 컬럼을 나열하는 것을 금지합니다.
* **선행 콤마 (Leading Comma):** 컬럼을 구분하는 콤마(`,`)는 뒷줄이 아닌 새로운 줄의 맨 앞에 위치시킵니다.
* **콤마 정렬:** 이 콤마 역시 키워드 우측 정렬 기준선에 맞추어 배치합니다.

### 3. JOIN 및 조건 절
* `LEFT JOIN` 키워드 역시 우측 정렬 기준선에 맞춥니다.
* JOIN의 조건인 `ON`과 추가 조건인 `AND` 역시 우측 정렬하여 키워드의 오른쪽 끝을 기준선에 맞춥니다.

### 4. WHERE 및 ORDER BY 절
* `WHERE` 키워드와 내부 조건인 `AND`도 동일한 우측 정렬 규칙을 따릅니다.
* 조건식 내부의 괄호 `(`는 텍스트(데이터) 시작 위치에 맞추어 정렬합니다.
* `ORDER BY` 절 역시 우측 정렬되어 있으며, 정렬 대상 컬럼은 다른 구문들과 동일한 세로선에서 시작합니다.

### 5. XML MyBatis 안전 규칙 (MyBatis 사용 시 필수)
* **부등호 처리:** SQL 내에서 `<`, `>`, `<=`, `>=` 등의 부등호를 사용할 때 XML 파싱 에러 및 SQL Injection 예방을 위해 반드시 `<![CDATA[ ... ]]>` 구문으로 감싸야 합니다. 이를 생략하는 것을 절대 허용하지 않습니다.
* **배열 및 리스트 조회:** 다건 조건 처리 시, 전달받은 파라미터 리스트가 비어있지 않은지 사전에 확인하여 비어있을 경우 SQL 에러가 발생하는 것을 방지해야 합니다.
* **NULL 처리:** 조건절이나 정렬 시 데이터가 NULL일 가능성이 있는 경우, DBMS 함수(예: `NVL`, `COALESCE`)를 사용하여 디폴트 값을 명시적으로 처리해야 합니다.
* **파라미터 매핑 규칙 (`#` 과 `$` 구분):**
    * 사용자 입력값 바인딩 시 반드시 `#{param}` 형식을 사용하여 SQL Injection을 원천 방지합니다.
    * 테이블명, 컬럼명, 정렬 방향 지정 등을 위해 부득이하게 `${column}` 형식을 사용하는 경우, SQL Injection 방지를 위해 사전 허용 패턴(White-list) 검증 로직을 Java 비즈니스 로직 단에서 필수로 선행 처리해야 합니다.
* **하드코딩 금지 및 `<bind>` 태그 사용:**
    * SQL 내 특정 고정값이나 문자열 결합(특히 LIKE절의 `'%'+#{keyword}+'%'` 등)을 하드코딩하는 것을 절대 금지합니다.
    * 동적 바인딩이나 값의 사전 변환이 필요할 때는 반드시 MyBatis의 `<bind>` 태그를 활용해 변수를 명확히 정의하고 사용해야 합니다.
* **공통 `<bind>` 영역의 모듈화 (`<sql>` 및 `<include>` 사용):**
    * 동일한 `<bind>` 구문(예: 검색 조건 문자열 결합, 빈 값 디폴트 세팅 등)이 복수의 쿼리(`select`, `update` 등)에서 중복 사용되는 경우, 각 쿼리에 매번 복사하여 붙여넣는 행위를 엄격히 금지합니다.
    * 중복되는 `<bind>` 영역은 별도의 `<sql id="...">` 블록으로 분리 정의한 뒤, 필요한 쿼리문 내에서 `<include refid="..." />`를 사용하여 참조하도록 설계해야 합니다.

```text
키워드 정렬 영역 (우측 정렬) | 데이터 정렬 영역 (좌측 정렬 시작선 일치)
------------------------|------------------------------------
               SELECT   | M.MENU_NUMB
                    ,   | M.SUBX_NUMB
                    ,   | M.MENU_NAME
                 FROM   | TM_MENUXM M
            LEFT JOIN   | TB_CODEXD R
                   ON   | R.COMM_CODE = #{authCode}
                  AND   | R.COMD_CODE = M.READ_AUTH
                WHERE   | NVL(M.USEE_YSNO, #{yes}) = #{yes}
                  AND   | (M.READ_AUTH IS NULL OR M.READ_AUTH = #{auth})
             ORDER BY   | NVL(M.SORT_ORDR, 9999)
```

---

### 6. 스칼라 서브쿼리(Scalar Subquery) 사용 금지 및 인라인 뷰(GROUP BY) 대체 규칙
* **스칼라 서브쿼리 사용 금지:** SELECT 절 내부에서 서브쿼리를 통해 다른 테이블의 집계 값(AVG, SUM 등)이나 단건 데이터를 매 로우마다 반복 조회하는 스칼라 서브쿼리 형태의 작성을 절대 금지합니다. 이는 데이터양이 많아질수록 무수한 Random I/O를 유발하여 성능을 심각하게 저하시킵니다.
* **GROUP BY 인라인 뷰 조인 대체:** 집계 및 대량 조회가 필요한 경우, 사전에 집계를 완료한 **GROUP BY 서브쿼리(인라인 뷰)를 만들고 이를 메인 쿼리와 Outer Join(또는 Inner Join)** 하도록 쿼리를 전면 개편해야 합니다.

#### [Before] 스칼라 서브쿼리를 사용한 나쁜 예 (금지)
```sql
SELECT B.BOOK_NUMB
     , B.BOOK_TITL
     , (SELECT ROUND(AVG(TO_NUMBER(P.REPORT_GRDE)), 1)
          FROM TM_BOOK_REPORT P
         WHERE P.BOOK_NUMB = B.BOOK_NUMB
        ) AS BOOK_AVG_GRDE
  FROM TM_BOOK_REPORT A
INNER JOIN TM_BOOK_INFO B
    ON A.BOOK_NUMB = B.BOOK_NUMB
 WHERE A.USER_NUMB = #{userNumb}
   AND A.REPORT_NUMB = #{reportNumb}
```

#### [After] GROUP BY 인라인 뷰 조인을 적용한 올바른 예 (우측 정렬 규칙 준수)
```text
               SELECT   | B.BOOK_NUMB
                    ,   | B.BOOK_TITL
                    ,   | B.BOOK_ATHR
                    ,   | B.BOOK_CVIM
                    ,   | B.BOOK_ISBN
                    ,   | B.BOOK_PUBL
                    ,   | B.BOOK_DESC
                    ,   | B.PUBL_DATE
                    ,   | G.BOOK_AVG_GRDE
                 FROM   | TM_BOOK_REPORT A
           INNER JOIN   | TM_BOOK_INFO B
                   ON   | A.BOOK_NUMB = B.BOOK_NUMB
            LEFT JOIN   | (  SELECT   | P.BOOK_NUMB
                        |         ,   | ROUND(AVG(TO_NUMBER(P.REPORT_GRDE)), 1) AS BOOK_AVG_GRDE
                        |      FROM   | TM_BOOK_REPORT P
                        |  GROUP BY   | P.BOOK_NUMB
                        | ) G
                   ON   | G.BOOK_NUMB = B.BOOK_NUMB
                WHERE   | A.USER_NUMB = #{userNumb}
                  AND   | A.REPORT_NUMB = #{reportNumb}
```

---

## 4. Java Method & Transaction Rule

메서드명은 기능(Action)에 따라 엄격한 접두사(Prefix)와 접미사(Suffix) 규칙을 준수해야 합니다. 이 규칙 이외의 임의의 동사(예: find, query, save, remove 등)를 사용하는 메서드 선언은 절대 허용하지 않습니다. (***는 해당 도메인 또는 엔티티의 명칭을 의미합니다.)

### 1. 명명 규칙
* **다건 조회 (List):** `get***List` (예: `getUserList()`)
* **카운트 조회 (Count):** `get***Cnt` (예: `getUserCnt()`)
* **단건 조회 (Detail):** `get***Dtl` (예: `getUserDtl()`)
* **데이터 등록 (Insert):** `set***` (예: `setUser()`)
* **데이터 수정 (Update):** `upt***` (예: `uptUser()`)
* **데이터 삭제 (Delete):** `del***` (예: `delUser()`)
* **중복 검사 (Duplicate Check):** `dup***` (예: `dupUser()`)
* **데이터 배포 (Distribution):** `dist***` (예: `distUser()`)

### 2. Java 구현 시 필수 예외 처리 규칙
* **Null Pointer Exception 방지:** 외부 API 호출 결과, DB 조회 결과, 혹은 파라미터 값에 대해 참조하기 전 반드시 Null 체크 검증 로직을 선행해야 합니다. (`Optional` 적극 활용 또는 `if (obj == null)` 예외 던지기 필수)
* **빈 값 처리:** 데이터 등록(`set***`) 및 수정(`upt***`) 메서드 진입 시, 필수 파라미터의 누락 여부(Null 또는 빈 문자열 `""`)를 검증하는 Validation 로직을 첫 줄에 강제 배치해야 합니다.

### 3. 트랜잭션(@Transactional) 적용 가이드
* **기본 조회 트랜잭션 제한:** 비즈니스 로직을 처리하는 Service 클래스 상단에는 기본적으로 `@Transactional(readOnly = true)`를 선언하여 읽기 전용 커넥션을 맺도록 강제합니다.
* **등록/수정/삭제 트랜잭션 개별 부여:** 데이터 상태가 변경되는 `set***`, `upt***`, `del***` 메서드 상단에만 구체적으로 `@Transactional` (읽기/쓰기 가능) 어노테이션을 선언하여 불필요한 데이터베이스 락과 리소스 점유를 차단합니다.

---

### 4. 컨트롤러 및 메서드 파라미터 개행 및 선행 콤마 정렬 규칙
* **1줄 1파라미터 규칙 (Line Break per Parameter):**
  * 컨트롤러나 서비스 등의 Java 메서드 선언부에서 입력받는 파라미터가 2개 이상인 경우, 한 줄에 나열하는 것을 금지하며 반드시 **새로운 줄(Line Break)로 분리하여 작성**해야 합니다.
* **선행 콤마 (Leading Comma) 및 시작선 정렬:**
  * SQL 포맷팅 규칙과 동일하게, 파라미터를 구분하는 콤마(`,`)는 뒷줄이 아닌 **새로운 줄의 맨 앞에 위치(선행 콤마)** 시킵니다.
  * 콤마 뒤에는 공백을 두어 다음 라인의 어노테이션 및 데이터 타입의 **시작 시작선(좌측 정렬)이 세로로 일치**하도록 줄을 맞춰 정렬합니다.
* **인증 및 유효성 검증 어노테이션 표준 탑재:**
  * 로그인 세션 정보나 회원 식별값 바인딩이 필요할 경우 반드시 `@AuthenticationPrincipal` 어노테이션을 파라미터 맨 앞에 정의합니다.
  * 외부로부터 유입되는 데이터 객체(DTO)에는 유효성 검증을 강제하기 위한 `@Valid`와 JSON 바인딩을 위한 `@RequestBody`를 빠짐없이 명시적으로 선언해야 합니다.

#### [Before] 한 줄로 나열된 나쁜 예 (금지)
```java
public ResultData createReport(@AuthenticationPrincipal Long userNumb, @Valid @RequestBody ReportDto requestDto) {
    // ... 로직
}
```

#### [After] 1줄 1파라미터 및 선행 콤마 정렬을 적용한 올바른 예 (준수)
```java
public ResultData createReport(@AuthenticationPrincipal Long userNumb
                             , @Valid @RequestBody ReportDto requestDto) {
    // ... 로직
}
```

---

## 5. 주석(Comment) 작성 가이드

모든 레이어(Frontend, Backend, DB XML)의 코드는 자가 문서화가 가능해야 하며, 타 개발자 및 AI가 코드의 흐름을 왜곡 없이 이해할 수 있도록 규격화된 주석 가이드를 준수해야 합니다. 주석이 누락되거나 규격을 위반한 코드는 정적 분석 및 리뷰 단계에서 즉시 반려됩니다.

### 1. Java 표준 Javadoc 규칙 (Public API 필수)
* 모든 클래스 및 퍼블릭(Public) 메서드 상단에는 반드시 Javadoc 형식의 주석을 작성합니다.
* 주석의 시작은 `/**`로, 끝은 `*/`로 묶어서 작성합니다.
* 내부의 각 줄은 `*` (별표)로 시작하며, 시작 부분의 `/` 중 두 번째 `*`와 세로 위치를 일치시킵니다.
* `*` 기호 뒤에는 들여쓰기와 가독성을 위해 반드시 하나의 공백(Space)을 둡니다.
* **요약 설명 (Description):** 주석 블록의 첫 번째 줄(태그가 나오기 전)에는 해당 메서드나 클래스의 핵심 역할(예: Redis 인증 필터 생성)을 간결하게 기술합니다.
* **애노테이션 태그(Tag) 규칙:** 요약 설명 아래부터 아래 태그들을 순서대로 적용합니다.
    * `@author`: 코드를 작성한 담당자명을 명시합니다. 이름과 성 사이에 반드시 마침표(`.`)를 넣어야 합니다. (예: `SeungHyeon.Kang` `[O]`, `SeungHyeon Kang` `[X]`)
    * `@param`: 입력받는 파라미터 변수명과 설명을 기재합니다. 설명이 누락된 빈 태그 선언은 반려됩니다.
    * `@return`: 반환하는 값의 타입과 의미를 명확히 기술합니다.
    * `@throws` / `@exception`: 전파될 수 있는 주요 예외 클래스와 그 발생 조건을 구체적으로 기술합니다.
* **[금지 사항]** 주석 내에 이모지(Emoticon/Emoji, 예: 💡, ✔, ❌ 등)의 사용을 엄격히 금지합니다. 무조건 텍스트와 표준 태그만 사용하십시오.

```java
/**
 * 사용자 정보 단건 상세 조회
 * 
 * @author SeungHyeon.Kang
 * @param userId 조회할 사용자의 고유 식별자 ID (Null 또는 빈 값 허용 안 됨)
 * @return 조회된 사용자 상세 정보 DTO (존재하지 않을 경우 Null 반환)
 * @throws IllegalArgumentException userId가 유효하지 않거나 빈 값일 경우 발생
 */
public UserDto getUserDtl(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
        throw new IllegalArgumentException("사용자 ID는 필수 값입니다.");
    }
    // ... 로직 구현
}
```

### 2. XML MyBatis 쿼리 주석 규칙
MyBatis XML Mapper 내에서 쿼리문을 선언할 때는 외부 구조와 내부 쿼리에 각각 이중으로 식별용 주석을 선언해야 합니다.

* **태그 상단 외부 주석 (`<!-- ... -->`):**
    * `<select>`, `<update>`, `<insert>`, `<delete>` 등 모든 쿼리 시작 태그의 **바로 윗줄**에 해당 쿼리가 수행하는 기능적 역할과 목적을 명확히 명시하는 XML 주석을 작성해야 합니다.
* **쿼리 내부 주석 (`/* id */`):**
    * 실제 쿼리문이 시작되는 첫 문두(SELECT, INSERT 등 키워드 바로 뒤 혹은 태그 내부 첫 라인)에, 해당 쿼리 ID를 명시하는 SQL 인라인 주석(`/* 쿼리아이디 */`)을 필수로 기재합니다. 이는 DBMS APM(애플리케이션 성능 모니터링) 도구 및 슬로우 쿼리 로그 추적 시 세션 실행 소스를 즉시 식별하기 위함입니다.

```xml
<!-- 사용자 ID를 기반으로 상세 정보를 단건 조회 -->
<select id="getUserDtl" parameterType="org.our.sadari.user.dto.UserDto" resultType="org.our.sadari.user.dto.UserDto">
    SELECT /* getUserDtl */
           U.USER_IDXX
         , U.USER_NAME
      FROM TM_USERXM U
     WHERE U.USER_IDXX = #{userId}
</select>
```

### 3. 분기 처리 및 복잡성 비즈니스 로직 주석 (공통)
* **대상 범위:** 백엔드(Java, SQL) 및 프론트엔드(React, JavaScript/TypeScript) 레이어에 관계없이 적용합니다.
* **필수 작성 조건:**
    * `if-else`, `switch` 등의 조건 분기가 일어나 코드의 실행 흐름이 갈라지는 지점
    * 복잡한 연산, 정규식 표현, 또는 아키텍처 흐름상 도메인 지식이 요구되는 비트/수식 연산 지점
    * 외부 API 통신 및 비동기 처리의 전후 처리 지점
* **가이드:** 로직이 복잡하여 한눈에 파악하기 힘든 제어문 직전에는 **"왜(Why) 이렇게 분기했는지"** 혹은 **"이 분기에서 어떤 도메인 정책이 구현되는지"** 한글 주석을 반드시 1~2줄 작성해야 합니다. 단순 코드 복사 수준의 주석(`// i가 1일 때` 등)은 반려 대상입니다.

### 4. 메시징 처리부 주석 규칙 (공통)
* **대상 범위:** 사용자 알림 창(`alert`), 시스템 메시지 토스트(`toast`), 다국어 프로퍼티 파일 변수 호출부, 소켓 통신 메시지 규격 정의 등 화면이나 터미널에 메시지를 노출시키는 모든 구현부
* **필수 작성 규칙:**
    * 사용자 화면에 노출되는 원본 메시지의 문장(또는 공통 메시지 코드에 매핑된 원본 텍스트 내용)을 **해당 메시징 처리 코드의 바로 윗줄에 주석으로 100% 동일하게 그대로 작성**해야 합니다.
    * 이 규칙은 다국어 코드나 메시지 프로퍼티 상수를 읽는 코드(`messageSource.getMessage("ERR_001")` 등)의 실제 화면 노출 텍스트를 개발 및 디버깅 과정에서 소스 코드 상에서 직관적으로 파악할 수 있도록 강제하기 위함입니다.

```javascript
// [주석] 필수 입력 값 누락 시 노출: "아이디와 비밀번호를 모두 입력해 주세요."
alert(getLocaleMessage("VALID_LOGIN_REQUIRED"));
```

---

## 6. API 설계 및 공통 규격 규칙

### 1. REST API URI Naming Rule
* **대소문자 규격:** URI 경로에는 알파벳 대문자를 절대 사용하지 않으며, 소문자만 사용합니다.
* **단어 구분 기호:** 단어 구분을 위해 언더바(`_`)를 사용하는 것을 금지하며, 하이픈(`-`)을 적용합니다. (예: `/api/v1/user-info` `[O]`, `/api/v1/user_info` 및 `/api/v1/userInfo` `[X]`)
* **명사 위주의 경로 설정:** 경로에 리소스를 조작하는 동사를 포함시키는 것을 배제하고, 행위는 HTTP Method(GET, POST, PUT, DELETE)를 조합하여 나타냅니다. (예: `POST /api/v1/book` `[O]`, `POST /api/v1/saveBook` `[X]`)

### 2. 공통 응답 포맷 (Common Response Wrapper)
* 모든 백엔드 컨트롤러가 반환하는 API 응답은 'ResultData' 를 활용하여 아래에 정의된 일관적인 구조의 공통 JSON 포맷을 유지해야 합니다.

```json
{
  "code": "200 | 501",
  "message": "처리가 완료되었습니다. | 필수 파라미터가 누락되었습니다.",
  "data": { ... } // 단건 DTO, List, null 혹은 성공 응답 객체
}
```

---

## 7. React Component & Code Rule

React 프론트엔드 환경에서 일관성 유지와 성능 최적화를 위해 다음 규칙을 필수로 적용합니다.

### 1. 폴더 구조 및 파일 명명 규칙
* **컴포넌트 파일명:** React 화면 및 공통 컴포넌트 파일은 반드시 **PascalCase**를 사용하여 정의합니다. (예: `UserDetail.jsx`, `BookSelector.jsx`)
* **유틸리티 및 비-컴포넌트 파일명:** 헬퍼 함수, 데이터 포맷터, 공통 API 등 순수 JavaScript/TypeScript 로직을 포함한 파일은 **camelCase**로 작성합니다. (예: `authHelper.js`, `dateFormatter.js`)

### 2. State 관리 및 불변성 유지 규칙
* **불변성(Immutability) 강제:** React 상태(State)를 변경할 때 객체/배열의 직접적인 참조 대입 및 수정을 금지합니다. 반드시 스프레드 연산자(`...`), 배열 내장 고차함수(`map`, `filter` 등)를 이용하여 새로운 객체와 배열로 복사본을 만들어 업데이트해야 합니다.

### 3. API 호출 및 예외 처리
* **비동기 예외 격리:** 모든 비동기 API 호출(`axios` 등)은 반드시 `try-catch` 블록으로 예외를 격리해야 합니다.
* **피드백 일관성:** API 통신에서 에러 발생 시 로우 데이터 에러를 그대로 방치하지 않고, 공통 에러 핸들러를 가동하여 사용자에게 규격화된 알림 창(공통 토스트, 공통 에러 모달 등)으로 피드백해야 합니다.

---

## 8. IDE Warning & Lint Error Zero Tolerance Rule

IDE(IntelliJ 등) 내부에서 정적 분석기(ESLint, SonarLint, Code Inspection) 등에 의해 감지되는 모든 코드 경고와 린트 에러를 철저하게 예방하고 관리해야 합니다. 아래 규칙을 지키지 않아 생성되는 "빨간 줄(Error)" 및 "노란/주황 줄(Warning)"은 절대 허용하지 않습니다.

### 1. 에러 및 경고 즉각 수정 (Zero Tolerance)
* 에러 하이라이팅(빨간 줄)뿐만 아니라 잠재적 버그, 사용되지 않는 코드, 최적화 제안 등에 해당하는 경고(노란 줄, 주황 줄) 역시 모두 해결한 뒤 커밋해야 합니다.
* 사용하지 않는 임포트(unused imports), 선언만 하고 사용하지 않은 변수/상수는 반드시 즉시 삭제하십시오.

### 2. React (JavaScript/TypeScript) 린트 규칙
* **ESLint 및 Prettier 연동:** ESLint가 경고하는 모든 항목을 철저히 준수합니다. 특히 `const` 재할당 금지, `===` 엄격한 비교 연산자 사용을 준수해야 합니다.
* **React Hooks 의존성:** `useEffect`, `useCallback`, `useMemo` 등을 사용할 때 내부에서 참조하는 모든 상태값과 함수를 의존성 배열(deps)에 빠짐없이 명시해야 합니다. ESLint의 `react-hooks/exhaustive-deps` 경고를 비활성화(`eslint-disable-next-line`)하는 행위를 엄격히 금지합니다.

### 3. Java 정적 분석 경고 대응
* **불필요한 박싱/언박싱 금지:** Primitive 타입과 Wrapper 타입 간 불필요한 자동 변환(Auto-boxing)으로 인해 경고가 뜨지 않도록 타입을 일치시켜 작성하십시오.
* **하드코딩 금지:** 문자열이나 설정 값이 코드 내에 직접 하드코딩되어 경고(Inspection)가 발생하지 않도록, 공통 상수로 정의하거나 설정 파일(.properties/.yml)로 분리하십시오.
* **안전한 제네릭 사용:** Raw Type으로 객체를 선언하여 발생하는 컴파일러 경고를 절대 방치하지 마십시오. 제네릭 타입을 명확히 정의해야 합니다. (예: `List` -> `List<String>`)

---

## 9. Git Branch & Commit Message 규칙

협업과 롤백의 안전성을 유지하기 위해 형상 관리 작업 시 정량화된 규격을 유지합니다.

### 1. 커밋 메시지 기본 프리픽스
* 모든 커밋 메시지의 도입부는 행위의 목적을 정의하는 표준 머리글(Type)로 시작합니다.

* `feat:` 새로운 기능 구현 및 추가
* `fix:` 오동작 해결 및 버그 수정
* `refactor:` 기능 변경이나 버그 해결 없이 코드 구조 및 내부 아키텍처 개선
* `docs:` Javadoc 작성, 리드미 등 가이드 문서 수정
* `style:` 로직 변경 없이 인덴트 정리, 콤마 수정 등 포맷터 일괄 적용

> **Example:** `feat: 사용자 정보 상세 조회(getUserDtl) 메서드 구현 및 예외 처리 적용`