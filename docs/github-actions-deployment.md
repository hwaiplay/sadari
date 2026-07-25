# GitHub Actions 운영 배포 설정

이 프로젝트는 `main` 브랜치에 push되면 다음 순서로 배포됩니다.

1. Java 17과 Node.js 24 환경에서 WAR 빌드를 검증합니다.
2. Docker 이미지를 빌드해 `ghcr.io/<owner>/<repository>`에 커밋 SHA와 `latest` 태그로 올립니다.
3. EC2의 `~/sadari`에 운영 `.env`, `docker-compose.yml`, Firebase 서비스 계정 파일을 전송합니다.
4. EC2가 새 이미지를 pull하고 Docker Compose로 애플리케이션과 Redis를 실행합니다.
5. `http://127.0.0.1:<APP_PORT>/` 응답을 최대 2분 동안 확인합니다.

실제 운영 값은 GitHub 저장소의 `Settings > Secrets and variables > Actions`에 등록합니다.
가능하면 `production` Environment를 만들고 승인 규칙과 아래 Secrets/Variables를 그 Environment에 등록합니다.

## Actions Secrets

| 이름 | 내용 |
| --- | --- |
| `EC2_HOST` | EC2 Public IP 또는 배포용 도메인 |
| `EC2_USER` | SSH 사용자명. Amazon Linux는 보통 `ec2-user`, Ubuntu는 `ubuntu` |
| `EC2_SSH_PRIVATE_KEY` | EC2 key pair의 PEM 전체 내용 |
| `GHCR_USERNAME` | GHCR 이미지를 읽을 GitHub 사용자명 |
| `GHCR_TOKEN` | 해당 패키지에 `read:packages` 권한이 있는 GitHub PAT |
| `DB_URL` | Oracle RDS JDBC URL |
| `DB_USERNAME` | 운영 DB 계정 |
| `DB_PASSWORD` | 운영 DB 비밀번호 |
| `FRONT_DOMAIN` | 외부에서 접속하는 프론트 HTTPS Origin |
| `BACK_DOMAIN` | 외부에서 접속하는 백엔드 HTTPS Origin |
| `JWT_SECRET` | JWT 서명용 충분히 긴 무작위 비밀키 |
| `KAKAO_REST_API_KEY` | Kakao REST API 키 |
| `KAKAO_JAVASCRIPT_KEY` | Kakao JavaScript 키 |
| `KAKAO_NATIVE_APP_KEY` | Kakao Native App 키. 사용하지 않으면 빈 값 가능 |
| `NAVER_CLIENT_ID` | Naver 도서 API Client ID |
| `NAVER_CLIENT_SECRET` | Naver 도서 API Client Secret |
| `FIREBASE_WEB_API_KEY` | Firebase Web App의 `apiKey` |
| `FIREBASE_WEB_AUTH_DOMAIN` | Firebase Web App의 `authDomain` |
| `FIREBASE_WEB_PROJECT_ID` | Firebase Web App의 `projectId` |
| `FIREBASE_WEB_STORAGE_BUCKET` | Firebase Web App의 `storageBucket` |
| `FIREBASE_WEB_MESSAGING_SENDER_ID` | Firebase Web App의 `messagingSenderId` |
| `FIREBASE_WEB_APP_ID` | Firebase Web App의 `appId` |
| `FIREBASE_VAPID_PUBLIC_KEY` | Firebase Cloud Messaging의 Web Push 공개키 |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase Admin SDK 서비스 계정 JSON 전체 원문 |

`GITHUB_TOKEN`은 Actions 실행 시 GitHub가 자동 발급하므로 직접 등록하지 않습니다. 이 토큰은
워크플로에서 GHCR 이미지 push에 사용됩니다. EC2의 pull에는 별도 `GHCR_TOKEN`이 필요합니다.

## Actions Variables

아래 값은 비밀정보가 아니며 등록하지 않으면 표의 기본값이 사용됩니다.

| 이름 | 기본값 | 용도 |
| --- | --- | --- |
| `EC2_SSH_PORT` | `22` | EC2 SSH 포트 |
| `APP_PORT` | `8080` | EC2에서 외부에 연결할 애플리케이션 포트 |
| `DB_CONNECTION_TIMEOUT` | `60000` | DB 커넥션 획득 제한시간(ms) |
| `DB_MAXIMUM_POOL_SIZE` | `10` | Hikari 최대 커넥션 수 |
| `DB_MINIMUM_IDLE` | `2` | Hikari 최소 유휴 커넥션 수 |
| `JWT_ACCESS_TOKEN_SECONDS` | `1800` | Access Token 유효시간(초) |
| `JWT_REFRESH_TOKEN_SECONDS` | `86400` | Refresh Token 유효시간(초) |
| `MULTIPART_MAX_FILE_SIZE` | `20MB` | 단일 업로드 파일 제한 |
| `MULTIPART_MAX_REQUEST_SIZE` | `40MB` | 전체 multipart 요청 제한 |
| `COOKIE_SECURE` | `true` | HTTPS 쿠키 전용 여부 |
| `COOKIE_SAME_SITE` | `None` | 인증 쿠키 SameSite 정책 |
| `LOGGING_LEVEL_ROOT` | `info` | 루트 로그 레벨 |
| `LOGGING_LEVEL_APP` | `info` | 프로젝트 패키지 로그 레벨 |

## EC2 사전 조건

- Docker Engine과 Docker Compose v2가 설치되어 있어야 합니다.
- 배포 사용자가 `sudo` 없이 `docker` 명령을 실행할 수 있어야 합니다.
- `curl`이 설치되어 있어야 배포 후 상태 검증이 가능합니다.
- EC2 보안 그룹에서 SSH 포트는 필요한 관리 IP로 제한하고, 서비스 포트는 로드밸런서나
  리버스 프록시를 통해 공개하는 구성을 권장합니다.
- EC2에서 Oracle RDS의 DB 포트로 접근할 수 있어야 하고, RDS 보안 그룹은 EC2 보안 그룹을
  소스로 허용해야 합니다.
- PWA와 Secure Cookie, Firebase Web Push를 사용하려면 최종 서비스 도메인에 HTTPS가 적용되어야 합니다.

## 최초 설정 순서

1. GitHub에서 `production` Environment를 생성합니다.
2. 위 Secrets와 필요한 Variables를 등록합니다.
3. GHCR pull용 PAT를 만들고 `read:packages` 권한을 부여합니다.
4. EC2에 Docker, Compose v2, curl을 설치하고 배포 사용자를 docker 그룹에 추가합니다.
5. `main` 브랜치에 push하거나 Actions 화면에서 `Sadari CI/CD`를 수동 실행합니다.

현재 `SadariApplicationTests`는 Git에서 제외된 로컬 설정과 실제 DB/Redis를 요구하므로 CI에서
자동 실행하지 않습니다. 추후 Testcontainers나 독립 `application-test.yml`을 추가하면 워크플로의
`-x test`를 제거해 통합 테스트까지 배포 차단 조건으로 사용할 수 있습니다.
