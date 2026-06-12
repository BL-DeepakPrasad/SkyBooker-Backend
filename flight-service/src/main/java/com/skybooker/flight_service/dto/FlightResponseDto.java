package com.skybooker.flight_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponseDto {
    private Long flightId;
    private String flightNumber;
    private Long airlineId;
    private String originAirportCode;
    private String destinationAirportCode;


    private LocalDateTime departureTime;


    private LocalDateTime arrivalTime;

    private Integer durationMinutes;
    private String status;
    private String aircraftType;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal basePrice;
    private Integer availablePercentage;

    public void calculateAvailablePercentage() {
        if (totalSeats != null && totalSeats > 0) {
            this.availablePercentage = (availableSeats * 100) / totalSeats;
        }
    }
}