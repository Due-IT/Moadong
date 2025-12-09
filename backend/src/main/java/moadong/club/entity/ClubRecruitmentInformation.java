package moadong.club.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import moadong.club.enums.ClubRecruitmentStatus;
import moadong.club.enums.UpcomingBefore;
import moadong.club.payload.request.ClubInfoRequest;
import moadong.club.payload.request.ClubRecruitmentInfoUpdateRequest;
import moadong.global.RegexConstants;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ClubRecruitmentInformation {

    @Id
    private String id;

    @Unique
    private String logo;

    @Unique
    private String cover;

    private String introduction;

    private String description;

    private String presidentName;

    @Pattern(regexp = RegexConstants.PHONE_NUMBER, message = "전화번호 형식이 올바르지 않습니다.")
    private String presidentTelephoneNumber;

    private LocalDateTime recruitmentStart;

    private LocalDateTime recruitmentEnd;

    private String recruitmentTarget;

    String externalApplicationUrl;

    private List<String> feedImages;

    private List<String> tags;

    private List<Faq> faqs;

    @NotNull
    private ClubRecruitmentStatus clubRecruitmentStatus;

    public void updateLogo(String logo) {
        this.logo = logo;
    }

    public void updateDescription(ClubRecruitmentInfoUpdateRequest request) {
        this.description = request.description();
        this.recruitmentStart = request.recruitmentStart();
        this.recruitmentEnd = request.recruitmentEnd();
        this.recruitmentTarget = request.recruitmentTarget();
        this.externalApplicationUrl = request.externalApplicationUrl();
        this.faqs = request.faqs();
        
        updateRecruitmentStatus();
    }

    public void updateRecruitmentStatus(ClubRecruitmentStatus status) {
        this.clubRecruitmentStatus = status;
    }

    protected void updateRecruitmentStatus() {
        LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        LocalDateTime upcomingDate = recruitmentStart.minusDays(UpcomingBefore.get());



        if(now.isBefore(upcomingDate)) {
            updateRecruitmentStatus(ClubRecruitmentStatus.CLOSED);
        } else if(now.isEqual(upcomingDate) || (now.isAfter(upcomingDate) && now.isBefore(recruitmentStart))){
            updateRecruitmentStatus(ClubRecruitmentStatus.UPCOMING);
        } else if(now.isEqual(recruitmentStart) || (now.isAfter(recruitmentStart) && now.isBefore(recruitmentEnd))) {
            updateRecruitmentStatus(ClubRecruitmentStatus.OPEN);
        } else if(now.isEqual(recruitmentEnd) || now.isAfter(recruitmentEnd)){
            updateRecruitmentStatus(ClubRecruitmentStatus.CLOSED);
        }
    }

    public boolean hasRecruitmentPeriod() {
        return recruitmentStart != null && recruitmentEnd != null;
    }

    public ZonedDateTime getRecruitmentStart() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        if (recruitmentStart == null) {
            return null;
        }
        return recruitmentStart.atZone(seoulZone);
    }

    public ZonedDateTime getRecruitmentEnd() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        if (recruitmentEnd == null) {
            return null;
        }
        return recruitmentEnd.atZone(seoulZone);
    }

    public int getFeedAmounts() {
        return this.feedImages.size();
    }

    public void updateFeedImages(List<String> feedImages) {
        this.feedImages = feedImages;
    }

    public void update(ClubInfoRequest request) {
        this.introduction = request.introduction();
        this.presidentName = request.presidentName();
        this.presidentTelephoneNumber = request.presidentPhoneNumber();
        this.tags = request.tags();
    }

    public void updateCover(String cover) {
        this.cover = cover;
    }
}
