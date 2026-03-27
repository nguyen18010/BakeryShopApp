package com.example.appshopbanhang;

import java.util.List;

public class OpenAIRequest {

    public String model = "gpt-4o-mini";
    public List<Message> messages;

    public OpenAIRequest(List<Message> messages) {
        this.messages = messages;
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
