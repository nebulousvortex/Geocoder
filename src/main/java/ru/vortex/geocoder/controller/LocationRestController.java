package ru.vortex.geocoder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vortex.geocoder.dto.LocationDto;
import ru.vortex.geocoder.dto.SearchRequest;
import ru.vortex.geocoder.model.Location;
import ru.vortex.geocoder.service.LocationService;
import ru.vortex.geocoder.util.GenericSpecificationBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*")
@Tag(name = "Локации")
public class LocationRestController {
    private final LocationService service;

    public LocationRestController(LocationService service) {
        this.service = service;
    }

    @Operation(summary = "Получить список локаций (USER + ADMIN)",
            security = { @SecurityRequirement(name = "bearer-key") })
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

    @Operation(summary = "Поиск локаций с фильтрами (USER + ADMIN)",
            security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<LocationDto>> search(@RequestBody SearchRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Location> spec = GenericSpecificationBuilder.buildSpecification(request, Location.class);
        Page<LocationDto> result = service.search(spec, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Создать локацию (только ADMIN)",
            security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDto> create(@RequestBody LocationDto request) {
        LocationDto created = service.createLocation(request.getAddress());
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Удалить локацию (только ADMIN)",
            security = { @SecurityRequirement(name = "bearer-key") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Импорт файла (только ADMIN)",
            security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> importFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
                service.importAddresses(bytes, filename);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Импорт запущен асинхронно.");
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

    private Pageable createPageable(SearchRequest request) {
        if (request.getSort() != null && !request.getSort().isEmpty()) {
            List<Sort.Order> orders = request.getSort().stream()
                    .map(sort -> {
                        String[] parts = sort.split(",");
                        String field = parts[0];
                        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc")
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, field);
                    })
                    .collect(Collectors.toList());
            return PageRequest.of(request.getPage(), request.getSize(), Sort.by(orders));
        }
        return PageRequest.of(request.getPage(), request.getSize());
    }
}