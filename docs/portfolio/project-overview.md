# 프로젝트 개요와 아키텍처

## 문서 목적

- 목적: Sadari가 해결하는 문제와 전체 시스템 구조를 포트폴리오 관점에서 설명
- 적용 범위: 사용자 기능, 관리자 기능, 애플리케이션 계층, 외부 시스템, 빌드와 실행 구조
- 기준일: 2026-08-11

## 해결하려는 문제

독서 기록 서비스는 단순한 메모 저장만으로는 지속적으로 사용하기 어렵다. Sadari는 다음 흐름을 하나의 서비스로 연결한다.

1. 카카오 도서 API에서 실제 도서를 검색한다.
2. 독서 상태, 기간, 평점, 공개 여부와 기록을 저장한다.
3. 주간·월간·연간 목표와 달성 현황을 확인한다.
4. 다른 사용자의 공개 독후감을 보고 팔로우와 좋아요로 관계를 형성한다.
5. 목표 기간 초과, 좋아요, 팔로우 상황을 알림센터와 웹 푸시로 전달한다.
6. 계정 상태와 탈퇴 정책까지 서비스 수명주기로 관리한다.

이 프로젝트의 포트폴리오 가치는 화면 수보다 각 기능을 데이터, 인증, 알림, 운영 정책과 연결해 실제 서비스 형태로 완성한 데 있다.

## 두 애플리케이션의 역할

| 애플리케이션 | 역할 |
| --- | --- |
| `sadari` | 사용자 로그인, 도서·독후감·목표·소셜·알림·탈퇴와 스케줄러 실행 |
| `sadari-admin` | 공통코드, 알림 템플릿, 사용자 메뉴, 관리자 권한과 스케줄러 로그 관리 |

두 저장소는 HTTP로 직접 결합하지 않고 동일 MySQL 운영 테이블을 공유한다. 관리자가 운영값을 등록하면 사용자 서비스가 다음 요청이나 스케줄러 실행에서 이를 읽고, 사용자 스케줄러가 기록한 실행 결과는 관리자 화면에서 다시 조회한다.

상세 구조는 [관리자와 사용자 서비스 연동](admin-user-integration.md)에 정리했다.

## 기술 스택

| 계층 | 기술 |
| --- | --- |
| 백엔드 | Java 17, Spring Boot 4.0.3, Spring MVC, Spring Security |
| 인증 | Kakao OAuth, JJWT 0.13.0, HttpOnly Cookie, Redis |
| 데이터 | MySQL 8.4, MyBatis 4.0.1, HikariCP |
| 프론트엔드 | React 19, TypeScript 5.9, Vite 7 |
| 상태 관리 | TanStack Query, Zustand |
| 스타일 | vanilla-extract |
| 알림 | Firebase Admin SDK, Firebase Web SDK, Service Worker |
| API 문서 | springdoc OpenAPI 3, Swagger UI |
| 배포 | Gradle, Docker, Docker Compose, GitHub Actions, GHCR, EC2 |

버전 근거는 `build.gradle`과 `src/main/frontend/package.json`이다.

관리자 애플리케이션은 Java 17, Spring Boot 4.1.0, Spring Security, Redis, MySQL 8.4·MyBatis와 React 19, TypeScript 6, Vite 8을 사용한다. 버전 근거는 `../sadari-admin/build.gradle`과 `../sadari-admin/src/main/frontend/package.json`이다.

## 논리 아키텍처

```text
Browser / PWA
    |
    | HTTPS API, HttpOnly Cookie
    v
React + TypeScript
    |
    | Axios / TanStack Query
    v
Spring MVC Controller
    |
    v
Domain Service
    |
    +------ MyBatis Mapper ------ MySQL 8.4
    |
    +------ Token Service ------- Redis
    |
    +------ Kakao / Firebase

Admin Browser
    |
    v
sadari-admin
    |
    +------ Redis Admin Session
    |
    +------ MySQL Shared Operation Tables
                  |
                  +------ common code / template / user menu
                  +------ scheduler master / failure log
```

## 백엔드 구조

백엔드는 기능별 패키지를 기준으로 Controller, Service, Mapper, DTO를 분리한다.

| 도메인 | 주요 책임 |
| --- | --- |
| `book` | 카카오 도서 검색, 도서 정보, 표지 색상 계산 |
| `report` | 독후감 등록·수정·삭제, 상태·평점·기간 검증, 독서 요약 |
| `myPage` | 내 프로필과 독서 활동 집계 |
| `social` | 팔로우, 좋아요, 공개 프로필과 관계 상태 |
| `alim` | 템플릿 알림 저장, 조회, 읽음과 삭제 |
| `push` | FCM 설정, 구독 토큰, 푸시 발송 |
| `user` | 사용자 프로필, 닉네임, 로그인 이력, 탈퇴 |
| `global` | 보안, 공통코드, 파일, 예외, 스케줄러 |

관리자 백엔드도 메뉴, 사용자 메뉴, 공통코드, 알림 템플릿, 권한 그룹, 관리자 권한 부여, 스케줄러 로그 도메인별로 Controller, Service, Mapper, VO를 분리한다.

대표 구현 경로:

- `src/main/java/org/our/sadari/report/service/ReportServiceImpl.java`
- `src/main/java/org/our/sadari/social/service/SocialServiceImpl.java`
- `src/main/java/org/our/sadari/alim/service/AlimServiceImpl.java`
- `src/main/java/org/our/sadari/global/security/jwt/JwtFilter.java`
- `src/main/java/org/our/sadari/global/scheduler/Scheduler.java`

## 프론트엔드 구조

프론트엔드는 애플리케이션 공통 기능, 재사용 기능, 화면을 구분한다.

| 경로 | 역할 |
| --- | --- |
| `src/main/frontend/src/app` | API 클라이언트, 메시지, PWA, 전역 유틸리티 |
| `src/main/frontend/src/features` | 도메인별 API, Hook, 공통 컴포넌트 |
| `src/main/frontend/src/pages` | 라우트 단위 화면 |
| `src/main/frontend/src/components` | Header, Navigation 등 공통 레이아웃 |

Spring Boot 빌드 과정에서 React 결과물을 `src/main/resources/static`에 포함해 하나의 WAR로 패키징한다. Dockerfile도 프론트 빌드, 백엔드 빌드, 실행 이미지를 단계별로 분리한다.

## 대표 업무 흐름

### 독후감 등록

1. 사용자가 카카오 도서 검색 결과를 선택한다.
2. 프론트가 도서 정보와 독후감 값을 함께 전송한다.
3. 서비스가 상태, 기간, 평점, 기록과 비속어를 검증한다.
4. ISBN 기준 도서 마스터가 없으면 `TM_BKINFO`에 먼저 등록한다.
5. 확보한 도서 번호로 `TM_REPORT`에 독후감을 등록한다.
6. 전체 과정은 `ReportServiceImpl.setReport`의 트랜잭션 안에서 수행한다.

### 로그인

1. Kakao OAuth 콜백에서 사용자 정보를 조회한다.
2. OAuth 식별값을 암호화한 값으로 기존 회원을 조회하거나 신규 회원을 생성한다.
3. Access Token과 Refresh Token을 발급한다.
4. 토큰은 HttpOnly Cookie로 전달하고 Redis에 기기별 `sid` Refresh Token 세션과 회원별 세션 색인을 생성한다.
5. 닉네임은 활성 세션 TTL 캐시로 저장하고 회원 상태는 로그아웃과 분리된 TTL 없는 캐시로 저장한다.
6. 이후 요청은 `JwtFilter`가 토큰, 블랙리스트, `sid` 세션과 회원 상태를 검증한다.
7. 로그아웃은 현재 `sid`만 제거하거나 회원의 모든 `sid`를 제거하는 범위를 사용자가 선택한다.

### 소셜 알림

1. 사용자가 공개 독후감에 좋아요를 누르거나 다른 사용자를 팔로우한다.
2. 관리자가 `sadari-admin`에서 등록한 활성 `TB_ALTEMP` 템플릿을 식별할 상황·템플릿 코드를 사용한다.
3. 소셜 트랜잭션이 성공하면 공통 알림 서비스에 수신자, 상황, 템플릿, 대상 번호, 치환값을 전달한다.
4. 알림 서비스가 관리 템플릿을 조회하고 문구와 링크를 치환해 DB에 저장한다.
5. DB 커밋 후 FCM 푸시 발송을 시도한다.

### 운영 설정과 스케줄러 관찰

1. 관리자가 공통코드에서 스케줄러 사용 여부를 변경한다.
2. 사용자 백엔드가 Cron 실행 직전에 활성 코드를 확인한다.
3. 활성 상태면 제한된 대상만 처리하고 `TL_SCLOGX`, `TL_SCFAIL`에 결과를 기록한다.
4. 관리자는 스케줄러 로그 목록과 상세 화면에서 실행 건수와 실패 원인을 조회한다.

## 설계상 강점

- 외부 도서 데이터와 내부 독후감의 정합성을 ISBN과 트랜잭션으로 관리한다.
- 공통코드를 화면 선택지뿐 아니라 스케줄러 활성화, 알림 아이콘, 닉네임 조합, 비속어 사전에 활용한다.
- 관리자 제어면과 사용자 실행면을 분리해 문구·메뉴·운영 코드를 배포 없이 조정하고 실행 결과를 다시 관찰한다.
- API 응답 형식과 예외 처리를 공통화해 프론트와 백엔드가 같은 성공 기준을 사용한다.
- 사용자 요청 기능뿐 아니라 탈퇴, 로그, 푸시 구독, 만료 처리 등 운영 수명주기를 포함한다.

## 범위와 한계

구현 완료 범위는 현재 저장소의 소스와 정책 문서로 확인했다. 실제 운영 트래픽, 장애 대응 시간, 배포 가용성은 저장소만으로 검증할 수 없으므로 포트폴리오에서 운영 실적으로 표현하지 않는다.
