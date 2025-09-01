package com.hospital_management.enumclasses;

// SystemHealthStatus.java

public enum SystemHealthStatus {
    HEALTHY("System is running normally"),
    WARNING("System has minor issues"),
    CRITICAL("System has critical issues"),
    DOWN("System is down"),
    MAINTENANCE("System is under maintenance");

    private final String description;

    SystemHealthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
