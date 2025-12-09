package moadong.club.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moadong.club.enums.ClubRecruitmentStatus;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitStatusSchedulerManager {

    // Spring이 초기화한 Quartz 스케줄러 인스턴스
    private final Scheduler scheduler;

    public void rescheduleRecruitmentUpdates(String clubId, ZonedDateTime today, ZonedDateTime startDate, ZonedDateTime endDate, int upcomingBefore) {
        // 기존 Job/Trigger 제거 (수정 시 중복 방지)
        removeExistingJobs(clubId);

        ZonedDateTime upcomingDate = startDate.minusDays(upcomingBefore);
        if(today.isBefore(upcomingDate)) {
            scheduleNewJob(clubId, ClubRecruitmentStatus.UPCOMING, upcomingDate);
            log.info("✨ 동아리 ID {}에 대한 UPCOMING Job이 예약되었습니다.", clubId);
        }
        if(today.isBefore(startDate)) {
            scheduleNewJob(clubId, ClubRecruitmentStatus.OPEN, startDate);
            log.info("✨ 동아리 ID {}에 대한 OPEN Job이 예약되었습니다.", clubId);
        }
        if(today.isBefore(endDate)) {
            scheduleNewJob(clubId, ClubRecruitmentStatus.CLOSED, endDate);
            log.info("✨ 동아리 ID {}에 대한 CLOSED Job이 예약되었습니다.", clubId);
        }


    }

    private void removeExistingJobs(String clubId) {
        try {
            scheduler.unscheduleJob(new TriggerKey("Trigger_UPCOMING" + clubId, "RECRUIT_GROUP"));
            scheduler.unscheduleJob(new TriggerKey("Trigger_START_" + clubId, "RECRUIT_GROUP"));
            scheduler.unscheduleJob(new TriggerKey("Trigger_CLOSED_" + clubId, "RECRUIT_GROUP"));

            scheduler.deleteJob(new JobKey("Job_UPCOMING_" + clubId, "RECRUIT_GROUP"));
            scheduler.deleteJob(new JobKey("Job_START_" + clubId, "RECRUIT_GROUP"));
            scheduler.deleteJob(new JobKey("Job_CLOSED_" + clubId, "RECRUIT_GROUP"));

            log.info("🗑️ 기존 동아리 ID {}의 Quartz Job/Trigger를 제거했습니다.", clubId);
        } catch (SchedulerException e) {
            log.error("❌ 기존 Quartz Job 제거 실패 (ID: {})", clubId, e);
        }
    }

    private void scheduleNewJob(String clubId, ClubRecruitmentStatus status, ZonedDateTime targetDate) {
        String jobName = "Job_" + status + "_" + clubId;
        String triggerName = "Trigger_" + status + "_" + clubId;
        String groupName = "RECRUIT_GROUP";

        try {
            // 1. JobDetail 생성
            JobDetail jobDetail = JobBuilder.newJob(RecruitmentStatusUpdateJob.class)
                    .withIdentity(jobName, groupName)
                    .usingJobData("clubId", clubId)
                    .usingJobData("status", status.toString())
                    .storeDurably() // Job이 Trigger 없이도 DB에 저장되도록 설정 (관리 용이)
                    .build();

            // 2. Simple Trigger 생성 (정확히 targetDate 시점에 한 번 실행)
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerName, groupName)
                    .startAt(Date.from(targetDate.toInstant())) // **미래의 특정 시점**
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .build();

            // 3. 스케줄러에 등록 (DB에 영속화됨)
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            log.error("❌ Quartz Job 스케줄링 실패 (ID: {}, Type: {})", clubId, status, e);
        }
    }
}
