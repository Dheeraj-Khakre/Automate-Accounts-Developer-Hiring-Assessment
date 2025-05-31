# Automate-Accounts-Developer-Hiring-Assessment
This assessment is designed to evaluate your ability to build a system for processing scanned receipts automatically. The goal is to extract relevant details from PDF receipts using OCR/AI techniques and store the extracted data in a structured format.


# Automate Accounts Developer Hiring Assessment

This project is a Spring Boot web application that demonstrates a system for processing scanned receipt PDFs using OCR/AI techniques. The system provides REST APIs to:

- **Upload** a receipt file
- **Validate** that the file is a valid PDF
- **Process** the receipt file using OCR/AI extraction (dummy extraction in this demo)
- **List** all processed receipts and
- **Retrieve** a specific receipt's details

All extracted data is stored in a lightweight SQLite database.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
  - [1. Upload Endpoint (`/api/upload`)](#1-upload-endpoint-apiupload)
  - [2. Validate Endpoint (`/api/validate`)](#2-validate-endpoint-apivalidate)
  - [3. Process Endpoint (`/api/process`)](#3-process-endpoint-apiprocess)
  - [4. List All Receipts (`/api/receipts`)](#4-list-all-receipts-apireceipts)
  - [5. Get Receipt By ID (`/api/receipts/{id}`)](#5-get-receipt-by-id-apireceiptsid)
- [Dependencies](#dependencies)
- [Execution Instructions](#execution-instructions)
- [Future Enhancements](#future-enhancements)
- [License](#license)

## Features

- **File Upload:** Accepts PDF files via `/api/upload`.
- **File Validation:** Verifies PDF header correctness via `/api/validate`.
- **OCR/AI Processing:** Simulates receipt detail extraction via `/api/process`.
- **Receipt Management:** Retrieves processed receipt details via `/api/receipts` and `/api/receipts/{id}`.

## Prerequisites

- **Java:** JDK 11 (or higher) is required.
- **Maven:** Ensure Maven is installed to manage dependencies and build the project.
- **SQLite:** SQLite will be used as the data storage (handled via the JDBC driver).

## Setup & Installation

1. **Clone the repository:**

   ```bash

