CREATE TABLE air_quality_1m_aggregate
(
    bucket_start    TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_end      TIMESTAMP WITH TIME ZONE NOT NULL,
    sum_pm25        BIGINT                   NOT NULL,
    max_pm25        INTEGER                  NOT NULL,
    min_pm25        INTEGER                  NOT NULL,
    sum_humidity    DOUBLE PRECISION         NOT NULL,
    sum_temperature DOUBLE PRECISION         NOT NULL,
    sum_eco2        DOUBLE PRECISION         NOT NULL,
    sum_bvoc        DOUBLE PRECISION         NOT NULL,
    sample_count    BIGINT                   NOT NULL,
    CONSTRAINT pk_air_quality_1m_aggregate PRIMARY KEY (bucket_start)
);

CREATE TABLE air_quality_5m_aggregate
(
    bucket_start    TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_end      TIMESTAMP WITH TIME ZONE NOT NULL,
    sum_pm25        BIGINT                   NOT NULL,
    max_pm25        INTEGER                  NOT NULL,
    min_pm25        INTEGER                  NOT NULL,
    sum_humidity    DOUBLE PRECISION         NOT NULL,
    sum_temperature DOUBLE PRECISION         NOT NULL,
    sum_eco2        DOUBLE PRECISION         NOT NULL,
    sum_bvoc        DOUBLE PRECISION         NOT NULL,
    sample_count    BIGINT                   NOT NULL,
    CONSTRAINT pk_air_quality_5m_aggregate PRIMARY KEY (bucket_start)
);

CREATE TABLE air_quality_15m_aggregate
(
    bucket_start    TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_end      TIMESTAMP WITH TIME ZONE NOT NULL,
    sum_pm25        BIGINT                   NOT NULL,
    max_pm25        INTEGER                  NOT NULL,
    min_pm25        INTEGER                  NOT NULL,
    sum_humidity    DOUBLE PRECISION         NOT NULL,
    sum_temperature DOUBLE PRECISION         NOT NULL,
    sum_eco2        DOUBLE PRECISION         NOT NULL,
    sum_bvoc        DOUBLE PRECISION         NOT NULL,
    sample_count    BIGINT                   NOT NULL,
    CONSTRAINT pk_air_quality_15m_aggregate PRIMARY KEY (bucket_start)
);

CREATE TABLE air_quality_1h_aggregate
(
    bucket_start    TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_end      TIMESTAMP WITH TIME ZONE NOT NULL,
    sum_pm25        BIGINT                   NOT NULL,
    max_pm25        INTEGER                  NOT NULL,
    min_pm25        INTEGER                  NOT NULL,
    sum_humidity    DOUBLE PRECISION         NOT NULL,
    sum_temperature DOUBLE PRECISION         NOT NULL,
    sum_eco2        DOUBLE PRECISION         NOT NULL,
    sum_bvoc        DOUBLE PRECISION         NOT NULL,
    sample_count    BIGINT                   NOT NULL,
    CONSTRAINT pk_air_quality_1h_aggregate PRIMARY KEY (bucket_start)
);

CREATE TABLE air_quality_4h_aggregate
(
    bucket_start    TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_end      TIMESTAMP WITH TIME ZONE NOT NULL,
    sum_pm25        BIGINT                   NOT NULL,
    max_pm25        INTEGER                  NOT NULL,
    min_pm25        INTEGER                  NOT NULL,
    sum_humidity    DOUBLE PRECISION         NOT NULL,
    sum_temperature DOUBLE PRECISION         NOT NULL,
    sum_eco2        DOUBLE PRECISION         NOT NULL,
    sum_bvoc        DOUBLE PRECISION         NOT NULL,
    sample_count    BIGINT                   NOT NULL,
    CONSTRAINT pk_air_quality_4h_aggregate PRIMARY KEY (bucket_start)
);

CREATE TABLE air_quality_1d_aggregate
(
    bucket_start    TIMESTAMP WITH TIME ZONE NOT NULL,
    bucket_end      TIMESTAMP WITH TIME ZONE NOT NULL,
    sum_pm25        BIGINT                   NOT NULL,
    max_pm25        INTEGER                  NOT NULL,
    min_pm25        INTEGER                  NOT NULL,
    sum_humidity    DOUBLE PRECISION         NOT NULL,
    sum_temperature DOUBLE PRECISION         NOT NULL,
    sum_eco2        DOUBLE PRECISION         NOT NULL,
    sum_bvoc        DOUBLE PRECISION         NOT NULL,
    sample_count    BIGINT                   NOT NULL,
    CONSTRAINT pk_air_quality_1d_aggregate PRIMARY KEY (bucket_start)
);

INSERT INTO air_quality_1m_aggregate
SELECT
    date_bin('1 minute', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00'),
    date_bin('1 minute', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00') + INTERVAL '1 minute',
    SUM(pm25), MAX(pm25), MIN(pm25), SUM(humidity), SUM(temperature), SUM(estimated_eco2ppm), SUM(estimated_bvocppm), COUNT(*)
FROM air_quality
GROUP BY 1, 2;

INSERT INTO air_quality_5m_aggregate
SELECT
    date_bin('5 minutes', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00'),
    date_bin('5 minutes', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00') + INTERVAL '5 minutes',
    SUM(pm25), MAX(pm25), MIN(pm25), SUM(humidity), SUM(temperature), SUM(estimated_eco2ppm), SUM(estimated_bvocppm), COUNT(*)
FROM air_quality
GROUP BY 1, 2;

INSERT INTO air_quality_15m_aggregate
SELECT
    date_bin('15 minutes', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00'),
    date_bin('15 minutes', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00') + INTERVAL '15 minutes',
    SUM(pm25), MAX(pm25), MIN(pm25), SUM(humidity), SUM(temperature), SUM(estimated_eco2ppm), SUM(estimated_bvocppm), COUNT(*)
FROM air_quality
GROUP BY 1, 2;

INSERT INTO air_quality_1h_aggregate
SELECT
    date_bin('1 hour', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00'),
    date_bin('1 hour', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00') + INTERVAL '1 hour',
    SUM(pm25), MAX(pm25), MIN(pm25), SUM(humidity), SUM(temperature), SUM(estimated_eco2ppm), SUM(estimated_bvocppm), COUNT(*)
FROM air_quality
GROUP BY 1, 2;

INSERT INTO air_quality_4h_aggregate
SELECT
    date_bin('4 hours', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00'),
    date_bin('4 hours', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00') + INTERVAL '4 hours',
    SUM(pm25), MAX(pm25), MIN(pm25), SUM(humidity), SUM(temperature), SUM(estimated_eco2ppm), SUM(estimated_bvocppm), COUNT(*)
FROM air_quality
GROUP BY 1, 2;

INSERT INTO air_quality_1d_aggregate
SELECT
    date_bin('1 day', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00'),
    date_bin('1 day', sensor_measured_at, TIMESTAMP WITH TIME ZONE '1970-01-01 00:00:00+00') + INTERVAL '1 day',
    SUM(pm25), MAX(pm25), MIN(pm25), SUM(humidity), SUM(temperature), SUM(estimated_eco2ppm), SUM(estimated_bvocppm), COUNT(*)
FROM air_quality
GROUP BY 1, 2;

DROP MATERIALIZED VIEW air_quality_1m_mv;
DROP MATERIALIZED VIEW air_quality_5m_mv;
DROP MATERIALIZED VIEW air_quality_15m_mv;
DROP MATERIALIZED VIEW air_quality_1h_mv;
DROP MATERIALIZED VIEW air_quality_4h_mv;
DROP MATERIALIZED VIEW air_quality_1d_mv;
