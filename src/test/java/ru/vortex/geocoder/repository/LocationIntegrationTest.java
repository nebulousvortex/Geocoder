package ru.vortex.geocoder.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.vortex.geocoder.model.Location;
import ru.vortex.geocoder.model.Status;
import ru.vortex.geocoder.service.GeocodingService;
import ru.vortex.geocoder.service.LocationService;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(LocationService.class)
public class LocationIntegrationTest {

    @Autowired
    private LocationService service;

    @Autowired
    private LocationRepository repository;

    @Autowired
    private ru.vortex.geocoder.repository.StatusRepository statusRepository;

    @MockBean
    private GeocodingService geocodingService;

    @Test
    void shouldPersistAndRetrieveLocationViaService() {
        Status pendingStatus = statusRepository.findByName("В процессе")
                .orElseGet(() -> {
                    Status s = new Status("В процессе", "#FFA500");
                    return statusRepository.save(s);
                });

        Location location = new Location();
        location.setAddress("Москва, Загородное ш., 2");
        location.setStatus(pendingStatus);

        Location saved = service.save(location);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getAddress()).isEqualTo("Москва, Загородное ш., 2");
        assertThat(saved.getStatus().getId()).isNotNull();

        java.util.List<Location> all = repository.findAll();
        assertThat(all).hasSizeGreaterThan(0);
        assertThat(all.stream().anyMatch(l -> l.getAddress().equals("Москва, Загородное ш., 2"))).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenAddressNotExists() {
        java.util.Optional<Location> found = repository.findByAddress("Нет такого адреса");
        assertThat(found).isEmpty();
    }
}