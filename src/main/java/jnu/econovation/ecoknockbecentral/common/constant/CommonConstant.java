package jnu.econovation.ecoknockbecentral.common.constant;

import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstant {

    private static final String CRITICAL_ERROR_MESSAGE = "알 수 없는 예외로 인한 %s 실패";

    public static final Function<String, String> criticalError = action
            -> String.format(CRITICAL_ERROR_MESSAGE, action);
}