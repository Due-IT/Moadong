package moadong.club.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import moadong.club.dto.ClubUpdateRequest;
import moadong.club.enums.ClubState;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("club")
@AllArgsConstructor
@Getter
@Builder
public class Club {

    @Id
    private String id;

    @NotNull
    @Column(length = 20)
    private String name;

    @NotNull
    @Column(length = 20)
    private String classification;

    @NotNull
    @Column(length = 20)
    private String division;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ClubState state;

    public void update(ClubUpdateRequest request) {
        this.name = request.name();
        this.classification = request.classification();
        this.division = request.division();
        this.state = ClubState.AVAILABLE;
    }
}
