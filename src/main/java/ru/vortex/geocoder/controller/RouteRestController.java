package ru.vortex.geocoder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vortex.geocoder.dto.RouteRequestDto;
import ru.vortex.geocoder.dto.RouteResponseDto;
import ru.vortex.geocoder.service.RouteService;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteRestController {
    private final RouteService routeService;

    public RouteRestController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ResponseEntity<RouteResponseDto> buildRoute(@RequestBody RouteRequestDto request) {
        RouteResponseDto response = routeService.buildRoute(request);
        return ResponseEntity.ok(response);
    }
}