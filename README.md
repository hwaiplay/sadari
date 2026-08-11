# Sadari

> 읽고, 기록하고, 함께 성장하는 독서 기록 플랫폼

[![Sadari CI/CD](https://github.com/hwaiplay/sadari/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/hwaiplay/sadari/actions/workflows/ci-cd.yml)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=20232A)
![Docker](https://img.shields.io/badge/Docker-GHCR%20%7C%20EC2-2496ED?logo=docker&logoColor=white)

Sadari는 도서 검색, 독서 기록과 목표, 소셜 활동, 독서 모임, 알림과 웹 푸시를 하나의 흐름으로 연결한 서비스입니다. 이 저장소는 사용자용 React PWA와 Spring Boot API를 함께 관리하며, 별도 [관리자 서비스](https://github.com/vellahw/sadari-admin)가 공통 운영 테이블을 통해 서비스의 제어면을 담당합니다.

## 프로젝트 핵심

| 관점 | 구현 내용 |
| --- | --- |
| 데이터 일관성 | 도서 마스터와 독후감 등록을 하나의 트랜잭션으로 처리하고, DB 커밋 결과에 맞춰 푸시 발송과 파일 정리를 수행합니다. |
| 인증과 세션 | Kakao OAuth 2.0, JWT Access/Refresh Token, HttpOnly Cookie, Redis 세션 메타데이터와 로그아웃 블랙리스트를 결합했습니다. |
| 인증과 세션 | Kakao OAuth 2.0, JWT, HttpOnly Cookie, CSRF Token, Redis 세션 메타데이터와 로그아웃 블랙리스트를 결합했습니다. |
| 조회 성능 | 마이페이지 독서 요약 SQL 왕복을 최대 19회에서 2회로 통합했습니다. 이는 소스 기준 호출 횟수 감소이며 응답 시간 개선율을 뜻하지 않습니다. |
| 운영 가능성 | 관리자가 공통 코드, 알림 템플릿, 사용자 메뉴와 스케줄러 상태를 관리하고 실행·실패 로그를 확인할 수 있습니다. |
| 서비스 간 정합성 | 관리자 회원 상태 변경을 DB Outbox에 기록하고, 사용자 서비스가 성공한 이벤트만 삭제해 Redis 반영 실패를 재시도합니다. |
| 파일 보안 | 이미지 시그니처·디코더·해상도를 검증하고 EXIF 방향 보정과 재인코딩을 거쳐 비공개 S3 또는 로컬 저장소에 보관합니다. |
| 운영 가능성 | 관리자가 공통 코드, 알림 템플릿, 사용자 메뉴와 스케줄러 상태를 관리하고 실행 건수·처리 시간·실패 원인을 확인할 수 있습니다. |
| 배포 자동화 | 멀티 스테이지 Docker 빌드, GHCR 이미지 배포, EC2 Docker Compose 갱신과 HTTP 상태 확인을 GitHub Actions로 구성했습니다. |

## 숫자로 보는 현재 구현

아래 값은 현재 저장소의 소스 구조에서 계산한 값이며 운영 성능 측정값이 아닙니다.

| 항목 | 소스 기준 값 | 의미 |
| --- | ---: | --- |
| 운영 Java 소스 | 154개 | API, 도메인 서비스, 보안, 스케줄러와 저장소 구현 |
| REST Controller | 19개 | 도서·기록·소셜·모임·알림·계정 등 사용자 API |
| MyBatis Mapper XML | 23개 | 업무 SQL을 도메인별로 분리 |
| `@Test` 메서드 | 102개 | 서비스, 보안, 파일, 스케줄러와 API 계약 검증 자산 |
| 최상위 도메인 패키지 | 14개 | `book`, `report`, `social`, `readingClub`, `user`, `global` 등 |
| 독서 요약 SQL 왕복 | 최대 19회 → 2회 | 소스 경로 기준 약 89.5% 감소 |

## 주요 기능

| 영역 | 사용자 경험 | 백엔드 구현 |

    API --> MYSQL[("MySQL 8.4")]
    ADMIN_APP --> MYSQL
    MYSQL --> OUTBOX["회원 상태 Outbox"]
    OUTBOX --> SCHEDULER["제한 배치 / 실패 재시도"]
    SCHEDULER --> REDIS
    API <--> REDIS[("Redis 7")]
    API --> S3["Private S3 / Local Storage"]
    API --> FCM["Firebase Cloud Messaging"]

`JwtFilter`는 서명과 만료만 확인하지 않고 Redis의 계정 상태도 함께 검사합니다. 비활성화 계정과 영구 탈퇴 대기 계정은 허용된 복구·취소 흐름을 제외한 API 접근이 제한됩니다.

쿠키 인증을 사용하더라도 CSRF 보호를 끄지 않았습니다. 상태 변경 요청은 서버가 발급한 CSRF Token을 `X-XSRF-TOKEN` Header로 검증하며, 브라우저에서 Token 불일치가 발생하면 새 Token을 받은 뒤 원 요청을 한 번만 재시도합니다. 여러 API가 동시에 인증 만료를 감지해도 하나의 Refresh 요청 Promise를 공유해 토큰 재발급 폭주를 막습니다.

- [인증과 보안 설계](docs/portfolio/auth-security.md)
- [Spring Security·CSRF·CORS 설정](src/main/java/org/our/sadari/global/security/config/SecurityConfig.java)
- [Redis 토큰·사용자 상태 관리](src/main/java/org/our/sadari/global/security/jwt/TokenRedisService.java)
- [JWT 요청 필터](src/main/java/org/our/sadari/global/security/jwt/JwtFilter.java)
- [CSRF·Refresh 단일 요청 처리](src/main/frontend/src/app/api/axios.ts)
- [계정 수명주기 정책](docs/policies/withdrawal-policy.md)

### 3. DB와 외부 시스템의 완료 시점을 분리
| 스케줄러 설정 | 실행 가능 여부와 배치 제한 확인 |
| 스케줄러 로그 | 실행 결과, 처리 건수와 실패 원인 기록 |

사용자 메뉴는 `TM_URMENU`의 관리자 설정을 MySQL 재귀 CTE로 최대 3단계까지 조립합니다. 현재 URL과 가장 길게 일치하는 메뉴를 찾고 활성·노출 상태가 유효한 하위 트리만 반환하므로, 사용자 앱을 다시 배포하지 않고도 메뉴 구조와 노출 여부를 바꿀 수 있습니다.

- [사용자·관리자 연동 구조](docs/portfolio/admin-user-integration.md)
- [사용자 메뉴 재귀 조회 SQL](src/main/java/org/our/sadari/menu/mapper/UserMenuMapper.xml)
- [스케줄러 정책](docs/policies/scheduler-policy.md)

### 5. 서비스 문제에 맞춘 알고리즘 적용
### 5. DB Outbox로 관리자 상태 변경을 Redis에 전달

관리자 서비스가 사용자를 정지하거나 해제할 때 DB 원본만 바뀌고 사용자 서비스의 Redis 상태가 남는 문제를 별도 이벤트 전달 흐름으로 해결했습니다.

```mermaid
sequenceDiagram
    participant A as Sadari Admin
    participant DB as MySQL
    participant B as User Status Scheduler
    participant R as Redis

    A->>DB: 회원 상태와 Outbox 이벤트를 같은 트랜잭션으로 저장
    B->>DB: 등록 순서대로 제한 건수 조회
    B->>DB: 처리 시점의 최신 회원 상태 조회
    B->>R: 로그인 세션 상태 갱신 또는 제거
    alt Redis 반영 성공
        B->>DB: 동기화 완료 표시 후 이벤트 삭제
    else Redis 반영 실패
        B-->>DB: 이벤트 유지
        Note over B,DB: 다음 실행 주기에 재시도
