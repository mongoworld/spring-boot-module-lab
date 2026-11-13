package site.gukho.learn.auth;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // AccessTokenCookieFilter 가 같은 이름으로 쿠키를 읽는다
    public static final String ACCESS_COOKIE = "access_token";

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    // 화면이 처음 열릴 때 부른다. 응답에 XSRF-TOKEN 쿠키가 실려 로그인 POST 에 쓸 값을 받는다
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        TokenResponse token = tokenService.issue(authentication);
        // 토큰을 본문 대신 HttpOnly 쿠키로 내려 JavaScript 가 읽지 못하게 한다
        ResponseCookie cookie = accessCookie(token.accessToken(), Duration.ofSeconds(token.expiresIn()));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    // HttpOnly 쿠키는 JavaScript 가 지울 수 없으므로 서버가 같은 이름·경로로 Max-Age 0 을 보내 지운다
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = accessCookie("", Duration.ZERO);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    private static ResponseCookie accessCookie(String value, Duration maxAge) {
        return ResponseCookie.from(ACCESS_COOKIE, value)
                .httpOnly(true)      // JavaScript 가 읽을 수 없다 (XSS 로 토큰을 꺼내 가지 못함)
                .sameSite("Lax")     // 다른 사이트에서 시작한 POST 에는 쿠키를 붙이지 않는다
                .path("/api")        // API 요청에만 쿠키를 보낸다
                .maxAge(maxAge)      // 토큰 수명과 같게 둔다
                .build();
    }
}
