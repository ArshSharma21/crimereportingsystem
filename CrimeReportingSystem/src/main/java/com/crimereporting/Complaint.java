package com.crimereporting;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a Crime Complaint in the Crime Reporting System.
 * This is a POJO for complaint data.
 */
public class Complaint {
    private int id;
    private int userId;
    private String crimeType;
    private String location;
    private LocalDate incidentDate;
    private String description;
    private String status;
    private LocalDateTime filedOn;
    private String filedByUsername; // To display the username of the filer in the UI

    // Constructors
    public Complaint() {
    }

    public Complaint(int id, int userId, String crimeType, String location, LocalDate incidentDate,
                     String description, String status, LocalDateTime filedOn) {
        this.id = id;
        this.userId = userId;
        this.crimeType = crimeType;
        this.location = location;
        this.incidentDate = incidentDate;
        this.description = description;
        this.status = status;
        this.filedOn = filedOn;
    }

    // Getters and Setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(String crimeType) {
        this.crimeType = crimeType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getFiledOn() {
        return filedOn;
    }

    public void setFiledOn(LocalDateTime filedOn) {
        this.filedOn = filedOn;
    }

    public String getFiledByUsername() {
        return filedByUsername;
    }

    public void setFiledByUsername(String filedByUsername) {
        this.filedByUsername = filedByUsername;
    }

    @Override
    public String toString() {
        return "Complaint{" +
                "id=" + id +
                ", userId=" + userId +
                ", crimeType='" + crimeType + '\'' +
                ", location='" + location + '\'' +
                ", incidentDate=" + incidentDate +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", filedOn=" + filedOn +
                ", filedByUsername='" + filedByUsername + '\'' +
                '}';
    }
}
