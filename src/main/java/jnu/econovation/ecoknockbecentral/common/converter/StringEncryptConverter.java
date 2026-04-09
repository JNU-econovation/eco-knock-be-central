package jnu.econovation.ecoknockbecentral.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jnu.econovation.ecoknockbecentral.common.security.util.AES256Util;
import org.jspecify.annotations.Nullable;

@Converter
public class StringEncryptConverter implements AttributeConverter<String, String> {

    private final AES256Util util;

    public StringEncryptConverter(AES256Util util) {
        this.util = util;
    }

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable String attribute) {
        return attribute != null ? util.encrypt(attribute) : null;
    }

    @Override
    @Nullable
    public String convertToEntityAttribute(@Nullable String dbData) {
        return dbData != null ? util.decrypt(dbData) : null;
    }
}