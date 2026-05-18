package com.example.mad_pop.model;

public class Message {
    private final long id;
    private final long senderId;
    private final long receiverId;
    private final String message;
    private final String senderName;
    private final long createdAt;

    public Message(long id, long senderId, long receiverId, String message, String senderName, long createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.senderName = senderName;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

