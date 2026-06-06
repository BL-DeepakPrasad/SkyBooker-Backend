package com.skybooker.flight_service.service;

import com.skybooker.flight_service.exception.FlightNotFoundException;
import com.skybooker.flight_service.model.Flight;
import com.skybooker.flight_service.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.builder()
                .flightId(1)
                .flightNumber("SKY123")
                .airlineId(101)
                .originAirportCode("JFK")
                .destinationAirportCode("LHR")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(7))
                .durationMinutes(420)
                .status("SCHEDULED")
                .aircraftType("Boeing 777")
                .totalSeats(300)
                .availableSeats(300)
                .basePrice(500.0)
                .build();
    }

    @Test
    void addFlight_ShouldReturnSavedFlight() {
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        Flight savedFlight = flightService.addFlight(flight);

        assertNotNull(savedFlight);
        assertEquals("SKY123", savedFlight.getFlightNumber());
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    void getFlightById_ShouldReturnFlight_WhenExists() {
        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));

        Optional<Flight> foundFlight = flightService.getFlightById(1);

        assertTrue(foundFlight.isPresent());
        assertEquals("SKY123", foundFlight.get().getFlightNumber());
        verify(flightRepository, times(1)).findById(1);
    }

    @Test
    void getFlightById_ShouldReturnEmpty_WhenNotExists() {
        when(flightRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Flight> foundFlight = flightService.getFlightById(99);

        assertFalse(foundFlight.isPresent());
        verify(flightRepository, times(1)).findById(99);
    }

    @Test
    void updateFlight_ShouldUpdateAndReturnFlight_WhenExists() {
        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        Flight updatedInfo = Flight.builder()
                .flightNumber("SKY999")
                .airlineId(101)
                .originAirportCode("JFK")
                .destinationAirportCode("LHR")
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(300)
                .availableSeats(250)
                .basePrice(600.0)
                .build();

        Flight result = flightService.updateFlight(1, updatedInfo);

        assertNotNull(result);
        assertEquals("SKY999", result.getFlightNumber());
        assertEquals(600.0, result.getBasePrice());
        verify(flightRepository, times(1)).findById(1);
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    void updateFlight_ShouldThrowException_WhenNotExists() {
        when(flightRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class, () -> {
            flightService.updateFlight(99, flight);
        });

        verify(flightRepository, times(1)).findById(99);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void decrementSeats_ShouldDecreaseAvailableSeats_WhenEnoughSeats() {
        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));

        flightService.decrementSeats(1, 2);

        assertEquals(298, flight.getAvailableSeats());
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    void decrementSeats_ShouldThrowException_WhenNotEnoughSeats() {
        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            flightService.decrementSeats(1, 301);
        });

        assertEquals("Not enough seats available", exception.getMessage());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void getFlightsByAirline_ShouldReturnList() {
        when(flightRepository.findByAirlineId(101)).thenReturn(List.of(flight));

        List<Flight> flights = flightService.getFlightsByAirline(101);

        assertNotNull(flights);
        assertEquals(1, flights.size());
        assertEquals(101, flights.get(0).getAirlineId());
        verify(flightRepository, times(1)).findByAirlineId(101);
    }
}
