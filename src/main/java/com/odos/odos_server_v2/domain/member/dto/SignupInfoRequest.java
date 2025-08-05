package com.odos.odos_server_v2.domain.member.dto;

import com.odos.odos_server_v2.domain.member.entity.Enum.Gender;
import com.odos.odos_server_v2.domain.member.entity.Enum.Job;
import com.odos.odos_server_v2.domain.shared.Enum.Category;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class SignupInfoRequest {

    @NotNull
    private String nickname;

    private String profileUrl;

    @NotNull private Job job;

    @NotNull private LocalDate birth;

    @NotNull private Gender gender;

    @NotNull private Boolean isPublic;

    @NotNull private List<Category> category; //ChallengeCategory
}
