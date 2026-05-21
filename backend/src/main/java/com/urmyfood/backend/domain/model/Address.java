package com.urmyfood.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Address extends BaseDomainModel {
    private Long accountId;
    private String label;
    private String name;
    private String phone;
    private String detail;
    private boolean isDefault;
}
