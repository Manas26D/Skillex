package com.example.mad_pop.model;

public class Course {
    private final long id;
    private final String title;
    private final String category;
    private final String description;
    private final long mentorId;
    private final String mentorName;
    private final String startDate;
    private final String endDate;
    private final String imageUrl;
    private final double price;

    public Course(long id, String title, String category, String description, long mentorId, String mentorName, String startDate, String endDate, String imageUrl, double price) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.mentorId = mentorId;
        this.mentorName = mentorName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public long getMentorId() {
        return mentorId;
    }

    public String getMentorName() {
        return mentorName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }
}

