package com.skybooker.flight_service.service;

import com.skybooker.flight_service.dto.FlightRequestDto;
import com.skybooker.flight_service.dto.FlightResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FlightService {

    FlightResponseDto addFlight(FlightRequestDto flightRequestDto);

    FlightResponseDto getFlightById(Long flightId);

    FlightResponseDto getFlightByNumber(String flightNumber);

    List<FlightResponseDto> searchFlights(String origin, String destination, LocalDate travelDate, int passengers);

    Page<FlightResponseDto> searchFlightsWithPagination(String origin, String destination, LocalDate travelDate, int passengers, Pageable pageable);

    Map<String, List<FlightResponseDto>> searchRoundTrip(String origin, String destination, LocalDate departureDate, LocalDate returnDate, int passengers);

    FlightResponseDto updateFlight(Long flightId, FlightRequestDto flightRequestDto);

    void updateStatus(Long flightId, String status);

    void decrementSeats(Long flightId, int seats);

    void incrementSeats(Long flightId, int seats);

    void deleteFlight(Long flightId);

    List<FlightResponseDto> getFlightsByAirline(Long airlineId);

    Page<FlightResponseDto> getFlightsByAirline(Long airlineId, Pageable pageable);
}