# Commit Style

이 저장소의 최근 커밋에서 확인한 패턴:

- `:fire: remove: Challenge 기능 제거`
- `:white_check_mark: test: 공통 에러 미들웨어 테스트를 추가`
- `:recycle: refactor: Gin 에러 응답을 공통 미들웨어로 통합`
- `:sparkles: feat: 챌린지 티켓 연동과 공통 에러 모델을 추가`
- `:sparkles: feat: 서버 엔트리포인트와 내부 계층 구조를 정리`

## Observed Rules

- 이모지는 자주 쓰지만 필수는 아니다.
- 타입은 영어 소문자 키워드를 쓴다.
- 타입 뒤에는 콜론과 공백을 둔다.
- 설명은 한국어 한 줄로 쓴다.
- 설명은 변경 목적이 드러나게 쓴다.
- 커밋 범위는 가능한 한 작고 논리적으로 닫혀 있어야 한다.

## Message Construction

이 저장소에서는 아래 형태를 기본으로 본다.

`<optional emoji> <type>: <한글 요약>`

예시:

- `:sparkles: feat: BME680 센서 리포터 초기화 로직 추가`
- `:recycle: refactor: 센서 스트리밍 서비스 이름과 책임 정리`
- `:white_check_mark: test: 윈도우용 fake sensor stub 테스트 보강`
- `:fire: remove: 사용하지 않는 센서 서비스 필드 제거`
- `:heavy_plus_sign: build: OAuth2 클라이언트 의존성을 추가`
- `:arrow_up: build: Spring Boot 버전을 4.0.6으로 올림`
- `:arrow_down: build: protobuf 플러그인 버전을 0.9.4로 내림`
- `:hammer: build: Gradle toolchain 설정을 정리`
- `:rocket: deploy: 운영 배포 환경 변수를 분리`
- `:truck: rename: oauth 패키지를 oauth2로 이동`

## Type Heuristics

- `feat`: 사용자 기능, 통신 기능, 센서 기능, 새 동작 추가
- `refactor`: 동작은 유지하면서 구조만 개선
- `test`: 테스트 추가 또는 테스트 코드 정리
- `remove`: 기능, 파일, 코드 경로 제거
- `build`: 의존성 추가, 버전 업다운, 빌드 스크립트, toolchain, 패키지 매니저 설정 변경
- `deploy`: 배포 파이프라인, 릴리스 설정, 서버 실행 환경, 인프라 배포 설정 변경
- `rename`: 디렉토리 이동, 파일 이동, 클래스명 변경, 패키지명 변경처럼 주된 변화가 이름이나 위치 재정의인 경우

권장 이모지 매핑:

- `:sparkles: feat:`
- `:recycle: refactor:`
- `:white_check_mark: test:`
- `:fire: remove:`
- `:heavy_plus_sign: build:` for dependency add
- `:arrow_up: build:` for dependency upgrade
- `:arrow_down: build:` for dependency downgrade
- `:hammer: build:` for other build-only changes
- `:rocket: deploy:` for deploy changes
- `:truck: rename:` for rename or move changes

판단 우선순위:

- 의존성 추가/업다운이 핵심이면 `build`
- 배포 설정이 핵심이면 `deploy`
- 이동이나 이름 변경이 핵심이면 `rename`
- 위 셋보다 새 동작 추가가 핵심이면 `feat`
- 동작 유지 구조개선이면 `refactor`

히스토리에 없는 타입이 꼭 필요하면 쓸 수 있지만, 가능하면 기존 타입 집합을 우선한다.

## Commit Size

이 skill은 큰 작업을 한 번에 커밋하지 않는다.

- 기능 추가와 리팩터링이 섞여 있으면 나눈다.
- 이동, 이름 변경, 포맷팅만 있는 변경은 별도 커밋으로 분리하는 편을 우선한다.
- 테스트는 변경을 설명하는 데 붙어야 하지만, 독립적으로 의미가 있으면 별도 커밋도 가능하다.
- 하나의 커밋 메시지로 설명하기 어려우면 이미 너무 큰 커밋으로 본다.
