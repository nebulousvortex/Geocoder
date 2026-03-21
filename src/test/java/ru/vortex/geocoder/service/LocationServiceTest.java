package ru.vortex.geocoder.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import ru.vortex.geocoder.dto.LocationDto;
import ru.vortex.geocoder.model.Location;
import ru.vortex.geocoder.model.Status;
import ru.vortex.geocoder.repository.LocationRepository;
import ru.vortex.geocoder.repository.StatusRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository repository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private LocationService service;

    private Location location;
    private Status status;

    @BeforeEach
    void setUp() {
        status = new Status("В процессе", "#FFA500");
        location = new Location();
        location.setId(1L);
        location.setAddress("Тестовый адрес");
        location.setStatus(status);
    }

    @Test
    void findAll_shouldReturnAllLocations() {
        when(repository.findAll()).thenReturn(List.of(location));
        List<Location> result = service.findAll();
        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findAllDto_shouldReturnPagedDto() {
        Page<Location> page = new PageImpl<>(List.of(location));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);
        Page<LocationDto> result = service.findAllDto(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void search_shouldReturnPagedDtoWithSpecification() {
        Page<Location> page = new PageImpl<>(List.of(location));
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        Page<LocationDto> result = service.search(mock(Specification.class), PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void save_shouldPersistNewLocationAndTriggerAsyncGeocode() {
        when(repository.findByAddress(anyString())).thenReturn(Optional.empty());
        when(statusRepository.findByName("В процессе")).thenReturn(Optional.of(status));
        when(repository.save(any(Location.class))).thenReturn(location);
        Location result = service.save(location);
        assertNotNull(result);
        verify(repository, times(1)).save(any(Location.class));
        verify(statusRepository, times(1)).findByName("В процессе");
    }

    @Test
    void createLocation_shouldReturnDtoAfterSave() {
        when(repository.findByAddress(anyString())).thenReturn(Optional.empty());
        when(statusRepository.findByName("В процессе")).thenReturn(Optional.of(status));
        when(repository.save(any(Location.class))).thenReturn(location);
        LocationDto result = service.createLocation("Новый тестовый адрес");
        assertNotNull(result);
        assertEquals("Тестовый адрес", result.getAddress());
    }

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        doNothing().when(repository).deleteById(1L);
        service.deleteById(1L);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void update_shouldTriggerGeocodeWhenAddressChanged() {
        Location updated = new Location();
        updated.setAddress("Москва, Загородное ш., 2");
        when(repository.findById(1L)).thenReturn(Optional.of(location));
        when(statusRepository.findByName("В процессе")).thenReturn(Optional.of(status));
        doNothing().when(repository).save(any(Location.class));
        service.update(1L, updated);
        verify(repository, times(1)).save(any(Location.class));
    }
}