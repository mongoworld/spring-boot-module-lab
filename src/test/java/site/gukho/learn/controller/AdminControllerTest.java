package site.gukho.learn.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

// 권한 규칙만 확인한다. jwt() 는 로그인과 JwtDecoder, roles 변환기를 건너뛰므로
// 그 부분은 HelloControllerTest 의 실제 토큰 테스트가 확인한다
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvcTester mvc;

    // 근거: 토큰이 없으면 누구인지 모르므로 403 이 아니라 401 이어야 한다
    @Test
    void without_token_is_401() {
        assertThat(mvc.get().uri("/api/admin/hello"))
                .hasStatus(401);
    }

    // 근거: 로그인했지만 ADMIN 역할이 없으면 401 이 아니라 403 이어야 한다 (누구인지는 알지만 권한이 없음)
    @Test
    void user_role_is_403() {
        assertThat(mvc.get().uri("/api/admin/hello")
                .with(jwt().jwt(token -> token.subject("user"))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .hasStatus(403);
    }

    // 근거: ROLE_ADMIN 권한이 있으면 관리자 API 를 통과해야 한다
    @Test
    void admin_role_is_200() {
        assertThat(mvc.get().uri("/api/admin/hello")
                .with(jwt().jwt(token -> token.subject("admin"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .hasStatusOk()
                .bodyText().isEqualTo("hello admin page, admin");
    }
}
