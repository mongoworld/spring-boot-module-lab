package site.gukho.learn.reading;

/**
 * 요청한 번호의 측정값이 없음.
 * 예외 종류 자체가 "측정값이 없다" 를 뜻하게 해서, 처리기가 오류 코드를 READING_NOT_FOUND 로 정할 수 있게 한다.
 * orElseThrow 의 람다 안에서 던지기 때문에 throws 선언이 필요 없는 RuntimeException 을 상속한다.
 */
public class ReadingNotFoundException extends RuntimeException {

    public ReadingNotFoundException(Long id) {
        super("측정값이 없습니다: " + id);
    }
}
