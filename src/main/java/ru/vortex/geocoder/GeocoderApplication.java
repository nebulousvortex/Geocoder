package ru.vortex.geocoder;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@OpenAPIDefinition(
		info = @Info(
				title = "Geocoder API",
				version = "1.0",
				description = "REST API для геокодирования локаций и построения маршрутов"
		)
)
public class GeocoderApplication {
	public static void main(String[] args) {
		SpringApplication.run(GeocoderApplication.class, args);
	}
}