package com.knusdp.SmartLedger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AssetInvestment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AssetInvestment extends Asset {

    @Column(precision = 15, scale = 6, nullable = false)
    private BigDecimal avgPrice;  // 매입평균가

    @Column(precision = 15, scale = 6, nullable = false)
    private BigDecimal quantity;  // 보유수량

    @UpdateTimestamp
    private LocalDateTime updateAt;
}
