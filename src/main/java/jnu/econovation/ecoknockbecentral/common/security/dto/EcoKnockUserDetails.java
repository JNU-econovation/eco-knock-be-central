package jnu.econovation.ecoknockbecentral.common.security.dto;

import jnu.econovation.ecoknockbecentral.member.dto.MemberInfoDTO;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EcoKnockUserDetails implements UserDetails, OAuth2User {
    private static final String ROLE_PREFIX = "ROLE_";

    @Getter
    private final MemberInfoDTO memberInfo;
    private final Map<String, Object> attributes;
    private final String userNameAttributeName;

    public EcoKnockUserDetails(MemberInfoDTO memberInfo) {
        this(memberInfo, null, null);
    }

    public EcoKnockUserDetails(
            MemberInfoDTO memberInfo,
            Map<String, Object> attributes,
            String userNameAttributeName
    ) {
        this.memberInfo = memberInfo;
        this.attributes = attributes;
        this.userNameAttributeName = userNameAttributeName;
    }

    @Override
    public @NonNull String getUsername() {
        return memberInfo.getName();
    }

    @Override
    public String getPassword() {
        return "Social";
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + memberInfo.getRole()));
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public @NonNull String getName() {
        if (attributes == null || userNameAttributeName == null) {
            return "UNKNOWN";
        }

        Object value = attributes.get(userNameAttributeName);
        return value != null ? value.toString() : "UNKNOWN";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}