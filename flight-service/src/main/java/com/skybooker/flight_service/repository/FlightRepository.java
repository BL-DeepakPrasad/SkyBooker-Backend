package com.skybooker.flight_service.repository;


import com.skybooker.flight_service.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    Optional<Flight> findByFlightId(Integer flightId);

    List<Flight> findByAirlineId(Integer airlineId);

    List<Flight> findByStatus(String status);

    long countByAirlineId(Integer airlineId);

    List<Flight> findByOriginAirportCodeAndDestinationAirportCode(
            String originAirportCode,
            String destinationAirportCode
    );
}