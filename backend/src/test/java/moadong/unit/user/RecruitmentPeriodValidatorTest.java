package moadong.unit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import moadong.club.payload.request.ClubRecruitmentInfoUpdateRequest;
import moadong.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecruitmentPeriodValidator 유효성 검증 테스트")
class RecruitmentPeriodValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("성공: 시작일과 종료일이 모두 null이면 (상시 모집) 성공한다")
    void isValid_Success_BothNull() {
        // given
        ClubRecruitmentInfoUpdateRequest request = new ClubRecruitmentInfoUpdateRequest(
                null, // recruitmentStart
                null, // recruitmentEnd
                "대상", "설명", "url", null
        );

        // when
        Set<ConstraintViolation<ClubRecruitmentInfoUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("성공: 시작일이 종료일보다 빠르면 성공한다")
    void isValid_Success_StartBeforeEnd() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        ClubRecruitmentInfoUpdateRequest request = new ClubRecruitmentInfoUpdateRequest(
                start, // recruitmentStart
                end,   // recruitmentEnd
                "대상", "설명", "url", null
        );

        // when
        Set<ConstraintViolation<ClubRecruitmentInfoUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("실패: 시작일만 null이고 종료일은 null이 아니면 실패한다")
    void isInvalid_Failure_StartIsNull() {
        // given
        LocalDateTime end = LocalDateTime.now().plusDays(5);

        ClubRecruitmentInfoUpdateRequest request = new ClubRecruitmentInfoUpdateRequest(
                null, // recruitmentStart
                end,  // recruitmentEnd
                "대상", "설명", "url", null
        );

        // when
        Set<ConstraintViolation<ClubRecruitmentInfoUpdateRequest>> violations = validator.validate(request);
        String violationMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining());

        // then
        assertThat(violationMessage).contains(ErrorCode.ONE_PERIOD_CANNOT_BE_NULL.getMessage());
    }

    @Test
    @DisplayName("실패: 시작일은 null이 아니고 종료일만 null이면 실패한다")
    void isInvalid_Failure_EndIsNull() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        ClubRecruitmentInfoUpdateRequest request = new ClubRecruitmentInfoUpdateRequest(
                start, // recruitmentStart
                null,  // recruitmentEnd
                "대상", "설명", "url", null
        );

        // when
        Set<ConstraintViolation<ClubRecruitmentInfoUpdateRequest>> violations = validator.validate(request);
        String violationMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining());

        // then
        assertThat(violationMessage).contains(ErrorCode.ONE_PERIOD_CANNOT_BE_NULL.getMessage());
    }

    @Test
    @DisplayName("실패: 시작일이 종료일과 같으면 실패한다 (늦을 수 없다는 조건에 따라)")
    void isInvalid_Failure_StartEqualsEnd() {
        // given
        LocalDateTime now = LocalDateTime.now().withNano(0); // 나노초 제거하여 완벽히 같도록

        ClubRecruitmentInfoUpdateRequest request = new ClubRecruitmentInfoUpdateRequest(
                now, // recruitmentStart
                now, // recruitmentEnd
                "대상", "설명", "url", null
        );

        // when
        Set<ConstraintViolation<ClubRecruitmentInfoUpdateRequest>> violations = validator.validate(request);
        String violationMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining());

        // then
        assertThat(violationMessage).contains(ErrorCode.START_SHOULD_BE_FASTER_THAN_END.getMessage());
    }

    @Test
    @DisplayName("실패: 시작일이 종료일보다 늦으면 실패한다")
    void isInvalid_Failure_StartAfterEnd() {
        // given
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        ClubRecruitmentInfoUpdateRequest request = new ClubRecruitmentInfoUpdateRequest(
                start,
                end,
                "대상", "설명", "url", null
        );

        // when
        Set<ConstraintViolation<ClubRecruitmentInfoUpdateRequest>> violations = validator.validate(request);
        String violationMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining());

        // then
        assertThat(violationMessage).contains(ErrorCode.START_SHOULD_BE_FASTER_THAN_END.getMessage());
    }
}
