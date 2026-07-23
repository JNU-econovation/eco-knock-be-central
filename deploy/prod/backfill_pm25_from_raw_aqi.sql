\set ON_ERROR_STOP on

-- CALL과 각 동시 물질화 뷰 갱신이 자체 트랜잭션을 관리할 수 있도록 명시적 트랜잭션 밖에서 psql로 실행한다.
-- 이 스크립트는 의도적으로 Flyway 마이그레이션에 포함하지 않는다.
SELECT coalesce(max(id), 0) AS target_max_id
FROM air_quality
\gset

\echo 보정 대상 스냅샷 id: :target_max_id
\echo 보정 전
SELECT count(*) AS rows_with_valid_raw_aqi
FROM air_quality
WHERE id <= :target_max_id
  AND raw_air_purifier ->> 'aqi' ~ '^-?[0-9]+$'
  AND (raw_air_purifier ->> 'aqi')::numeric BETWEEN -2147483648 AND 2147483647;

SELECT count(*) AS pm25_mismatches
FROM air_quality
WHERE id <= :target_max_id
  AND raw_air_purifier ->> 'aqi' ~ '^-?[0-9]+$'
  AND (raw_air_purifier ->> 'aqi')::numeric BETWEEN -2147483648 AND 2147483647
  AND pm25 IS DISTINCT FROM (raw_air_purifier ->> 'aqi')::integer;

CREATE OR REPLACE PROCEDURE pg_temp.backfill_pm25_from_raw_aqi(
    target_id bigint,
    INOUT updated_rows bigint
)
LANGUAGE plpgsql
AS $$
DECLARE
    batch_start_id bigint := 0;
    batch_end_id bigint;
    batch_updated_rows bigint;
BEGIN
    updated_rows := 0;
    PERFORM set_config('lock_timeout', '2s', false);

    WHILE batch_start_id < target_id LOOP
        batch_end_id := least(batch_start_id + 10000, target_id);

        WITH target_rows AS (
            SELECT id, (raw_air_purifier ->> 'aqi')::integer AS aqi
            FROM air_quality
            WHERE id > batch_start_id
              AND id <= batch_end_id
              AND raw_air_purifier ->> 'aqi' ~ '^-?[0-9]+$'
              AND (raw_air_purifier ->> 'aqi')::numeric BETWEEN -2147483648 AND 2147483647
        )
        UPDATE air_quality
        SET pm25 = target_rows.aqi
        FROM target_rows
        WHERE air_quality.id = target_rows.id
          AND air_quality.pm25 IS DISTINCT FROM target_rows.aqi;

        GET DIAGNOSTICS batch_updated_rows = ROW_COUNT;
        updated_rows := updated_rows + batch_updated_rows;
        RAISE NOTICE 'id 범위 (% - %] 처리 완료, %건 갱신', batch_start_id, batch_end_id, batch_updated_rows;

        batch_start_id := batch_end_id;
        COMMIT;

        IF batch_start_id < target_id THEN
            PERFORM pg_sleep(0.25);
        END IF;
    END LOOP;
END;
$$;

\echo 보정 실행
CALL pg_temp.backfill_pm25_from_raw_aqi(:target_max_id, 0);

\echo 보정 후
SELECT count(*) AS rows_with_valid_raw_aqi
FROM air_quality
WHERE id <= :target_max_id
  AND raw_air_purifier ->> 'aqi' ~ '^-?[0-9]+$'
  AND (raw_air_purifier ->> 'aqi')::numeric BETWEEN -2147483648 AND 2147483647;

SELECT count(*) AS pm25_mismatches
FROM air_quality
WHERE id <= :target_max_id
  AND raw_air_purifier ->> 'aqi' ~ '^-?[0-9]+$'
  AND (raw_air_purifier ->> 'aqi')::numeric BETWEEN -2147483648 AND 2147483647
  AND pm25 IS DISTINCT FROM (raw_air_purifier ->> 'aqi')::integer;

\echo 공기질 물질화 뷰 갱신
REFRESH MATERIALIZED VIEW CONCURRENTLY air_quality_1m_mv;
REFRESH MATERIALIZED VIEW CONCURRENTLY air_quality_5m_mv;
REFRESH MATERIALIZED VIEW CONCURRENTLY air_quality_15m_mv;
REFRESH MATERIALIZED VIEW CONCURRENTLY air_quality_1h_mv;
REFRESH MATERIALIZED VIEW CONCURRENTLY air_quality_4h_mv;
REFRESH MATERIALIZED VIEW CONCURRENTLY air_quality_1d_mv;
