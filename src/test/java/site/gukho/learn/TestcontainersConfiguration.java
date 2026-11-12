package site.gukho.learn;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

// 앱 전체를 올리는 테스트(@SpringBootTest)가 learn-pg 대신 쓰는 전용 DB.
// 컨테이너는 Spring 컨텍스트마다 하나씩 뜬다. 어노테이션 구성이 같은 테스트끼리는 컨텍스트와 컨테이너를 함께 쓴다.
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:18.6");
    }
}
