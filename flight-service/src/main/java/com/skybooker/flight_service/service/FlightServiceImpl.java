package com.skybooker.flight_service.service;

import com.skybooker.flight_service.dto.FlightRequestDto;
import com.skybooker.flight_service.dto.FlightResponseDto;
import com.skybooker.flight_service.exception.FlightNotFoundException;
import com.skybooker.flight_service.exception.InsufficientSeatsException;
import com.skybooker.flight_service.exception.InvalidFlightException;
import com.skybooker.flight_service.mapper.FlightMapper;
import com.skybooker.flight_service.model.Flight;
import com.skybooker.flight_service.model.FlightStatus;
import com.skybooker.flight_service.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    @Override
    @Transactional
    @CacheEvict(value = {"flights", "flightSearch"}, allEntries = true)
    public FlightResponseDto addFlight(FlightRequestDto flightRequestDto) {
        log.info("Adding new flight: {}", flightRequestDto.getFlightNumber());

        // Check if flight number already exists
        if (flightRepository.findByFlightNumber(flightRequestDto.getFlightNumber()).isPresent()) {
            throw new InvalidFlightException("Flight number already exists: " + flightRequestDto.getFlightNumber());
        }

        Flight flight = flightMapper.toEntity(flightRequestDto);
        Flight savedFlight = flightRepository.save(flight);
        log.info("Flight added successfully with ID: {}", savedFlight.getFlightId());

        return flightMapper.toDto(savedFlight);
    }

    @Override
    public FlightResponseDto getFlightById(Long flightId) {
        log.info("Fetching flight by ID: {}", flightId);
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));
        return flightMapper.toDto(flight);
    }

    @Override
    public FlightResponseDto getFlightByNumber(String flightNumber) {
        log.info("Fetching flight by number: {}", flightNumber);
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with number: " + flightNumber));
        return flightMapper.toDto(flight);
    }

    @Override
    @Cacheable(value = "flightSearch", key = "{#origin, #destination, #travelDate, #passengers}")
    public List<FlightResponseDto> searchFlights(String origin, String destination, LocalDate travelDate, int passengers) {
        log.info("Searching for flights from {} to {} on {} for {} passengers",
                origin, destination, travelDate, passengers);

        validateSearchParams(origin, destination, travelDate, passengers);

        LocalDateTime startOfDay = travelDate.atStartOfDay();
        LocalDateTime endOfDay = travelDate.plusDays(1).atStartOfDay();

        List<Flight> flights = flightRepository.searchAvailableFlights(
                origin.toUpperCase(),
                destination.toUpperCase(),
                startOfDay,
                endOfDay,
                passengers
        );

        log.info("Found {} flights for search criteria", flights.size());
        return flightMapper.toDtoList(flights);
    }

    @Override
    public Page<FlightResponseDto> searchFlightsWithPagination(String origin, String destination,
                                                               LocalDate travelDate, int passengers,
                                                               Pageable pageable) {
        log.info("Searching flights with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        LocalDateTime departureDate = travelDate.atStartOfDay();
        Page<Flight> flights = flightRepository.findAvailableFlightsWithPagination(
                origin.toUpperCase(),
                destination.toUpperCase(),
                departureDate,
                passengers,
                pageable
        );

        return flights.map(flightMapper::toDto);
    }

    @Override
    public Map<String, List<FlightResponseDto>> searchRoundTrip(String origin, String destination,
                                                                LocalDate departureDate, LocalDate returnDate,
                                                                int passengers) {
        log.info("Searching round trip from {} to {} on {} and return on {}",
                origin, destination, departureDate, returnDate);

        List<FlightResponseDto> outbound = searchFlights(origin, destination, departureDate, passengers);
        List<FlightResponseDto> inbound = searchFlights(destination, origin, returnDate, passengers);

        return Map.of("outbound", outbound, "inbound", inbound);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"flights", "flightSearch"}, allEntries = true)
    public FlightResponseDto updateFlight(Long flightId, FlightRequestDto flightRequestDto) {
        log.info("Updating flight with ID: {}", flightId);

        Flight existingFlight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));

        // Check if new flight number conflicts with another flight
        if (!existingFlight.getFlightNumber().equals(flightRequestDto.getFlightNumber()) &&
                flightRepository.findByFlightNumber(flightRequestDto.getFlightNumber()).isPresent()) {
            throw new InvalidFlightException("Flight number already exists: " + flightRequestDto.getFlightNumber());
        }

        flightMapper.updateEntityFromDto(flightRequestDto, existingFlight);
        Flight updatedFlight = flightRepository.save(existingFlight);
        log.info("Flight updated successfully with ID: {}", flightId);

        return flightMapper.toDto(updatedFlight);
    }

    @Override
    @Transactional
    public void updateStatus(Long flightId, String status) {
        log.info("Updating status for flight ID: {} to {}", flightId, status);

        Flight existingFlight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));

        try {
            FlightStatus newStatus = FlightStatus.valueOf(status.toUpperCase());
            existingFlight.setStatus(newStatus);
            flightRepository.save(existingFlight);
            log.info("Status updated successfully for flight ID: {}", flightId);
        } catch (IllegalArgumentException e) {
            throw new InvalidFlightException("Invalid status: " + status);
        }
    }

    @Override
    @Transactional
    public void decrementSeats(Long flightId, int seats) {
        log.info("Decrementing {} seats for flight ID: {}", seats, flightId);

        if (seats <= 0) {
            throw new InvalidFlightException("Number of seats to decrement must be positive");
        }

        int updated = flightRepository.decrementSeatsIfAvailable(flightId, seats);
        if (updated == 0) {
            Flight flight = flightRepository.findById(flightId).orElse(null);
            if (flight == null) {
                throw new FlightNotFoundException("Flight not found with ID: " + flightId);
            }
            throw new InsufficientSeatsException("Not enough seats available. Available: " + flight.getAvailableSeats() + ", Requested: " + seats);
        }

        log.info("Successfully decremented {} seats for flight ID: {}", seats, flightId);
    }

    @Override
    @Transactional
    public void incrementSeats(Long flightId, int seats) {
        log.info("Incrementing {} seats for flight ID: {}", seats, flightId);

        if (seats <= 0) {
            throw new InvalidFlightException("Number of seats to increment must be positive");
        }

        int updated = flightRepository.incrementSeatsIfPossible(flightId, seats);
        if (updated == 0) {
            Flight flight = flightRepository.findById(flightId).orElse(null);
            if (flight == null) {
                throw new FlightNotFoundException("Flight not found with ID: " + flightId);
            }
            throw new InvalidFlightException("Cannot increment seats. Would exceed total seats limit.");
        }

        log.info("Successfully incremented {} seats for flight ID: {}", seats, flightId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"flights", "flightSearch"}, allEntries = true)
    public void deleteFlight(Long flightId) {
        log.info("Deleting flight with ID: {}", flightId);

        Flight existingFlight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + flightId));

        // Soft delete instead of hard delete
        flightRepository.softDelete(flightId);
        log.info("Soft deleted flight with ID: {}", flightId);
    }

    @Override
    public List<FlightResponseDto> getFlightsByAirline(Long airlineId) {
        log.info("Fetching all flights for airline ID: {}", airlineId);
        List<Flight> flights = flightRepository.findByAirlineId(airlineId);
        return flightMapper.toDtoList(flights);
    }

    @Override
    public Page<FlightResponseDto> getFlightsByAirline(Long airlineId, Pageable pageable) {
        log.info("Fetching paginated flights for airline ID: {}", airlineId);
        Page<Flight> flights = flightRepository.findByAirlineId(airlineId, pageable);
        return flights.map(flightMapper::toDto);
    }

    private void validateSearchParams(String origin, String destination, LocalDate travelDate, int passengers) {
        if (origin == null || origin.trim().isEmpty()) {
            throw new InvalidFlightException("Origin airport code is required");
        }

        if (destination == null || destination.trim().isEmpty()) {
            throw new InvalidFlightException("Destination airport code is required");
        }

        if (origin.equalsIgnoreCase(destination)) {
            throw new InvalidFlightException("Origin and destination cannot be the same");
        }

        if (travelDate == null) {
            throw new InvalidFlightException("Travel date is required");
        }

        if (travelDate.isBefore(LocalDate.now())) {
            throw new InvalidFlightException("Travel date cannot be in the past");
        }

        if (passengers < 1 || passengers > 9) {
            throw new InvalidFlightException("Passenger count must be between 1 and 9");
        }
    }
}