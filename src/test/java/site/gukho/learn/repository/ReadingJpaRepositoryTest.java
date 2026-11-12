package site.gukho.learn.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import site.gukho.learn.model.Reading;
import site.gukho.learn.model.ReadingEntity;

import static org.assertj.core.api.Assertions.assertThat;

// JdbcClient 로 만든 ReadingRepository 와 같은 일을 Spring Data JPA 로 하면 어떻게 다른지 비교한다.
// show-sql 로 JPA 가 실제로 보내는 SQL 을 같이 확인한다.
@DataJpaTest
@Testcontainers
class ReadingJpaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6");

    @Autowired
    private ReadingJpaRepository repository;

    // 저장한 내용을 DB 로 내보내고(flush) 1차 캐시를 비워서(clear), 다음 조회가 실제 SQL 로 나가게 한다
    @Autowired
    private TestEntityManager em;

    // 근거: save 와 findById 를 메서드 없이 상속만으로 쓸 수 있고, DB 가 만든 id 가 채워져야 한다
    @Test
    void save_then_find() {
        ReadingEntity saved = repository.save(new ReadingEntity(21.5, 48));
        em.flush();
        em.clear();

        Reading found = repository.findById(saved.getId()).orElseThrow().toReading();
        System.out.println("found = " + found);

        assertThat(found).isEqualTo(new Reading(saved.getId(), 21.5, 48));
    }

    // 근거: 메서드 이름만으로 where, order by 가 만들어지는지 (JdbcClient 에서는 SQL 을 직접 썼던 부분)
    @Test
    void derived_query_from_method_name() {
        repository.save(new ReadingEntity(21.5, 48));
        repository.save(new ReadingEntity(19.0, 62));

        var humid = repository.findByHumidityGreaterThanEqualOrderById(60);
        System.out.println("humid = " + humid.stream().map(ReadingEntity::toReading).toList());

        assertThat(humid).extracting(ReadingEntity::getHumidity).containsExactly(62.0);
    }

    // 근거: 조회한 엔티티의 값을 바꾸기만 해도 save 없이 update 가 나가는지 (변경 감지, JdbcClient 에는 없는 동작)
    @Test
    void change_is_saved_without_calling_save() {
        Long id = repository.save(new ReadingEntity(21.5, 48)).getId();
        em.flush();
        em.clear();

        ReadingEntity found = repository.findById(id).orElseThrow();
        found.changeHumidity(55);
        em.flush();
        em.clear();

        assertThat(repository.findById(id).orElseThrow().getHumidity()).isEqualTo(55.0);
    }
}
