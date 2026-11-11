package site.gukho.learn.reading;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CreateReadingRequest(
        @DecimalMin("10")
        @DecimalMax("30")
        double temperature,
        @DecimalMin("0")
        @DecimalMax("100")
        double humidity) {
}
