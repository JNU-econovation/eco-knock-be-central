package jnu.econovation.ecoknockbecentral.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 하나의 애플리케이션 작업을 담당하는 클래스에 붙이는 애너테이션이다.
 * 예를 들어 회원을 저장하거나, 공기질 데이터를 기록하거나, 특정 조건으로
 * 데이터를 조회하는 기능처럼 하나의 목적이 분명한 작업에 사용한다.
 *
 * <p>유스케이스 클래스는 여러 도메인 객체, 리포지토리, 외부 시스템 호출을
 * 묶어서 하나의 흐름으로 실행하는 역할을 맡는다. 이를 통해 비즈니스 흐름을
 * 한 곳에서 읽고 관리할 수 있다.
 *
 * <p>이 애너테이션은 {@link Component @Component}의 특수화로 동작하므로,
 * 스프링이 클래스패스 스캐닝을 통해 해당 클래스를 자동으로 빈으로 등록할 수 있다.
 *
 * @see Component
 * @see Service
 * @see Repository
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface UseCase {
    @AliasFor(annotation = Component.class)
    String value() default "";
}