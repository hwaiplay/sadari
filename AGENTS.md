
# Sadari Project Instructions

## Mandatory Rule Loading

모든 요청은 분석, 계획 수립, 상세 파일 조회, 명령 실행 및 코드 수정에 앞서 `.aiassistant/rules/coreRules.md`를 먼저 읽는다.

Core 규칙을 읽은 뒤에는 파일명, 확장자, 디렉터리 구조 및 Git 변경 목록만 확인하여 작업 영역을 식별할 수 있다. 상세 소스 내용은 아래 표에서 해당하는 영역 규칙을 모두 읽은 뒤 확인한다.

| 작업 대상 | 추가로 읽을 규칙 |
| --- | --- |
| Java, Spring, Controller, Service, DTO, Java Mapper 선언체 | `.aiassistant/rules/javaRules.md` |
| SQL, MyBatis XML, DDL, DML, DB 스크립트 | `.aiassistant/rules/sqlRules.md` |
| TypeScript, JavaScript 및 화면 로직 | `.aiassistant/rules/scriptRules.md` |
| React, TSX, JSX | `.aiassistant/rules/viewRules.md`와 `.aiassistant/rules/scriptRules.md` |
| CSS, SCSS 및 화면 스타일 | `.aiassistant/rules/viewRules.md` |
| PowerShell, Bash 및 배포 스크립트 | `.aiassistant/rules/scriptRules.md` |
| `docs` 아래 정책, 분석, 설계, 성능, 배포 및 운영 Markdown | `.aiassistant/rules/reportRules.md` |
| YML, 환경변수, GitHub Actions, Docker 및 배포 설정 | `.aiassistant/rules/deploymentRules.md` |
| `AGENTS.md`, `.aiassistant/rules` 아래 규칙 문서 | 수정 대상 규칙 문서 |

하나의 파일이나 변경에 여러 영역이 포함되면 관련 규칙을 모두 읽고 함께 적용한다. 작업 도중 Core 또는 활성 규칙이 변경되면 변경된 문서를 다시 읽고 이후 작업에 즉시 반영한다.

## Active Rule Declaration

파일을 수정하기 전에 이번 작업에 적용할 규칙 문서와 규칙 ID를 사용자에게 간단히 알린다. 단순 조회 요청은 최종 답변에서 근거가 된 규칙 ID를 필요할 때만 보고한다.

## Rule Application

- 규칙 강제 수준은 `MUST`, `SHOULD`, `MAY`로 구분한다.
- `MUST`는 명시된 예외가 없으면 반드시 적용한다.
- `SHOULD`를 적용하지 않으면 최종 결과에 이유를 보고한다.
- 규칙이 충돌하면 `CORE-PRIORITY-001`에 따라 상위 지침, 보안, 데이터 무결성, 실행 안정성 순으로 판단한다.
- 규칙 ID는 완료 보고와 재발 방지 기록에서 안정적인 참조값으로 사용한다.

## Account Lifecycle Policy Gate

새로운 기능 또는 데이터 처리 범위 확장은 `CORE-ACCOUNT-001`을 적용한다. 사용자와 정책을 확정하기 전에는 관련 구현을 시작하지 않는다.

## Rule Maintenance

- 기존 규칙으로 다룰 수 없는 기준이나 재발 방지 규칙이 필요하면 `CORE-MAINT-001`에 따라 같은 작업에서 규칙을 갱신한다.
- 여러 영역의 공통 기준은 `coreRules.md`에 한 번만 정의하고 영역 규칙에서는 Core 규칙 ID를 참조한다.
- 영역에만 적용되는 기준은 해당 영역 규칙에 추가한다.
- 규칙을 추가하거나 변경할 때는 적용 대상, 강제 수준, 예외 및 검증 방법을 함께 기록한다.

## Completion Check

작업을 마치기 전에 변경 파일별 활성 규칙을 다시 대조하고 `scripts/verify-project-rules.ps1`을 실행한다. Java 또는 프론트엔드 실행 결과에 영향을 주는 변경은 가능한 경우 `scripts/verify-project-rules.ps1 -Full`도 실행한다.

최종 결과에는 다음 항목을 간단히 보고한다.

1. 적용한 주요 규칙 ID
2. 실행한 검증과 결과
3. 실행하지 못한 검증 또는 남은 위험
