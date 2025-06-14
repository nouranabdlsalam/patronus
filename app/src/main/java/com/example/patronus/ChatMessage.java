package com.example.patronus;
public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    private final String message;
    private final int type;
    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }
    public String getMessage() {
        return message;
    }
    public int getType() {
        return type;
    }
}