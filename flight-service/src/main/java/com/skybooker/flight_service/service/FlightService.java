package com.skybooker.flight_service.service;

import com.skybooker.flight_service.model.Flight;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FlightService {

    Flight addFlight(Flight flight);

    Optional<Flight> getFlightById(Integer flightId);

    Optional<Flight> getFlightByNumber(String flightNumber);

    List<Flight> searchFlights(String origin, String destination, LocalDate travelDate, int passengers);

    Map<String, List<Flight>> searchRoundTrip(
            String origin,
            String destination,
            LocalDate departureDate,
            LocalDate returnDate,
            int passengers
    );

    Flight updateFlight(Integer flightId, Flight flight);

    void updateStatus(Integer flightId, String status);

    void decrementSeats(Integer flightId, int seats);

    void incrementSeats(Integer flightId,int seats );

    void deleteFlight(Integer flightId);

    List<Flight> getFlightsByAirline(Integer airlineId );

}