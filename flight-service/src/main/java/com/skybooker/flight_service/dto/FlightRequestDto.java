package com.skybooker.flight_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRequestDto {

    @NotBlank(message = "Flight number is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{3,4}$", message = "Flight number must be 2 letters followed by 3-4 digits")
    private String flightNumber;

    @NotNull(message = "Airline ID is required")
    @Min(value = 1, message = "Invalid airline ID")
    private Long airlineId;

    @NotBlank(message = "Origin airport code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Airport code must be 3 uppercase letters")
    private String originAirportCode;

    @NotBlank(message = "Destination airport code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Airport code must be 3 uppercase letters")
    private String destinationAirportCode;

    @NotNull(message = "Departure time is required")

    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")

    private LocalDateTime arrivalTime;

    private String aircraftType;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 900, message = "Total seats cannot exceed 900")
    private Integer totalSeats;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be at least 0.01")
    @DecimalMax(value = "99999.99", message = "Base price cannot exceed 99999.99")
    private BigDecimal basePrice;
}