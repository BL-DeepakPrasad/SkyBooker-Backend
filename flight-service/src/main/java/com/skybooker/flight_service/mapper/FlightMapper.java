package com.skybooker.flight_service.mapper;

import com.skybooker.flight_service.dto.FlightRequestDto;
import com.skybooker.flight_service.dto.FlightResponseDto;
import com.skybooker.flight_service.model.Flight;

public class FlightMapper {

    // Request DTO -> Entity
    public static Flight toEntity(FlightRequestDto dto) {

        if (dto == null) return null;

        Flight flight = new Flight();

        flight.setFlightNumber(dto.getFlightNumber());
        flight.setAirlineId(dto.getAirlineId());
        flight.setOriginAirportCode(dto.getOriginAirportCode());
        flight.setDestinationAirportCode(dto.getDestinationAirportCode());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setTotalSeats(dto.getTotalSeats());
        flight.setAvailableSeats(dto.getTotalSeats()); // important
        flight.setBasePrice(dto.getBasePrice());
        flight.setAircraftType(dto.getAircraftType());
        flight.setStatus("SCHEDULED");

        return flight;
    }

    // Entity -> Response DTO
    public static FlightResponseDto toDto(Flight flight) {

        if (flight == null) return null;

        return FlightResponseDto.builder()
                .flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber())
                .airlineId(flight.getAirlineId())
                .originAirportCode(flight.getOriginAirportCode())
                .destinationAirportCode(flight.getDestinationAirportCode())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .durationMinutes(flight.getDurationMinutes())
                .status(flight.getStatus())
                .aircraftType(flight.getAircraftType())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .basePrice(flight.getBasePrice())
                .build();
    }

    // Update entity from DTO
    public static void updateEntity(Flight flight, FlightRequestDto dto) {

        if (dto == null || flight == null) return;

        flight.setFlightNumber(dto.getFlightNumber());
        flight.setAirlineId(dto.getAirlineId());
        flight.setOriginAirportCode(dto.getOriginAirportCode());
        flight.setDestinationAirportCode(dto.getDestinationAirportCode());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setAircraftType(dto.getAircraftType());
        flight.setBasePrice(dto.getBasePrice());
    }
}
