package com.skybooker.flight_service.model;

import lombok.Getter;

@Getter
public enum AircraftType {
    BOEING_737("Boeing 737", 180, 1500),
    BOEING_777("Boeing 777", 396, 3000),
    BOEING_787("Boeing 787 Dreamliner", 290, 2500),
    AIRBUS_A320("Airbus A320", 180, 1400),
    AIRBUS_A330("Airbus A330", 300, 2800),
    AIRBUS_A380("Airbus A380", 853, 4000),
    EMBRAER_E175("Embraer E175", 88, 800),
    BOMBARDIER_CRJ900("Bombardier CRJ900", 90, 850);

    private final String model;
    private final int typicalSeats;
    private final int maxRangeKm;

    AircraftType(String model, int typicalSeats, int maxRangeKm) {
        this.model = model;
        this.typicalSeats = typicalSeats;
        this.maxRangeKm = maxRangeKm;
    }
}