package ru.vortex.geocoder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.vortex.geocoder.dto.LocationDto;
import ru.vortex.geocoder.service.LocationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
@Tag(name = "Локации")
public class LocationRestController {
    private final LocationService service;

    public LocationRestController(LocationService service) {
        this.service = service;
    }

    @Operation(summary = "Получить список локаций (USER + ADMIN)")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<LocationDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LocationDto> locations = service.findAllDto(pageable);
        return ResponseEntity.ok(locations);
    }

    @Operation(summary = "Создать локацию (только ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDto> create(@RequestBody LocationDto request) {
        LocationDto created = service.createLocation(request.getAddress());
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Удалить локацию (только ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Импорт файла с прогрессом (только ADMIN)")
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter importFile(@RequestParam("file") MultipartFile file) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
                service.importAddresses(bytes, filename, emitter);
                return emitter;
            }
            emitter.completeWithError(new RuntimeException("Файл пустой"));
            return emitter;
        } catch (Exception e) {
            emitter.completeWithError(e);
            return emitter;
        }
    }
}