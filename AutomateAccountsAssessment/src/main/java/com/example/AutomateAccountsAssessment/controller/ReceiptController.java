package com.example.AutomateAccountsAssessment.controller;
import com.example.AutomateAccountsAssessment.entites.Receipt;
import com.example.AutomateAccountsAssessment.entites.ReceiptFile;
import com.example.AutomateAccountsAssessment.service.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReceiptController {

    private final ReceiptService receiptService;

    @Autowired
    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    /**
     * Endpoint to handle receipt file uploads.
     *
     * Validates the file ensuring it is non-empty and of PDF type, saves
     * it via the service layer, and records its metadata in the database.
     *
     * @param file Multipart file uploaded by the client.
     * @return A ResponseEntity containing the result of the operation.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadReceipt(@RequestParam("file") MultipartFile file) {
        try {
            ReceiptFile receiptFile = receiptService.uploadReceipt(file);
            return ResponseEntity.ok("File uploaded successfully with ID: " + receiptFile.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving file: " + e.getMessage());
        }
    }

    /**
     * Endpoint to validate whether an uploaded file is a valid PDF.
     * It performs a header check and updates the is_valid and invalid_reason fields
     * in the receipt_file record.
     *
     * @param id ID of the ReceiptFile record to validate.
     * @return A ResponseEntity indicating the result of the validation.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateReceipt(@RequestParam("id") Long id) {
        try {
            ReceiptFile validatedFile = receiptService.validateReceipt(id);
            String message = "File validation complete. isValid: " + validatedFile.getIsValid() +
                    ", Invalid Reason: " + validatedFile.getInvalidReason();
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validating file: " + e.getMessage());
        }
    }

    /**
     * Endpoint to process a receipt file.
     *
     * It extracts receipt details using OCR/AI, stores the extracted details in the receipt table,
     * and marks the associated receipt file as processed.
     *
     * @param id ID of the ReceiptFile record to process.
     * @return A ResponseEntity with the details of the processed receipt.
     */
    @PostMapping("/process")
    public ResponseEntity<?> processReceipt(@RequestParam("id") Long id) {
        try {
            Receipt receipt = receiptService.processReceipt(id);
            return ResponseEntity.ok("File processed successfully. Receipt ID: " + receipt.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing file: " + e.getMessage());
        }
    }

    /**
     * Endpoint to list all receipts stored in the database.
     *
     * @return A list of all Receipt entities.
     */
    @GetMapping("/receipts")
    public ResponseEntity<?> getAllReceipts() {
        try {
            List<Receipt> receipts = receiptService.getAllReceipts();
            return ResponseEntity.ok(receipts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving receipts: " + e.getMessage());
        }
    }

    /**
     * Endpoint to retrieve details of a specific receipt by its ID.
     *
     * @param id The ID of the Receipt entity.
     * @return The Receipt entity details.
     */
    @GetMapping("/receipts/{id}")
    public ResponseEntity<?> getReceiptById(@PathVariable("id") Long id) {
        try {
            Receipt receipt = receiptService.getReceiptById(id);
            return ResponseEntity.ok(receipt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving receipt: " + e.getMessage());
        }
    }

}