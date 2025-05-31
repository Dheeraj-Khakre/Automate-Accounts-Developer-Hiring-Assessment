package com.example.AutomateAccountsAssessment.repository;

import com.example.AutomateAccountsAssessment.entites.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    // Custom query methods can be defined here if needed in the future
}