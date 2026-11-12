package site.gukho.learn.reading;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readings")
public class ReadingController {
    List<Reading> list = new ArrayList<>(List.of(
            new Reading(1L, 21.5, 48),
            new Reading(2L, 19.0, 62)
    ));
    @GetMapping
    public List<Reading> readings(){
        return list;
    }
    @GetMapping("/{id}")
    public Reading reading(@PathVariable Long id){
        return list.stream()
                .filter(reading -> reading.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ReadingNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Reading> saveReading(@Valid  @RequestBody CreateReadingRequest request) {
        long newId = list.stream()
                .mapToLong(Reading::id)
                .max()
                .orElse(0L) + 1;
        Reading newReading = new Reading(newId, request.temperature(), request.humidity());
        list.add(newReading);
        return ResponseEntity.created(URI.create("/api/readings/"+newId)).body(newReading);
    }
}
