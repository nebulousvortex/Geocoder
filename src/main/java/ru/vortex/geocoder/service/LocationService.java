package ru.vortex.geocoder.service;

import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vortex.geocoder.model.Location;
import ru.vortex.geocoder.model.Status;
import ru.vortex.geocoder.repository.LocationRepository;
import ru.vortex.geocoder.repository.StatusRepository;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {
    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repository;
    private final GeocodingService geocodingService;
    private final StatusRepository statusRepository;

    public LocationService(
            LocationRepository repository,
            GeocodingService geocodingService,
            StatusRepository statusRepository
    ) {
        this.repository = repository;
        this.geocodingService = geocodingService;
        this.statusRepository = statusRepository;
    }

    public List<Location> findAll() {
        return repository.findAll();
    }

    public Page<Location> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public Location save(Location location) {
        Optional<Location> existingByAddress = repository.findByAddress(location.getAddress());
        if (existingByAddress.isPresent()) {
            log.info("Location with address already exists: {}", location.getAddress());
            return existingByAddress.get();
        }

        Status pendingStatus = statusRepository.findByName("В процессе")
                .orElseGet(() -> createDefaultStatuses().getFirst());
        location.setStatus(pendingStatus);

        repository.save(location);
        geocodeAsync(location.getId());

        return location;
    }

    @Async
    @Transactional
    public void geocodeAsync(Long locationId) {
        Optional<Location> locationOpt = repository.findById(locationId);
        if (locationOpt.isEmpty()) return;

        Location location = locationOpt.get();
        Status readyStatus = statusRepository.findByName("Готово").orElse(null);
        Status errorStatus = statusRepository.findByName("Ошибка").orElse(null);

        try {
            var result = geocodingService.geocode(location.getAddress());

            if (result.isPresent()) {
                Optional<Location> existingByCoordinates = repository.findByLatitudeAndLongitude(
                        result.get().latitude(),
                        result.get().longitude()
                );

                if (existingByCoordinates.isPresent()) {
                    Location existing = existingByCoordinates.get();
                    addAsAlias(existing, location.getAddress());
                    repository.delete(location);
                    log.info("Duplicate found by coordinates. Added '{}' as alias to location {}",
                            location.getAddress(), existing.getId());
                    return;
                }

                location.setLatitude(result.get().latitude());
                location.setLongitude(result.get().longitude());
                location.setNormalizedAddress(result.get().displayName());
                location.setCountry(result.get().country());
                location.setCity(result.get().city());
                location.setStatus(readyStatus != null ? readyStatus : location.getStatus());
            } else {
                location.setStatus(errorStatus != null ? errorStatus : location.getStatus());
            }
        } catch (Exception e) {
            location.setStatus(errorStatus != null ? errorStatus : location.getStatus());
            log.error("Geocoding error for location {}", locationId, e);
        }

        repository.save(location);
    }

    private void addAsAlias(Location existing, String newAlias) {
        String normalizedNewAlias = normalizeString(newAlias);
        String existingAliases = existing.getAliases() != null ? existing.getAliases() : "";

        if (existingAliases.contains(normalizedNewAlias)) return;

        if (existingAliases.isEmpty()) {
            existing.setAliases(normalizedNewAlias);
        } else {
            existing.setAliases(existingAliases + ";" + normalizedNewAlias);
        }
    }

    private String normalizeString(String str) {
        if (str == null) return "";
        return str.trim().toLowerCase();
    }

    @Transactional
    public List<Status> createDefaultStatuses() {
        List<Status> statuses = statusRepository.findAll();
        if (statuses.isEmpty()) {
            Status pending = new Status("В процессе", "#FFA500");
            Status ready = new Status("Готово", "#008000");
            Status error = new Status("Ошибка", "#FF0000");
            statusRepository.saveAll(List.of(pending, ready, error));
            return List.of(pending, ready, error);
        }
        return statuses;
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Async
    @Transactional
    public void importAddresses(byte[] fileBytes, String originalFilename) {
        List<String> addresses = extractAddresses(fileBytes, originalFilename);
        for (String address : addresses) {
            if (!address.isEmpty()) {
                Location location = new Location();
                location.setAddress(address);
                save(location);
            }
        }
    }

    private List<String> extractAddresses(byte[] fileBytes, String filename) {
        List<String> addresses = new ArrayList<>();
        String lower = filename.toLowerCase();
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            if (lower.endsWith(".csv")) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    int addressIndex = -1;
                    boolean firstLine = true;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] parts = line.split(",", -1);
                        if (firstLine) {
                            for (int i = 0; i < parts.length; i++) {
                                if (parts[i].trim().equalsIgnoreCase("Адрес")) {
                                    addressIndex = i;
                                    break;
                                }
                            }
                            firstLine = false;
                            continue;
                        }
                        if (addressIndex != -1 && parts.length > addressIndex) {
                            String val = parts[addressIndex].trim().replace("\"", "");
                            if (!val.isEmpty()) addresses.add(val);
                        }
                    }
                }
            } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
                Workbook workbook = WorkbookFactory.create(is);
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);
                int colIndex = -1;
                if (headerRow != null) {
                    for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                        Cell cell = headerRow.getCell(c);
                        if (cell != null && cell.getStringCellValue().trim().equalsIgnoreCase("Адрес")) {
                            colIndex = c;
                            break;
                        }
                    }
                }
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    Cell cell = row.getCell(colIndex);
                    if (cell != null) {
                        String val = "";
                        switch (cell.getCellType()) {
                            case STRING -> val = cell.getStringCellValue();
                            case NUMERIC -> val = String.valueOf((long) cell.getNumericCellValue());
                            default -> val = "";
                        }
                        val = val.trim();
                        if (!val.isEmpty()) addresses.add(val);
                    }
                }
                workbook.close();
            }
        } catch (Exception ignored) {
        }
        return addresses;
    }

    @Transactional
    public void update(Long id, Location updatedLocation) {
        Optional<Location> optional = repository.findById(id);
        if (optional.isPresent()) {
            Location location = optional.get();
            boolean addressChanged = updatedLocation.getAddress() != null && !updatedLocation.getAddress().equals(location.getAddress());
            if (addressChanged) {
                location.setAddress(updatedLocation.getAddress());
                location.setLatitude(null);
                location.setLongitude(null);
                location.setNormalizedAddress(null);
                location.setCountry(null);
                location.setCity(null);
                Status pendingStatus = statusRepository.findByName("В процессе")
                        .orElseGet(() -> createDefaultStatuses().getFirst());
                location.setStatus(pendingStatus);
                repository.save(location);
                geocodeAsync(id);
            }
        }
    }
}