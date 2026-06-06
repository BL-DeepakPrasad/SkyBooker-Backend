package com.skybooker.flight_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponseDto {

    private Integer flightId;

    private String flightNumber;

    private Integer airlineId;

    private String originAirportCode;

    private String destinationAirportCode;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private Integer durationMinutes;

    private String status;

    private String aircraftType;

    private Integer totalSeats;

    private Integer availableSeats;

    private Double basePrice;
}