package site.gukho.learn.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.gukho.learn.exception.ReadingNotFoundException;
import site.gukho.learn.model.CreateReadingRequest;
import site.gukho.learn.model.Reading;
import site.gukho.learn.repository.ReadingRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Spring 과 DB 없이 Service 의 판단만 확인한다.
// Repository 는 Mockito 가짜 객체로 바꿔서, "DB 가 이렇게 답했다면" 을 테스트가 정한다.
@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private ReadingRepository readingRepository;

    @InjectMocks
    private ReadingService readingService;

    // 근거: Repository 가 찾은 측정값을 Service 가 그대로 돌려줘야 한다
    @Test
    void findReading_returns_found_reading() {
        Reading reading = new Reading(1L, 21.5, 48);
        when(readingRepository.findById(1L)).thenReturn(Optional.of(reading));

        assertThat(readingService.findReading(1L)).isEqualTo(reading);
    }

    // 근거: 없는 id 를 404 로 바꾸는 판단은 Service 의 몫이다.
    // 통합 테스트에서 orElse(null) 실수를 잡았던 규칙을, 앱 전체 없이 빠르게 확인한다
    @Test
    void findReading_throws_when_missing() {
        when(readingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingService.findReading(99L))
                .isInstanceOf(ReadingNotFoundException.class)
                .hasMessage("측정값이 없습니다: 99");
    }

    // 근거: 저장은 받은 요청을 Repository 에 그대로 넘기고, Repository 가 돌려준 결과(DB 가 만든 id 포함)를 돌려줘야 한다
    @Test
    void saveReading_passes_request_to_repository() {
        CreateReadingRequest request = new CreateReadingRequest(21.5, 48);
        Reading saved = new Reading(7L, 21.5, 48);
        when(readingRepository.save(request)).thenReturn(saved);

        assertThat(readingService.saveReading(request)).isEqualTo(saved);
        verify(readingRepository).save(request);
    }

    // 근거: 조회만 하는 메서드가 저장을 부르면 안 된다 (verify 로 "부르지 않았음" 을 확인하는 예)
    @Test
    void findReading_does_not_save() {
        when(readingRepository.findById(1L)).thenReturn(Optional.of(new Reading(1L, 21.5, 48)));

        readingService.findReading(1L);

        verify(readingRepository, never()).save(any());
    }
}
