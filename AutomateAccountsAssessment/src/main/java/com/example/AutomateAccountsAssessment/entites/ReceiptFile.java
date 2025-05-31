package com.example.AutomateAccountsAssessment.entites;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
@Entity
@Table(name = "receipt_file")
public class ReceiptFile extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "is_valid")
    private Boolean isValid;

    @Column(name = "invalid_reason")
    private String invalidReason;

    @Column(name = "is_processed")
    private Boolean isProcessed;

    // Constructors, Getters & Setters

    public ReceiptFile() {
        // Default constructor
    }

}