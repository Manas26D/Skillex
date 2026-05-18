package com.example.mad_pop.model;

public class MentorAnalytics {
    private final int totalCourses;
    private final int totalEnrollments;
    private final String topCourseTitle;
    private final int topCourseEnrollments;
    private final String enrolledStudentsSummary;

    public MentorAnalytics(int totalCourses, int totalEnrollments, String topCourseTitle, int topCourseEnrollments, String enrolledStudentsSummary) {
        this.totalCourses = totalCourses;
        this.totalEnrollments = totalEnrollments;
        this.topCourseTitle = topCourseTitle;
        this.topCourseEnrollments = topCourseEnrollments;
        this.enrolledStudentsSummary = enrolledStudentsSummary;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public int getTotalEnrollments() {
        return totalEnrollments;
    }

    public String getTopCourseTitle() {
        return topCourseTitle;
    }

    public int getTopCourseEnrollments() {
        return topCourseEnrollments;
    }

    public String getEnrolledStudentsSummary() {
        return enrolledStudentsSummary;
    }
}


