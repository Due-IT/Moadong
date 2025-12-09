package moadong.club.payload.request;

import moadong.club.entity.Faq;
import moadong.global.annotation.RecruitmentPeriod;

import java.time.LocalDateTime;
import java.util.List;

@RecruitmentPeriod
public record ClubRecruitmentInfoUpdateRequest(
    LocalDateTime recruitmentStart,
    LocalDateTime recruitmentEnd,
    String recruitmentTarget,
    String description,
    String externalApplicationUrl,
    List<Faq> faqs
) {

}
