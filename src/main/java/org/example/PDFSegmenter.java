package org.example;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PDFSegmenter {

    public void readPDF() throws IOException {

        File pdfFile = new File("C:\\Users\\vinay\\Java\\PDFSegmenter\\src\\main\\java\\org\\example\\pdfFiles\\Resume_Vinayak_Sharma (2).pdf");
        //Load the PDF
        try(PDDocument document = Loader.loadPDF(pdfFile)){
            List<Segment> segments = segmentPDF(document, 5); // Assuming we want 3 segments

            for(int i=0; i<segments.size(); i++){
                saveSegmentAsPDF(segments.get(i), document, "segment_" + (i+1) + ".pdf");
            }

        }

    }
    public static List<Segment> segmentPDF(PDDocument document, int cuts) throws IOException {
        List<TextBlock> textBlocks = extractTextBlocks(document);

        textBlocks.sort(Comparator.comparingDouble(TextBlock::getY)); // Sort blocks by Y position

        List<Double> whiteSpaces = new ArrayList<>();
        for (int i = 1; i < textBlocks.size(); i++) {
            double whiteSpace = textBlocks.get(i).getY() - textBlocks.get(i - 1).getY();
            whiteSpaces.add(whiteSpace);
        }

        List<Double> largestWhiteSpaces = findLargestWhiteSpaces(whiteSpaces, cuts);

        List<Segment> segments = new ArrayList<>();
        List<TextBlock> currentSegment = new ArrayList<>();
        double previousY = textBlocks.get(0).getY();

        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock block = textBlocks.get(i);
            double currentY = block.getY();

            if (largestWhiteSpaces.contains(currentY - previousY)) {
                segments.add(new Segment(currentSegment));
                currentSegment = new ArrayList<>();
            }

            currentSegment.add(block);
            previousY = currentY;
        }

        if (!currentSegment.isEmpty()) {
            segments.add(new Segment(currentSegment));
        }

        return segments;
    }
    public static List<TextBlock> extractTextBlocks(PDDocument document) throws IOException {
        List<TextBlock> textBlocks = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            private StringBuilder currentText = new StringBuilder();
            private float currentY = -1;
            private static final float LINE_SPACING_THRESHOLD = 10; // Adjust as needed

            @Override
            protected void processTextPosition(TextPosition text) {
                if (currentY == -1 || Math.abs(text.getYDirAdj() - currentY) < LINE_SPACING_THRESHOLD) {
                    currentText.append(text.getUnicode());
                } else {
                    // Close the current block and start a new one
                    textBlocks.add(new TextBlock(currentText.toString(), text.getXDirAdj(), currentY));
                    currentText.setLength(0);
                    currentText.append(text.getUnicode());
                }
                currentY = text.getYDirAdj();
            }

            @Override
            protected void writePage() throws IOException {
                // Add the last block of text
                if (currentText.length() > 0) {
                    textBlocks.add(new TextBlock(currentText.toString(), -1, currentY));
                }
                super.writePage();
            }
        };
        stripper.setSortByPosition(true);

        for (int i = 0; i < document.getNumberOfPages(); i++) {
            stripper.setStartPage(i + 1);
            stripper.setEndPage(i + 1);
            stripper.getText(document); // Trigger processTextPosition for each text position
        }

        return textBlocks;
    }
    private static List<Double> findLargestWhiteSpaces(List<Double> whiteSpaces, int xCuts) {
        List<Double> sortedWhiteSpaces = new ArrayList<>(whiteSpaces);
        sortedWhiteSpaces.sort(Collections.reverseOrder());
        return sortedWhiteSpaces.subList(0, xCuts);
    }
    public static void saveSegmentAsPDF(Segment segment, PDDocument originalDocument, String outputFileName) throws IOException {
        try (PDDocument newDocument = new PDDocument()) {
            PDPage page = new PDPage(originalDocument.getPage(0).getMediaBox());
            newDocument.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(newDocument, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12); // Set a font and size

                for (TextBlock block : segment.getBlocks()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset((float) block.getX(), (float) block.getY());
                    contentStream.showText(block.getText());
                    contentStream.endText();
                }
            }

            newDocument.save(outputFileName);
        }
    }
    public static class TextBlock{
        private String text;
        private double x;
        private double y;

        public TextBlock(String text, double x, double y){
            this.text = text;
            this.x=x;
            this.y=y;
        }

        public String getText() {
            return text;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        @Override
        public String toString() {
            return "TextBlock{" +
                    "text='" + text + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    public static class Segment{
        private List<TextBlock> blocks;

        public Segment(List<TextBlock> blocks){
            this.blocks = blocks;
        }
        public List<TextBlock> getBlocks(){
            return blocks;
        }
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            for(TextBlock block : blocks){
                str.append(block.text).append("\n");
            }
            return str.toString();
        }
    }
}
