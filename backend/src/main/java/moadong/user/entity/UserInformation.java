package moadong.user.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moadong.global.annotation.Korean;
import moadong.global.annotation.PhoneNumber;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInformation {

    private String id;

    @NotNull
    @Korean
    private String name;
    @PhoneNumber
    private String phoneNumber;

    public UserInformation(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
}
