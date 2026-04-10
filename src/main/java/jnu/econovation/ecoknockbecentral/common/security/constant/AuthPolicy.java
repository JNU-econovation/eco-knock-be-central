package jnu.econovation.ecoknockbecentral.common.security.constant;

public enum AuthPolicy {
    SKIP,               // 인증 아예 안 함
    REQUIRED,           // 인증 필수
    OPTIONAL            // 메서드 별로 정책이 다를 때
}