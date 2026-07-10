# Project Naming and Coding Conventions

이 문서는 프로젝트의 데이터베이스 및 백엔드(Java) 개발 시 AI와 개발자가 반드시 준수해야 하는 엄격한 명명 규칙(Naming Convention)을 정의합니다. 코드를 생성하거나 수정할 때 아래의 규칙을 예외 없이 적용하십시오.

## 1. Database Table Naming Rule
테이블 명칭은 성격에 따라 접두사를 구분하며, 접두사 이후의 글자 수를 엄격하게 제한합니다.

* **마스터 테이블 (Master Data):** `TM_` 로 시작
* **파생/트랜잭션 테이블 (Derived Data):** `TB_` 로 시작
* **길이 및 패딩 규칙:**
    * 접두사(`TM_`, `TB_`) 뒤에는 **반드시 영문 6자리**가 와야 합니다.
    * 의미를 담기에 글자 수가 6자리보다 짧거나 애매한 경우, 남는 뒷자리는 대문자 `X`로 채워서 6자리를 맞춥니다.
* **Examples:**
    * `TM_USERXM` (TM_ + USERXM 6자)
    * `TM_ADMINX` (TM_ + ADMINX 6자)

## 2. Database Column Naming Rule
컬럼 명칭은 언더바(`_`)를 기준으로 앞뒤 글자 수를 고정합니다.

* **형식 규칙:** `[영문 4자]_[영문 4자]` (예: `AAAA_BBBB`)
* **길이 및 패딩 규칙:**
    * 언더바 앞과 뒤는 각각 **반드시 영문 4자리**여야 합니다.
    * 단어의 길이가 4자리보다 짧은 경우, 남는 뒷자리는 대문자 `X`로 채워서 4자리를 맞춥니다.
* **필수 : 컬럼:**
    * REGI_ADMN: 등록자
    * REGI_DATE: 등록일
    * UPDT_ADMN: 수정자 
    * UPDT_DATE: 수정일
* **고정으로 사용해야할 컬럼 단어:**
  * 사용자: USER
  * 사용: USEE
  * 'Y'나 'N'으로 여부 표시: YSNO
  * 등록: REGI(등록일 -> REGI_DATE)
  * 관리자: ADMN(관리자 아이디 - > IDXX)
* **Examples:**
    * `USER_IDXX` (`USER` 4자 + `_` + `IDXX` 4자)
    * `MENU_NMSX` (`MENU` 4자 + `_` + `NMSX` 4자)

## 3. SQL SQL 쿼리 들여쓰기 및 포맷팅 가이드

#### 1. 핵심 규칙: 키워드 우측 정렬
* SQL의 주요 절(Clause)을 나타내는 키워드들의 우측 끝을 가상의 세로선에 맞추어 정렬합니다.
* `SELECT`, `FROM`, `LEFT JOIN`, `WHERE`, `ORDER BY` 등 길이가 다른 키워드들의 오른쪽 끝이 한 줄로 일치합니다.
* 키워드 뒤에 오는 컬럼명, 테이블명, 조건식 등은 모두 동일한 시작 위치(왼쪽 정렬)를 가집니다.

#### 2. SELECT 절 및 컬럼 포맷팅
* **1줄 1컬럼:** `SELECT` 절의 각 컬럼은 반드시 새로운 줄에 작성합니다.
* **선행 콤마 (Leading Comma):** 컬럼을 구분하는 콤마(`,`)는 뒷줄이 아닌 새로운 줄의 맨 앞에 위치시킵니다.
* **콤마 정렬:** 이 콤마 역시 키워드 우측 정렬 기준선에 맞추어 배치합니다.

#### 3. JOIN 및 조건 절
* `LEFT JOIN` 키워드 역시 우측 정렬 기준선에 맞춥니다.
* JOIN의 조건인 `ON`과 추가 조건인 `AND` 역시 우측 정렬하여 키워드의 오른쪽 끝을 기준선에 맞춥니다.

#### 4. WHERE 및 ORDER BY 절
* `WHERE` 키워드와 내부 조건인 `AND`도 동일한 우측 정렬 규칙을 따릅니다.
* 조건식 내부의 괄호 `(`는 텍스트(데이터) 시작 위치에 맞추어 정렬합니다.
* `ORDER BY` 절 역시 우측 정렬되어 있으며, 정렬 대상 컬럼은 다른 구문들과 동일한 세로선에서 시작합니다.

#### 5. 기타 코드 컨벤션
* **테이블 별칭(Alias):** 테이블명 뒤에 `AS` 없이 띄어쓰기 후 짧은 알파벳 별칭(`M`, `R`)을 일관되게 사용합니다.
* **변수 바인딩:** `#{authCode}`, `#{yes}` 등 MyBatis(또는 유사 프레임워크)의 파라미터 바인딩 문법을 쿼리 내에 배치합니다.
* **특수문자 처리:** 부등호(`<=`) 사용 시 XML 파싱 에러를 방지하기 위해 `<![CDATA[ <= ]]>` 구문을 조건식 내부에 인라인으로 사용합니다.
* 
키워드 정렬 부분 | 데이터(컬럼/테이블/조건) 정렬 부분
------------------------|------------------------------------
               SELECT   | M.MENU_NUMB
                    ,   | M.SUBX_NUMB
                    ,   | M.MENU_NAME
                 FROM   | TM_MENUXM M
            LEFT JOIN   | TB_CODEXD R
                   ON   | R.COMM_CODE = #{authCode}
                  AND   | R.COMD_CODE = M.READ_AUTH
                WHERE   | NVL(M.USEE_YSNO, #{yes}) = #{yes}
                  AND   | (M.READ_AUTH IS NULL OR ... )
             ORDER BY   | NVL(M.SORT_ORDR, 9999)

참고:
C:\Users\USER\IdeaProjects\sadari-admin\src\main\java\org\sadari\admin\sadariadmin\common\code\mapper\CodeMapper.xml

## 4. Java Method Naming Rule
비즈니스 로직 및 데이터베이스 접근(DAO/Repository)을 처리하는 메서드는 수행하는 기능(Action)에 따라 접두사(Prefix)와 접미사(Suffix) 규칙을 준수해야 합니다. (`***`는 해당 도메인 또는 엔티티의 명칭을 의미합니다.)

* **다건 조회 (List):** `get***List`
    * 예: `getUserList()`, `getBoardList()`
* **카운트 조회 (Count):** `get***Cnt`
  * 예: `getUserCnt()`, `getBoardCnt()`
* **단건 조회 (Detail):** `get***Dtl`
    * 예: `getUserDtl()`, `getBoardDtl()`
* **데이터 등록 (Insert):** `set***`
    * 예: `setUser()`, `setBoard()`
* **데이터 수정 (Update):** `upt***`
    * 예: `uptUser()`, `uptBoard()`
* **데이터 삭제 (Delete):** `del***`
    * 예: `delUser()`, `delBoard()`
* **중복 검사 (Duplicate Check):** `dup***`
    * 예: `dupUser()`, `dupEmail()`

## 5. 주석(Javadoc) 작성 규칙 가이드

#### 1. 기본 구조 (Block Comment)
* 주석의 시작은 `/**`로, 끝은 `*/`로 묶어서 작성합니다.
* 내부의 각 줄은 `*`(별표)로 시작하며, 시작 부분의 `/**` 중 두 번째 `*`와 세로 위치를 일치시킵니다.
* `*` 기호 뒤에는 들여쓰기와 가독성을 위해 반드시 하나의 공백(Space)을 둡니다.
* 이모티콘 사용은 금지한다.

#### 2. 요약 설명 (Description)
* 주석 블록의 첫 번째 줄(태그가 나오기 전)에는 해당 메서드나 클래스의 핵심 역할(예: `Redis 인증 필터 생성`)을 간결하게 작성합니다.

#### 3. 애노테이션 태그(Tag) 규칙
* 요약 설명 아래부터는 `@` 기호를 사용하여 표준 메타데이터 태그들을 순서대로 작성합니다.
* **`@Author`**: 코드를 작성한 담당자 또는 생성자를 명시합니다. (예: `SeungHyeon.Kang` - 이름과 성 사이에 마침표를 사용하는 네이밍 룰 적용)
* **`@param`**: 메서드가 입력받는 파라미터 변수명을 기재합니다. (예: `adminRedisAuthService`)
* **`@return`**: 메서드의 최종 반환값에 대해 명시합니다. (제시된 예시처럼 변수명이나 설명 없이 태그만 선언해 둘 수도 있습니다.)