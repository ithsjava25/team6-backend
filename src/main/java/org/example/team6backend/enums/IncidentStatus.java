package org.example.team6backend.enums;

public enum IncidentStatus {
    OPEN("Open", "Incident has been reported"),
    IN_PROGRESS("In Progress", "Work is ongoing"),
    RESOLVED("Resolved", "Issue has been fixed"),
    CLOSED("Closes", "Incident is closed");

    private final String displayName;
    private final String description;

    IncidentStatus(String displayName, String description){
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName(){
        return displayName;
    }

    public String getDescription(){
        return description;
    }
}
