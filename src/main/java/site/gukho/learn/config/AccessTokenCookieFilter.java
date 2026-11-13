package site.gukho.learn.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import site.gukho.learn.auth.AuthController;

/**
 * access_token 쿠키의 값을 Authorization: Bearer 헤더로 옮겨 resource server 가 그대로 검사하게 한다.
 * resource server 는 토큰을 찾은 요청을 CSRF 검사에서 빼므로, 토큰을 쿠키에서 직접 찾게 하면 쿠키가 붙은 요청이
 * 모두 CSRF 검사를 건너뛴다. 그래서 CsrfFilter 뒤에서 옮겨, CSRF 검사 때는 쿠키만 있는 요청으로 보이게 한다.
 */
public class AccessTokenCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, AuthController.ACCESS_COOKIE);
        // 쿠키가 없거나, 이미 Authorization 헤더가 있으면 손대지 않는다
        if (cookie == null || cookie.getValue().isBlank() || request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            chain.doFilter(request, response);
            return;
        }
        String bearer = "Bearer " + cookie.getValue();
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name) ? bearer : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)
                        ? Collections.enumeration(Collections.singletonList(bearer)) : super.getHeaders(name);
            }
        }, response);
    }
}
