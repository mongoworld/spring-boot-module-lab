package site.gukho.learn.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReadingNotFoundException extends RuntimeException {
    public ReadingNotFoundException(Long id) {
        super("측정값이 없습니다: " + id);
    }
}