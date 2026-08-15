# Sadari 정책 문서

Sadari 서비스에 구현된 주요 사용자 정책과 운영 정책의 기준 문서입니다.

## 문서 목록

| 문서 | 주요 내용 |
| --- | --- |
| [계정 및 인증 정책](account-auth-policy.md) | Kakao 로그인, `sid` JWT, Redis 기기별 세션, 현재·전체 디바이스 로그아웃 |
| [최초 로그인 웰컴 및 닉네임 정책](user-onboarding-policy.md) | 서비스 소개 슬라이드, 랜덤 닉네임 확정, 최초 1회 노출 |
| [계정 비활성화 및 영구 탈퇴 정책](withdrawal-policy.md) | 계정 비활성화, 영구 탈퇴, 재인증, 복귀 및 삭제 |
| [관리자 회원 이용 정지 정책](user-suspension-policy.md) | 기간·무기한 정지, 동일 Kakao 계정 차단, 해제와 상태 우선순위 |
| [관리자 사용자 통계 정책](user-statistics-policy.md) | 실시간 사용자 현황, 활성 회원, 유지율과 이탈 추세 |
| [독서 및 독후감 정책](reading-report-policy.md) | 독서 상태, 기간, 별점, 공개 여부, 도서 저장 |
| [도서 검색 및 외부 API 쿼터 보호 정책](book-search-policy.md) | 주간·월간·연간 인기 도서, 평균 평점, 50권 선조회, Redis 요청 제한과 앱 전체 쿼터 보호 |
| [독서 목표 정책](reading-goal-policy.md) | 주간·월간·연간 목표, 수정 제한, 이전 목표 복사 |
| [독서 타이머 및 주간 출석 정책](reading-timer-attendance-policy.md) | 서버 시간 측정, 일일 10분 출석, 주간 현황과 보존 기준 |
| [마이페이지 독서 통계 정책](my-page-reading-statistics-policy.md) | 연도별 독서 시간 잔디, 독서 상태 비율, 공개 범위와 지연 조회 |
| [소셜 정책](social-policy.md) | 팔로우 상태, 좋아요, 공개 프로필 및 목록 |
| [댓글 정책](reply-policy.md) | 댓글 식별, 대댓글 깊이, 삭제 표시 |
| [신고 접수 및 처리 정책](abuse-report-policy.md) | 사용자·독후감·댓글·모임 신고 대상, 사유, 처리 상태 및 보존 |
| [알림 및 푸시 정책](notification-push-policy.md) | 알림 생성, 중복 방지, 읽음·삭제, FCM 구독 |
| [메뉴 및 화면 노출 정책](menu-policy.md) | URL별 헤더, 햄버거 메뉴 노출 |
| [사용자 안내 팝업 콘텐츠 정책](popup-content-policy.md) | 정책·도움말 팝업의 JSON 목록 저장, 조회 및 실패 대체 |
| [공지사항 운영 정책](notice-policy.md) | 공지 버전, 배포, Summernote 본문과 전용 이미지 보존 |
| [콘텐츠 및 파일 정책](content-file-policy.md) | 비속어, 닉네임, 텍스트 정규화, 이미지 업로드 |
| [스케줄러 운영 정책](scheduler-policy.md) | 스케줄러 실행 조건, 배치 크기, 실행 로그 |
| [설치형 웹앱 자동 업데이트 정책](pwa-update-policy.md) | 서비스워커 갱신, 정적 자원 캐시, 홈 화면 아이콘 업데이트 |

## 구현 전 정책 결정 문서

아래 문서는 현재 구현된 정책이 아니라 신규 기능 구현 전에 사용자와 확정할 정책을 관리합니다.

| 문서 | 상태 | 주요 내용 |
| --- | --- | --- |
| [독서 모임 정책 결정 항목](reading-club-policy-decisions.md) | 요구사항 일부 확정, 세부 정책 미확정 | 공개·비공개 모임, 관심분야, 가입·퇴장, 회차, 독후감 공개, 도서 선정, 채팅 및 계정 수명주기 |

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
| `TMRX_STAT` | 독서 타이머 상태 |
| `BOOK_COLR` | 책장 색상 |
| `COMM_YSNO` | 공통 Y/N |
| `USER_STAT` | 회원 상태 |
| `WTHD_TYPE` | 탈퇴 유형 |
| `WTHD_RSON` | 탈퇴 사유 |
| `SPND_TYPE` | 회원 정지 유형 |
| `SPND_RSON` | 회원 정지 사유 |
| `SPND_STAT` | 회원 정지 처리 상태 |
| `FOLW_STAT` | 팔로우 버튼 상태 |
| `ALIM_SITU` | 알림 상황 |
| `POPU_SITU` | 팝업 사용 화면 구분 |
| `SCHD_CODE` | 스케줄러 활성 상태 |
| `BADX_WORD` | 차단 비속어 |
| `EXCP_WORD` | 비속어 예외 허용어 |
| `CMPL_TAGT` | 신고 대상 유형 |
| `CMPL_RSON` | 신고 사유 |
| `CMPL_STAT` | 신고 처리 상태 |

## 관리 정보

- 기준일: 2026-08-16
- 저장 위치: `docs/policies`
- 구현 근거: `src/main/java`, `src/main/frontend/src`, `src/main/resources`
