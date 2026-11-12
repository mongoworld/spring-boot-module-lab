package site.gukho.learn.service;

import org.springframework.stereotype.Service;
import site.gukho.learn.exception.ReadingNotFoundException;
import site.gukho.learn.model.CreateReadingRequest;
import site.gukho.learn.model.Reading;
import site.gukho.learn.repository.ReadingRepository;

import java.util.List;

@Service
public class ReadingService {
    private final ReadingRepository readingRepository;

    public ReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public List<Reading> findReadings() {
        return readingRepository.findAll();
    }

    public Reading findReading(Long id) {
        return readingRepository.findById(id)
                .orElseThrow(() -> new ReadingNotFoundException(id));
    }

    public Reading saveReading(CreateReadingRequest request) {
        return readingRepository.save(request);
    }
}
