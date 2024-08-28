package org.example.test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.PDFSegmenter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDFSegmenterTest {

    private PDDocument document;
    private PDFSegmenter pdfSegmenter;

    @BeforeEach
    void setUp() throws IOException {
        document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("This is a test PDF.");
            contentStream.newLine();
            contentStream.showText("This is a second line.");
            contentStream.newLineAtOffset(0, -100);
            contentStream.showText("This is a section separator.");
            contentStream.endText();
        }


    }

    @AfterEach
    void tearDown() throws IOException {
        document.close();
    }

    @Test
    void testSegmentPDF() throws IOException {
        List<PDFSegmenter.Segment> segments = PDFSegmenter.segmentPDF(document, 1);
        assertNotNull(segments, "Segments list should not be null");
        assertEquals(2, segments.size(), "Should create 2 segments");


    }

    @Test
    void testExtractTextBlocks() throws IOException {
        List<PDFSegmenter.TextBlock> textBlocks = PDFSegmenter.extractTextBlocks(document);
        assertNotNull(textBlocks, "Text blocks list should not be null");
        assertEquals(3, textBlocks.size(), "Should extract 3 text blocks");

        // Verify positions and content
        assertEquals("This is a test PDF.", textBlocks.get(0).getText(), "First text block should match");
        assertEquals("This is a second line.", textBlocks.get(1).getText(), "Second text block should match");
        assertEquals("This is a section separator.", textBlocks.get(2).getText(), "Third text block should match");
    }

    @Test
    void testSaveSegmentAsPDF() throws IOException {
        List<PDFSegmenter.Segment> segments = PDFSegmenter.segmentPDF(document, 1);
        assertNotNull(segments, "Segments list should not be null");
        assertFalse(segments.isEmpty(), "Segments list should not be empty");

        String outputFileName = "test_segment_1.pdf";
        File outputFile = new File(outputFileName);

        PDFSegmenter.saveSegmentAsPDF(segments.get(0), document, outputFileName);
        assertTrue(outputFile.exists(), "Segment PDF should be created");

        // Clean up after test
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }
}
