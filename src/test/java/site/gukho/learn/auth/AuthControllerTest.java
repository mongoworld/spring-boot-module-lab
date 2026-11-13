package site.gukho.learn.auth;

import java.time.Duration;

import jakarta.servlet.http.Cookie;
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
class AuthControllerTest {

    @Autowired
    private MockMvcTester mvc;

    // 근거: 맞는 아이디와 비밀번호면 토큰을 본문이 아니라 HttpOnly 쿠키로 내려야 JavaScript 가 읽지 못한다
    @Test
    void login_sets_http_only_cookie() {
        assertThat(mvc.post().uri("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "admin", "password": "admin-pass"}
                        """))
                .hasStatus(204)
                .cookies()
                .isHttpOnly("access_token", true)
                .hasPath("access_token", "/api")
                .hasMaxAge("access_token", Duration.ofMinutes(30))
                .hasCookieSatisfying("access_token",
                        cookie -> assertThat(cookie.getValue()).matches("[^.]+\\.[^.]+\\.[^.]+"));
    }

    // 근거: 비밀번호가 틀리면 쿠키 없이 401 이어야 한다
    @Test
    void wrong_password_is_401() {
        assertThat(mvc.post().uri("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "admin", "password": "wrong"}
                        """))
                .hasStatus(401)
                .cookies().doesNotContainCookie("access_token");
    }

    // 근거: CSRF 값 없이 오는 POST 는 다른 사이트가 보낸 요청일 수 있으므로 로그인도 403 이어야 한다
    @Test
    void login_without_csrf_is_403() {
        assertThat(mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "admin", "password": "admin-pass"}
                        """))
                .hasStatus(403);
    }

    // 근거: 로그인 쿠키가 붙어 있어도 CSRF 값이 없으면 403 이어야 한다.
    // resource server 는 토큰을 찾은 요청을 CSRF 검사에서 빼므로, 쿠키에서 토큰을 바로 찾게 만들면 이 요청이 통과해 버린다
    @Test
    void cookie_without_csrf_is_403() {
        Cookie cookie = new Cookie("access_token", login());

        assertThat(mvc.post().uri("/api/auth/logout").cookie(cookie))
                .hasStatus(403);
    }

    // 근거: HttpOnly 쿠키는 JavaScript 가 지울 수 없으므로 로그아웃 응답이 Max-Age 0 쿠키로 지워야 한다
    @Test
    void logout_clears_cookie() {
        Cookie cookie = new Cookie("access_token", login());

        assertThat(mvc.post().uri("/api/auth/logout").cookie(cookie).with(csrf()))
                .hasStatus(204)
                .cookies()
                .hasValue("access_token", "")
                .hasMaxAge("access_token", Duration.ZERO);
    }

    private String login() {
        return mvc.post().uri("/api/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "admin", "password": "admin-pass"}
                        """)
                .exchange()
                .getResponse()
                .getCookie("access_token")
                .getValue();
    }
}
