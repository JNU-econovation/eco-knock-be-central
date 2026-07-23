package jnu.econovation.ecoknockbecentral.common.security.resolver;

import jnu.econovation.ecoknockbecentral.common.security.constant.AuthPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class AuthPolicyResolverTest {
    private final AuthPolicyResolver resolver = new AuthPolicyResolver();

    @Test
    void publicAndInternalAuthenticationEndpointsSkipJwtFilter() {
        assertThat(resolve(HttpMethod.POST, "/auth/logout")).isEqualTo(AuthPolicy.SKIP);
        assertThat(resolve(HttpMethod.GET, "/sso/callback")).isEqualTo(AuthPolicy.SKIP);
        assertThat(resolve(HttpMethod.POST, "/sso/passport")).isEqualTo(AuthPolicy.SKIP);
        assertThat(resolve(HttpMethod.GET, "/admin/login")).isEqualTo(AuthPolicy.SKIP);
        assertThat(resolve(HttpMethod.GET, "/scalar")).isEqualTo(AuthPolicy.SKIP);
    }

    @Test
    void airQualityReadIsOptionalAndBusinessEndpointsRequireJwt() {
        assertThat(resolve(HttpMethod.GET, "/air-quality/timeseries/latest")).isEqualTo(AuthPolicy.OPTIONAL);
        assertThat(resolve(HttpMethod.GET, "/air-quality/timeseries/history/default")).isEqualTo(AuthPolicy.REQUIRED);
        assertThat(resolve(HttpMethod.PUT, "/air-quality/timeseries/history/default")).isEqualTo(AuthPolicy.REQUIRED);
        assertThat(resolve(HttpMethod.PUT, "/overview/shortcuts")).isEqualTo(AuthPolicy.REQUIRED);
        assertThat(resolve(HttpMethod.PUT, "/overview/shortcuts/reset")).isEqualTo(AuthPolicy.REQUIRED);
        assertThat(resolve(HttpMethod.PUT, "/overview/layout")).isEqualTo(AuthPolicy.REQUIRED);
        assertThat(resolve(HttpMethod.GET, "/profile")).isEqualTo(AuthPolicy.REQUIRED);
    }

    private AuthPolicy resolve(HttpMethod method, String path) {
        return resolver.resolve(new MockHttpServletRequest(method.name(), path));
    }
}
