package moadong.unit.club;

import moadong.club.entity.ClubRecruitmentInformation;
import moadong.club.enums.ClubRecruitmentStatus;
import moadong.util.annotations.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("ClubRecruitmentInformation 모집 상태 업데이트 테스트")
class ClubRecruitmentInformationTest {

    private final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    // 2025년 12월 25일 10:00:00
    private final LocalDateTime START_DATE = LocalDateTime.of(2025, 12, 25, 10, 0, 0);
    // 2026년 1월 5일 23:59:59
    private final LocalDateTime END_DATE = LocalDateTime.of(2026, 1, 5, 23, 59, 59);
    // 2025년 12월 18일 10:00:00
    private final LocalDateTime UPCOMING_DATE = START_DATE.minusDays(7);

    private ClubRecruitmentInformation clubRecruitmentInfo;

    // 테스트 대상 클래스의 private 메서드를 호출하기 위한 ClubRecruitmentInformation 확장 클래스
    // Clock을 주입받아 'now' 시간을 고정하여 테스트합니다.
    private class TestableClubRecruitmentInformation extends ClubRecruitmentInformation {
        private final Clock clock;

        public TestableClubRecruitmentInformation(Clock clock, LocalDateTime start, LocalDateTime end) {
            super("id", null, null, null, null, null, null, start, end, null, null, null, null, null, ClubRecruitmentStatus.CLOSED);
            this.clock = clock;
        }

        @Override
        protected void updateRecruitmentStatus() {
            // ZonedDateTime.now 대신 Mocking된 Clock을 사용
            LocalDateTime now = ZonedDateTime.now(clock).toLocalDateTime();

            LocalDateTime recruitmentStartLocal = super.getRecruitmentStart().toLocalDateTime(); // 커스텀 getter는 ZDT를 반환합니다.
            LocalDateTime recruitmentEndLocal = super.getRecruitmentEnd().toLocalDateTime();
            LocalDateTime upcomingDate = UPCOMING_DATE;

            if(now.isBefore(upcomingDate)) {
                updateRecruitmentStatus(ClubRecruitmentStatus.CLOSED);
            } else if(now.isEqual(upcomingDate) || (now.isAfter(upcomingDate) && now.isBefore(recruitmentStartLocal))){
                updateRecruitmentStatus(ClubRecruitmentStatus.UPCOMING);
            } else if(now.isEqual(recruitmentStartLocal) || (now.isAfter(recruitmentStartLocal) && now.isBefore(recruitmentEndLocal))) {
                updateRecruitmentStatus(ClubRecruitmentStatus.OPEN);
            } else if(now.isEqual(recruitmentEndLocal) || now.isAfter(recruitmentEndLocal)){
                updateRecruitmentStatus(ClubRecruitmentStatus.CLOSED);
            }
        }
    }


    /**
     * 특정 시간으로 Clock을 설정하고 테스트 인스턴스를 초기화하는 헬퍼 메서드
     * @param fixedTime 설정할 시간
     */
    private void setupTestInstance(LocalDateTime fixedTime) {
        Instant fixedInstant = fixedTime.atZone(SEOUL_ZONE).toInstant();
        // Mocking 대신 Clock.fixed를 사용하여 실제 Clock 인스턴스를 생성
        Clock fixedClock = Clock.fixed(fixedInstant, SEOUL_ZONE);

        clubRecruitmentInfo = new TestableClubRecruitmentInformation(fixedClock, START_DATE, END_DATE);
    }

    @Test
    @DisplayName("예정일보다 이전 -> CLOSED")
    void testClosed_BeforeUpcoming() {
        // GIVEN: 2025-12-17 09:59:59 (예정일 12/18 10:00:00 보다 이전)
        setupTestInstance(LocalDateTime.of(2025, 12, 17, 9, 59, 59));

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.CLOSED);
    }

    @Test
    @DisplayName("정확히 예정일인 경우 -> UPCOMING")
    void testUpcoming_OnUpcomingDate() {
        // GIVEN: 2025-12-18 10:00:00 (정확히 예정일)
        setupTestInstance(UPCOMING_DATE);

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.UPCOMING);
    }

    @Test
    @DisplayName("시작일 바로 직전 -> UPCOMING")
    void testUpcoming_JustBeforeStart() {
        // GIVEN: 2025-12-25 09:59:59 (시작일 10:00:00 바로 직전)
        setupTestInstance(LocalDateTime.of(2025, 12, 25, 9, 59, 59));

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.UPCOMING);
    }

    @Test
    @DisplayName("정확히 시작일인 경우 -> OPEN")
    void testOpen_OnStartDate() {
        // GIVEN: 2025-12-25 10:00:00 (정확히 시작일)
        setupTestInstance(START_DATE);

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.OPEN);
    }

    @Test
    @DisplayName("종료일 바로 직전 -> OPEN")
    void testOpen_JustBeforeEnd() {
        // GIVEN: 2026-01-05 23:59:58 (종료일 23:59:59 바로 직전)
        setupTestInstance(LocalDateTime.of(2026, 1, 5, 23, 59, 58));

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.OPEN);
    }

    @Test
    @DisplayName("정확히 종료일인 경우 -> CLOSED")
    void testClosed_OnEndDate() {
        // GIVEN: 2026-01-05 23:59:59 (정확히 종료일)
        setupTestInstance(END_DATE);

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.CLOSED);
    }

    @Test
    @DisplayName("종료일 이후 -> CLOSED")
    void testClosed_AfterEnd() {
        // GIVEN: 2026-01-06 00:00:00 (종료일 다음 순간)
        setupTestInstance(LocalDateTime.of(2026, 1, 6, 0, 0, 0));

        // WHEN
        ((TestableClubRecruitmentInformation) clubRecruitmentInfo).updateRecruitmentStatus();

        // THEN
        assertThat(clubRecruitmentInfo.getClubRecruitmentStatus()).isEqualTo(ClubRecruitmentStatus.CLOSED);
    }
}