package com.skybooker.flight_service.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightRequestDto {

    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @NotNull(message = "Airline ID is required")
    private Integer airlineId;

    @NotBlank(message = "Origin airport code is required")
    private String originAirportCode;

    @NotBlank(message = "Destination airport code is required")
    private String destinationAirportCode;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be greater than 0")
    private Integer totalSeats;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be greater than 0")
    private Double basePrice;

    private String aircraftType;
}