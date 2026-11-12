package site.gukho.learn.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import site.gukho.learn.model.CreateReadingRequest;
import site.gukho.learn.model.Reading;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ReadingRepository.class)
public class ReadingRepositoryTest {
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
