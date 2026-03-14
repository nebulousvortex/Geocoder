package ru.vortex.geocoder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vortex.geocoder.dto.RouteRequestDto;
import ru.vortex.geocoder.dto.RouteResponseDto;
import ru.vortex.geocoder.service.RouteService;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
@Tag(name = "Маршруты")
public class RouteRestController {
    private final RouteService routeService;

    public RouteRestController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Operation(summary = "Построить маршрут (USER + ADMIN)",
            security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RouteResponseDto> buildRoute(@RequestBody RouteRequestDto request) {
        RouteResponseDto response = routeService.buildRoute(request);
        return ResponseEntity.ok(response);
    }
}