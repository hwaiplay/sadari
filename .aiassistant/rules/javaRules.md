---
apply: always
---

# Java Rules

이 문서는 Sadari 프로젝트의 Java 백엔드, REST API, 트랜잭션, 주석 및 정적 분석 규칙을 정의합니다.
Java 코드를 생성하거나 수정할 때 아래 규칙을 예외 없이 적용합니다.
규칙은 보안과 실행 안정성, 데이터 무결성, API 계약, 문서화, 명명과 형식, 형상 관리 순으로 중요도가 높은 항목부터 나열합니다.

## 1. 공통 주석 작성 규칙

- 일반 주석과 Javadoc은 모두 `한다`, `이다`, `된다` 형태의 반말 서술체로 작성합니다.
- 주석에 `합니다`, `입니다`, `됩니다` 형태의 존댓말을 사용하지 않습니다.
- 주석에 이모티콘과 이모지를 사용하지 않습니다.
- 주석에 느낌표와 물음표를 사용하지 않습니다.
- 사용자에게 노출되는 실제 문구를 인용한 주석은 접두사 없이 원문만 작성하며 문구 안의 문체와 문장부호는 예외로 둡니다.
- 코드 내용을 그대로 읽는 주석은 작성하지 않고 구현 이유와 업무 정책을 설명합니다.

## 2. 설정 및 보안 상수

- URL, 포트, 비밀값, 만료 시간 등 환경별 설정을 Java 코드에 하드코딩하지 않습니다.
- 환경별 값은 `.yml` 또는 환경변수로 관리하고 `@Value` 또는 설정 바인딩을 통해 읽습니다.
- 반복되는 도메인 코드는 공통 상수 또는 공통코드로 관리합니다.
- 사용자 입력값이 SQL의 `${}` 치환에 사용될 경우 Java 계층에서 허용 목록을 먼저 검증합니다.

## 3. 검증 및 예외 처리

- 외부 API 응답, DB 조회 결과, 메서드 파라미터를 참조하기 전에 Null 또는 빈 값 여부를 검증합니다.
- 문자열, 객체, 컬렉션, 배열의 Null 또는 빈 값 검증에는 `StringUtil.isEmpty`를 우선 사용합니다.
- `value != null`, `value == null`처럼 직접 Null을 비교하지 않고 각각 `!StringUtil.isEmpty(value)`, `StringUtil.isEmpty(value)`를 사용합니다.
- Primitive처럼 `StringUtil.isEmpty`를 적용할 수 없거나 외부 라이브러리 계약상 Null 자체만 구분해야 하는 경우에만 직접 Null 비교를 허용합니다.
- 여러 값 중 하나라도 비어 있는지 검사할 때는 `StringUtil.hasEmpty`를 사용합니다.
- `Optional` 또는 `StringUtil.isEmpty`를 사용하여 `NullPointerException`을 방지합니다.
- `set***`, `upt***` 메서드는 진입 직후 필수 파라미터의 Null 및 빈 문자열 여부를 검증합니다.
- 외부 API 통신과 비동기 처리 전후에는 예외 경로를 포함한 처리 흐름을 명확히 작성합니다.
- 사용자에게 반환하는 실패 응답은 원시 예외를 노출하지 않고 프로젝트 공통 응답과 메시지를 사용합니다.

## 4. 트랜잭션 규칙

- 비즈니스 Service 구현체에는 기본적으로 `@Transactional(readOnly = true)`를 선언합니다.
- 데이터가 변경되는 `set***`, `upt***`, `del***` 메서드에만 `@Transactional`을 개별 선언합니다.
- 쓰기 메서드의 일부 단계가 실패하면 전체 변경이 롤백되도록 트랜잭션 경계를 Service 계층에 둡니다.
- 조회 로직에 불필요한 쓰기 트랜잭션을 적용하지 않습니다.

## 5. 메서드 파라미터 정렬

- 어노테이션이 없는 일반 파라미터 1개는 `1`로 계산합니다.
- 어노테이션이 1개 선언된 파라미터 1개는 `1.5`로 계산합니다.
- 한 파라미터에 어노테이션이 2개 선언되면 해당 파라미터 1개를 `3`으로 계산합니다.
- 어노테이션이 2개 선언된 파라미터 뒤에는 다른 파라미터를 추가하지 않고 다음 파라미터부터 줄바꿈합니다.
- 한 줄에 작성한 파라미터 가중치의 합계는 최대 `3.5`까지 허용합니다.
- 다음 파라미터를 추가했을 때 가중치 합계가 `3.5`를 초과하면 해당 파라미터부터 다음 줄에 작성합니다.
- 어노테이션이 있는 파라미터는 한 줄에 최대 2개까지만 작성합니다.
- 어노테이션이 없는 파라미터는 한 줄에 최대 3개까지만 작성합니다.
- 가중치 합계가 `3.5` 이하더라도 파라미터 종류별 최대 개수에 도달하면 다음 줄로 줄바꿈합니다.
- 일반 파라미터와 어노테이션 파라미터가 섞인 경우에도 선언 순서를 유지하면서 앞에서부터 가중치를 계산합니다.
- 두 번째 줄부터는 줄의 맨 앞에 선행 콤마를 작성합니다.
- 줄바꿈한 모든 줄의 선행 콤마와 첫 번째 파라미터 시작 위치를 각각 세로로 일치시킵니다.
- 파라미터에 어노테이션이 있으면 어노테이션부터 해당 파라미터의 시작 위치로 간주합니다.
- 로그인 사용자 식별값에는 `@AuthenticationPrincipal`을 명시합니다.
- 외부 요청 DTO에는 용도에 맞게 `@Valid`와 `@RequestBody`를 명시합니다.

```java
public ResultData setReport(@AuthenticationPrincipal Long userNumb, Long reptNumb, String sourceType
                          , @Valid @RequestBody ReportDto reportDto
                          , @RequestParam(required = false) String requestType, HttpServletRequest request) {
    // 독후감 등록에 필요한 사용자 번호와 요청 데이터를 서비스에 전달한다
    return reportService.setReport(userNumb, reptNumb, sourceType, reportDto, requestType, request);
}
```

위 예시의 첫 번째 줄은 `1.5 + 1 + 1 = 3.5`, 두 번째 줄은 `3`, 세 번째 줄은 `1.5 + 1 = 2.5`로 계산합니다.

## 6. REST API 규칙

- URI에는 영문 소문자만 사용합니다.
- 단어 구분에는 하이픈(`-`)을 사용하며 언더바와 camelCase를 사용하지 않습니다.
- URI는 명사형 리소스로 작성하고 행위는 HTTP Method로 표현합니다.
- 예: `POST /api/v1/book`은 허용하며 `POST /api/v1/saveBook`은 허용하지 않습니다.
- 모든 Controller 응답은 `ResultData`를 사용합니다.

```json
{
  "code": 200,
  "message": "처리가 완료되었습니다.",
  "data": {}
}
```

## 7. Javadoc 규칙

- 모든 클래스 선언 바로 위에는 파일 정보와 변경 이력을 확인할 수 있는 클래스 Javadoc을 작성합니다.
- 클래스 Javadoc은 `fileName`, `author`, `date`, `description` 순서로 파일 정보를 작성합니다.
- `fileName`에는 확장자를 제외한 실제 클래스명을 작성합니다.
- `author`에는 작성자의 영문 성과 이름 사이를 마침표로 구분하여 작성합니다. 예: `SeungHyeon.Kang`
- `date`에는 클래스 최초 생성일을 `yyyy-MM-dd` 형식으로 작성합니다.
- `description`에는 해당 클래스의 역할을 한 문장으로 작성합니다.
- 파일 정보 아래에는 구분선과 `DATE`, `AUTHOR`, `NOTE` 열을 사용하여 변경 이력을 작성합니다.
- 변경 이력의 최초 행에는 클래스 생성일, 최초 작성자, `최초 생성` 문구를 작성합니다.
- 클래스를 수정할 때 기존 변경 이력을 삭제하거나 덮어쓰지 않고 새 이력을 마지막 행에 추가합니다.
- 변경 이력의 `NOTE`에는 변경한 기능이나 책임을 짧고 구체적으로 작성합니다.
- 클래스 Javadoc의 항목명, 구분선, 열 순서와 들여쓰기는 아래 형식을 유지합니다.

```java
/**
 * fileName       : BookController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 도서 검색과 도서 평점 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 */
public class BookController {
}
```

- 모든 클래스와 Public 메서드에 Javadoc을 작성합니다.
- 첫 문장에는 클래스 또는 메서드의 핵심 역할을 구체적으로 설명합니다.
- 메서드 JavaDoc에는 메서드명을 반복하거나 `요청을 검증하고 업무 처리 결과를 반환한다`와 같은 공통 문구만 작성하지 않습니다.
- 메서드 JavaDoc에는 도서 검색, 평점 조회, 사용자 수정처럼 메서드가 처리하는 실제 업무 대상과 기능을 반드시 작성합니다.
- DTO, VO, Entity와 일반 클래스의 필드 주석에는 해당 필드가 나타내는 실제 업무 데이터나 설정의 목적을 작성합니다.
- 필드와 중첩 데이터 클래스의 설명은 반드시 `//` 주석으로 작성합니다.
- 필드와 중첩 데이터 클래스의 `//` 주석은 공통 서술체 규칙의 예외로 두며 `도서 제목`, `yyyyMMdd 형식의 도서 출간일`처럼 종결어미 없이 명사형으로 작성합니다.
- 필드에 `@Schema(description = "...")`이 선언되어 있으면 동일한 설명의 `//` 주석을 작성하지 않습니다.
- 필드에 `@Schema`가 없거나 `@Schema.description`이 없을 때만 해당 필드의 `//` 설명 주석을 작성합니다.
- 필드 주석에는 `클래스 내부에서 사용하는 상태 또는 설정 값이다`처럼 어느 필드에도 적용할 수 있는 공통 문구만 작성하지 않습니다.
- 필드 주석에는 도서 제목, 전체 검색 결과 건수, 이미지 URL처럼 값의 구체적인 의미를 작성하고 필요한 경우 형식, 단위, 허용값 또는 데이터 출처를 함께 작성합니다.
- 중첩 클래스의 주석에는 내부 클래스라는 사실만 적지 않고 해당 클래스가 표현하거나 관리하는 데이터의 단위를 작성합니다.
- `@author`, `@param`, `@return`, `@throws`를 해당 항목이 존재할 때 순서대로 작성합니다.
- `@param`과 `@return`에는 대상의 의미를 반드시 작성합니다.
- 작성자 이름의 성과 이름 사이에는 마침표를 사용합니다. 예: `SeungHyeon.Kang`

```java
/**
 * 사용자 식별자로 사용자 상세 정보를 조회한다
 *
 * @author SeungHyeon.Kang
 * @param userId 조회할 사용자 식별자
 * @return 조회된 사용자 상세 정보
 * @throws IllegalArgumentException 사용자 식별자가 비어 있을 때 발생
 */
public UserDto getUserDtl(String userId) {
    // 사용자 식별자는 Mapper 호출 전에 검증하여 불필요한 DB 접근과 Null 조회를 차단한다
    if (StringUtil.isEmpty(userId)) {
        // 비어 있는 사용자 식별자가 Mapper까지 전달되지 않도록 예외를 생성한다
        throw new IllegalArgumentException("사용자 ID는 필수 값입니다.");
    }

    // 검증이 끝난 사용자 식별자로 사용자 상세 정보를 조회한다
    return userMapper.getUserDtl(userId);
}
```

## 8. 로직 주석 규칙

- 모든 메서드 선언의 여는 중괄호 바로 다음에는 빈 줄을 한 줄 작성한 뒤 첫 실행문을 작성합니다.
- 생성자, 일반 메서드, Controller, Service, Mapper 기본 메서드, 익명 클래스와 콜백 메서드에도 메서드 시작 공백 규칙을 동일하게 적용합니다.
- 모든 `return` 문 바로 위에는 반환하는 값이나 반환 목적을 구체적으로 설명하는 한글 주석을 작성합니다.
- `return` 문과 반환 주석 사이에는 빈 줄을 작성하지 않습니다.
- `ResultData.fail(ResultEnum.***)`을 반환할 때는 해당 `ResultEnum.messageKey`가 가리키는 실제 사용자 메시지를 주석에 작성합니다.
- 메시지 치환 인자가 있는 실패 응답은 치환 전 메시지와 치환되는 값의 의미를 주석에 함께 작성합니다.
- `ResultData.success`, DTO, 컬렉션, boolean, 숫자 또는 문자열을 반환할 때도 성공 응답, 조회 데이터, 판정 결과 또는 변환 결과 중 무엇을 반환하는지 작성합니다.
- `반환 결과를 반환한다`, `호출한 계층에 전달한다`처럼 어느 반환문에도 적용할 수 있는 포괄적 주석은 사용하지 않습니다.
- `if`, `else if`, `else`, `switch`, `case`, 삼항 연산자 등 모든 분기 처리 바로 위에는 한글 주석을 반드시 작성합니다.
- `for`, 향상된 `for`, `while`, `do-while`, `try`, `catch`, `finally`, 람다, 익명 클래스 등 새로운 실행 블록 바로 위에는 한글 주석을 반드시 작성합니다.
- 메서드 안에서 중괄호로 새로운 지역 블록을 시작할 때도 해당 블록의 목적을 설명하는 한글 주석을 바로 위에 작성합니다.
- 분기 또는 블록이 단순하더라도 주석 생략을 허용하지 않습니다.
- 주석에는 코드가 무엇을 하는지만 반복하지 말고 해당 분기나 블록이 필요한 이유, 적용되는 도메인 정책, 실패 시 처리 방향을 작성합니다.
- Service, Mapper, 유틸리티, 외부 라이브러리 등 함수를 호출하는 코드 바로 위에는 호출 목적을 설명하는 한글 주석을 반드시 작성합니다.
- DTO, VO, Entity 등에 `set***` 메서드로 값을 넣는 코드 바로 위에는 어떤 업무 값을 설정하는지 설명하는 한글 주석을 반드시 작성합니다.
- 여러 함수 호출 또는 여러 setter가 하나의 동일한 목적을 가지더라도 각 호출 바로 위에 주석을 개별 작성합니다.
- 정규식, 수식, 비트 연산, 외부 API 통신 전후에는 의도를 설명합니다.
- 코드 내용을 그대로 읽는 주석은 작성하지 않습니다.
- 사용자 메시지를 반환하거나 노출하는 코드 바로 위에는 실제 노출 문구를 주석으로 작성합니다.
```java
// 조회 결과가 없으면 이후 로직에서 사용자 정보가 참조되지 않도록 공통 실패 응답을 즉시 반환한다
if (StringUtil.isEmpty(userDto)) {
    // "사용자 정보를 찾을 수 없습니다."
    return ResultData.fail(ResultEnum.USER_NOT_FOUND);
}

// 로그인 사용자 번호를 독후감 조회 조건에 설정한다
reportDto.setUserNumb(userNumb);

// 검증된 조회 조건으로 독후감 목록을 조회한다
List<ReportDto> reportList = reportMapper.getReportList(reportDto);
```

## 9. Swagger 및 OpenAPI 규칙

- 모든 API Controller 클래스에 `@Tag`를 선언합니다.
- `@Tag.name`에는 Swagger 화면의 업무 영역명을, `description`에는 해당 Controller가 제공하는 API 범위를 작성합니다.
- 모든 Controller 엔드포인트에 `@Operation`을 선언합니다.
- `@Operation.summary`에는 API 기능을 짧게, `description`에는 조회·등록 대상과 주요 처리 정책을 구체적으로 작성합니다.
- 요청 파라미터에는 `@Parameter`를 선언하고 `description`과 실제 형식에 맞는 `example`을 작성합니다.
- `@AuthenticationPrincipal`, `HttpServletRequest`, `HttpServletResponse` 등 사용자 입력 항목이 아닌 내부 주입 파라미터에는 `@Parameter(hidden = true)`를 선언합니다.
- 요청 및 응답 DTO 클래스에는 `@Schema(description = "...")`를 선언합니다.
- Swagger에 노출되는 DTO 필드에는 의미가 분명한 `@Schema.description`과 실제 데이터 형식에 맞는 `example`을 작성합니다.
- 코드값 또는 Y/N처럼 허용값이 정해진 필드는 `allowableValues`를 명시합니다.
- 서버 내부에서만 사용하는 DTO 필드는 `@Schema(hidden = true)`를 사용하여 API 요청·응답 계약과 구분합니다.
- 필수 입력값은 Jakarta Validation 어노테이션과 Swagger 스키마의 필수 여부가 서로 모순되지 않게 작성합니다.
- 프로젝트 인증 방식은 `OpenApiConfig`의 `accessTokenCookie` 보안 스키마를 기준으로 유지하며 Controller마다 별도의 인증 스키마를 임의로 만들지 않습니다.
- `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**`는 `SecurityConfig`에서 `ADMIN` 권한만 접근할 수 있도록 유지합니다.
- Swagger 문서 경로를 `permitAll()`에 추가하거나 일반 사용자에게 공개하지 않습니다.
- API URI, 파라미터명, DTO 필드명 또는 동작이 변경되면 관련 Swagger 어노테이션도 같은 변경에서 함께 수정합니다.

```java
@RestController
@RequestMapping("/api/report")
@Tag(name = "독후감", description = "독후감 조회, 등록, 수정 및 삭제 API")
public class ReportController {

    @GetMapping("/{reptNumb}")
    @Operation(summary = "독후감 상세 조회", description = "독후감 번호로 로그인 사용자의 독후감 상세 정보를 조회한다.")
    public ResultData getReportDtl(@Parameter(hidden = true) @AuthenticationPrincipal Long userNumb
                                 , @Parameter(description = "조회할 독후감 번호", example = "1") @PathVariable Long reptNumb) {
        // 로그인 사용자 번호와 독후감 번호로 독후감 상세 정보를 조회한다
        return reportService.getReportDtl(userNumb, reptNumb);
    }
}
```

```java
@Schema(description = "독후감 수정 요청 DTO")
public class ReportDto {

    @Schema(description = "독서 상태 코드", example = "DONE", allowableValues = {"READ", "DONE", "STOP"})
    private String reptStat;
}
```

## 10. 메서드 명명 규칙

메서드명은 기능에 따라 다음 접두사와 접미사를 사용합니다. `find`, `query`, `save`, `remove` 등 임의의 동사는 사용하지 않습니다.

| 기능 | 형식 | 예시 |
| --- | --- | --- |
| 다건 조회 | `get***List` | `getUserList()` |
| 카운트 조회 | `get***Cnt` | `getUserCnt()` |
| 단건 조회 | `get***Dtl` | `getUserDtl()` |
| 등록 | `set***` | `setUser()` |
| 수정 | `upt***` | `uptUser()` |
| 삭제 | `del***` | `delUser()` |
| 중복 검사 | `dup***` | `dupUser()` |
| 배포 | `dist***` | `distUser()` |

## 11. 정적 분석

- IntelliJ의 오류 및 경고, 컴파일 경고를 남기지 않습니다.
- 사용하지 않는 import, 변수, 메서드는 즉시 제거합니다.
- Primitive와 Wrapper 사이의 불필요한 박싱 및 언박싱을 피합니다.
- Raw Type을 사용하지 않고 제네릭 타입을 명확히 선언합니다.
- 경고 억제 어노테이션으로 문제를 숨기지 말고 원인을 수정합니다.
