package site.gukho.learn.controller;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvcTester mvc;

    private String loginAs(String username, String password) {
        return mvc.post().uri("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"%s\", \"password\": \"%s\"}".formatted(username, password))
                .exchange()
                .getResponse()
                .getCookie("access_token")      // 본문 JSON 대신 응답 쿠키에서 꺼낸다
                .getValue();
    }

    // 근거: 토큰 없이 보호된 API 를 부르면 401 과 Bearer 방식 안내가 와야 한다
    @Test
    void without_token_is_401() {
        assertThat(mvc.get().uri("/api/hello"))
                .hasStatus(401)
                .headers().hasHeaderSatisfying("WWW-Authenticate", values -> assertThat(values.getFirst()).startsWith("Bearer"));
    }

    // 근거: 로그인으로 받은 쿠키의 실제 토큰이 쿠키→헤더 필터, JwtDecoder, roles 변환기를 거쳐 사용자 이름과 역할이 된다.
    // jwt() 로 만든 가짜 사용자는 이 부품들을 건너뛰므로, 실제 토큰으로 확인하는 테스트가 하나는 있어야 한다
    @Test
    void with_login_token_is_200() {
        String token = loginAs("admin", "admin-pass");
        assertThat(mvc.get().uri("/api/hello").cookie(new Cookie("access_token", token)))
                .hasStatusOk()
                .bodyText().startsWith("hello admin").contains("ROLE_ADMIN", "ROLE_USER");
    }

    // 근거: 서명이 맞지 않는 토큰은 401 이어야 한다 (payload 위조 방지)
    @Test
    void tampered_token_is_401() {
        String token = loginAs("admin", "admin-pass");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(mvc.get().uri("/api/hello").cookie(new Cookie("access_token", tampered)))
                .hasStatus(401);
    }
}
