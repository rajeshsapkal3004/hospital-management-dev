package com.hospital_management.enumclasses;

public enum BackupType {
    FULL("Full backup of entire database"),
    INCREMENTAL("Incremental backup of changes"),
    DIFFERENTIAL("Differential backup from last full backup"),
    SCHEMA_ONLY("Database structure only"),
    DATA_ONLY("Data only, no structure");

    private final String description;

    BackupType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}