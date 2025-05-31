package com.example.AutomateAccountsAssessment.interfaceService;

import com.example.AutomateAccountsAssessment.entites.Receipt;
import com.example.AutomateAccountsAssessment.entites.ReceiptFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IReceiptService {

    /**
     * Uploads a receipt file and stores its metadata.
     *
     * @param file Multipart file uploaded by the client.
     * @return Saved ReceiptFile entity.
     * @throws Exception if the file is empty, invalid, or if saving fails.
     */
    ReceiptFile uploadReceipt(MultipartFile file) throws Exception;

    /**
     * Validates an uploaded file by checking if it is a valid PDF.
     * This method reads the file header and updates the 'is_valid' and
     * 'invalid_reason' fields in the ReceiptFile entity.
     *
     * @param id The ID of the ReceiptFile record to validate.
     * @return Updated ReceiptFile entity.
     * @throws Exception if validation fails or if the receipt file cannot be found.
     */
    ReceiptFile validateReceipt(Long id) throws Exception;

    /**
     * Processes the receipt file by extracting receipt details using OCR/AI.
     * Stores the extracted information in the receipt table and marks the file as processed.
     *
     * @param id The ID of the ReceiptFile record to process.
     * @return The saved Receipt entity with the extracted details.
     * @throws Exception if processing fails or if the file cannot be processed.
     */
    Receipt processReceipt(Long id) throws Exception;

    /**
     * Retrieves all stored receipts.
     *
     * @return A list of all Receipt entities.
     * @throws Exception in case of data access issues.
     */
    List<Receipt> getAllReceipts() throws Exception;

    /**
     * Retrieves a specific receipt by its ID.
     *
     * @param id The ID of the Receipt entity.
     * @return The Receipt entity.
     * @throws Exception if the receipt is not found.
     */
    Receipt getReceiptById(Long id) throws Exception;



}
