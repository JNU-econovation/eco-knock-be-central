package jnu.econovation.ecoknockbecentral.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeUtil {
    public static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static ZonedDateTime convert(Instant instant) {
        return ZonedDateTime.ofInstant(instant, SEOUL_ZONE_ID);
    }
}
