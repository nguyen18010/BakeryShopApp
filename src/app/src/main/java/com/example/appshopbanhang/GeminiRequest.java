package com.example.appshopbanhang;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {

    public List<Content> contents;

    public GeminiRequest(String userMessage) {
        this.contents = new ArrayList<>();

        // Tạo content với parts
        Content content = new Content();
        content.parts = new ArrayList<>();

        Part part = new Part();
        part.text = userMessage;

        content.parts.add(part);
        this.contents.add(content);
    }

    public static class Content {
        public List<Part> parts;
    }

    public static class Part {
        public String text;
    }
}
