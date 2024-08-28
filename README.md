# PDF Segmenter

## Overview

The PDF Segmenter is a Java application that processes PDF documents to segment them into distinct sections based on whitespace between blocks of text. The goal is to identify logical sections such as headings, paragraphs, and distinct blocks that are visually separated by increased whitespace. The application uses Apache PDFBox for handling PDF files and JUnit 5 for testing.

## Features

- **Extract Text Blocks**: Identifies and extracts text blocks from a PDF document based on vertical spacing.
- **Segment PDF**: Segments the PDF into distinct sections based on whitespace.
- **Save Segments**: Saves each segment into a separate PDF file.
- **Testing**: Includes unit tests to verify the functionality of text extraction, PDF segmentation, and PDF saving.

## Prerequisites

- Java 19 or later
- Maven 3.8 or later

## Testing
The project includes unit tests using JUnit 5. To run the tests, use the following command:
mvn test

## Acknowledgements
- Apache PDFBox - A library for working with PDF documents in Java.
- JUnit 5 - A testing framework for Java.