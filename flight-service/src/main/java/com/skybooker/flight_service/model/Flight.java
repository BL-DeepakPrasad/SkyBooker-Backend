package com.skybooker.flight_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    @Version
    private Long version;

    @Column(nullable = false, unique = true, length = 20)
    private String flightNumber;

    @Column(nullable = false)
    private Long airlineId;

    @Column(nullable = false, length = 3)
    private String originAirportCode;

    @Column(nullable = false, length = 3)
    private String destinationAirportCode;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private AircraftType aircraftType;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // Audit fields
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @PrePersist
    @PreUpdate
    private void validateFlight() {
        // Validate dates
        if (departureTime.isAfter(arrivalTime)) {
            throw new IllegalStateException("Departure time must be before arrival time");
        }

        if (departureTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Departure time cannot be in the past");
        }

        // Validate airports
        if (originAirportCode.equals(destinationAirportCode)) {
            throw new IllegalStateException("Origin and destination cannot be the same");
        }

        // Validate seats
        if (totalSeats <= 0) {
            throw new IllegalStateException("Total seats must be positive");
        }

        if (availableSeats > totalSeats) {
            throw new IllegalStateException("Available seats cannot exceed total seats");
        }

        if (availableSeats < 0) {
            throw new IllegalStateException("Available seats cannot be negative");
        }

        // Validate price
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Base price must be positive");
        }

        // Validate duration
        long actualDuration = java.time.Duration.between(departureTime, arrivalTime).toMinutes();
        if (Math.abs(actualDuration - durationMinutes) > 5) {
            throw new IllegalStateException("Duration doesn't match departure and arrival times");
        }

        // Validate flight number format
        if (!flightNumber.matches("^[A-Z]{2}[0-9]{3,4}$")) {
            throw new IllegalStateException("Invalid flight number format. Expected: 2 letters followed by 3-4 digits");
        }
    }

    // Business methods
    public boolean hasAvailableSeats(int requestedSeats) {
        return availableSeats >= requestedSeats;
    }

    public void decrementSeats(int seats) {
        if (!hasAvailableSeats(seats)) {
            throw new IllegalStateException("Not enough seats available. Available: " + availableSeats + ", Requested: " + seats);
        }
        this.availableSeats -= seats;
    }

    public void incrementSeats(int seats) {
        if (this.availableSeats + seats > this.totalSeats) {
            throw new IllegalStateException("Cannot exceed total seats. Total: " + totalSeats + ", Available: " + availableSeats);
        }
        this.availableSeats += seats;
    }

    public boolean isDepartureToday() {
        return departureTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    public boolean isCancellable() {
        return status != FlightStatus.CANCELLED &&
                status != FlightStatus.COMPLETED &&
                departureTime.isAfter(LocalDateTime.now().plusHours(2));
    }
}