package com.skybooker.flight_service.repository;

import com.skybooker.flight_service.model.Flight;
import com.skybooker.flight_service.model.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByAirlineId(Long airlineId);

    Page<Flight> findByAirlineId(Long airlineId, Pageable pageable);

    List<Flight> findByStatus(FlightStatus status);

    Page<Flight> findByStatus(FlightStatus status, Pageable pageable);

    long countByAirlineId(Long airlineId);

    List<Flight> findByOriginAirportCodeAndDestinationAirportCodeAndDepartureTimeBetween(
            String originAirportCode,
            String destinationAirportCode,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    @Query("SELECT f FROM Flight f WHERE f.originAirportCode = :origin " +
            "AND f.destinationAirportCode = :destination " +
            "AND f.departureTime >= :startOfDay AND f.departureTime < :endOfDay " +
            "AND f.availableSeats >= :passengers " +
            "AND f.status = 'SCHEDULED' " +
            "AND f.deleted = false")
    List<Flight> searchAvailableFlights(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("passengers") int passengers
    );

    @Query("SELECT f FROM Flight f WHERE f.originAirportCode = :origin " +
            "AND f.destinationAirportCode = :destination " +
            "AND DATE(f.departureTime) = DATE(:departureDate) " +
            "AND f.availableSeats >= :passengers " +
            "AND f.status = 'SCHEDULED' " +
            "AND f.deleted = false")
    Page<Flight> findAvailableFlightsWithPagination(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureDate") LocalDateTime departureDate,
            @Param("passengers") int passengers,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Flight f SET f.availableSeats = f.availableSeats - :seats " +
            "WHERE f.flightId = :flightId AND f.availableSeats >= :seats AND f.deleted = false")
    int decrementSeatsIfAvailable(@Param("flightId") Long flightId, @Param("seats") int seats);

    @Modifying
    @Query("UPDATE Flight f SET f.availableSeats = f.availableSeats + :seats " +
            "WHERE f.flightId = :flightId AND f.availableSeats + :seats <= f.totalSeats AND f.deleted = false")
    int incrementSeatsIfPossible(@Param("flightId") Long flightId, @Param("seats") int seats);

    @Query("SELECT f FROM Flight f WHERE f.departureTime BETWEEN :start AND :end AND f.status = 'SCHEDULED'")
    List<Flight> findFlightsByDepartureDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(f) FROM Flight f WHERE f.airlineId = :airlineId AND f.status = 'SCHEDULED' AND f.departureTime > :now")
    long countUpcomingFlightsByAirline(@Param("airlineId") Long airlineId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Flight f SET f.deleted = true WHERE f.flightId = :flightId")
    void softDelete(@Param("flightId") Long flightId);
}