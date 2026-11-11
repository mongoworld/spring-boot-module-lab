package site.gukho.learn.reading;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
public class ReadingController {
    List<Reading> list = new ArrayList<>(List.of(
            new Reading(1L, 21.5, 48),
            new Reading(2L, 19.0, 62)
    ));
    @GetMapping("/api/readings")
    public List<Reading> readings(){
        return list;
    }
    @GetMapping("/api/readings/{id}")
    public Reading reading(@PathVariable Long id){
        return list.stream().filter(reading -> reading.id().equals(id)).findFirst().orElse(null);
    }

    @PostMapping("/api/readings")
    public Reading saveReading(@RequestBody CreateReadingRequest request) {
        long newId = list.stream()
                .mapToLong(Reading::id)
                .max()
                .orElse(0L) + 1;
        Reading newReading = new Reading(newId, request.temperature(), request.humidity());
        list.add(newReading);
        return newReading;
        
    }
}
