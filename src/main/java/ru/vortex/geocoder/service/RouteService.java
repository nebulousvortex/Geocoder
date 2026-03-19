package ru.vortex.geocoder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import ru.vortex.geocoder.dto.RouteRequestDto;
import ru.vortex.geocoder.dto.RouteResponseDto;
import ru.vortex.geocoder.model.Location;
import ru.vortex.geocoder.repository.LocationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {
    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    private final LocationRepository locationRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RouteService(LocationRepository locationRepository, ObjectMapper objectMapper) {
        this.locationRepository = locationRepository;
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public RouteResponseDto buildRoute(RouteRequestDto request) {
        if (request.getLocationIds() == null || request.getLocationIds().length < 2) {
            RouteResponseDto error = new RouteResponseDto();
            error.setStatus("ERROR");
            error.setSteps(List.of("Нужно минимум 2 локации"));
            return error;
        }

        List<Location> locations = new ArrayList<>();
        for (Long id : request.getLocationIds()) {
            Optional<Location> loc = locationRepository.findById(id);
            loc.ifPresent(locations::add);
        }

        if (locations.size() < 2) {
            RouteResponseDto error = new RouteResponseDto();
            error.setStatus("ERROR");
            error.setSteps(List.of("Не все локации найдены или не имеют координат"));
            return error;
        }

        StringBuilder coords = new StringBuilder();
        for (Location loc : locations) {
            if (loc.getLatitude() == null || loc.getLongitude() == null) {
                RouteResponseDto error = new RouteResponseDto();
                error.setStatus("ERROR");
                error.setSteps(List.of("Одна из локаций без координат"));
                return error;
            }
            coords.append(loc.getLongitude()).append(",").append(loc.getLatitude()).append(";");
        }
        String url = "https://router.project-osrm.org/route/v1/driving/" +
                coords.substring(0, coords.length() - 1) +
                "?overview=full&geometries=geojson&steps=true&alternatives=false";

        try {
            String responseBody = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);

            if (!"Ok".equals(root.path("code").asText())) {
                RouteResponseDto error = new RouteResponseDto();
                error.setStatus("ERROR");
                error.setSteps(List.of("OSRM вернул ошибку"));
                return error;
            }

            JsonNode route = root.path("routes").get(0);
            double distance = route.path("distance").asDouble();
            double duration = route.path("duration").asDouble();
            String geometry = route.path("geometry").toString();

            List<String> steps = new ArrayList<>();
            JsonNode legs = route.path("legs");
            for (JsonNode leg : legs) {
                JsonNode stepNodes = leg.path("steps");
                for (JsonNode step : stepNodes) {
                    steps.add(step.path("name").asText() + " (" + step.path("distance").asDouble() + "м)");
                }
            }

            RouteResponseDto result = new RouteResponseDto();
            result.setDistance(distance);
            result.setDuration(duration);
            result.setGeometry(geometry);
            result.setSteps(steps);
            result.setStatus("OK");
            return result;
        } catch (Exception e) {
            log.error("OSRM routing error", e);
            RouteResponseDto error = new RouteResponseDto();
            error.setStatus("ERROR");
            error.setSteps(List.of("Ошибка связи с OSRM"));
            return error;
        }
    }
}