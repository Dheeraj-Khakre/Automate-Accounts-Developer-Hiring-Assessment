package com.example.AutomateAccountsAssessment.entites;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Getter
@Setter
@Entity
@Table(name = "receipt")
public class Receipt extends BaseEntity {

    // Date and time when the purchase occurred (extracted from the receipt)
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    // Local file path linking the receipt to its scanned file
    @Column(name = "file_path", nullable = false)
    private String filePath;

}