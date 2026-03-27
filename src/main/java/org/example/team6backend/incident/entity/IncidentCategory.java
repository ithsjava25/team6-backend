package org.example.team6backend.incident.entity;

public enum IncidentCategory {
    LAUNDRY_ROOM("Laundry Room"),
    NOISE_DISTURBANCE("Noise Disturbance"),
    DAMAGE("Damage"),
    OTHER("Other");

    private final String displayName;

    IncidentCategory(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
