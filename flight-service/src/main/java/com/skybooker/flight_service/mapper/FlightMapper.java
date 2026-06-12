package com.skybooker.flight_service.mapper;

import com.skybooker.flight_service.dto.FlightRequestDto;
import com.skybooker.flight_service.dto.FlightResponseDto;
import com.skybooker.flight_service.model.AircraftType;
import com.skybooker.flight_service.model.Flight;
import com.skybooker.flight_service.model.FlightStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlightMapper {

    public Flight toEntity(FlightRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Flight flight = new Flight();
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setAirlineId(dto.getAirlineId());
        flight.setOriginAirportCode(dto.getOriginAirportCode());
        flight.setDestinationAirportCode(dto.getDestinationAirportCode());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setTotalSeats(dto.getTotalSeats());
        flight.setAvailableSeats(dto.getTotalSeats()); // Initially available = total
        flight.setBasePrice(dto.getBasePrice());
        flight.setStatus(FlightStatus.SCHEDULED);

        // Calculate duration
        long durationMinutes = Duration.between(dto.getDepartureTime(), dto.getArrivalTime()).toMinutes();
        flight.setDurationMinutes((int) durationMinutes);

        // Set aircraft type if provided
        if (dto.getAircraftType() != null && !dto.getAircraftType().isEmpty()) {
            try {
                flight.setAircraftType(AircraftType.valueOf(dto.getAircraftType()));
            } catch (IllegalArgumentException e) {
                // Default to BOEING_737 if invalid
                flight.setAircraftType(AircraftType.BOEING_737);
            }
        }

        return flight;
    }

    public FlightResponseDto toDto(Flight flight) {
        if (flight == null) {
            return null;
        }

        FlightResponseDto dto = FlightResponseDto.builder()
                .flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber())
                .airlineId(flight.getAirlineId())
                .originAirportCode(flight.getOriginAirportCode())
                .destinationAirportCode(flight.getDestinationAirportCode())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .durationMinutes(flight.getDurationMinutes())
                .status(flight.getStatus().name())
                .aircraftType(flight.getAircraftType() != null ? flight.getAircraftType().name() : null)
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .basePrice(flight.getBasePrice())
                .build();

        dto.calculateAvailablePercentage();
        return dto;
    }

    public List<FlightResponseDto> toDtoList(List<Flight> flights) {
        if (flights == null) {
            return null;
        }
        return flights.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDto(FlightRequestDto dto, Flight flight) {
        if (dto == null || flight == null) {
            return;
        }

        flight.setFlightNumber(dto.getFlightNumber());
        flight.setAirlineId(dto.getAirlineId());
        flight.setOriginAirportCode(dto.getOriginAirportCode());
        flight.setDestinationAirportCode(dto.getDestinationAirportCode());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setTotalSeats(dto.getTotalSeats());
        flight.setBasePrice(dto.getBasePrice());

        // Update duration
        long durationMinutes = Duration.between(dto.getDepartureTime(), dto.getArrivalTime()).toMinutes();
        flight.setDurationMinutes((int) durationMinutes);

        // Update aircraft type
        if (dto.getAircraftType() != null && !dto.getAircraftType().isEmpty()) {
            try {
                flight.setAircraftType(AircraftType.valueOf(dto.getAircraftType()));
            } catch (IllegalArgumentException e) {
                // Keep existing if invalid
            }
        }
    }
}