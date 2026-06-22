package com.urmyfood.backend.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavedVoucherResponse {
    @JsonProperty("voucher_id")
    private Long voucherId;

    @JsonProperty("is_saved")
    private boolean isSaved;
}
