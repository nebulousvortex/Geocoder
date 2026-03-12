package ru.vortex.geocoder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vortex.geocoder.model.Location;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByAddress(String address);
    Optional<Location> findByLatitudeAndLongitude(Double latitude, Double longitude);
}