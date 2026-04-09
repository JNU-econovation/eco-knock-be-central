# Gitmoji Cheatsheet

이 문서는 `git-commit-korean` 스킬이 기본 예시보다 더 다양한 커밋 의도를 고를 때 참고하는 저장소 전용 치트시트다.

## Core Patterns

- `:sparkles: feat:` 새 동작, 새 API, 새 플로우 추가
- `:bug: fix:` 일반 버그 수정
- `:ambulance: hotfix:` 운영 장애나 긴급 수정
- `:recycle: refactor:` 동작 유지 구조 개선
- `:white_check_mark: test:` 테스트 추가나 테스트 보강
- `:fire: remove:` 기능, 파일, 코드 경로 제거

## Build And Dependency

- `:heavy_plus_sign: build:` 의존성 추가
- `:arrow_up: build:` 의존성 업그레이드
- `:arrow_down: build:` 의존성 다운그레이드
- `:hammer: build:` 빌드 스크립트, toolchain, plugin, packaging 설정 정리

예시:

- `:heavy_plus_sign: build: PostgreSQL 드라이버 의존성을 추가`
- `:arrow_up: build: Spring Boot 버전을 4.0.6으로 올림`
- `:arrow_down: build: protobuf 플러그인 버전을 0.9.4로 내림`
- `:hammer: build: Gradle toolchain 설정을 정리`

## Deploy And Infra

- `:rocket: deploy:` 배포 파이프라인, 릴리스 설정, 런타임 배포 구성
- `:construction_worker: ci:` CI workflow 추가나 변경
- `:green_heart: ci:` 깨진 CI, flaky pipeline, 환경 차이 복구
- `:building_construction: infra:` 로컬/운영 인프라 구성 추가나 정리

예시:

- `:rocket: deploy: 운영 배포 환경 변수를 분리`
- `:construction_worker: ci: GitHub Actions 배포 워크플로를 추가`
- `:green_heart: ci: 깨진 통합 테스트 파이프라인을 복구`
- `:building_construction: infra: 로컬 Postgres 도커 구성을 추가`

## Rename And Move

- `:truck: rename:` 디렉토리 이동, 파일 이동, 패키지 이동, 클래스명 변경

예시:

- `:truck: rename: oauth 패키지를 oauth2로 이동`
- `:truck: rename: MemberResponse 클래스를 MemberInfoResponse로 변경`

## Security And Auth

- `:lock: security:` 민감 정보 보호, 권한 강화, 보안 정책 강화
- `:bug: fix:` 인증/인가 버그지만 핵심이 일반 동작 수정일 때

예시:

- `:lock: security: 민감 정보 로그 출력을 제거`
- `:lock: security: JWT 시크릿 키 주입 방식을 분리`
- `:bug: fix: 만료된 JWT를 401로 처리`

## Database And Persistence

- `:card_file_box: db:` 스키마, 인덱스, 마이그레이션, DB 설정 중심 변경
- `:truck: rename:` 컬럼명이나 테이블명 변경이 핵심일 때

예시:

- `:card_file_box: db: 회원 테이블 인덱스를 추가`
- `:card_file_box: db: OAuth2 로그인 이력 테이블을 생성`
- `:truck: rename: member_status 컬럼을 active_status로 변경`

## Docs And Developer Experience

- `:memo: docs:` README, 실행 문서, API 설명, 운영 가이드
- `:wrench: chore:` 로컬 도구, IDE 설정, 개발 편의 설정

예시:

- `:memo: docs: 로컬 실행 환경 변수를 문서화`
- `:memo: docs: OAuth2 로그인 플로우를 정리`
- `:wrench: chore: IntelliJ 실행 구성을 정리`

## Performance And UX

- `:zap: perf:` 병목 개선, 불필요한 호출 감소, 응답 속도 개선
- `:children_crossing: ux:` 메시지, 흐름, 사용자 피드백 개선

예시:

- `:zap: perf: JWT 파싱 중복 호출을 줄임`
- `:children_crossing: ux: 로그인 실패 메시지를 구체화`

## Assets And Localization

- `:bento: assets:` 템플릿, 이미지, 정적 리소스 추가
- `:globe_with_meridians: i18n:` 다국어 키, 번역 문자열, locale 처리

예시:

- `:bento: assets: 기본 이메일 템플릿을 추가`
- `:globe_with_meridians: i18n: 공통 에러 메시지 다국어 키를 추가`

## Selection Rules

- 의존성 추가/업다운이 핵심이면 `build`
- 배포 또는 실행 환경이 핵심이면 `deploy` 또는 `ci`
- 이동이나 이름 변경이 핵심이면 `rename`
- 보안 강화가 핵심이면 `security`
- DB 구조 변화가 핵심이면 `db`
- 새 동작 추가가 핵심이면 `feat`
- 동작 유지 구조개선이면 `refactor`
- 긴급 장애 대응이면 `hotfix`

핵심은 이모지 자체보다 커밋의 주된 의도를 빠르게 읽히게 만드는 것이다. 하나의 커밋에 여러 의도가 섞이면 이모지를 고민하지 말고 커밋을 먼저 쪼갠다.
