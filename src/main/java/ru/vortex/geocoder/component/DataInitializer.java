package ru.vortex.geocoder.component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.vortex.geocoder.service.LocationService;

@Component
public class DataInitializer implements CommandLineRunner {
    private final LocationService locationService;

    public DataInitializer(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void run(String... args) {
        locationService.createDefaultStatuses();
    }
}