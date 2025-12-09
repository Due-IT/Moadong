package moadong.club.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moadong.club.enums.ClubRecruitmentStatus;
import moadong.club.service.ClubProfileService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruitmentStatusUpdateJob implements Job {

    private final ClubProfileService clubProfileService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //JobDataMap에서 필요한 정보 추출
        String clubId = context.getJobDetail().getJobDataMap().getString("clubId");
        String status = context.getJobDetail().getJobDataMap().getString("status");

        log.info("📢 Quartz Job 실행! 동아리 ID: {}, 갱신 유형: {}", clubId, status);

        ClubRecruitmentStatus recruitmentStatus = ClubRecruitmentStatus.fromString(status);
        try {
            clubProfileService.updateRecruitmentStatus(clubId, recruitmentStatus);
            log.info("✅ {} 갱신 완료: ID {}", recruitmentStatus.getDescription(), clubId);
        } catch (Exception e) {
            log.error("❌ 모집 현황 갱신 중 오류 발생 (ID: {})", clubId, e);
            // 오류 발생 시 Quartz가 재시도할 수 있도록 JobExecutionException을 던질 수 있음
            throw new JobExecutionException(e);
        }
    }
}
