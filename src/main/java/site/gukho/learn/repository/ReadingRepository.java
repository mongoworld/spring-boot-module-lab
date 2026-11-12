package site.gukho.learn.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import site.gukho.learn.model.CreateReadingRequest;
import site.gukho.learn.model.Reading;

import java.util.List;
import java.util.Optional;

@Repository
public class ReadingRepository {
    private final JdbcClient jdbcClient;

    public ReadingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Reading> findAll() {
        return jdbcClient.sql("""
                select id, temperature, humidity
                from reading
                order BY id
                """)
                .query(Reading.class)
                .list();
    }

    public Optional<Reading> findById(Long id) {
        return jdbcClient.sql("""
                select id, temperature, humidity
                from reading
                where id = :id
                """)
                .param("id",id)
                .query(Reading.class)
                .optional();
    }

    public Reading save(CreateReadingRequest request) {
        return jdbcClient.sql("""
                insert into reading (temperature, humidity)
                values(:temperature, :humidity)
                returning id, temperature, humidity
                """)
                .param("temperature", request.temperature())
                .param("humidity", request.humidity())
                .query(Reading.class)
                .single();
    }
}
