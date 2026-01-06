package com.example.veridex.veridex.service;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfService {

    public String extractText(MultipartFile file){

        try(PDDocument document = PDDocument.load(file.getInputStream())){

            PDFTextStripper stripper = new PDFTextStripper();

            if(document.getNumberOfPages() >50){
                stripper.setEndPage(50);
            }

            String text = stripper.getText(document);

            return text.replaceAll("\\s+", " ").trim();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }

    }
}
