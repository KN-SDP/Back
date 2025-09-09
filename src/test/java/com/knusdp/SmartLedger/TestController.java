package com.knusdp.SmartLedger;



import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.web.servlet.MockMvc;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(TestController.class) // 컨트롤러만 테스트하도록 설정

@AutoConfigureMockMvc(addFilters = false)

class TestControllerTest {



    @Autowired // MockMvc 객체를 주입받아 사용

    private MockMvc mockMvc;



    @Test

    @DisplayName("GET /hello 요청 시 'Hello World!'를 반환해야 한다.")

    void helloWorldTest() throws Exception {

        // given

        String expectedMessage = "Hello World!";



        // when & then

        mockMvc.perform(get("/hello"))

                .andExpect(status().isOk())

                .andExpect(content().string(expectedMessage));



    }

}