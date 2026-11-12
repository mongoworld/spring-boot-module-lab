package site.gukho.learn.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvcTester mvc;

    // 근거: 맞는 아이디와 비밀번호면 Bearer 토큰을 발급하고, 토큰은 header.payload.signature 세 부분이다
    @Test
    void login_issues_token() {
        assertThat(mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "admin", "password": "admin-pass"}
                        """))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.tokenType").isEqualTo("Bearer");
                    assertThat(json).extractingPath("$.expiresIn").isEqualTo(1800);
                    assertThat(json).extractingPath("$.accessToken").asString().matches("[^.]+\\.[^.]+\\.[^.]+");
                });
    }

    // 근거: 비밀번호가 틀리면 토큰 없이 401 이어야 한다
    @Test
    void wrong_password_is_401() {
        assertThat(mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "admin", "password": "wrong"}
                        """))
                .hasStatus(401);
    }
}

