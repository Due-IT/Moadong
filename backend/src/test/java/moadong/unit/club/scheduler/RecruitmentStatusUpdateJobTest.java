package moadong.unit.club.scheduler;

import moadong.club.enums.ClubRecruitmentStatus;
import moadong.club.scheduler.RecruitmentStatusUpdateJob;
import moadong.club.service.ClubProfileService;
import moadong.util.annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("RecruitmentStatusUpdateJob Job 수행 테스트")
class RecruitmentStatusUpdateJobTest {

    @InjectMocks
    private RecruitmentStatusUpdateJob recruitmentStatusUpdateJob;

    @Mock
    private ClubProfileService clubProfileService;

    // Quartz 실행에 필요한 객체
    @Mock
    private JobExecutionContext jobExecutionContext;
    @Mock
    private JobDetail jobDetail;

    // 테스트용 고정 값
    private static final String CLUB_ID = "test-club-001";
    private static final String STATUS_STRING = ClubRecruitmentStatus.OPEN.toString();
    private static final ClubRecruitmentStatus EXPECTED_STATUS = ClubRecruitmentStatus.OPEN;

    @BeforeEach
    void setUp() {
        // JobDataMap 설정 (JobExecutionContext가 데이터를 읽을 수 있도록 Mock 설정)
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("clubId", CLUB_ID);
        jobDataMap.put("status", STATUS_STRING);

        // Mock 객체들이 서로 연결되도록 설정
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobDetail.getJobDataMap()).thenReturn(jobDataMap);
    }

    @Test
    @DisplayName("정상 실행 검증 : Job 실행 시 ClubProfileService의 모집 현황 갱신이 정상적으로 호출되어야 함")
    void execute_Success() throws JobExecutionException {
        // Given: updateRecruitmentStatus 호출 시 아무 일도 하지 않음 (성공 가정)
        doNothing().when(clubProfileService).updateRecruitmentStatus(CLUB_ID, EXPECTED_STATUS);

        // When
        recruitmentStatusUpdateJob.execute(jobExecutionContext);

        // Then
        verify(clubProfileService, times(1)).updateRecruitmentStatus(CLUB_ID, EXPECTED_STATUS);
    }

    @Test
    @DisplayName("ClubProfileService에서 예외 발생 시 JobExecutionException으로 래핑하여 던져야 함")
    void execute_Failure() throws Exception {
        // Given
        // service 호출 시 RuntimeException 발생하도록 Mock 설정
        RuntimeException testException = new RuntimeException("DB update failed");
        doThrow(testException).when(clubProfileService).updateRecruitmentStatus(CLUB_ID, EXPECTED_STATUS);

        // When & Then
        // execute 메서드 호출 시 JobExecutionException이 던져지는지 검증
        JobExecutionException thrown = assertThrows(JobExecutionException.class, () -> {
            recruitmentStatusUpdateJob.execute(jobExecutionContext);
        });

        // service 메서드는 호출되었는지 검증
        verify(clubProfileService, times(1)).updateRecruitmentStatus(CLUB_ID, EXPECTED_STATUS);

        // 던져진 예외의 원인(Cause)이 Mock 설정한 예외인지 검증
        assertNotNull(thrown.getCause(), "JobExecutionException은 원인 예외를 포함해야 합니다.");
        assertEquals(testException, thrown.getCause(), "던져진 예외의 원인은 service에서 발생한 예외여야 합니다.");
    }
}