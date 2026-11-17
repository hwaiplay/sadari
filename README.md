# Sadari

> [독서의 즐거움에 오르다](https://sadaribooks.com/)

[![Sadari CI/CD](https://github.com/hwaiplay/sadari/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/hwaiplay/sadari/actions/workflows/ci-cd.yml)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![React](https://img.shields.io/badge/React-19.2-61DAFB?logo=react&logoColor=20232A)
![Docker](https://img.shields.io/badge/Docker-GHCR%20%7C%20EC2-2496ED?logo=docker&logoColor=white)

Sadari는 독서 기록, 목표, 소셜 활동과 독서 모임을 연결한 React PWA 기반 독서 플랫폼입니다. 인증 수명주기, 데이터 정합성, 동시성, 파일 보안과 운영 자동화까지 실제 서비스 문제를 중심으로 설계했습니다.

## 프로젝트 정보

| 항목 | 내용 |
| --- | --- |
| 개발 기간 | 2026.03 ~ 진행 중 |
| 개발 형태 | 2인 팀 프로젝트 |
| 서비스 구성 | 사용자 React PWA, Spring Boot API, 별도 관리자 서비스 |
| 주요 기여 영역 | 독서 모임 기능의 프론트엔드·백엔드 구현과 사용자 흐름 개선 |
| 배포 구성 | GitHub Actions, GHCR, EC2, Docker Compose |

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 4.0.3, Spring MVC, Spring Security, MyBatis 4.0.1 |
| Data | MySQL 8.4, Redis 7, HikariCP |
| Auth | Kakao OAuth 2.0, JJWT 0.13, HttpOnly Cookie, CSRF Token, Redis Lua Script |
| Frontend | React 19.2, TypeScript 5.9, Vite 7.3, TanStack Query 5, Zustand 5 |
| Messaging | Firebase Admin SDK, Firebase Cloud Messaging, PWA Push Subscription |
| Storage | Local File Storage, AWS SDK for Java 2.x, Private S3 |
| Delivery | Gradle, Multi-stage Docker, GitHub Actions, GHCR, EC2, Docker Compose |

## 시스템 구성

[![Sadari 전체 아키텍처](docs/architecture/assets/sadari-architecture-overview.svg)](docs/architecture/assets/sadari-architecture-overview.svg)

[전체 데이터베이스 ERD](docs/architecture/database-erd/README.md)에서 현재 DDL 기준 테이블·컬럼·관계와 영역별 구조를 확인할 수 있습니다.

## 주요 기능

| 영역 | 사용자 기능 | 주요 구현 |
| --- | --- | --- |
| 도서 | 기간별 인기 도서와 언어별 Kakao·Google Books 검색 및 표지 색상 탐색 | [계정 언어 공급자 전환, 공급자별 40·50권 선조회와 Redis 쿼터 보호](docs/technical-review/book-search-and-ranking.md) |
| 독서 기록 | 읽기 상태, 별점, 독후감, 기간별 독서량 | [도서·독후감 원자적 등록, 상태별 저장 정책과 편집 충돌 감지](docs/technical-review/reading-report-transaction.md) |
| 독서 목표 | 주간·월간·연간 목표와 달성 현황 | [ISO 기간 경계, 이전 목표 복사와 조건부 집계](docs/technical-review/reading-goal-aggregation.md) |
| 소셜 | 프로필, 팔로우, 좋아요와 댓글 | [본인·타인 공개 범위 분리, 대상 유형 검증과 차단 관계](docs/technical-review/social-feed-reactions.md) |
| 독서 모임 | 모임 생성, 가입 신청, 초대와 승인 | [모임장 권한, 정원 행 잠금, 초대 예약 좌석과 만료 정책](docs/technical-review/reading-club-concurrency.md) |
| 알림 | 서비스 알림과 PWA 웹 푸시 | [언어별 템플릿 치환, 업무별 중복 방지와 커밋 이후 FCM 발송](docs/technical-review/notification-push-transaction.md) |
| 계정 | 로그인, 기기별 로그아웃, 비활성화와 탈퇴 예약 | [JWT 세션 식별자, Redis 토큰 회전과 30일 삭제 유예](docs/technical-review/authentication-session-lifecycle.md) |
| 콘텐츠 안전 | 비속어 입력 차단과 이미지 업로드 검증 | [Aho-Corasick 사전 탐지, 이미지 재인코딩과 비공개 저장](docs/technical-review/content-validation-file-security.md) |
| 다국어·번역 | 한국어·영어 UI와 공개 독후감 번역 보기 | [기기 언어 기본값, DB 문구 다국어 처리, Google 번역 캐시와 월 50만 자 제한](docs/technical-review/multilingual-translation-cache.md) |

## 독서 모임 담당 기능

Sadari의 독서 모임 기능을 담당해 모임을 만들고 함께 읽을 사람을 모아 회차별 독서 활동을 이어갈 수 있도록 구현했습니다. 화면 흐름과 서버의 참여·권한 처리를 함께 다룹니다.

### 담당 기능

- 모임 생성, 탐색, 상세 정보 조회와 모임 정보 수정
- 공개·승인제 가입 신청, 초대 및 수락, 모임 탈퇴
- 모임원 관리와 모임장 권한에 따른 관리 기능
- 참여자 프로필 이동
- 함께 읽을 도서 투표, 회차별 목표·독서 기록 및 결과 확인
- 가입·초대·승인 과정의 알림과 모임 상태에 따른 사용자 흐름

### 모임 중심의 화면 흐름

모임 탐색부터 상세 확인, 가입·초대, 멤버 관리, 독서 회차 활동까지 한 모임 안에서 이어지도록 관련 화면과 상호작용을 구현했습니다. 가입 상태와 모임 상태에 따라 사용할 수 있는 기능을 구분합니다.

### 관련 코드

- 백엔드: `src/main/java/org/our/sadari/readingClub/`
- 프론트엔드 화면: `src/main/frontend/src/pages/ReadingClub/`
- 프론트엔드 기능: `src/main/frontend/src/features/ReadingClub/`
