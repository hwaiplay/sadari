
# Sadari Project Instructions

## Mandatory Rule Loading

모든 요청은 분석, 계획 수립, 파일 조회, 명령 실행, 코드 수정에 앞서 아래 규칙 문서를 모두 읽고 시작한다.

1. `.aiassistant/rules/javaRules.md`
2. `.aiassistant/rules/sqlRules.md`
3. `.aiassistant/rules/scriptRules.md`
4. `.aiassistant/rules/viewRules.md`
5. `.aiassistant/rules/reportRules.md`
6. `.aiassistant/rules/deploymentRules.md`

요청이 특정 기술 영역에만 해당하더라도 여섯 문서를 모두 읽는다. 작업 도중 변경된 규칙이 있으면 해당 문서를 다시 읽고 이후 작업에 즉시 반영한다.

## Rule Application

- Java, Spring, Controller, Service, DTO, Mapper 선언체 작업에는 `javaRules.md`를 적용한다.
- SQL, MyBatis XML, DDL, DML 작업에는 `sqlRules.md`를 적용한다.
- TypeScript와 JavaScript의 로직 작업에는 `scriptRules.md`를 적용한다.
- React, TSX, JSX와 화면 표현 작업에는 `viewRules.md`를 적용한다.
- 정책, 성능 개선, 기술 분석, 설계, 배포 및 운영 보고성 Markdown 문서 작업에는 `reportRules.md`를 적용한다.
- YML, 환경변수, GitHub Actions, Docker 및 배포 설정 작업에는 `deploymentRules.md`를 적용한다.
- 하나의 파일이나 변경에 여러 영역이 포함되면 관련 규칙을 함께 적용한다.
- 규칙이 충돌하면 보안, 데이터 무결성, 실행 안정성에 더 직접적인 규칙을 우선한다.
- 상위 시스템 지침이나 사용자의 현재 요청과 충돌하는 프로젝트 규칙은 적용하지 않는다.

## Account Lifecycle Policy Gate

- 새로운 기능을 추가하거나 기존 기능의 데이터 처리 범위를 확장할 때마다 구현 전에 계정 비활성화와 영구 탈퇴 시 해당 기능의 데이터 및 화면을 어떻게 처리할지 사용자에게 반드시 질문한다.
- 사용자와 함께 접근 제한, 공개 범위, 데이터 보존 또는 삭제, 복귀 시 복원 범위, 알림 및 소셜 관계 영향을 결정한 뒤 구현한다.
- 결정된 내용은 같은 작업에서 `docs/policies/withdrawal-policy.md`와 해당 기능의 정책 Markdown 문서에 추가하고 코드 및 테스트에 반영한다.
- 사용자가 정책을 확정하지 않은 항목은 기존 정책으로 임의 추정하거나 구현하지 않는다.
- 현재 계정 비활성화는 `WITHDRAWN` 상태이며 재로그인하면 `ACTIVE`로 전환하지만 비활성화 과정에서 비공개·삭제·중지된 독후감 공개 설정, 댓글, 알림 및 푸시 구독은 자동 복원하지 않는다.
- 현재 영구 탈퇴는 `DELETE_PENDING` 상태로 기본 30일 유예하며 유예기간 안에는 취소할 수 있고, 유예기간이 지나 물리 삭제된 계정과 데이터는 복구하지 않는다.
- 계정 수명주기의 상세 기준은 `docs/policies/withdrawal-policy.md`를 단일 기준 문서로 사용한다.

## Rule Maintenance

- 작업 중 기존 규칙으로 다룰 수 없는 새로운 기준이나 재발 방지 규칙이 필요하면 코드만 수정하고 끝내지 않는다.
- 새 규칙은 적용 대상에 따라 `javaRules.md`, `sqlRules.md`, `scriptRules.md`, `viewRules.md`, `reportRules.md`, `deploymentRules.md` 중 해당하는 규칙 파일에 같은 작업에서 추가한다.
- 여러 기술 영역에 공통으로 적용되는 규칙이면 관련된 모든 규칙 파일에 반영하거나 공통 적용 위치를 명확히 정한다.
- 규칙을 추가하기 전에 기존 규칙과 중복되거나 충돌하는지 확인하고, 기존 항목을 확장할 수 있으면 새로운 항목을 중복 생성하지 않는다.
- 새 규칙은 빠른 탐색 목차와 기존 중요도 순서를 유지하며 가장 관련 있는 절에 배치한다.
- 규칙 추가 후에는 변경된 규칙 파일을 다시 읽고 현재 작업 결과에도 즉시 적용한다.

## Completion Check

작업을 마치기 전에 변경된 파일별로 관련 규칙을 다시 대조하고, 규칙 위반 여부를 검증한 뒤 결과를 보고한다.
