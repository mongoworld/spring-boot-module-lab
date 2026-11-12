package site.gukho.learn.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
