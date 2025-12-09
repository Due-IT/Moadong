package moadong.unit.club.scheduler;

import moadong.club.enums.ClubRecruitmentStatus;
import moadong.club.scheduler.RecruitStatusSchedulerManager;
import moadong.util.annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("RecruitStatusSchedulerManager 스케줄링 테스트")
public class RecruitStatusSchedulerManagerTest {

    @InjectMocks
    private RecruitStatusSchedulerManager recruitStatusSchedulerManager;

    @Mock
    private Scheduler scheduler;

    private static final String CLUB_ID = "testClub123";
    private static final int UPCOMING_BEFORE = 7;

    private ZonedDateTime today;
    private ZonedDateTime upcomingDate;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    @BeforeEach
    void setUp() {
        // 테스트 실행 시점 고정 (2025년 1월 1일 10시)
        today = ZonedDateTime.of(2025, 1, 1, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));

        // 모집 시작일 (2025년 1월 10일)
        startDate = ZonedDateTime.of(2025, 1, 10, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));

        // 모집 마감일 (2025년 1월 20일)
        endDate = ZonedDateTime.of(2025, 1, 20, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));

        // UPCOMING 전환일: startDate의 7일 전 (2025년 1월 3일)
        upcomingDate = ZonedDateTime.of(2025, 1, 3, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));

    }

    // Job 중복제거 및 초기화 로직 자체의 호출만 검증
    @Test
    @DisplayName("rescheduleRecruitmentUpdates 호출 시 기존 Job 제거 로직이 호출되는지 확인")
    void testJobRemovalIsCalled() throws SchedulerException {
        // 실행
        recruitStatusSchedulerManager.rescheduleRecruitmentUpdates(CLUB_ID, today, startDate, endDate, UPCOMING_BEFORE);

        // Job/Trigger 제거가 3회씩 호출되었는지 검증 (clean-up 로직 자체의 검증)
        verify(scheduler, times(3)).unscheduleJob(any(TriggerKey.class));
        verify(scheduler, times(3)).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("모든 스케줄이 오늘 날짜 이후인 경우 : UPCOMING, OPEN, CLOSED 세 Job 모두 예약")
    void testRescheduleRecruitmentUpdates_AllJobsScheduled() throws SchedulerException {
        // 오늘: 1월 1일 (upcomingDate: 1월 3일, startDate: 1월 10일, endDate: 1월 20일)

        // 실행
        recruitStatusSchedulerManager.rescheduleRecruitmentUpdates(CLUB_ID, today, startDate, endDate, UPCOMING_BEFORE);

        // 새로운 Job 스케줄링 검증 (scheduleNewJob 호출 3회)
        verify(scheduler, times(3)).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // 캡처를 사용하여 JobData와 실행 시간을 확인
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

        // scheduleJob이 3번 호출되었을 때의 인자를 캡처
        verify(scheduler, times(3)).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());

        java.util.List<JobDetail> capturedJobDetails = jobDetailCaptor.getAllValues();
        java.util.List<Trigger> capturedTriggers = triggerCaptor.getAllValues();

        // 1. 첫 번째 예약 (UPCOMING) 검증
        JobDetail upcomingJob = capturedJobDetails.get(0);
        Trigger upcomingTrigger = capturedTriggers.get(0);
        assertThat(upcomingJob.getKey().getName()).contains(ClubRecruitmentStatus.UPCOMING.toString());
        assertThat(upcomingJob.getJobDataMap().getString("status")).isEqualTo(ClubRecruitmentStatus.UPCOMING.toString());
        assertThat(upcomingTrigger.getStartTime()).isEqualTo(upcomingDate.toInstant()); // 1월 3일

        // 2. 두 번째 예약 (OPEN) 검증
        JobDetail openJob = capturedJobDetails.get(1);
        Trigger openTrigger = capturedTriggers.get(1);
        assertThat(openJob.getKey().getName()).contains(ClubRecruitmentStatus.OPEN.toString());
        assertThat(openJob.getJobDataMap().getString("status")).isEqualTo(ClubRecruitmentStatus.OPEN.toString());
        assertThat(openTrigger.getStartTime()).isEqualTo(startDate.toInstant()); // 1월 10일

        // 3. 세 번째 예약 (CLOSED) 검증
        JobDetail closedJob = capturedJobDetails.get(2);
        Trigger closedTrigger = capturedTriggers.get(2);
        assertThat(closedJob.getKey().getName()).contains(ClubRecruitmentStatus.CLOSED.toString());
        assertThat(closedJob.getJobDataMap().getString("status")).isEqualTo(ClubRecruitmentStatus.CLOSED.toString());
        assertThat(closedTrigger.getStartTime()).isEqualTo(endDate.toInstant()); // 1월 20일
    }

    @Test
    @DisplayName("오늘이 모집 시작일(OPEN) 이후일 때: CLOSED Job만 예약")
    void testRescheduleRecruitmentUpdates_OnlyClosedScheduled() throws SchedulerException {
        // 오늘 설정: 모집 시작일(1월 10일) 이후이지만 마감일(1월 20일) 이전인 날짜 (예: 1월 15일
        ZonedDateTime midRecruitmentToday = ZonedDateTime.of(2025, 1, 15, 12, 0, 0, 0, ZoneId.of("Asia/Seoul"));

        // 실행
        recruitStatusSchedulerManager.rescheduleRecruitmentUpdates(CLUB_ID, midRecruitmentToday, startDate, endDate, UPCOMING_BEFORE);

        // 새로운 Job 스케줄링 검증 (scheduleNewJob 호출 1회)
        // UPCOMING (1월 3일) < 1월 15일 -> 예약 안 함
        // OPEN (1월 10일) < 1월 15일 -> 예약 안 함
        // CLOSED (1월 20일) > 1월 15일 -> 예약 함
        verify(scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // 캡처를 사용하여 예약된 Job이 CLOSED인지 확인
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);

        verify(scheduler).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());

        JobDetail jobDetail = jobDetailCaptor.getValue();
        Trigger trigger = triggerCaptor.getValue();

        assertThat(jobDetail.getKey().getName()).contains(ClubRecruitmentStatus.CLOSED.toString());
        assertThat(jobDetail.getJobDataMap().getString("status")).isEqualTo(ClubRecruitmentStatus.CLOSED.toString());
        assertThat(trigger.getStartTime()).isEqualTo(endDate.toInstant()); // 1월 20일
    }

    @Test
    @DisplayName("오늘이 모집 마감일 이후일 때: 예약되는 Job 없음")
    void testRescheduleRecruitmentUpdates_NoJobsScheduled() throws SchedulerException {
        // 오늘 설정: 모집 마감일(1월 20일) 이후인 날짜 (예: 1월 25일)
        ZonedDateTime afterRecruitmentToday = ZonedDateTime.of(2025, 1, 25, 12, 0, 0, 0, ZoneId.of("Asia/Seoul"));

        // 실행
        recruitStatusSchedulerManager.rescheduleRecruitmentUpdates(CLUB_ID, afterRecruitmentToday, startDate, endDate, UPCOMING_BEFORE);

        // 새로운 Job 스케줄링 검증 (scheduleNewJob 호출 0회)
        verify(scheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("새 Job 스케줄링 시 SchedulerException이 발생하면 다른 Job은 시도됨")
    void testScheduleNewJob_ThrowsException() throws SchedulerException {
        // Mock 반환값으로 사용할 Date 객체 (실제 값은 중요하지 않음)
        Date mockScheduledDate = new Date();

        // 세 번의 scheduleJob 중 첫 번째 호출에서만 예외를 발생시키도록 Mock 설정
        doThrow(new SchedulerException("Test Schedule Error"))
                // 두 번째 호출은 정상, Mock Date 반환
                .doReturn(mockScheduledDate)
                // 세 번째 호출은 정상, Mock Date 반환
                .doReturn(mockScheduledDate)
                .when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // 실행
        recruitStatusSchedulerManager.rescheduleRecruitmentUpdates(CLUB_ID, today, startDate, endDate, UPCOMING_BEFORE);

        // 새로운 Job 스케줄링 검증 (3번 시도됨)
        verify(scheduler, times(3)).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // 캡처를 사용하여 JobData와 실행 시간을 확인 (성공한 2, 3번째 Job 검증)
        ArgumentCaptor<JobDetail> jobDetailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler, times(3)).scheduleJob(jobDetailCaptor.capture(), triggerCaptor.capture());

        // 첫 번째(UPCOMING)는 예외 발생했으나 시도는 되었고,
        // 두 번째(OPEN)와 세 번째(CLOSED)는 성공했음을 확인 (캡처 리스트에서 확인)
        assertThat(jobDetailCaptor.getAllValues().get(1).getKey().getName()).contains(ClubRecruitmentStatus.OPEN.toString());
        assertThat(jobDetailCaptor.getAllValues().get(2).getKey().getName()).contains(ClubRecruitmentStatus.CLOSED.toString());
    }


}
