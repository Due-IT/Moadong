package moadong.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import moadong.club.payload.request.ClubRecruitmentInfoUpdateRequest;
import moadong.global.annotation.RecruitmentPeriod;
import moadong.global.exception.ErrorCode;

import java.time.LocalDateTime;

public class RecruitmentPeriodValidator implements ConstraintValidator<RecruitmentPeriod, ClubRecruitmentInfoUpdateRequest> {

    @Override
    public boolean isValid(ClubRecruitmentInfoUpdateRequest value, ConstraintValidatorContext context) {
        final LocalDateTime start = value.recruitmentStart();
        final LocalDateTime end = value.recruitmentEnd();

        // 동시에 null 이거나 동시에 null이 아닐 것
        boolean isBothNull = (start == null && end == null);
        boolean isBothNotNull = (start != null && end != null);

        // 한쪽만 null인 경우, 유효성 검증 실패
        if (!isBothNull && !isBothNotNull) {
            context.buildConstraintViolationWithTemplate(
                    ErrorCode.ONE_PERIOD_CANNOT_BE_NULL.getMessage()
            ).addConstraintViolation();
            return false;
        }

        // 둘 다 null이면 상시 모집
        if (isBothNull) {
            return true;
        }

        // 모집시작일은 모집종료일보다 같거나 늦을 수 없다
        if (start.isEqual(end) || start.isAfter(end)){
            context.buildConstraintViolationWithTemplate(
                    ErrorCode.START_SHOULD_BE_FASTER_THAN_END.getMessage()
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}