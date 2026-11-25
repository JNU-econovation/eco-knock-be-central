package jnu.econovation.ecoknockbecentral.common.security.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class SecurityPath {
    public static final String ROOT = "/";

    public static final String AUTH_SUCCESS = "/auth/success";
    public static final String AUTH_REISSUE = "/auth/reissue";
    public static final String AUTH_LOGOUT = "/auth/logout";
    public static final String AUTH_GUEST = "/auth/guest";
    public static final String AUTH_ADMIN = "/auth/admin";

    public static final String SSO_LOGIN = "/sso/login";
    public static final String SSO_CALLBACK = "/sso/callback";
    public static final String SSO_PASSPORT = "/sso/passport";

    public static final String ADMIN = "/admin";
    public static final String ADMIN_SLASH = "/admin/";
    public static final String ADMIN_ALL = "/admin/**";
    public static final String ADMIN_LOGIN = "/admin/login";
    public static final String ADMIN_STATIC_CSS = "/admin/*.css";
    public static final String ADMIN_STATIC_JS = "/admin/*.js";
    public static final String ADMIN_LOGIN_MASTER = "/admin/login/master";
    public static final String ADMIN_LOGOUT = "/admin/logout";
    public static final String ADMIN_ACCESS_DENIED = "/admin/access-denied";

    public static final String SCALAR = "/scalar";
    public static final String SCALAR_ALL = "/scalar/**";
    public static final String OPENAPI_JSON = "/v3/api-docs";
    public static final String OPENAPI_ALL = "/v3/api-docs/**";
    public static final String OPENAPI_YAML = "/v3/api-docs.yaml";
}
