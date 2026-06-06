package com.skybooker.flight_service.controller;

import com.skybooker.flight_service.dto.FlightRequestDto;
import com.skybooker.flight_service.dto.FlightResponseDto;
import com.skybooker.flight_service.mapper.FlightMapper;
import com.skybooker.flight_service.model.Flight;
import com.skybooker.flight_service.service.FlightService;
import com.skybooker.flight_service.exception.FlightNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    public ResponseEntity<FlightResponseDto> addFlight(@Valid @RequestBody FlightRequestDto flightRequestDto) {
        Flight flight = FlightMapper.toEntity(flightRequestDto);
        // set some defaults
        flight.setStatus("SCHEDULED");
        flight.setAvailableSeats(flight.getTotalSeats());
        flight.setDurationMinutes((int) java.time.Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes());
        
        Flight createdFlight = flightService.addFlight(flight);
        return new ResponseEntity<>(FlightMapper.toDto(createdFlight), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponseDto> getFlightById(@PathVariable Integer id) {
        return flightService.getFlightById(id)
                .map(FlightMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with ID: " + id));
    }

    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<FlightResponseDto> getFlightByNumber(@PathVariable String flightNumber) {
        return flightService.getFlightByNumber(flightNumber)
                .map(FlightMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found with number: " + flightNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlightResponseDto> updateFlight(@PathVariable Integer id, @Valid @RequestBody FlightRequestDto flightRequestDto) {
        Flight flight = FlightMapper.toEntity(flightRequestDto);
        flight.setDurationMinutes((int) java.time.Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes());
        Flight updatedFlight = flightService.updateFlight(id, flight);
        return ResponseEntity.ok(FlightMapper.toDto(updatedFlight));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Integer id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/airline/{airlineId}")
    public ResponseEntity<List<FlightResponseDto>> getFlightsByAirline(@PathVariable Integer airlineId) {
        List<Flight> flights = flightService.getFlightsByAirline(airlineId);
        List<FlightResponseDto> dtos = flights.stream()
                .map(FlightMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
