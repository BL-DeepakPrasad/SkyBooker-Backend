package com.skybooker.flight_service.service;

import com.skybooker.flight_service.exception.FlightNotFoundException;
import com.skybooker.flight_service.model.Flight;
import com.skybooker.flight_service.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    public Flight addFlight(Flight flight) {
        log.info("Adding new flight: {}", flight.getFlightNumber());
        return flightRepository.save(flight);
    }

    @Override
    public Optional<Flight> getFlightById(Integer flightId) {
        return flightRepository.findById(flightId);
    }

    @Override
    public Optional<Flight> getFlightByNumber(String flightNumber) {
        log.info("Fetching flight by number: {}", flightNumber);
        return flightRepository.findByFlightNumber(flightNumber);
    }

    @Override
    public List<Flight> searchFlights(String origin, String destination, LocalDate travelDate, int passengers) {
        log.info("Searching flights from {} to {}", origin, destination);
        // Simplified search for now (not considering date or passengers in DB query to keep it simple, just route)
        return flightRepository.findByOriginAirportCodeAndDestinationAirportCode(origin, destination);
    }

    @Override
    public Map<String, List<Flight>> searchRoundTrip(String origin, String destination, LocalDate departureDate, LocalDate returnDate, int passengers) {
        List<Flight> outbound = searchFlights(origin, destination, departureDate, passengers);
        List<Flight> inbound = searchFlights(destination, origin, returnDate, passengers);
        return Map.of("outbound", outbound, "inbound", inbound);
    }

    @Override
    public Flight updateFlight(Integer flightId, Flight flight) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
        
        existing.setFlightNumber(flight.getFlightNumber());
        existing.setAirlineId(flight.getAirlineId());
        existing.setOriginAirportCode(flight.getOriginAirportCode());
        existing.setDestinationAirportCode(flight.getDestinationAirportCode());
        existing.setDepartureTime(flight.getDepartureTime());
        existing.setArrivalTime(flight.getArrivalTime());
        existing.setDurationMinutes(flight.getDurationMinutes());
        existing.setStatus(flight.getStatus());
        existing.setAircraftType(flight.getAircraftType());
        existing.setTotalSeats(flight.getTotalSeats());
        existing.setAvailableSeats(flight.getAvailableSeats());
        existing.setBasePrice(flight.getBasePrice());
        
        log.info("Updated flight details for ID: {}", flightId);
        return flightRepository.save(existing);
    }

    @Override
    public void updateStatus(Integer flightId, String status) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
        existing.setStatus(status);
        log.info("Status updated for flight ID: {} to {}", flightId, status);
        flightRepository.save(existing);
    }

    @Override
    public void decrementSeats(Integer flightId, int seats) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
        if (existing.getAvailableSeats() >= seats) {
            existing.setAvailableSeats(existing.getAvailableSeats() - seats);
            flightRepository.save(existing);
        } else {
            throw new RuntimeException("Not enough seats available");
        }
    }

    @Override
    public void incrementSeats(Integer flightId, int seats) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
        existing.setAvailableSeats(existing.getAvailableSeats() + seats);
        flightRepository.save(existing);
    }

    @Override
    public void deleteFlight(Integer flightId) {
        Flight existing = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
        flightRepository.delete(existing);
        log.info("Deleted flight with ID: {}", flightId);
    }

    @Override
    public List<Flight> getFlightsByAirline(Integer airlineId) {
        return flightRepository.findByAirlineId(airlineId);
    }
}