package jnu.econovation.ecoknockbecentral.airquality.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jnu.econovation.ecoknockbecentral.airquality.exception.BadAirQualityResolutionException;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AirQualityResolution {
    ONE_MINUTE("1m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    ONE_HOUR("1h"),
    FOUR_HOURS("4h"),
    ONE_DAY("1d");

    private final String code;

    AirQualityResolution(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static String supportedCodes() {
        return Arrays.stream(values())
                .map(AirQualityResolution::getCode)
                .collect(Collectors.joining(", "));
    }

    public static AirQualityResolution from(String code) {
        return Arrays.stream(values())
                .filter(resolution -> resolution.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AirQualityResolution fromOrThrowBusinessException(String code) {
        AirQualityResolution resolution = from(code);
        if (resolution == null) {
            throw new BadAirQualityResolutionException();
        }
        return resolution;
    }
}
