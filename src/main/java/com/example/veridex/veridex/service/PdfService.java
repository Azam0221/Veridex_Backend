package com.example.veridex.veridex.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Service
@Slf4j
public class PdfService {

    private static final int MAX_PAGES = 300;

    public String extractText(MultipartFile file){

        log.info("Starting text extraction for file: {}", file.getOriginalFilename());


        try(PDDocument document = PDDocument.load(file.getInputStream())){

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            if (totalPages > MAX_PAGES) {
                log.warn("Document exceeds {} pages. Truncating to protect memory.", MAX_PAGES);
                stripper.setEndPage(MAX_PAGES);
            } else {
                log.info("Extracting all {} pages.", totalPages);
            }

            String text = stripper.getText(document);
            String cleanText = text.replaceAll("\\s+", " ").trim();

            log.info("Successfully extracted {} characters of text.", cleanText.length());

            return cleanText;

        }
        catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to extract text from PDF", e);
        }

    }
}
