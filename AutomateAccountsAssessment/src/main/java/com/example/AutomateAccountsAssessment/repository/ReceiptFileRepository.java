package com.example.AutomateAccountsAssessment.repository;

import com.example.AutomateAccountsAssessment.entites.ReceiptFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptFileRepository extends JpaRepository<ReceiptFile, Long> {
    // Custom query methods can be defined here if needed in the future
}