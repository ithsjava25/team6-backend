package org.example.team6backend.enums;

public enum IncidentCategory {
    LAUNDRY_ROOM("Laundry Room"),
    NOICE_DISTURBANCE("Noice Disturbance"),
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
