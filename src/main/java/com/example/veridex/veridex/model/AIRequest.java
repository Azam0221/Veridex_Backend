package com.example.veridex.veridex.model;

import lombok.Data;

import java.util.Collections;
import java.util.List;

public class AIRequest {


    @Data
    public static class GeminiRequest {
        private List<Content> contents;
        public GeminiRequest(String text) {
            this.contents = Collections.singletonList(new Content(new Part(text)));
        }
    }

    @Data
    public static class Content {
        private List<Part> parts;
        public Content(Part part) { this.parts = Collections.singletonList(part); }
    }

    @Data
    public static class Part {
        private String text;
        public Part(String text) { this.text = text; }
    }

    @Data
    public static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    public static class Candidate {
        private Content content;
    }
}
