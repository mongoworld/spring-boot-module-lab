package site.gukho.learn.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvcTester mvc;

    private String loginAs(String username, String password) throws Exception {
        String body = mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"%s\", \"password\": \"%s\"}".formatted(username, password))
                .exchange()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    // 근거: 토큰 없이 보호된 API 를 부르면 401 과 Bearer 방식 안내가 와야 한다
    @Test
    void without_token_is_401() {
        assertThat(mvc.get().uri("/api/hello"))
                .hasStatus(401)
                .headers().hasHeaderSatisfying("WWW-Authenticate", values -> assertThat(values.getFirst()).startsWith("Bearer"));
    }

    // 근거: 로그인으로 받은 토큰을 붙이면 통과하고, 토큰의 sub 가 로그인한 사용자 이름이 된다
    @Test
    void with_login_token_is_200() throws Exception {
        String token = loginAs("admin", "admin-pass");

        assertThat(mvc.get().uri("/api/hello").header("Authorization", "Bearer " + token))
                .hasStatusOk()
                .bodyText().startsWith("hello admin").contains("ROLE_ADMIN", "ROLE_USER");

    }

    // 근거: 서명이 맞지 않는 토큰은 401 이어야 한다 (payload 위조 방지)
    @Test
    void tampered_token_is_401() throws Exception {
        String token = loginAs("admin", "admin-pass");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(mvc.get().uri("/api/hello").header("Authorization", "Bearer " + tampered))
                .hasStatus(401);
    }
}
