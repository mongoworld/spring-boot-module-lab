package site.gukho.learn.error;

import java.util.List;

/**
 * 모든 오류 응답의 공통 양식.
 * 프론트엔드가 상태 코드가 아닌 code 로 화면 문구를 고르고, errors 로 입력칸별 오류를 표시할 수 있게 한다.
 * errors 는 칸별 오류가 없어도 null 대신 빈 목록으로 보내 프론트엔드가 null 검사를 하지 않게 한다.
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldError> errors) {

    /** 입력칸 하나의 오류. field 는 요청 JSON 의 키 이름과 같다. */
    public record FieldError(String field, String reason) {
    }

    /** 칸별 오류가 없는 응답. */
    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, List.of());
    }
}
