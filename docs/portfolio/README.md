# Sadari 포트폴리오 문서

## 문서 목적

- 목적: Sadari 프로젝트에서 포트폴리오와 기술 면접에 활용할 수 있는 구현 경험을 영역별로 정리
- 적용 범위: 사용자·관리자 연동, 백엔드, 데이터, 인증·보안, 성능·알고리즘, 알림·스케줄러, 프론트엔드, 배포·품질
- 기준일: 2026-07-30
- 작성 기준: 현재 저장소의 소스와 기존 정책·성능 문서에서 확인되는 구현

## 프로젝트 한 줄 소개

Sadari는 도서 검색, 독서 기록, 목표 관리, 소셜 활동, 알림과 푸시까지 연결한 독서 기록 플랫폼이다. 사용자 서비스와 별도 관리자 서비스를 공유 운영 테이블로 연결해 공통코드, 알림 템플릿, 사용자 메뉴, 스케줄러 제어와 실행 로그까지 관리한다. 기능 구현에 그치지 않고 인증 수명주기, 데이터 일관성, 성능 최적화, 운영 자동화와 관리 권한을 하나의 서비스 흐름으로 구성했다.

## 핵심 경쟁력

| 영역 | 포트폴리오 핵심 내용 | 구현 상태 |
| --- | --- | --- |
| 관리자 연동 | 별도 `sadari-admin`에서 사용자 공통코드·알림 템플릿·메뉴를 관리하고 스케줄러 로그를 조회 | 구현 완료 |
| 인증·세션 | Kakao OAuth, JWT Access/Refresh Token, Redis 세션 메타데이터와 로그아웃 블랙리스트 | 구현 완료 |
| 데이터 일관성 | 도서가 없을 때 ISBN 기준 도서 마스터와 독후감을 하나의 트랜잭션으로 등록 | 구현 완료 |
| 성능 개선 | 마이페이지 독서 요약 SQL 왕복을 최대 19회에서 2회로 축소 | 소스 기준 검증 완료 |
| 문자열 알고리즘 | DB 비속어·예외 사전을 캐시하고 Aho-Corasick 자동자로 탐지 | 구현 완료 |
| 이미지 알고리즘 | 도서 표지 대표색을 추출하고 CIELAB 거리로 가장 가까운 공통 색상 선택 | 구현 완료 |
| 알림·푸시 | 템플릿 알림, 1시간 중복 방지, 트랜잭션 커밋 후 FCM 발송, PWA 구독 | 구현 완료 |
| 운영 자동화 | 공통코드 기반 스케줄러 활성화, 실행·실패 로그, 제한된 배치 처리 | 구현 완료 |
| 회원 탈퇴 | Kakao 재인증, 서비스 탈퇴와 예약 영구 탈퇴, 복구·삭제 정책 분리 | 구현 완료 |
| 프론트 안정성 | 공통 API 응답 검증, 토큰 재발급 동시 요청 제어, React Query 캐시 | 구현 완료 |
| 배포 | 멀티 스테이지 Docker, GHCR, EC2 배포와 상태 확인을 포함한 GitHub Actions | 구성 완료 |

마이페이지 개선의 `89.5%`는 실제 응답 시간 개선율이 아니라 소스에서 계산한 SQL 왕복 횟수 감소율이다. 실제 운영 응답 시간은 별도 부하 측정이 필요하다.

## 문서 구성

| 문서 | 설명 |
| --- | --- |
| [프로젝트 개요와 아키텍처](project-overview.md) | 서비스 문제 정의, 기술 스택, 시스템 구성과 요청 흐름 |
| [관리자와 사용자 서비스 연동](admin-user-integration.md) | 공유 운영 테이블, 설정 전파, 스케줄러 관찰과 관리자 권한 |
| [백엔드와 데이터 설계](backend-data.md) | 도서·독후감, 공통코드, 소셜, 닉네임 발급과 트랜잭션 |
| [인증과 보안](auth-security.md) | OAuth, JWT, Redis, 탈퇴 재인증, 파일·외부 URL 보안 |
| [성능과 알고리즘](performance-algorithms.md) | SQL 통합, Aho-Corasick, CIELAB, 캐시와 복잡도 |
| [알림과 스케줄러](notification-scheduler.md) | 템플릿 알림, FCM, 중복 방지, 배치와 실행 로그 |
| [프론트엔드 경험](frontend-experience.md) | API 안정성, 서버 상태 캐시, PWA, 화면 일관성 |
| [배포와 품질](deployment-quality.md) | Docker, GitHub Actions, 테스트 현황과 운영 과제 |
| [포트폴리오 활용 가이드](portfolio-guide.md) | 이력서 문장, 면접 사례, 시연 순서와 예상 질문 |

## 추천 열람 순서

1. `project-overview.md`에서 전체 서비스와 아키텍처를 확인한다.
2. `admin-user-integration.md`에서 사용자 실행면과 관리자 제어면의 연결을 확인한다.
3. 지원 직무에 따라 백엔드는 `backend-data.md`, `auth-security.md`, `performance-algorithms.md`를 우선 확인한다.
4. 운영 역량을 강조할 때 `notification-scheduler.md`, `deployment-quality.md`를 확인한다.
5. 실제 이력서와 면접 답변은 `portfolio-guide.md`를 기준으로 정리한다.

## 근거 문서

- [Sadari 정책 문서](../policies/README.md)
- [마이페이지 독서 요약 조회 성능 개선](../performance/my-page-reading-summary-optimization.md)
- [GitHub Actions 배포 설정](../github-actions-deployment.md)

## 현재 한계

- CI의 빌드 검증 단계는 로컬 DB와 Redis에 의존하는 테스트 환경이 정리되지 않아 `-x test`로 실행한다.
- 배포 설정은 EC2, 외부 Oracle DB, 컨테이너 Redis를 대상으로 준비되어 있으나 저장소만으로 실제 운영 배포 완료 여부를 판단할 수 없다.
- 파일 저장은 Docker 볼륨의 로컬 업로드 경로를 사용하며 AWS S3 저장소 연동은 현재 구현 근거가 없다.
- 성능 개선 수치는 SQL 호출 구조를 기준으로 산정했으며 운영 환경의 평균, P95, P99 응답 시간은 측정되지 않았다.
- `sadari-admin`은 현재 Docker·GitHub Actions 구성이 없고 Context Load 테스트 1건만 확인되므로 사용자 서비스와 별도의 배포·회귀 검증 보강이 필요하다.
