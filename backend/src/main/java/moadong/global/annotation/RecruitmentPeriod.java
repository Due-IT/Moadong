package moadong.global.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import moadong.global.validator.RecruitmentPeriodValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RecruitmentPeriodValidator.class)
@Target({ElementType.TYPE}) // 클래스 레벨에 적용
@Retention(RetentionPolicy.RUNTIME)
public @interface RecruitmentPeriod {

    String message() default "모집 시작일과 종료일의 유효성 검증에 실패했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
