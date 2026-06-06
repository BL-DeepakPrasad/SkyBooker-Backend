package com.skybooker.flight_service.controller;

import com.skybooker.flight_service.dto.ApiResponse;
import com.skybooker.flight_service.dto.FlightRequestDto;
import com.skybooker.flight_service.dto.FlightResponseDto;
import com.skybooker.flight_service.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flight Management", description = "Endpoints for managing flights")
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    @Operation(summary = "Add a new flight", description = "Creates a new flight record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Flight created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Flight number already exists")
    })
    public ResponseEntity<ApiResponse<FlightResponseDto>> addFlight(@Valid @RequestBody FlightRequestDto flightRequestDto) {
        FlightResponseDto createdFlight = flightService.addFlight(flightRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flight added successfully", createdFlight));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID", description = "Retrieves a flight by its ID")
    public ResponseEntity<ApiResponse<FlightResponseDto>> getFlightById(@PathVariable Long id) {
        FlightResponseDto flight = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success(flight));
    }

    @GetMapping("/number/{flightNumber}")
    @Operation(summary = "Get flight by number", description = "Retrieves a flight by its flight number")
    public ResponseEntity<ApiResponse<FlightResponseDto>> getFlightByNumber(@PathVariable String flightNumber) {
        FlightResponseDto flight = flightService.getFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flight));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update flight", description = "Updates an existing flight")
    public ResponseEntity<ApiResponse<FlightResponseDto>> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightRequestDto flightRequestDto) {
        FlightResponseDto updatedFlight = flightService.updateFlight(id, flightRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Flight updated successfully", updatedFlight));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete flight", description = "Deletes a flight by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok(ApiResponse.success("Flight deleted successfully", null));
    }

    @GetMapping("/airline/{airlineId}")
    @Operation(summary = "Get flights by airline", description = "Retrieves all flights for a specific airline")
    public ResponseEntity<ApiResponse<List<FlightResponseDto>>> getFlightsByAirline(@PathVariable Long airlineId) {
        List<FlightResponseDto> flights = flightService.getFlightsByAirline(airlineId);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/airline/{airlineId}/paged")
    @Operation(summary = "Get flights by airline with pagination", description = "Retrieves paginated flights for a specific airline")
    public ResponseEntity<ApiResponse<Page<FlightResponseDto>>> getFlightsByAirlinePaged(
            @PathVariable Long airlineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FlightResponseDto> flights = flightService.getFlightsByAirline(airlineId, pageable);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Search for available flights based on criteria")
    public ResponseEntity<ApiResponse<List<FlightResponseDto>>> searchFlights(
            @Parameter(description = "Origin airport code (3 letters)", required = true)
            @RequestParam String origin,

            @Parameter(description = "Destination airport code (3 letters)", required = true)
            @RequestParam String destination,

            @Parameter(description = "Travel date (dd-MM-yyyy)", required = true)
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate travelDate,

            @Parameter(description = "Number of passengers (1-9)")
            @RequestParam(defaultValue = "1") @Min(1) int passengers) {

        List<FlightResponseDto> flights = flightService.searchFlights(origin, destination, travelDate, passengers);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/search/paged")
    @Operation(summary = "Search flights with pagination", description = "Search for available flights with pagination")
    public ResponseEntity<ApiResponse<Page<FlightResponseDto>>> searchFlightsPaged(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate travelDate,
            @RequestParam(defaultValue = "1") int passengers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FlightResponseDto> flights = flightService.searchFlightsWithPagination(
                origin, destination, travelDate, passengers, pageable);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/search/roundtrip")
    @Operation(summary = "Search round trip flights", description = "Search for round trip flights")
    public ResponseEntity<ApiResponse<Map<String, List<FlightResponseDto>>>> searchRoundTrip(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate departureDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate returnDate,
            @RequestParam(defaultValue = "1") int passengers) {

        Map<String, List<FlightResponseDto>> flights = flightService.searchRoundTrip(
                origin, destination, departureDate, returnDate, passengers);
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update flight status", description = "Updates the status of a flight")
    public ResponseEntity<ApiResponse<Void>> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        flightService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Flight status updated successfully", null));
    }

    @PostMapping("/{id}/decrement-seats")
    @Operation(summary = "Decrement seats", description = "Decreases available seats (for bookings)")
    public ResponseEntity<ApiResponse<Void>> decrementSeats(
            @PathVariable Long id,
            @RequestParam int seats) {

        flightService.decrementSeats(id, seats);
        return ResponseEntity.ok(ApiResponse.success("Seats decremented successfully", null));
    }

    @PostMapping("/{id}/increment-seats")
    @Operation(summary = "Increment seats", description = "Increases available seats (for cancellations)")
    public ResponseEntity<ApiResponse<Void>> incrementSeats(
            @PathVariable Long id,
            @RequestParam int seats) {

        flightService.incrementSeats(id, seats);
        return ResponseEntity.ok(ApiResponse.success("Seats incremented successfully", null));
    }
}