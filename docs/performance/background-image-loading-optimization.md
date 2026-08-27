# 배경사진 로딩 성능 및 체감 개선

## 개요

- 작업일: 2026-08-26
- 대상 화면: 팔로잉 피드, 다른 사용자 프로필, 마이페이지
- 대상 기능: 배경사진 공개 조회, 브라우저 캐시, 화면용 이미지 전송, 로드 상태 표시
- 목표: 원본 배경사진의 반복 전송과 과도한 픽셀 디코딩을 줄이고 실제 로드 중임을 사용자에게 알린다.

DB에는 기존 원본 파일 경로만 유지한다. 화면용 파생본은 원본과 같은 개인정보 수명주기로 저장하고 교체, 트랜잭션 롤백, 영구 탈퇴 시 함께 삭제한다. 새로운 사용자 데이터 범위나 보존기간은 추가하지 않는다.

## 개선 전 문제

### 원본 이미지 반복 전송

배경사진 URL 응답은 `no-store` 정책을 사용했다. 같은 UUID 이미지가 변경되지 않아도 피드와 프로필을 다시 방문할 때 브라우저가 응답 본문을 재사용할 수 없어 로컬 디스크 또는 S3에서 원본 바이트를 반복 조회하고 전송했다.

### 일반 화면에서도 원본 해상도 사용

피드의 16:9 카드와 프로필 상단 영역은 표시 크기가 제한되어 있지만 전체화면 보기와 같은 원본 URL을 사용했다. 고해상도 사진일수록 네트워크 전송량, 브라우저 이미지 디코딩 픽셀 수와 메모리 사용량이 화면에 필요한 수준보다 커질 수 있었다.

### 공개 상태 확인 쿼리와 로드 상태

이미지 요청마다 현재 활성 회원이 참조하는 파일인지 확인해야 하지만 파일명 조회 인덱스가 없었고, 사용자 프로필·배경 컬럼을 `OR JOIN`으로 연결했다. 또한 배경사진이 늦게 내려올 때 영역이 비어 보여 사용자가 멈춘 화면으로 인식할 수 있었다.

## 개선 내용

### 활성 상태 재검증형 브라우저 캐시

이미지 응답에 UUID 저장 파일명을 기반으로 한 `ETag`를 반환하고 `private, no-cache, must-revalidate` 정책을 적용했다. 브라우저는 이미지 바이트를 보관할 수 있지만 다음 사용 전에 서버에 조건부 요청을 보낸다.

서버는 현재 활성 회원의 프로필 또는 배경사진인지 먼저 확인한다. 활성 상태이고 `If-None-Match`가 일치하면 저장소 원본을 읽지 않고 `304 Not Modified`를 반환한다. 탈퇴나 삭제 대기 전환 후에는 이전 브라우저 캐시가 승인 없이 다시 표시되지 않는다.

### 공개 파일 확인 쿼리 최적화

파일 메타정보의 저장 파일명에 `IX_TM_FILEXM_STOR` 인덱스를 추가했다. 신규 스키마는 `01-create.sql`, 기존 운영 스키마는 `04-user-statistics-indexes.sql`을 기준으로 적용한다. 사용자 프로필과 배경사진 참조 확인은 `OR JOIN` 대신 각각의 `EXISTS`로 분리하여 파일 한 건을 기준으로 활성 참조 존재 여부만 확인한다.

### 긴 변 1600px 화면용 파생본

원본 경로와 DB 컬럼은 변경하지 않고 Java 공통 URL 변환에서 다음 응답 경로를 추가한다.

| 용도 | 응답 필드 | 요청 경로 |
| --- | --- | --- |
| 전체화면·원본 보존 | `bgimPath`, `contentImagePath` | 기존 `/uploads/background/...` |
| 피드·프로필 일반 표시 | `bgimDisplayPath`, `contentImageDisplayPath` | 기존 경로에 `?variant=display` 추가 |

화면용 파생본은 원본 비율과 JPG 또는 PNG 형식을 유지하고 긴 변만 최대 1600px로 제한한다. 작은 원본은 확대하지 않는다. 전체화면 이미지 뷰어는 계속 원본 경로를 사용하므로 확대 확인 품질은 바뀌지 않는다.

신규 업로드는 저장 시점에 `background/{yyMMdd}/display/{uuid}.{ext}` 파생본을 함께 생성한다. 기존 배경사진은 첫 `variant=display` 요청에서 한 번 생성해 저장하고 이후 요청부터 재사용한다. 파생본 저장에 실패하거나 이전 파일을 변환할 수 없으면 원본을 반환해 화면 표시 자체는 유지한다.

### 중앙 소형 로딩 표시

공통 `BackgroundImage` 컴포넌트가 실제 `<img>`의 `load`와 `error` 상태를 추적한다. 피드, 다른 사용자 프로필과 마이페이지의 배경 영역 중앙에는 공통 `Loading`의 compact 회전 링을 표시하고 로드가 끝나면 숨긴다. 원본 실패 후 대체 이미지를 요청하는 동안에도 로딩 상태를 유지하며 최종 실패 시에는 링이 계속 남지 않는다.

## 구조 기준 개선 효과

| 항목 | 개선 전 | 개선 후 |
| --- | --- | --- |
| 동일 이미지 재방문 | 원본 바이트 재전송 | 활성 상태 확인 후 ETag 일치 시 304 |
| 일반 화면 이미지 | 업로드 원본 | 긴 변 최대 1600px 파생본 |
| 전체화면 보기 | 원본 | 원본 유지 |
| 기존 이미지 전환 | 별도 일괄 작업 필요 | 첫 요청에서 지연 생성 후 재사용 |
| DB 스키마 | 원본 파일 경로 | 변경 없음 |
| 로드 중 피드백 | 빈 배경 영역 | 영역 중앙 공통 소형 회전 링 |

예를 들어 4000×3000 원본은 일반 화면에서 1600×1200으로 디코딩된다. 픽셀 수는 1,200만 개에서 192만 개로 84% 줄어든다. 이는 픽셀 수 계산값이며 실제 파일 바이트 감소율과 로딩 시간은 사진 내용, JPG·PNG 압축률, 네트워크와 기기 성능에 따라 달라진다.

## 데이터 및 장애 처리

- SQL은 원본 파일 경로만 조회하며 URL 문자열 결합이나 신규 컬럼을 사용하지 않는다.
- 파생본은 원본과 별도 DB 행을 만들지 않는 재생성 가능한 저장 객체다.
- 신규 파일 메타정보 등록이 실패하거나 외부 업무 트랜잭션이 롤백되면 원본과 파생본을 함께 삭제한다.
- 사진 교체 또는 영구 탈퇴가 커밋되면 원본과 파생본을 함께 삭제한다.
- 활성 회원의 현재 이미지가 아니면 원본과 파생본 모두 저장소 조회 전에 404로 차단한다.
- 파생본의 ETag에는 `display-1600` 규격을 포함해 원본 캐시와 구분한다.

## 검증

- 신규 2000×1000 PNG 업로드 후 원본 2000×1000 유지 및 파생본 1600×800 생성 확인
- 기존 2000×1000 PNG의 최초 화면용 조회 후 1600×800 파생본 생성·저장 확인
- 배경사진 삭제 시 원본과 `display` 파생본 동시 삭제 확인
- 화면용 요청의 별도 ETag, MIME 유형과 저장소 조회 경로 확인
- 프로필 사진의 `variant=display` 및 알 수 없는 변형 요청 차단 확인
- 내부 배경사진 경로에만 `?variant=display`가 추가되고 외부 URL은 유지되는지 확인

실제 운영 환경의 평균·P95 로딩 시간과 전송 바이트는 아직 측정하지 않았다. 배포 후 브라우저 Resource Timing과 서버 저장소 지표로 원본 대비 파생본 전송량, 304 비율, 첫 지연 생성 시간과 실패율을 추가 측정해야 한다.

## 관련 파일

- `src/main/java/org/our/sadari/global/file/service/FileService.java`
- `src/main/java/org/our/sadari/global/file/controller/FileResourceController.java`
- `src/main/java/org/our/sadari/global/file/util/FileUrlUtil.java`
- `src/main/java/org/our/sadari/user/dto/UserDto.java`
- `src/main/java/org/our/sadari/feed/dto/FeedDto.java`
- `src/main/java/org/our/sadari/global/file/mapper/FileMapper.xml`
- `scripts/db/mysql/01-create.sql`
- `scripts/db/mysql/04-user-statistics-indexes.sql`
- `src/main/frontend/src/components/BackgroundImage/BackgroundImage.tsx`
- `src/main/frontend/src/pages/Feed/FeedPage.tsx`
- `src/main/frontend/src/pages/Social/SocialProfilePage.tsx`
- `src/main/frontend/src/pages/My/ProfileEditPage.tsx`
