package com.skybooker.flight_service.model;


import lombok.Getter;

@Getter
public enum FlightStatus {
    SCHEDULED("Scheduled to depart"),
    DELAYED("Departure delayed"),
    BOARDING("Now boarding"),
    DEPARTED("Flight has departed"),
    IN_AIR("En route"),
    LANDED("Flight has landed"),
    CANCELLED("Flight cancelled"),
    COMPLETED("Journey completed");

    private final String description;

    FlightStatus(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return this == SCHEDULED || this == DELAYED || this == BOARDING;
    }

    public boolean isFinished() {
        return this == LANDED || this == COMPLETED || this == CANCELLED;
    }
}
