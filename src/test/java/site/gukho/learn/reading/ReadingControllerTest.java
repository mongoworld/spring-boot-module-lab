package site.gukho.learn.reading;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ReadingController.class)
public class ReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void 번호로_측정값_하나_조회하기() throws Exception {
        mockMvc.perform(get("/api/readings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.humidity").value(48.0));
    }

    @Test
    void 없는번호는_404와_ReadingNotFound() throws Exception {
//        mockMvc.perform(get("/api/readings/99"))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.code").value("READING_NOT_FOUND"))
//                .andExpect(jsonPath("$.message").value("측정값이 없습니다: 99"))
//                .andExpect(jsonPath("$.errors").isEmpty());
        assertThat(mockMvcTester.get()
                .uri("/api/readings/99"))
                .debug()
                .hasStatus(404)
                .bodyJson()
                .extractingPath("$.code")
                .isEqualTo("READING_NOT_FOUND"
                );
    }

    // 근거: 2단계-2 검증. @Valid 를 엉뚱한 메서드에 붙였을 때 습도 150 이 201 로 저장된 일이 있어, 막히는지 확인한다.
    @Test
    void 습도_150은_400과_칸별_오류() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"temperature\":20.1,\"humidity\":150}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("humidity"));
    }

    // 근거: 2단계-1 등록. 새로 만든 결과는 201 과 새 주소(Location)로 응답해야 한다.
    @Test
    void 등록은_201과_Location() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"temperature\":20.1,\"humidity\":55}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/readings/3"))
                .andExpect(jsonPath("$.id").value(3));
    }

    // 근거: 2단계-3 읽기 실패. JSON 을 읽지 못한 400 은 검사 실패(VALIDATION_FAILED)와 code 로 구분되어야 한다.
    @Test
    void 깨진_JSON은_400과_INVALID_BODY() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"temperature\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_BODY"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    // 근거: 2단계-3 함정 2. 숫자 자리에 글자를 넣으면 500 으로 뭉개졌던 일이 있어, 400 으로 나가는지 확인한다.
    @Test
    void 번호_자리에_글자는_400과_INVALID_PARAMETER() throws Exception {
        mockMvc.perform(get("/api/readings/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }


}
