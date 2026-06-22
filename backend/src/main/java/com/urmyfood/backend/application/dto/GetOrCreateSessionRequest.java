package com.urmyfood.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetOrCreateSessionRequest {

    @NotNull
    private Long shopId;
}
