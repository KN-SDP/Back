package com.knusdp.SmartLedger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class) // 컨트롤러만 테스트하도록 설정
class TestControllerTest {

    @Autowired // MockMvc 객체를 주입받아 사용
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /hello 요청 시 'Hello World!'를 반환해야 한다.")
    void helloWorldTest() throws Exception {
        // given
        String expectedMessage = "Hello World!";

        // when & then
        mockMvc.perform(get("/hello")) // '/hello' 경로로 GET 요청을 보냄
                .andExpect(status().isOk()) // HTTP 상태 코드가 200 (OK)인지 검증
                .andExpect(content().string(expectedMessage)); // 응답 본문이 'Hello World!'인지 검증
    }
}