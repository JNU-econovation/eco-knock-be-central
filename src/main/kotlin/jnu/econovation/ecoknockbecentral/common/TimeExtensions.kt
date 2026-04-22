package jnu.econovation.ecoknockbecentral.common

import jnu.econovation.ecoknockbecentral.common.util.TimeUtil
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

fun Instant.toZonedDateTime(zoneId: ZoneId = TimeUtil.SEOUL_ZONE_ID): ZonedDateTime {
    return ZonedDateTime.ofInstant(this, zoneId)
}