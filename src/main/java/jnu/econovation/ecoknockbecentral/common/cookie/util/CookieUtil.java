package jnu.econovation.ecoknockbecentral.common.cookie.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtil {
    public static void addCookie(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull String key,
            @NonNull String value,
            int maxAge
    ) {

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(key, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite("Lax");

        if (request.isSecure()) {
            cookieBuilder.secure(true);
            cookieBuilder.sameSite("None");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }

    public static void removeCookie(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            String key
    ) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
                .from(key, "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .sameSite("Lax");

        if (request.isSecure()) {
            cookieBuilder.secure(true);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }
}