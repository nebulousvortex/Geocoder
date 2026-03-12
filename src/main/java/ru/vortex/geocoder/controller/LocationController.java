package ru.vortex.geocoder.controller;

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
        model.addAttribute("locations", service.findAll());
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
}