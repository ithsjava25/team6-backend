package org.example.team6backend.user.entity;

public enum UserRole {
    PENDING("Pending", "Awaiting approval, cannot access system"),
    RESIDENT("Resident", "Can create and view own incidents"),
    HANDLER("Handler", "Can manage assigned incidents"),
    ADMIN("Admin", "Full access to the system");

    private final String displayName;
    private final String description;


    UserRole(String displayName, String description){
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
