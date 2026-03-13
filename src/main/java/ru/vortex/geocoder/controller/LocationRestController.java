package ru.vortex.geocoder.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vortex.geocoder.dto.LocationDto;
import ru.vortex.geocoder.service.LocationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
public class LocationRestController {
    private final LocationService service;

    public LocationRestController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<LocationDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LocationDto> locations = service.findAllDto(pageable);
        return ResponseEntity.ok(locations);
    }

    @PostMapping
    public ResponseEntity<LocationDto> create(@RequestBody LocationDto request) {
        LocationDto created = service.createLocation(request.getAddress());
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, String>> importFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
                service.importAddresses(bytes, filename);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Импорт запущен асинхронно. Геокодирование выполняется в фоне.");
                return ResponseEntity.ok(response);
            }
            Map<String, String> response = new HashMap<>();
            response.put("error", "Файл пустой");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ошибка импорта: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}