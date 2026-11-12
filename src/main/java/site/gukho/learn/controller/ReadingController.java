package site.gukho.learn.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.gukho.learn.model.CreateReadingRequest;
import site.gukho.learn.model.Reading;
import site.gukho.learn.service.ReadingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping
    public List<Reading> findReadings() {
        return readingService.findReadings();
    }

    @GetMapping("/{id}")
    public Reading findReading(@PathVariable Long id) {
        return readingService.findReading(id);
    }

    @PostMapping
    public ResponseEntity<Reading> saveReading(@Valid @RequestBody CreateReadingRequest request) {
        Reading saved = readingService.saveReading(request);
        return ResponseEntity.created(URI.create("/api/readings/" + saved.id())).body(saved);
    }
}
