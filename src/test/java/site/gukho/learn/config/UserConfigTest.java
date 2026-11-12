package site.gukho.learn.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserConfigTest {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 근거: 비밀번호는 원문이 아니라 BCrypt 해시로 저장되고, 같은 원문도 매번 다른 해시가 되지만 matches 로는 맞아야 한다
    @Test
    void password_is_stored_as_bcrypt_hash() {
        UserDetails user = userDetailsService.loadUserByUsername("user");
        System.out.println("stored    = " + user.getPassword());
        System.out.println("encode #1 = " + passwordEncoder.encode("user-pass"));
        System.out.println("encode #2 = " + passwordEncoder.encode("user-pass"));
        System.out.println("matches(user-pass)  = " + passwordEncoder.matches("user-pass", user.getPassword()));
        System.out.println("matches(wrong-pass) = " + passwordEncoder.matches("wrong-pass", user.getPassword()));

        assertThat(user.getPassword()).startsWith("$2a$10$").isNotEqualTo("user-pass");
        assertThat(passwordEncoder.matches("user-pass", user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("wrong-pass", user.getPassword())).isFalse();
    }

    // 근거: roles("USER", "ADMIN") 은 권한 이름 앞에 ROLE_ 을 붙여 저장한다 (6단계 권한 검사의 기준)
    @Test
    void roles_become_authorities_with_prefix() {
        System.out.println("user  authorities = " + userDetailsService.loadUserByUsername("user").getAuthorities());
        System.out.println("admin authorities = " + userDetailsService.loadUserByUsername("admin").getAuthorities());

        assertThat(userDetailsService.loadUserByUsername("admin").getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    // 근거: 없는 사용자는 null 이 아니라 예외로 알려야 로그인 단계에서 실패로 처리된다
    @Test
    void unknown_user_throws() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
