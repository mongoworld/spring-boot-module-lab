package site.gukho.learn.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import site.gukho.learn.model.CreateReadingRequest;
import site.gukho.learn.model.Reading;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Testcontainers
@Import(ReadingRepository.class)
public class ReadingRepositoryTest {

    // 개발용 DB(learn-pg)와 같은 이미지로, 테스트마다 비어 있는 전용 DB 를 띄운다
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6");

    @Autowired
    private ReadingRepository repository;

    // 근거: returning 으로 받은 행이 실제 저장된 행과 같은지 (DB 가 만든 id 포함)
    @Test
    void save_then_find() {
        Reading saved = repository.save(new CreateReadingRequest(21.5, 48));
        System.out.println("saved = " + saved);
        System.out.println("findAll = " + repository.findAll());
        System.out.println("findById = " + repository.findById(saved.id()));
        assertThat(repository.findById(saved.id())).contains(saved);
    }
}
