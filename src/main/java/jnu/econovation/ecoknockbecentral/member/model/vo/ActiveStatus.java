package jnu.econovation.ecoknockbecentral.member.model.vo;

import org.jspecify.annotations.Nullable;

public enum ActiveStatus {
    AM, RM, CM, OB;

    @Nullable
    public static ActiveStatus from(@Nullable String value) {
        if (value == null) {
            return null;
        }

        try {
            return ActiveStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }
}
