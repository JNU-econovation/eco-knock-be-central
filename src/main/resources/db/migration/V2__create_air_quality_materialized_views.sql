create materialized view air_quality_5m_mv as
select
    date_bin(
            interval '5 minutes',
            sensor_measured_at,
            timestamp with time zone '1970-01-01 00:00:00+00'
    ) as bucket_start,
    date_bin(
            interval '5 minutes',
            sensor_measured_at,
            timestamp with time zone '1970-01-01 00:00:00+00'
    ) + interval '5 minutes' as bucket_end,
    avg(pm25) as avg_pm25,
    max(pm25) as max_pm25,
    min(pm25) as min_pm25,
    avg(humidity) as avg_humidity,
    avg(temperature) as avg_temperature,
    avg(estimated_eco2ppm) as avg_eco2,
    avg(estimated_bvocppm) as avg_bvoc,
    count(*) as sample_count
from air_quality
group by bucket_start;

create unique index idx_air_quality_5m_mv_bucket_start
    on air_quality_5m_mv (bucket_start);

create materialized view air_quality_15m_mv as
select
    date_bin(
            interval '15 minutes',
            sensor_measured_at,
            timestamp with time zone '1970-01-01 00:00:00+00'
    ) as bucket_start,
    date_bin(
            interval '15 minutes',
            sensor_measured_at,
            timestamp with time zone '1970-01-01 00:00:00+00'
    ) + interval '15 minutes' as bucket_end,
    avg(pm25) as avg_pm25,
    max(pm25) as max_pm25,
    min(pm25) as min_pm25,
    avg(humidity) as avg_humidity,
    avg(temperature) as avg_temperature,
    avg(estimated_eco2ppm) as avg_eco2,
    avg(estimated_bvocppm) as avg_bvoc,
    count(*) as sample_count
from air_quality
group by bucket_start;

create unique index idx_air_quality_15m_mv_bucket_start
    on air_quality_15m_mv (bucket_start);

create materialized view air_quality_1h_mv as
select
    date_bin(
            interval '1 hour',
            sensor_measured_at,
            timestamp with time zone '1970-01-01 00:00:00+00'
    ) as bucket_start,
    date_bin(
            interval '1 hour',
            sensor_measured_at,
            timestamp with time zone '1970-01-01 00:00:00+00'
    ) + interval '1 hour' as bucket_end,
    avg(pm25) as avg_pm25,
    max(pm25) as max_pm25,
    min(pm25) as min_pm25,
    avg(humidity) as avg_humidity,
    avg(temperature) as avg_temperature,
    avg(estimated_eco2ppm) as avg_eco2,
    avg(estimated_bvocppm) as avg_bvoc,
    count(*) as sample_count
from air_quality
group by bucket_start;

create unique index idx_air_quality_1h_mv_bucket_start
    on air_quality_1h_mv (bucket_start);
