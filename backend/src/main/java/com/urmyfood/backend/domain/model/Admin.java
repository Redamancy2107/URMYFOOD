package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {
    private Long id;
    private Long accountId;
    private String position;
    private String shortBio;
    private boolean is2FaEnabled;
}
