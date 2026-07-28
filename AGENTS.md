
# Sadari Project Instructions

## Mandatory Rule Loading

모든 요청은 분석, 계획 수립, 파일 조회, 명령 실행, 코드 수정에 앞서 아래 규칙 문서를 모두 읽고 시작한다.

1. `.aiassistant/rules/javaRules.md`
2. `.aiassistant/rules/sqlRules.md`
3. `.aiassistant/rules/scriptRules.md`
4. `.aiassistant/rules/viewRules.md`

요청이 특정 기술 영역에만 해당하더라도 네 문서를 모두 읽는다. 작업 도중 변경된 규칙이 있으면 해당 문서를 다시 읽고 이후 작업에 즉시 반영한다.

## Rule Application

- Java, Spring, Controller, Service, DTO, Mapper 선언체 작업에는 `javaRules.md`를 적용한다.
- SQL, MyBatis XML, DDL, DML 작업에는 `sqlRules.md`를 적용한다.
- TypeScript와 JavaScript의 로직 작업에는 `scriptRules.md`를 적용한다.
- React, TSX, JSX와 화면 표현 작업에는 `viewRules.md`를 적용한다.
- 하나의 파일이나 변경에 여러 영역이 포함되면 관련 규칙을 함께 적용한다.
- 규칙이 충돌하면 보안, 데이터 무결성, 실행 안정성에 더 직접적인 규칙을 우선한다.
- 상위 시스템 지침이나 사용자의 현재 요청과 충돌하는 프로젝트 규칙은 적용하지 않는다.

## Completion Check

작업을 마치기 전에 변경된 파일별로 관련 규칙을 다시 대조하고, 규칙 위반 여부를 검증한 뒤 결과를 보고한다.
