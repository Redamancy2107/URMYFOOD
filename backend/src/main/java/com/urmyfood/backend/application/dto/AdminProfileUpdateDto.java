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
public class AdminProfileUpdateDto {

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("work_email")
    private String workEmail;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String position;

    @JsonProperty("short_bio")
    private String shortBio;
}
