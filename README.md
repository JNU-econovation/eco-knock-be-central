# eco-knock-be-central

`eco-knock-be-central`은 임베디드 장치의 센서/공기청정기 gRPC 데이터를 수집하고, 공기질 데이터를 저장·조회하는 Spring Boot 기반 중앙 백엔드입니다.

현재 구현은 PostgreSQL 저장소, Flyway 스키마 관리, JWT 기반 보안 필터, 공기질 timeseries 조회 API, SSE 실시간 스트림, Actuator/Prometheus 메트릭 엔드포인트를 포함합니다.

## 현재 구현 범위

- Spring Boot 4 + Java 25 + Kotlin 혼합 프로젝트
- PostgreSQL + Spring Data JPA
- Flyway 기반 DB 스키마 및 materialized view 관리
- `air_quality` 원본 데이터 저장
- `1m`, `5m`, `15m`, `1h`, `4h`, `1d` 공기질 materialized view
- 매분 materialized view refresh scheduler
- 공기질 timeseries 조회 API
- 공기질 과거 데이터 history 조회 API
- 공기질 SSE 실시간 스트림
- 센서/공기청정기 현재 상태 gRPC polling producer
- queue 기반 공기질 저장 consumer
- JWT 인증 필터 및 optional 인증 정책
- Actuator health/info/prometheus 엔드포인트
- proto 기반 코드 생성 Gradle 설정

## 아직 미완성인 부분

- `EcoKnockOAuth2UserService#loadUser()`는 `TODO` 상태입니다.
- `SecurityConfig`의 `oauth2Login()` 설정은 현재 주석 처리되어 있습니다.
- gRPC 서버가 떠 있지 않으면 producer가 연결 실패 로그를 남깁니다.

## 기술 스택

- Java 25
- Kotlin
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- gRPC / Protocol Buffers
- Actuator / Micrometer Prometheus
- JJWT
- Gradle

## 프로젝트 구조

```text
src/main/java/jnu/econovation/ecoknockbecentral
├─ airquality
│  ├─ model/entity
│  └─ readmodel/entity
├─ common
│  ├─ exception
│  ├─ security
│  ├─ converter
│  └─ util
└─ member
   └─ model

src/main/kotlin/jnu/econovation/ecoknockbecentral
├─ airquality
│  ├─ controller
│  ├─ dto
│  ├─ messaging
│  ├─ queue
│  ├─ repository
│  ├─ scheduler
│  ├─ service
│  └─ usecase
├─ grpc
│  ├─ client
│  └─ config
├─ member
└─ oauth2

src/main/resources/db/migration
├─ V1__init_schema.sql
├─ V2__create_air_quality_materialized_views.sql
├─ V3__add_air_quality_materialized_view_resolutions.sql
└─ V4__recreate_air_quality_materialized_views_with_double_precision.sql

src/main/proto
├─ sensor/v1/sensor.proto
├─ sensor/v2/sensor.proto
└─ airpurifier/v1/airpurifier.proto
```

## 실행 전 요구사항

- JDK 25
- PostgreSQL
- `sensor.v2.SensorService`와 `airpurifier.v1.AirPurifierService`를 제공하는 gRPC 서버

기본 설정:

- HTTP 서버 포트: `18081`
- PostgreSQL: `localhost:5432`, 데이터베이스명 `ecoknock`
- gRPC 서버: `localhost:6565`

## 환경 변수

이 프로젝트는 개발 환경에서 `springboot4-dotenv`를 사용하므로 루트 `.env` 파일 또는 시스템 환경 변수로 값을 주입할 수 있습니다.

필수:

- `JWT_SECRET_KEY`
  - JWT 서명 키입니다.
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

테스트는 현재 개발 DB 설정을 사용합니다. E2E 테스트는 `2099-01-01T00:00:00Z`부터 `2099-01-01T00:10:00Z` 전까지의 테스트 데이터만 삽입/삭제합니다.

## proto 생성

수동으로 proto 생성만 실행하려면:

```bash
./gradlew generateProto
```

생성 대상 proto:

- [`sensor.proto`](./src/main/proto/sensor/v1/sensor.proto)
- [`sensor.v2.proto`](./src/main/proto/sensor/v2/sensor.proto)
- [`airpurifier.proto`](./src/main/proto/airpurifier/v1/airpurifier.proto)

## DB 마이그레이션

Flyway는 `classpath:db/migration` 아래 SQL을 실행합니다.

- `V1`: `air_quality`, `member` 테이블 생성
- `V2`: `5m`, `15m`, `1h` 공기질 materialized view 생성
- `V3`: `1m`, `4h`, `1d` 공기질 materialized view 추가
- `V4`: 공기질 materialized view를 `avg_pm25 double precision` 기준으로 재생성

JPA 설정은 `ddl-auto: validate`이므로 애플리케이션 시작 시 엔티티와 DB 스키마가 맞는지 검증합니다.

## 공기질 수집 흐름

1. `AirQualityProducer`가 앱 시작 후 센서/공기청정기 gRPC 서버를 polling합니다.
2. 조회한 원시 데이터를 `AirQualityDTO`로 병합합니다.
3. `SaveAirQualityQueue`에 저장 명령을 넣습니다.
4. `AirQualityConsumer`가 명령을 소비해 `air_quality` 테이블에 저장합니다.
5. 저장 후 SSE 구독자에게 최신 데이터를 발행합니다.

gRPC 조회 실패 시 producer는 백오프 delay를 적용합니다.

## 공기질 조회 API

현재 timeseries API는 `GET` 요청에 JSON body를 받습니다.

### 범위 조회

```http
GET /air-quality/timeseries
Content-Type: application/json

{
  "resolution": "5m",
  "from": "2026-04-22T00:00:00Z",
  "to": "2026-04-22T01:00:00Z"
}
```

응답은 Spring Data `Slice<AirQualityTimeseriesPointResponse>`입니다.

```json
{
  "content": [
    {
      "time": "2026-04-22T09:00:00+09:00",
      "end": "2026-04-22T09:05:00+09:00",
      "pm25": 15.0,
      "pm25Min": 10,
      "pm25Max": 20,
      "humidity": 50.0,
      "temperature": 21.0,
      "eco2": 600.0,
      "bvoc": 0.2,
      "sampleCount": 2
    }
  ],
  "last": true
}
```

### 과거 데이터 조회

```http
GET /air-quality/timeseries/history
Content-Type: application/json

{
  "resolution": "5m",
  "before": "2026-04-22T00:00:00Z",
  "limit": 100
}
```

`history`는 `before` 이전 bucket을 조회합니다. 내부적으로 `limit + 1`개를 조회해 다음 데이터 존재 여부를 판단하고, 응답은 그래프에 바로 쓰기 좋도록 시간 오름차순으로 반환합니다.

`last`가 `false`이면 더 과거의 데이터가 남아 있다는 뜻입니다.

지원 resolution:

- `1m`
- `5m`
- `15m`
- `1h`
- `4h`
- `1d`

`limit`은 `1` 이상 `500` 이하만 허용합니다.

## SSE

```http
GET /air-quality/stream
Accept: text/event-stream
```

연결 직후 `connected` 이벤트를 전송합니다.

공기질 저장 성공 시 `air-quality` 이벤트로 최신 데이터를 전송합니다.

## 보안 동작

현재 보안 설정은 JWT 기반 stateless 인증을 전제로 합니다.

- 기본적으로 모든 요청은 인증이 필요합니다.
- `GET /air-quality/**`는 optional 인증입니다.
- `/air-quality/stream`은 SSE 특성상 인증 없이 접근 가능합니다.
- `/actuator/health`, `/actuator/info`, `/actuator/prometheus`는 인증 없이 접근 가능합니다.
- JWT 필터는 `Authorization` 헤더를 기준으로 인증을 시도합니다.
- OAuth2 관련 클래스는 존재하지만 로그인 플로우는 아직 활성화되어 있지 않습니다.

## Actuator / Prometheus

노출된 Actuator 엔드포인트:

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/prometheus`

Prometheus는 `/actuator/prometheus`를 scrape하면 됩니다.

## gRPC

현재 사용하는 RPC:

- `sensor.v2.SensorService/GetCurrentSensor`
- `airpurifier.v1.AirPurifierService/GetCurrentAirPurifier`

현재 센서 RPC 응답에는 기본 측정값 외에도 자체 알고리즘으로 계산한 다음 지표가 포함됩니다.

- `static_iaq`
- `estimated_eco2_ppm`
- `estimated_bvoc_ppm`
- `accuracy`
- `stabilization_progress_pct`
- `gas_percentage`
- `learning_complete_at_unix_ms`

## 현재 상태 요약

이 저장소는 중앙 백엔드의 공기질 수집·조회 흐름을 구현 중입니다. gRPC polling, DB 저장, materialized view 기반 timeseries 조회, SSE 실시간 전송, Actuator 메트릭 노출은 구현되어 있습니다. OAuth2 로그인 플로우는 아직 활성화되어 있지 않습니다.
