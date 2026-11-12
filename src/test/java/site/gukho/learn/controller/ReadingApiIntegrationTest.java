package site.gukho.learn.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;
import site.gukho.learn.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

// Controller → Service → Repository → DB 를 한 번에 지나가는 테스트.
// @Transactional: 테스트마다 저장한 데이터를 되돌려서 다른 테스트의 결과에 영향을 주지 않게 한다.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ReadingApiIntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    // 근거: 저장 API 가 DB 가 만든 id 로 Location 을 주고, 그 주소로 같은 값을 다시 읽을 수 있어야 한다
    @Test
    void post_then_get() {
        MvcTestResult created = mvc.post().uri("/api/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"temperature": 21.5, "humidity": 48}
                        """)
                .exchange();

        assertThat(created).hasStatus(201);
        String location = created.getResponse().getHeader("Location");
        System.out.println("Location = " + location);

        assertThat(mvc.get().uri(location))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"temperature": 21.5, "humidity": 48.0}
                        """);
    }

    // 근거: DB 에 없는 id 는 Service 의 ReadingNotFoundException 을 거쳐 404 가 되어야 한다
    @Test
    void get_missing_returns_404() {
        assertThat(mvc.get().uri("/api/readings/{id}", 999_999))
                .hasStatus(404);
    }

    // 근거: 검증에 실패한 요청은 400 으로 막히고 DB 에 저장되지 않아야 한다.
    // 목록이 비어 있는 것은 다른 테스트의 저장이 @Transactional 로 되돌려졌다는 확인이기도 하다
    @Test
    void invalid_post_is_rejected_and_not_saved() {
        assertThat(mvc.post().uri("/api/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"temperature": 50, "humidity": 48}
                        """))
                .hasStatus(400);

        assertThat(mvc.get().uri("/api/readings"))
                .hasStatusOk()
                .bodyJson().extractingPath("$").asArray().isEmpty();
    }
}
