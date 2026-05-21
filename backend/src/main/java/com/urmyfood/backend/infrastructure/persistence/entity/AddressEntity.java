package com.urmyfood.backend.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AddressEntity extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detail;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}
