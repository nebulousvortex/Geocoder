package ru.vortex.geocoder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.vortex.geocoder.model.Location;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    Optional<Location> findByAddress(String address);
    Optional<Location> findByLatitudeAndLongitude(Double latitude, Double longitude);
}