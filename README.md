# eco-knock-be-central

`eco-knock-be-central`은 Spring Boot 기반의 중앙 백엔드입니다. 현재 저장소는 PostgreSQL 기반 회원 영속화 스캐폴딩, JWT 인증 유틸리티, OAuth2 로그인 준비 코드, 그리고 임베디드 장치에서 제공하는 gRPC 데이터를 조회하는 클라이언트를 포함합니다.

아직 구현 초기 단계입니다. REST API 컨트롤러는 없고, OAuth2 로그인 플로우도 완성되지 않았습니다. README는 현재 코드에 맞는 범위만 설명합니다.

## 현재 구현 범위

- Spring Boot 4 + Java 25 + Kotlin 혼합 프로젝트
- PostgreSQL + Spring Data JPA 설정
- `Member` 엔티티 및 기본 회원 조회/저장 서비스
- AES-256 문자열 암호화 컨버터
- JWT 생성/검증 유틸과 인증 필터
- 공통 에러 응답 모델
- 센서/공기청정기 상태를 조회하는 gRPC 클라이언트
- 앱 시작 후 gRPC 상태를 주기적으로 조회하는 로거
- proto 기반 코드 생성 Gradle 설정

## 아직 미완성인 부분

- REST API 컨트롤러가 없습니다.
- `MemberService#getOrSave()`는 `TODO` 상태입니다.
- `EcoKnockOAuth2UserService#loadUser()`는 `TODO` 상태입니다.
- `SecurityConfig`의 `oauth2Login()` 설정은 현재 주석 처리되어 있습니다.
- 테스트는 `contextLoads()` 수준만 존재합니다.

## 기술 스택

- Java 25
- Kotlin
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- gRPC / Protocol Buffers
- JJWT
- Gradle

## 프로젝트 구조

```text
src/main/java/jnu/econovation/ecoknockbecentral
├─ common
│  ├─ exception
│  ├─ security
│  └─ converter
└─ member
   └─ model

src/main/kotlin/jnu/econovation/ecoknockbecentral
├─ grpc
│  ├─ client
│  ├─ config
│  └─ runner
├─ member
├─ oauth2
├─ sensor
└─ airpurifier

src/main/proto
├─ sensor/v1/sensor.proto
└─ airpurifier/v1/airpurifier.proto
```

## 실행 전 요구사항

- JDK 25
- PostgreSQL
- `sensor.v1.SensorService`와 `airpurifier.v1.AirPurifierService`를 제공하는 gRPC 서버

기본 설정 기준으로 다음 대상이 필요합니다.

- HTTP 서버 포트: `18081`
- PostgreSQL: `localhost:5432`, 데이터베이스명 `ecoknock`
- gRPC 서버: `localhost:6565`

## 환경 변수

이 프로젝트는 개발 환경에서 `springboot4-dotenv`를 사용하므로 루트 `.env` 파일 또는 시스템 환경 변수로 값을 주입할 수 있습니다.

필수:

- `JWT_SECRET_KEY`
  - JWT 서명 키
  - `Keys.hmacShaKeyFor(...)`에 사용할 만큼 충분히 긴 문자열이어야 합니다.
- `AES256_KEY`
  - 정확히 32바이트 문자열이어야 합니다.

선택:

- `DEV_POSTGRES_HOST` 기본값 `localhost`
- `DEV_POSTGRES_PORT` 기본값 `5432`
- `DEV_POSTGRES_USERNAME` 기본값 `postgres`
- `DEV_POSTGRES_PASSWORD` 기본값 빈 문자열

예시:

```dotenv
JWT_SECRET_KEY=replace-with-a-long-secret-key
AES256_KEY=12345678901234567890123456789012
DEV_POSTGRES_HOST=localhost
DEV_POSTGRES_PORT=5432
DEV_POSTGRES_USERNAME=postgres
DEV_POSTGRES_PASSWORD=postgres
```

## 실행 방법

proto 생성은 `compileJava`, `compileKotlin`, `bootRun` 전에 자동으로 연결되어 있습니다.

로컬 실행:

```bash
./gradlew bootRun
```

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

테스트 실행:

```bash
./gradlew test
```

## proto 생성

수동으로 proto 생성만 실행하려면:

```bash
./gradlew generateProto
```

생성 대상 proto:

- [`sensor.proto`](./src/main/proto/sensor/v1/sensor.proto)
- [`airpurifier.proto`](./src/main/proto/airpurifier/v1/airpurifier.proto)

## 보안 동작

현재 보안 설정은 JWT 기반 stateless 인증을 전제로 합니다.

- 기본적으로 모든 요청은 인증이 필요합니다.
- 예외 경로는 `/auth/success`, `/error`, `/favicon.ico` 입니다.
- JWT 필터는 `Authorization` 헤더를 기준으로 인증을 시도합니다.
- OAuth2 관련 클래스는 존재하지만 로그인 플로우는 아직 활성화되어 있지 않습니다.

## gRPC 동작

앱 시작 후 `EmbeddedGrpcStartupLogger`가 센서와 공기청정기 현재 상태를 주기적으로 조회합니다.

- 정상 시 1초 간격으로 조회합니다.
- 실패 시 30초부터 시작하는 백오프를 적용합니다.

현재 사용하는 RPC는 다음 두 개입니다.

- `sensor.v1.SensorService/GetCurrentSensor`
- `airpurifier.v1.AirPurifierService/GetCurrentAirPurifier`

## 현재 상태 요약

이 저장소는 중앙 백엔드의 기반 코드를 정리하는 단계입니다. 회원/보안/gRPC 연결의 뼈대는 들어와 있지만, 실제 사용자 흐름을 제공하는 HTTP API와 완성된 OAuth2 로그인 로직은 아직 구현되지 않았습니다.
