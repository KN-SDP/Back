package com.knusdp.SmartLedger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "AssetLiquid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AssetLiquid extends Asset {

    @Column(nullable = false)
    private Long amount;  // 현재 잔액

    @UpdateTimestamp
    private LocalDateTime updateAt;
}
