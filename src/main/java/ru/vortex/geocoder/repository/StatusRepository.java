package ru.vortex.geocoder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vortex.geocoder.model.Status;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Long> {
    Optional<Status> findByName(String name);
}