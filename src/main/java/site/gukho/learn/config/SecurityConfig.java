package site.gukho.learn.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 토큰을 Authorization 헤더로 보내므로 브라우저가 자동으로 붙이는 쿠키를 노린 CSRF 공격 대상이 아니다
            .csrf(AbstractHttpConfigurer::disable)
            // 로그인은 JSON API 로 직접 만들므로 기본 /login 화면과 그 로그인 필터를 쓰지 않는다
            .formLogin(AbstractHttpConfigurer::disable)
            // 요청마다 아이디·비밀번호를 보내는 Basic 인증 대신 토큰을 보낸다
            .httpBasic(AbstractHttpConfigurer::disable)
            // JWT 가 요청마다 누구인지 증명하므로 서버가 세션(JSESSIONID)에 로그인 상태를 저장하지 않는다
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Authorization: Bearer 헤더의 JWT 를 JwtDecoder 로 검사해서 로그인 사용자로 인정한다
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            // 주소별 허용 규칙. 위에서부터 처음 맞는 규칙이 적용되므로 anyRequest 는 맨 아래에 둔다
            .authorizeHttpRequests(auth -> auth
                    // 404 같은 오류 응답을 만들려고 /error 로 가는 내부 이동이 막히면 원래 오류가 401 로 가려진다
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    // 로그인 API 는 토큰을 받기 전에 불러야 하므로 누구나 허용한다
                    .requestMatchers("/api/auth/**").permitAll()
                    // 관리자 API 는 토큰의 roles 에 ROLE_ADMIN 이 있어야 한다
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // 그 밖의 모든 요청은 로그인(유효한 토큰)이 필요하다
                    .anyRequest().authenticated());
        // 위 설정대로 필터들을 순서에 맞게 조립해 하나의 필터 체인으로 만든다
        return http.build();
    }
}
