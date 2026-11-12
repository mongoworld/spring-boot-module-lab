package site.gukho.learn.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reading")
public class ReadingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double temperature;

    @Column(nullable = false)
    private double humidity;

    protected ReadingEntity() {
    }

    public ReadingEntity(double temperature, double humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
    }

    public Long getId() { return id; }
    public double getTemperature() { return temperature; }
    public double getHumidity() { return humidity; }

    public void changeHumidity(double humidity) { this.humidity = humidity; }

    public Reading toReading() {
        return new Reading(id, temperature, humidity);
    }
}
