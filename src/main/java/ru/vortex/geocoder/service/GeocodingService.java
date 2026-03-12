package ru.vortex.geocoder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Service
public class GeocodingService {
    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${geocode.api.key}")
    private String apiKey;

    public GeocodingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public record GeoResult(double latitude, double longitude, String displayName, String country, String city) {}

    public Optional<GeoResult> geocode(String address) {
        try {
            String normalizedAddress = normalizeAddress(address);
            String url = "https://geocode.maps.co/search?q=" + normalizedAddress + "&api_key=" + apiKey;

            log.info("Geocoding request for address: {}", address);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.isArray() && !root.isEmpty()) {
                    JsonNode first = root.get(0);
                    double lat = first.get("lat").asDouble();
                    double lon = first.get("lon").asDouble();
                    String displayName = first.get("display_name").asText();

                    JsonNode addressNode = first.get("address");
                    String country = (addressNode != null && addressNode.has("country")) ? addressNode.get("country").asText() : "";
                    String city = (addressNode != null && addressNode.has("city")) ? addressNode.get("city").asText() : "";

                    return Optional.of(new GeoResult(lat, lon, displayName, country, city));
                } else {
                    log.warn("No results found for address: {}", address);
                    return Optional.empty();
                }
            } else {
                log.error("Geocoding request failed with status: {}", response.getStatusCode());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error during geocoding for address: {}", address, e);
            return Optional.empty();
        }
    }

    private String normalizeAddress(String address) {
        if (address == null) {
            return "";
        }

        String result = address.trim();
        result = result.replaceAll("/\\S*", "");
        result = result.replaceAll("(?i)\\s*(дом|д\\.|д\\s+)", " ");
        result = result.replaceAll("(?i)\\s*(корпус|к\\.|к\\s+)([\\d\\w]+)", " к$2");
        result = result.replaceAll("(?i)\\s*(строение|стр\\.?|стр\\s+|с\\.?|с\\s+)\\s*([\\d\\w]+)", " с$2");
        result = result.replaceAll("(?i)\\s*(литера|лит\\.|литера\\s+)([А-Яа-яA-Za-z\\d]+)", " лит.$2");
        result = result.replaceAll("(?i)\\s*(квартира|кв\\.|кв\\s+)[\\d\\w-]*", "");
        result = result.replaceAll("[,;]", " ");
        result = result.replaceAll("\\s+", " ");
        result = result.replaceAll("(\\d)\\s+([кс])", "$1$2");
        result = result.replaceAll("([кс])\\s+(\\d)", "$1$2");

        return result.trim();
    }
}