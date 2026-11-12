package site.gukho.learn.error;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import site.gukho.learn.reading.ReadingNotFoundException;

/**
 * 모든 Controller 에서 던져진 예외를 받아 ErrorResponse 양식으로 바꾼다.
 * 예외마다 응답 모양이 달라지지 않도록 오류 응답을 만드는 곳을 여기 한곳으로 모은다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 요청 본문 검사(@Valid) 실패. 어느 칸이 왜 틀렸는지를 errors 에 담는다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "VALIDATION_FAILED", "입력값을 확인해주세요.", errors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 요청 본문을 읽지 못함 (JSON 문법 오류, 타입 불일치, 기본 타입 칸 누락).
     * 어느 칸인지 알 수 없어 errors 는 비운다. Jackson 의 내부 문구는 손님에게 보내지 않는다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "INVALID_BODY", "요청 형식이 올바르지 않습니다."));
    }

    /**
     * 경로 변수나 요청 파라미터의 타입이 맞지 않음 (예: /api/readings/abc).
     * 이 예외는 Spring 의 ErrorResponse 를 구현하지 않아 아래 handleUnexpected 로 가면 500 이 되므로 따로 받는다.
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(TypeMismatchException e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(), "INVALID_PARAMETER", "요청 값의 형식이 올바르지 않습니다."));
    }

    /** 요청한 번호의 측정값이 없음. */
    @ExceptionHandler(ReadingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReadingNotFound(ReadingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(), "READING_NOT_FOUND", e.getMessage()));
    }

    /**
     * 위에서 받지 않은 나머지 전부.
     * Spring 이 상태를 알고 던진 예외(없는 주소 404, 허용 안 된 메서드 405, 지원하지 않는 Content-Type 415 등)는
     * org.springframework.web.ErrorResponse 를 구현하므로, 500 으로 뭉개지 않고 그 상태를 살려서 응답한다.
     * 그 밖의 예상하지 못한 예외만 500 으로 응답하고, 내부 사정은 로그에만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // Spring 의 ErrorResponse 는 우리 ErrorResponse 와 이름이 같아 전체 이름으로 쓴다.
        if (e instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatusCode status = springError.getStatusCode();
            return ResponseEntity.status(status).body(ErrorResponse.of(
                    status.value(), "REQUEST_FAILED", "요청을 처리할 수 없습니다."));
        }
        log.error("처리하지 못한 예외", e);
        return ResponseEntity.internalServerError().body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR",
                "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
