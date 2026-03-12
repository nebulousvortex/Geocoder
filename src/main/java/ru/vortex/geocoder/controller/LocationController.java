package ru.vortex.geocoder.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vortex.geocoder.model.Location;
import ru.vortex.geocoder.service.LocationService;

@Controller
@RequestMapping("/locations")
public class LocationController {
    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        return "redirect:/locations?page=0&size=7";
    }

    @GetMapping(params = "page")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            Model model
    ) {
        if (page < 0) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Location> locationsPage = service.findAll(pageable);
        model.addAttribute("locationsPage", locationsPage);
        return "locations";
    }

    @PostMapping
    public String create(@ModelAttribute Location location) {
        service.save(location);
        return "redirect:/locations";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/locations";
    }

    @GetMapping("/status/init")
    public String initStatuses() {
        service.createDefaultStatuses();
        return "redirect:/locations";
    }

    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
                service.importAddresses(bytes, filename);
            }
        } catch (Exception e) {
        }
        return "redirect:/locations";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Location location) {
        service.update(id, location);
        return "redirect:/locations";
    }
}