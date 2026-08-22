# 배포와 품질

## 문서 목적

- 목적: Sadari의 빌드·배포 자동화와 테스트 현황을 사실에 근거해 설명
- 적용 범위: Gradle, Docker, Docker Compose, GitHub Actions, 테스트, 운영 한계
- 기준일: 2026-07-30

## 저장소별 빌드 범위

### 사용자 서비스

Gradle은 백엔드 빌드 전에 React 의존성을 설치하고 Vite 빌드를 실행한 뒤 결과물을 Spring Boot 정적 리소스에 복사한다. 최종 산출물은 프론트와 백엔드를 함께 포함한 WAR다.

구현 근거:

- `build.gradle`
- `src/main/frontend/package.json`

### 관리자 서비스

`sadari-admin`도 Spring Boot와 React를 한 저장소에서 관리하지만 현재 사용자 서비스의 Docker·GitHub Actions 파이프라인에는 포함되지 않는다.

- 백엔드: Java 17, Spring Boot 4.1.0, MyBatis, MySQL 8.4, Redis
- 프론트엔드: React 19, TypeScript 6, Vite 8
- 테스트: Context Load 테스트 1건
- 확인되지 않은 구성: Dockerfile, Docker Compose, GitHub Actions

구현 근거:

- `../sadari-admin/build.gradle`
- `../sadari-admin/src/main/frontend/package.json`
- `../sadari-admin/src/test/java/org/sadari/admin/sadariadmin/SadariAdminApplicationTests.java`

## 멀티 스테이지 Docker

`Dockerfile`은 세 단계로 구성된다.

1. `node:24-alpine`: `npm ci`와 Vite 프로덕션 빌드
2. `eclipse-temurin:17-jdk`: Gradle `bootWar` 빌드
3. `eclipse-temurin:17-jre`: 실행에 필요한 WAR만 포함

빌드 도구와 프론트 소스가 최종 실행 이미지에 남지 않아 이미지 역할을 분리한다.

## Docker Compose

`docker-compose.yml`은 애플리케이션과 Redis를 함께 실행한다.

- 애플리케이션 프로필: `prod`
- Redis 내부 호스트: `redis`
- 업로드 파일: Named Volume `sadari-uploads`
- Redis AOF 데이터: Named Volume `sadari-redis-data`
- Firebase 서비스 계정: 읽기 전용 파일 마운트
- 환경값: 저장소에 포함하지 않는 `.env`

현재 파일 업로드는 Docker Volume에 저장한다. AWS S3 연동은 구현 근거가 없으므로 배포 완료 기능으로 설명하지 않는다.

## GitHub Actions 파이프라인

`.github/workflows/ci-cd.yml`은 다음 단계로 구성된다.

### Pull Request

1. 소스 Checkout
2. Java 17과 Node 24 설정
3. Gradle·npm 캐시 사용
4. 프론트와 백엔드 통합 WAR 빌드 검증

### Main Push

1. 검증 작업 통과
2. Docker Buildx로 이미지 빌드
3. Git Commit SHA와 `latest` 태그로 GHCR Push
4. GitHub Secrets와 Variables를 배포용 `.env`로 구성
5. Firebase 서비스 계정 JSON을 임시 배포 파일로 생성
6. EC2에 Docker Compose 파일과 환경 파일 전송
7. GHCR 로그인 후 새 이미지 Pull
8. `docker compose up -d --remove-orphans`
9. 최대 2분 동안 HTTP 상태 확인
10. 실패 시 컨테이너 상태와 최근 로그 출력

동일 브랜치의 이전 실행은 `concurrency.cancel-in-progress`로 취소해 오래된 이미지가 뒤늦게 배포되는 상황을 줄인다.

## 비밀값 관리

다음 값은 저장소에 직접 기록하지 않고 GitHub Secrets 또는 운영 환경변수로 주입한다.

- MySQL 접속 정보
- JWT 비밀키
- Kakao 로그인과 도서 검색 API Key
- Firebase Web 설정
- Firebase 서비스 계정 JSON
- EC2 SSH Key와 GHCR Token
- 운영 Front/Back Domain

전체 환경변수 목록과 등록 위치는 `docs/github-actions-deployment.md`에서 관리한다.

## 테스트 자산

현재 `src/test/java`에는 14개 테스트 클래스와 42개 `@Test` 메서드가 있다.

주요 검증 영역:

- JWT 발급·파싱·만료
- 파일 시그니처와 이미지 검증
- 도서 표지 대표색 계산과 폴백
- 공통코드 다중 조회
- 알림 템플릿과 중복 처리
- 스케줄러 실행 조건과 로그 실패 격리
- 목표기간 초과 알림
- 알림 삭제
- 닉네임 발급 동시 충돌 처리
- 사용자 메뉴 조회

대표 경로:

- `src/test/java/org/our/sadari/global/security/jwt/JwtProviderTest.java`
- `src/test/java/org/our/sadari/global/file/service/FileServiceTest.java`
- `src/test/java/org/our/sadari/book/service/BookCoverColorServiceTest.java`
- `src/test/java/org/our/sadari/global/scheduler/SchedulerTest.java`
- `src/test/java/org/our/sadari/alim/service/AlimServiceImplTest.java`

## 현재 품질 한계

### CI에서 테스트 제외

현재 GitHub Actions의 검증 명령은 `-x test`를 사용한다. 일부 테스트가 Git에서 제외한 `application-loc.yml`, MySQL, Redis에 의존하기 때문이다.

따라서 다음 표현은 사용하지 않는다.

- 모든 테스트를 통과한 뒤 자동 배포한다.
- CI가 회귀를 완전히 차단한다.

정확한 표현은 다음과 같다.

- CI는 프론트·백엔드 통합 빌드를 검증한다.
- 단위 테스트 자산은 존재하지만 CI 필수 단계로 아직 연결되지 않았다.

### 배포 상태

저장소에는 EC2 자동 배포 구성이 있으나 실제 AWS 계정과 실행 이력은 저장소에서 확인할 수 없다. 포트폴리오에서는 “배포 파이프라인 구성”으로 표현하고 실제 배포 성공 로그가 있을 때 “운영 배포”로 확장한다.

### 저장소와 데이터베이스

MySQL은 외부 접속 URL로 주입하므로 Amazon RDS for MySQL 또는 별도 MySQL 8.4 서버에 연결할 수 있다. 현재 구성만으로 실제 RDS 사용 여부는 확정하지 않는다.

사용자 서비스와 관리자 서비스의 연동은 공통코드 그룹과 세부 항목, 알림 템플릿, 사용자 화면 메뉴, 스케줄러 실행 로그, 스케줄러 실행 실패 상세를 같은 MySQL 데이터베이스에서 공유한다는 전제가 필요하다. 두 애플리케이션을 서로 다른 데이터베이스에 배포하면 운영 설정과 실행 로그 연동이 끊어진다.

관리자 서비스는 독립 인증 Cookie와 Redis 세션을 사용하므로 운영 배포 시 사용자 서비스와 Cookie 이름, 경로, 도메인, Redis Key Prefix가 충돌하지 않도록 분리해야 한다.

## 우선 개선 순서

1. MySQL Testcontainers 또는 테스트 전용 DB 전략을 마련한다.
2. Redis를 CI Service Container로 실행한다.
3. 단위 테스트와 통합 테스트를 분리한다.
4. `test` 작업을 PR 필수 Check로 전환한다.
5. 배포 Health Check를 전용 Actuator Endpoint로 변경한다.
6. 실패 배포의 자동 롤백 또는 이전 SHA 이미지 복구 절차를 추가한다.
7. S3를 도입할 경우 업로드 인터페이스를 로컬·S3 구현체로 분리하고 기존 파일을 마이그레이션한다.
8. `sadari-admin` 전용 Dockerfile과 CI/CD를 추가하고 사용자·관리자 두 애플리케이션의 공유 DB 마이그레이션 순서를 정의한다.
9. 관리자 권한과 운영 설정 변경을 검증하는 통합 테스트를 배포 전 단계에 추가한다.