package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileDto {
    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("work_email")
    private String workEmail;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String position;

    @JsonProperty("short_bio")
    private String shortBio;

    @JsonProperty("is_2fa_enabled")
    private boolean is2FaEnabled;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
