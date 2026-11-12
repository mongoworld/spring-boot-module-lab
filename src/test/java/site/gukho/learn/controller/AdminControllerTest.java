package site.gukho.learn.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

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

    // 근거: 토큰이 없으면 누구인지 모르므로 403 이 아니라 401 이어야 한다
    @Test
    void without_token_is_401() {
        assertThat(mvc.get().uri("/api/admin/hello"))
                .hasStatus(401);
    }

    // 근거: 로그인했지만 ADMIN 역할이 없으면 401 이 아니라 403 이어야 한다 (누구인지는 알지만 권한이 없음)
    @Test
    void user_role_is_403() throws Exception {
        String token = loginAs("user", "user-pass");

        assertThat(mvc.get().uri("/api/admin/hello").header("Authorization", "Bearer " + token))
                .hasStatus(403);
    }

    // 근거: 토큰의 roles 에 ROLE_ADMIN 이 있으면 관리자 API 를 통과해야 한다
    @Test
    void admin_role_is_200() throws Exception {
        String token = loginAs("admin", "admin-pass");

        assertThat(mvc.get().uri("/api/admin/hello").header("Authorization", "Bearer " + token))
                .hasStatusOk()
                .bodyText().isEqualTo("hello admin page, admin");
    }
}
