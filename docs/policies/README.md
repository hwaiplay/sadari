# Sadari 정책 문서

Sadari 서비스에 구현된 주요 사용자 정책과 운영 정책의 기준 문서입니다.

## 문서 목록

| 문서 | 주요 내용 |
| --- | --- |
| [계정 및 인증 정책](account-auth-policy.md) | Kakao 로그인, JWT, Redis 세션, 로그아웃 |
| [회원 탈퇴 정책](withdrawal-policy.md) | 서비스 탈퇴, 영구 탈퇴, 재인증, 복구 및 삭제 |
| [독서 및 독후감 정책](reading-report-policy.md) | 독서 상태, 기간, 별점, 공개 여부, 도서 저장 |
| [독서 목표 정책](reading-goal-policy.md) | 주간·월간·연간 목표, 수정 제한, 이전 목표 복사 |
| [소셜 정책](social-policy.md) | 팔로우 상태, 좋아요, 공개 프로필 및 목록 |
| [댓글 정책](reply-policy.md) | 댓글 식별, 대댓글 깊이, 삭제 표시 |
| [알림 및 푸시 정책](notification-push-policy.md) | 알림 생성, 중복 방지, 읽음·삭제, FCM 구독 |
| [메뉴 및 화면 노출 정책](menu-policy.md) | URL별 헤더, 햄버거 메뉴 노출 |
| [콘텐츠 및 파일 정책](content-file-policy.md) | 비속어, 닉네임, 텍스트 정규화, 이미지 업로드 |
| [스케줄러 운영 정책](scheduler-policy.md) | 스케줄러 실행 조건, 배치 크기, 실행 로그 |

## 적용 원칙

1. 이 문서는 현재 소스에 구현된 동작을 기준으로 작성합니다.
2. 코드와 문서가 다르면 실제 실행 기준은 배포된 코드와 공통코드 데이터입니다.
3. 정책을 변경할 때는 관련 코드, 공통코드, 데이터베이스 구조와 이 문서를 함께 변경합니다.
4. 환경별 유효시간, 도메인, 파일 제한처럼 설정으로 관리되는 값은 해당 환경의 YML 또는 환경변수를 최종 기준으로 사용합니다.
5. 정책에 사용되는 공통코드는 `USEE_YSNO = 'Y'`인 상세코드만 유효한 값으로 취급합니다.

## 주요 공통코드

| 공통코드 | 용도 |
| --- | --- |
| `READ_STAT` | 독서 상태 |
| `BOOK_COLR` | 책장 색상 |
| `COMM_YSNO` | 공통 Y/N |
| `USER_STAT` | 회원 상태 |
| `WTHD_TYPE` | 탈퇴 유형 |
| `WTHD_RSON` | 탈퇴 사유 |
| `FOLW_STAT` | 팔로우 버튼 상태 |
| `ALIM_SITU` | 알림 상황 |
| `SCHD_CODE` | 스케줄러 활성 상태 |
| `BADX_WORD` | 차단 비속어 |
| `EXCP_WORD` | 비속어 예외 허용어 |

## 관리 정보

- 기준일: 2026-07-29
- 저장 위치: `docs/policies`
- 구현 근거: `src/main/java`, `src/main/frontend/src`, `src/main/resources`
