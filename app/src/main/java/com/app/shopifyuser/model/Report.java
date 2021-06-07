package com.app.shopifyuser.model;

public class Report {

    private String reportId;
    private String title;
    private String description;
    private String sender;
    private long timeSent;

    public Report() {

    }

    public Report(String reportId, String title, String description, String sender, long timeSent) {
        this.reportId = reportId;
        this.title = title;
        this.description = description;
        this.sender = sender;
        this.timeSent = timeSent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
