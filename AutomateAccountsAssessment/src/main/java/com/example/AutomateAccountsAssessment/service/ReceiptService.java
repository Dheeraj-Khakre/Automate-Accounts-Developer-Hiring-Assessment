package com.example.AutomateAccountsAssessment.service;

import com.example.AutomateAccountsAssessment.entites.Receipt;
import com.example.AutomateAccountsAssessment.entites.ReceiptFile;
import com.example.AutomateAccountsAssessment.interfaceService.IReceiptService;
import com.example.AutomateAccountsAssessment.repository.ReceiptFileRepository;
import com.example.AutomateAccountsAssessment.repository.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;

import java.util.List;

@Service
public class ReceiptService implements IReceiptService {

    private  final ReceiptFileRepository receiptFileRepository;
    private  final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptFileRepository receiptFileRepository, ReceiptRepository receiptRepository) {
        this.receiptFileRepository = receiptFileRepository;
        this.receiptRepository = receiptRepository;
    }

    @Override
    public ReceiptFile uploadReceipt(MultipartFile file) throws IOException {
        // Validate that the file is not empty.
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        // Validate file type (only accept PDFs).
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only PDF files are allowed.");
        }

        // Define and create the upload directory if it does not exist.
       String uploadDir = "uploads/receipts";
      //  String uploadDir = "C:\\Users\\khakr\\Downloads\\receipts";
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists() && !uploadDirectory.mkdirs()) {
            throw new IOException("Failed to create upload directory.");
        }

        // Clean up the original file name to prevent path traversal issues.
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String filePath = uploadDir + File.separator + originalFilename;

        // Save the file to the local file system.
        File destinationFile = new File(filePath);
        file.transferTo(destinationFile);

        // Create a new ReceiptFile instance and populate its fields.
        ReceiptFile receiptFile = new ReceiptFile();
        receiptFile.setFileName(originalFilename);
        receiptFile.setFilePath(filePath);
        receiptFile.setIsValid(true);     // Basic validation passed for PDF.
        receiptFile.setIsProcessed(false);  // File not processed yet.
        receiptFile.setInvalidReason(null);

        // Save the ReceiptFile entity to the database.
        return receiptFileRepository.save(receiptFile);
    }

    @Override
    public ReceiptFile validateReceipt(Long id) throws Exception {
        // Retrieve the ReceiptFile record from the database.
        ReceiptFile receiptFile = receiptFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receipt file not found for id: " + id));

        // Validate that the file exists on disk.
        File file = new File(receiptFile.getFilePath());
        if (!file.exists() || !file.isFile()) {
            receiptFile.setIsValid(false);
            receiptFile.setInvalidReason("File does not exist on disk.");
            return receiptFileRepository.save(receiptFile);
        }
        // Perform a basic header check to ensure it's a valid PDF.
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[5];
            int bytesRead = fis.read(header);
            String headerStr = new String(header, "UTF-8");
            if (bytesRead < 5 || !headerStr.equals("%PDF-")) {
                receiptFile.setIsValid(false);
                receiptFile.setInvalidReason("Invalid PDF file header.");
            } else {
                receiptFile.setIsValid(true);
                receiptFile.setInvalidReason(null);
            }
        } catch (IOException e) {
            receiptFile.setIsValid(false);
            receiptFile.setInvalidReason("Error reading file: " + e.getMessage());
        }
        // Persist and return the updated ReceiptFile entity.
        return receiptFileRepository.save(receiptFile);
    }

// Other imports as needed...

    @Override
    public Receipt processReceipt(Long id) throws Exception {
        // Retrieve the ReceiptFile from the database.
        ReceiptFile receiptFile = receiptFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receipt file not found for id: " + id));

        // Ensure the file is valid before processing.
        if (receiptFile.getIsValid() == null || !receiptFile.getIsValid()) {
            throw new IllegalArgumentException("The file is invalid and cannot be processed.");
        }

        // Optional: prevent re-processing.
        if (Boolean.TRUE.equals(receiptFile.getIsProcessed())) {
            throw new IllegalArgumentException("This file has already been processed.");
        }

        // Verify the file exists.
        File file = new File(receiptFile.getFilePath());
        if (!file.exists()) {
            throw new Exception("The file does not exist on disk.");
        }

        // Extract text from the PDF using Apache PDFBox.
        String extractedText = extractTextFromPdf(file);

        // Process the extracted text and extract details.
        Receipt receipt = extractReceiptDetails(extractedText);
        receipt.setFilePath(receiptFile.getFilePath());

        // Save the populated Receipt entity to the database.
        Receipt savedReceipt = receiptRepository.save(receipt);

        // Mark the receipt file as processed.
        receiptFile.setIsProcessed(true);
        receiptFileRepository.save(receiptFile);

        return savedReceipt;
    }

    // Utility method to extract text from the PDF.
    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // Utility method to parse extracted text and identify key receipt details.
    private Receipt extractReceiptDetails(String text) {
        Receipt receipt = new Receipt();

        // --- Extract Merchant Name ---
        // Expecting a line like "Merchant: Some Merchant Name"
        Pattern merchantPattern = Pattern.compile("Merchant:\\s*(.+)", Pattern.MULTILINE);
        Matcher merchantMatcher = merchantPattern.matcher(text);
        if (merchantMatcher.find()) {
            receipt.setMerchantName(merchantMatcher.group(1).trim());
        } else {
            receipt.setMerchantName("Unknown Merchant");
        }

        // --- Extract Purchase Date ---
        // For example, expecting "Date: YYYY-MM-DD"
        Pattern datePattern = Pattern.compile("Date:\\s*(\\d{4}-\\d{2}-\\d{2})", Pattern.MULTILINE);
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            // A simple conversion: append "T00:00:00" to convert to LocalDateTime
            String dateString = dateMatcher.group(1).trim();
            LocalDateTime purchasedAt = LocalDateTime.parse(dateString + "T00:00:00");
            receipt.setPurchasedAt(purchasedAt);
        } else {
            // Fallback if no date found: use current date/time
            receipt.setPurchasedAt(LocalDateTime.now());
        }

        // --- Extract Total Amount ---
        // Expecting a line like "Total: 123.45"
        Pattern amountPattern = Pattern.compile("Total:\\s*([0-9]+\\.?[0-9]*)", Pattern.MULTILINE);
        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            receipt.setTotalAmount(new BigDecimal(amountMatcher.group(1).trim()));
        } else {
            receipt.setTotalAmount(BigDecimal.ZERO);
        }

        return receipt;
    }

//    @Override
//    public Receipt processReceipt(Long id) throws Exception {
//        // Retrieve the ReceiptFile from the database.
//        ReceiptFile receiptFile = receiptFileRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Receipt file not found for id: " + id));
//
//        // Ensure the file is valid before processing.
//        if (receiptFile.getIsValid() == null || !receiptFile.getIsValid()) {
//            throw new IllegalArgumentException("The file is invalid and cannot be processed.");
//        }
//        // Optional: prevent re-processing.
//        if (Boolean.TRUE.equals(receiptFile.getIsProcessed())) {
//            throw new IllegalArgumentException("This file has already been processed.");
//        }
//
//        // Verify the file exists.
//        File file = new File(receiptFile.getFilePath());
//        if (!file.exists()) {
//            throw new Exception("The file does not exist on disk.");
//        }
//
//        // Simulate OCR/AI extraction. In an actual implementation,
//        // integrate an OCR/AI library (e.g., Tesseract, Google Cloud Vision) to extract these values.
//        String extractedMerchant = "Dummy Merchant";
//        LocalDateTime extractedPurchasedAt = LocalDateTime.now(); // Simulated extraction
//        BigDecimal extractedTotalAmount = new BigDecimal("123.45");
//
//        // Create and populate the Receipt entity with the extracted information.
//        Receipt receipt = new Receipt();
//        receipt.setMerchantName(extractedMerchant);
//        receipt.setPurchasedAt(extractedPurchasedAt);
//        receipt.setTotalAmount(extractedTotalAmount);
//        receipt.setFilePath(receiptFile.getFilePath());
//
//        // Save the extracted receipt details in the receipt table.
//        Receipt savedReceipt = receiptRepository.save(receipt);
//
//        // Mark the receipt file as processed.
//        receiptFile.setIsProcessed(true);
//        receiptFileRepository.save(receiptFile);
//
//        return savedReceipt;
//    }

    @Override
    public List<Receipt> getAllReceipts() throws Exception {
        return receiptRepository.findAll();
    }

    @Override
    public Receipt getReceiptById(Long id) throws Exception {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found for id: " + id));
    }


}
